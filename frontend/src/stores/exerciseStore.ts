import { defineStore } from 'pinia'
import { validateTreeData } from '@/feature/aufgabendatenbank/validation'
import { useNotificationStore } from '@/stores/useNotificationStore'
import { ensurePurposeInXml, removePurposeFromXml, splitPurposeInXml, unsplitPurposeFromXml, escapeRegex, escapeXml } from '@/feature/aufgabendatenbank/representation/templateXml'
import {
  type Item,
  type Collection,
  type CollectionItem,
  type TreeItem,
  type Content,
  getInnerItem,
  isCollection as checkIsCollection,
  isCollectionItem
} from '@/lib/types'
import type {
  ApiAdapter,
  ItemDTO,
  ContentDTO,
  ContentSummaryDTO,
  CollectionItemDTO,
  AuthorDTO,
  LicenseDTO,
  ItemTypeDTO,
  ContentTypeDTO,
  ValidatorDTO,
  TagDTO,
  ReprTemplateDTO,
  SearchParams
} from '@/feature/aufgabendatenbank/api-adapter.types'

/** Ein Knoten im hierarchischen Tag-Baum (für die Filter-Auswahl). */
export interface TagNode {
  id: string
  tag: string
  children: TagNode[]
}

// ── Default-UUIDs (identisch mit database/init/init.sql) ───────────────────────
// Werden genutzt, solange der Benutzer im UI keine eigene Auswahl trifft.

const DEFAULT_AUTHOR_ID = 'd0000000-0000-0000-0000-000000000001'
const DEFAULT_LICENSE_ID = 'b0000000-0000-0000-0000-000000000001'
const DEFAULT_ITEM_TYPE_ID = 'e0000000-0000-0000-0000-000000000001'
const DEFAULT_CONTENT_TYPE_ID = 'a0000000-0000-0000-0000-000000000003'

// ── Adapter injection ─────────────────────────────────────────────────────────

let _adapter: ApiAdapter | null = null

/**
 * Inject the API adapter into the exercise store.
 * Must be called before `useExerciseStore()` is invoked.
 */
export function setApiAdapter(adapter: ApiAdapter): void {
  _adapter = adapter
}

// ── DTO → Store type mappers ──────────────────────────────────────────────────

function toContent(dto: ContentSummaryDTO): Content {
  return {
    id: dto.itemContentId,
    license: null,
    contentType: dto.itemContentTypeName,
    author: '',
    tags: [],
    purpose: '',
    jsonContent: {} as Record<string, any>,
    blobContent: dto.hasBlobContent ? '(binary)' : ''
  }
}

function toFullContent(dto: ContentDTO): Content {
  let jsonContent: Record<string, any> = {}
  if (dto.jsonSerializedContent) {
    try {
      jsonContent = JSON.parse(dto.jsonSerializedContent)
    } catch {
      jsonContent = { text: dto.jsonSerializedContent }
    }
  }
  const mime = dto.blobMimeType ?? ''
  return {
    id: dto.itemContentId,
    license: dto.licenseName ?? null,
    contentType: dto.itemContentTypeName,
    author: dto.authorDescriptor,
    tags: dto.tagIds,
    purpose: dto.purpose ?? '',
    jsonContent,
    blobContent: dto.hasBlobContent ? (mime.startsWith('image/') ? '(image)' : '(binary)') : '',
    blobMimeType: dto.blobMimeType ?? '',
    authorId: dto.authorId ?? null,
    licenseId: dto.licenseId ?? null,
    contentTypeId: dto.itemContentTypeId ?? null
  }
}

function toItem(dto: ItemDTO): Item {
  return {
    id: dto.itemId,
    item_type: dto.isCollection ? 'collection' : 'exercise',
    author: dto.authorDescriptor,
    representationTemplate: dto.itemTemplateId ?? null,
    license: dto.licenseName ?? null,
    tags: dto.tagIds ?? [],
    validators: dto.validatorIds ?? [],
    modifiers: dto.modifierIds ?? [],
    rootItemId: dto.rootItemId ?? null,
    contents: (dto.contents ?? []).map(toContent),
    // Eine Kollektion hat IMMER ein items-Array (zunächst leer, bis die
    // Kinder via _loadChildrenRecursively nachgeladen werden). Sonst
    // crasht z. B. die Validierung mit undefined.flatMap(...).
    items: dto.items?.map(toCollectionItem) ?? (dto.isCollection ? [] : undefined),
    order: dto.order,
    collectionId: dto.collectionId ?? null,
    authorId: dto.authorId ?? null,
    licenseId: dto.licenseId ?? null,
    itemTypeId: dto.itemTypeId ?? null,
    itemTypeName: dto.itemTypeName ?? null
  }
}

function toCollectionItem(dto: CollectionItemDTO): CollectionItem {
  return {
    id: dto.subItemId,
    collectionId: '',
    item: toItem(dto.item!),
    position: dto.position
  }
}

// ── State ─────────────────────────────────────────────────────────────────────

interface ExerciseState {
  rootItems: Item[]
  selectedItem: TreeItem | null
  variants: Item[]
  loading: boolean
  loadingContent: boolean
  error: string | null
  loadingChildrenIds: string[]
  // Referenzdaten für Dropdowns (Autor/Lizenz/Typ/Content-Typ)
  authors: AuthorDTO[]
  licenses: LicenseDTO[]
  itemTypes: ItemTypeDTO[]
  contentTypes: ContentTypeDTO[]
  // Hierarchische Tags + aktiver Tag-Filter (null = kein Filter)
  tags: TagDTO[]
  tagFilter: string | null
  // Templates für die Darstellung der Contents
  templates: ReprTemplateDTO[]
  // Erstellungs-Dialog (von Toolbar und Collection-Kontextmenü geteilt)
  createDialogOpen: boolean
  createDialogTarget: Collection | null
  allValidators: ValidatorDTO[]
  selectedAuthorId: string | null
  selectedLicenseId: string | null
  selectedItemTypeId: string | null
  selectedContentTypeId: string | null
  // Aktuelle Live-XML aus dem Editor, damit _getTemplateXml / _ensurePurpose / _removePurpose
  // gegen die aktuellste Version mergen und nicht gegen eine veraltete persisted template.
  liveTemplateXml: string | null
  // Such-/Filterzustand
  searchQuery: string
  filterAuthorId: string | null
  filterItemTypeId: string | null
  filterTag: string
  filteredItems: ItemDTO[] | null
  filtering: boolean
}

// ── Debounce timer (module-level, nicht im Store-State) ──────────────────────

let _filterTimer: ReturnType<typeof setTimeout> | null = null

// ── Store ─────────────────────────────────────────────────────────────────────

export const useExerciseStore = defineStore('exercise', {
  state: (): ExerciseState => ({
    rootItems: [],
    selectedItem: null,
    variants: [],
    loading: false,
    loadingContent: false,
    error: null,
    loadingChildrenIds: [],
    authors: [],
    licenses: [],
    itemTypes: [],
    contentTypes: [],
    tags: [],
    tagFilter: null,
    templates: [],
    createDialogOpen: false,
    createDialogTarget: null,
    allValidators: [],
    selectedAuthorId: null,
    selectedLicenseId: null,
    selectedItemTypeId: null,
    selectedContentTypeId: null,
    liveTemplateXml: null,
    searchQuery: '',
    filterAuthorId: null,
    filterItemTypeId: null,
    filterTag: '',
    filteredItems: null,
    filtering: false
  }),

  getters: {
    authorId: (state): string => state.selectedAuthorId ?? DEFAULT_AUTHOR_ID,
    licenseId: (state): string => state.selectedLicenseId ?? DEFAULT_LICENSE_ID,
    itemTypeId: (state): string => state.selectedItemTypeId ?? DEFAULT_ITEM_TYPE_ID,
    contentTypeId: (state): string => state.selectedContentTypeId ?? DEFAULT_CONTENT_TYPE_ID,

    selectedInnerItem: (state): Item | null => {
      if (!state.selectedItem) return null
      return getInnerItem(state.selectedItem)
    },

    // Default-IDs für die Erstellung: erster Eintrag der jeweiligen
    // Referenzliste, Fallback auf die festen Seed-UUIDs (falls Liste leer).
    defaultAuthorId: (state): string => state.authors[0]?.id ?? DEFAULT_AUTHOR_ID,
    defaultLicenseId: (state): string => state.licenses[0]?.id ?? DEFAULT_LICENSE_ID,
    defaultItemTypeId: (state): string => state.itemTypes[0]?.id ?? DEFAULT_ITEM_TYPE_ID,
    defaultContentTypeId: (state): string => state.contentTypes[0]?.id ?? DEFAULT_CONTENT_TYPE_ID,

    isCollectionSelected(): boolean {
      const inner = this.selectedInnerItem
      return inner ? checkIsCollection(inner) : false
    },

    selectedCollection(): Collection | null {
      const inner = this.selectedInnerItem
      return inner && checkIsCollection(inner) ? (inner as Collection) : null
    },

    isOrdered(): boolean {
      const coll = this.selectedCollection
      return coll ? coll.order === true : false
    },

    // ── Tags ──────────────────────────────────────────────────────────────────

    /** Lesbarer Pfad eines Tags, z. B. "Mathematik / Analysis / Ableitungen". */
    tagPath() {
      return (id: string): string => {
        const parts: string[] = []
        let cur = this.tags.find((t) => t.id === id)
        let guard = 0
        while (cur && guard++ < 20) {
          parts.unshift(cur.tag)
          const parentId = cur.parentTagId
          cur = parentId ? this.tags.find((t) => t.id === parentId) : undefined
        }
        return parts.join('/')
      }
    },

    /** Alle Tags als { id, path } für Dropdowns, nach Pfad sortiert. */
    tagOptions(): { id: string; path: string }[] {
      return this.tags
        .map((t) => ({ id: t.id, path: this.tagPath(t.id) }))
        .sort((a, b) => a.path.localeCompare(b.path))
    },

    /** Hierarchischer Tag-Baum (nur Wurzeln, mit verschachtelten Kindern). */
    tagTree(): TagNode[] {
      const byId = new Map<string, TagNode>()
      for (const t of this.tags) byId.set(t.id, { id: t.id, tag: t.tag, children: [] })
      const roots: TagNode[] = []
      for (const t of this.tags) {
        const node = byId.get(t.id)!
        const parent = t.parentTagId ? byId.get(t.parentTagId) : undefined
        if (parent) parent.children.push(node)
        else roots.push(node)
      }
      const sortRec = (nodes: TagNode[]) => {
        nodes.sort((a, b) => a.tag.localeCompare(b.tag))
        for (const n of nodes) sortRec(n.children)
      }
      sortRec(roots)
      return roots
    },

    /** Ein Tag plus alle seine Nachfahren (für den vererbenden Filter). */
    descendantTagIds() {
      return (id: string): Set<string> => {
        const ids = new Set<string>([id])
        let added = true
        while (added) {
          added = false
          for (const t of this.tags) {
            if (t.parentTagId && ids.has(t.parentTagId) && !ids.has(t.id)) {
              ids.add(t.id)
              added = true
            }
          }
        }
        return ids
      }
    },

    /**
     * Root-Items unter Berücksichtigung des Tag-Filters. Ohne Filter alle;
     * mit Filter nur Items, deren Tags den gewählten Tag oder einen seiner
     * Nachfahren enthalten (Vererbung).
     */
    visibleRootItems(): Item[] {
      if (!this.tagFilter) return this.rootItems
      const wanted = this.descendantTagIds(this.tagFilter)
      return this.rootItems.filter((it) => it.tags.some((tg) => wanted.has(tg)))
    },

    templateById(): (id: string | null) => string | null {
      const map: Record<string, string | null> = {}
      for (const t of this.templates) map[t.id] = t.template
      return (id: string | null) => (id ? map[id] ?? null : null)
    },

    hasActiveFilters(): boolean {
      return !!(
        this.searchQuery?.trim() ||
        this.filterAuthorId ||
        this.filterItemTypeId ||
        this.filterTag?.trim()
      )
    }
  },

  actions: {
    // ── Notifications ─────────────────────────────────────────────────────────

    /** Push an API error string to the global notification store. */
    _notifyError(e: unknown) {
      const notifStore = useNotificationStore()
      const msg = (e as any)?.response?.data?.message || (e as any)?.response?.data || String(e)
      const status = (e as any)?.response?.status ? `[${(e as any).response.status}] ` : ''
      notifStore.push(status + JSON.stringify(msg), 'error', 12000)
    },

    // ── Referenzdaten ─────────────────────────────────────────────────────────

    /**
     * Load reference lists (authors, licenses, item types, content types)
     * used to populate the editor dropdowns. Called once on mount.
     */
    async loadReferenceData() {
      if (!_adapter) return
      try {
        const [authors, licenses, itemTypes, contentTypes, tags] = await Promise.all([
          _adapter.getAuthors(),
          _adapter.getLicenses(),
          _adapter.getItemTypes(),
          _adapter.getContentTypes(),
          _adapter.getTags()
        ])
        this.authors = authors
        this.licenses = licenses
        this.itemTypes = itemTypes
        this.contentTypes = contentTypes
        this.tags = tags
      } catch (e) {
        this._notifyError(e)
      }
    },

    async loadRepresentationTemplates() {
      if (!_adapter) return
      try {
        this.templates = await _adapter.getRepresentationTemplates()
      } catch (e) {
        this._notifyError(e)
      }
    },

    /**
     * Neue Referenzdaten anlegen (Autor/Lizenz/Typ/Inhaltstyp). Legt sie per
     * POST an, hängt sie an die passende Liste und gibt den neuen Datensatz
     * zurück, damit die UI ihn direkt auswählen kann.
     * `primary` = Name/Descriptor, `secondary` = Mail (Autor) bzw. Beschreibung.
     */
    async createReference(
      type: 'author' | 'license' | 'itemType' | 'contentType',
      primary: string,
      secondary = ''
    ): Promise<{ id: string } | null> {
      if (!_adapter || !primary.trim()) return null
      const name = primary.trim()
      const extra = secondary.trim() || null

      // Kein Duplikat (Name-Vergleich ohne Gross-/Kleinschreibung)
      const lower = name.toLowerCase()
      const exists =
        type === 'author'
          ? this.authors.some((a) => a.descriptor.toLowerCase() === lower)
          : type === 'license'
            ? this.licenses.some((l) => l.name.toLowerCase() === lower)
            : type === 'itemType'
              ? this.itemTypes.some((t) => t.name.toLowerCase() === lower)
              : this.contentTypes.some((c) => c.name.toLowerCase() === lower)
      if (exists) {
        useNotificationStore().push(`„${name}" existiert bereits.`, 'error', 6000)
        return null
      }

      try {
        if (type === 'author') {
          const dto = await _adapter.createAuthor({ descriptor: name, mail: extra })
          this.authors.push(dto)
          return dto
        }
        if (type === 'license') {
          const dto = await _adapter.createLicense({ name })
          this.licenses.push(dto)
          return dto
        }
        if (type === 'itemType') {
          const dto = await _adapter.createItemType({ name, description: extra })
          this.itemTypes.push(dto)
          return dto
        }
        const dto = await _adapter.createContentType({ name, description: extra })
        this.contentTypes.push(dto)
        return dto
      } catch (e) {
        this._notifyError(e)
        return null
      }
    },

    // ── Tags ────────────────────────────────────────────────────────────────

    /** Neues Tag anlegen (optional mit Eltern-Tag) und in die Liste aufnehmen. */
    async createTag(
      name: string,
      parentTagId: string | null = null,
      description = ''
    ): Promise<TagDTO | null> {
      if (!_adapter || !name.trim()) return null
      try {
        const dto = await _adapter.createTag({
          tag: name.trim(),
          description: description.trim() || null,
          parentTagId: parentTagId || null
        })
        this.tags.push(dto)
        return dto
      } catch (e) {
        this._notifyError(e)
        return null
      }
    },

    /**
     * Erstellt einen Tag aus einem Pfad "A/B/C": bestehende Segmente werden
     * per Name wiederverwendet, fehlende neu angelegt. Leerzeichen sind
     * verboten (auch zwischen Wörtern). Gibt den Blatt-Tag zurück.
     */
    async createTagPath(
      rawName: string,
      parentId: string | null = null,
      description = ''
    ): Promise<TagDTO | null> {
      const segments = rawName.split('/').map((s) => s.trim()).filter((s) => s.length > 0)
      if (segments.length === 0) return null
      if (segments.some((s) => /\s/.test(s))) {
        useNotificationStore().push('Tags dürfen keine Leerzeichen enthalten.', 'error', 6000)
        return null
      }
      let parent = parentId
      let leaf: TagDTO | null = null
      for (let i = 0; i < segments.length; i++) {
        const seg = segments[i]
        const isLeaf = i === segments.length - 1
        const existing = this.tags.find((t) => t.tag.toLowerCase() === seg.toLowerCase())
        if (existing) {
          // Ein bereits existierender Blatt-Tag ist ein Duplikat -> Fehler.
          // Nur Zwischenebenen (Vorfahren) werden wiederverwendet.
          if (isLeaf) {
            useNotificationStore().push(`Tag „${seg}" existiert bereits.`, 'error', 6000)
            return null
          }
          parent = existing.id
          leaf = existing
          continue
        }
        const created = await this.createTag(seg, parent, isLeaf ? description : '')
        if (!created) return null
        parent = created.id
        leaf = created
      }
      return leaf
    },

    /**
     * Tag einem Item zuordnen (optimistisch). Exklusivität: bereits
     * zugewiesene Vorfahren oder Nachfahren desselben Astes werden entfernt,
     * da der spezifischste Tag die übrigen Ebenen bereits impliziert.
     */
    assignTagToItem(item: Item, tagId: string) {
      if (!tagId || item.tags.includes(tagId)) return
      const related = this._relatedTagIds(tagId)
      for (const existing of [...item.tags]) {
        if (related.has(existing)) this.removeTagFromItem(item, existing)
      }
      item.tags.push(tagId)
      _adapter?.addTagToItem(item.id, tagId).catch((e) => {
        item.tags = item.tags.filter((t) => t !== tagId)
        this._notifyError(e)
      })
    },

    /** Vorfahren + Nachfahren eines Tags (ohne den Tag selbst). */
    _relatedTagIds(tagId: string): Set<string> {
      const related = new Set<string>()
      let cur = this.tags.find((t) => t.id === tagId)
      let guard = 0
      while (cur?.parentTagId && guard++ < 50) {
        const pid: string = cur.parentTagId
        related.add(pid)
        cur = this.tags.find((t) => t.id === pid)
      }
      for (const id of this.descendantTagIds(tagId)) {
        if (id !== tagId) related.add(id)
      }
      return related
    },

    /** Tag-Zuordnung von einem Item entfernen (optimistisch). */
    removeTagFromItem(item: Item, tagId: string) {
      const before = item.tags
      item.tags = item.tags.filter((t) => t !== tagId)
      _adapter?.removeTagFromItem(item.id, tagId).catch((e) => {
        item.tags = before
        this._notifyError(e)
      })
    },

    /** Aktiven Tag-Filter setzen (null = aus). */
    setTagFilter(tagId: string | null) {
      this.tagFilter = tagId
    },

    /**
     * Einen Tag vollständig löschen. Backend + Datenbank räumen die Folgen auf
     * (Unter-Tags werden zu Wurzel-Tags, Zuordnungen verschwinden). Der lokale
     * Zustand wird passend nachgezogen.
     */
    async deleteTag(tagId: string) {
      if (!_adapter) return
      try {
        await _adapter.deleteTag(tagId)
        // Kinder zu Wurzeln machen (parentTagId === tagId -> null)
        this.tags.forEach((t) => {
          if (t.parentTagId === tagId) t.parentTagId = null
        })
        this.tags = this.tags.filter((t) => t.id !== tagId)
        this._stripTagFromAllItems(tagId)
        if (this.tagFilter === tagId) this.tagFilter = null
      } catch (e) {
        this._notifyError(e)
      }
    },

    /** Entfernt eine Tag-ID aus allen Items im Baum (nach dem Löschen). */
    _stripTagFromAllItems(tagId: string) {
      const walk = (items: Item[]) => {
        for (const it of items) {
          it.tags = it.tags.filter((t) => t !== tagId)
          if (it.items) walk(it.items.map((ci) => ci.item))
        }
      }
      walk(this.rootItems)
    },

    // ── Initialisation (progressive loading) ──────────────────────────────────

    /**
     * Load the exercise tree progressively.
     *
     * Phase 1: `getRootItems()` — roots appear in the UI immediately.
     * Phase 2: `_loadChildrenRecursively()` — each collection's children
     * load in the background with a per-node spinner in the tree view.
     */
    async loadTree() {
      this.loading = true
      this.error = null
      try {
        const [dtos] = await Promise.all([
          _adapter!.getRootItems(),
          this.loadValidators()
        ])
        this.rootItems = dtos.map(toItem)
        // Fire background recursive loading — no await so UI shows roots now
        this._loadChildrenRecursively(this.rootItems)
      } catch (e) {
        this.error = String(e)
        this._notifyError(e)
      } finally {
        this.loading = false
      }
    },

    /**
     * Recursively load children for every collection in `items`.
     *
     * Each collection is flagged in `loadingChildrenIds` while its
     * children are being fetched. Sub-collections inside the loaded
     * children trigger their own fetch. All siblings at the same depth
     * load in parallel.
     */
    async _loadChildrenRecursively(items: Item[]) {
      const promises = items
        .filter((item) => item.item_type === 'collection' || checkIsCollection(item))
        .map(async (item) => {
          const collection = item as Collection
          // Kollektion-Endpunkte brauchen die item_collection_id, nicht die item_id.
          // Fehlt sie (z. B. optimistisch angelegt, Backend-Antwort noch unterwegs),
          // überspringen — der Reload liefert sie später.
          if (!collection.collectionId) return
          this.loadingChildrenIds = [...this.loadingChildrenIds, collection.id]
          try {
            const dtos = await _adapter!.getCollectionItems(collection.collectionId)
            collection.items = dtos.map(toCollectionItem)
            // Recurse into sub-collections
            await this._loadChildrenRecursively(collection.items.map((ci) => ci.item))
          } catch (e) {
            this._notifyError(e)
          } finally {
            this.loadingChildrenIds = this.loadingChildrenIds.filter((id) => id !== collection.id)
          }
        })
      await Promise.all(promises)
    },

    // ── Search / Filters ──────────────────────────────────────────────────────

    setSearchQuery(query: string) {
      this.searchQuery = query
      this._debouncedApplyFilters()
    },

    setFilterAuthorId(id: string | null) {
      this.filterAuthorId = id
      this._debouncedApplyFilters()
    },

    setFilterItemTypeId(id: string | null) {
      this.filterItemTypeId = id
      this._debouncedApplyFilters()
    },

    setFilterTag(tag: string) {
      this.filterTag = tag
      this._debouncedApplyFilters()
    },

    clearFilters() {
      this.searchQuery = ''
      this.filterAuthorId = null
      this.filterItemTypeId = null
      this.filterTag = ''
      this.filteredItems = null
      this._debouncedApplyFilters()
    },

    _debouncedApplyFilters() {
      if (_filterTimer) clearTimeout(_filterTimer)
      _filterTimer = setTimeout(() => this.applyFilters(), 250)
    },

    async applyFilters() {
      if (!this.hasActiveFilters) {
        this.filteredItems = null
        if (this.rootItems.length === 0) {
          await this.loadTree()
        }
        return
      }

      this.filtering = true
      try {
        const params: SearchParams = {}
        if (this.searchQuery?.trim()) params.search = this.searchQuery.trim()
        if (this.filterAuthorId) params.authorId = this.filterAuthorId
        if (this.filterItemTypeId) params.itemTypeId = this.filterItemTypeId
        if (this.filterTag?.trim()) params.tag = this.filterTag.trim()

        this.filteredItems = await _adapter!.searchItems(params)
      } catch (e) {
        this._notifyError(e)
        this.filteredItems = null
      } finally {
        this.filtering = false
      }
    },

    // ── Selection ─────────────────────────────────────────────────────────────

    selectItem(item: TreeItem) {
      this.liveTemplateXml = null
      this.selectedItem = item
      this.variants = []
      const inner = getInnerItem(item)
      this.loadItemContent(inner.id)
      if (!checkIsCollection(inner)) {
        const baseId = inner.rootItemId ?? inner.id
        this.loadVariants(baseId)
      }
    },

    setLiveTemplateXml(xml: string | null) {
      this.liveTemplateXml = xml
    },

    async loadItemContent(itemId: string) {
      this.loadingContent = true
      try {
        const dtos = await _adapter!.getContents(itemId)
        if (dtos.length > 0) {
          const item = this._findItemById(itemId)
          if (item) {
            item.contents = dtos.map(toFullContent)
          }
        }
      } catch (e) {
        this._notifyError(e)
      } finally {
        this.loadingContent = false
      }
    },

    _findItemById(id: string, items?: Item[], visited?: Set<string>): Item | null {
      const searchItems = items ?? this.rootItems
      visited = visited ?? new Set()
      for (const item of searchItems) {
        if (visited.has(item.id)) continue
        visited.add(item.id)
        if (item.id === id) return item
        if (item.items) {
          for (const ci of item.items) {
            if (ci.item.id === id) return ci.item
            const found = this._findItemById(id, ci.item.items?.map((sci) => sci.item), visited)
            if (found) return found
          }
        }
      }
      return null
    },

    /**
     * Toggle a collection's `order` flag.
     *
     * When enabling, items without a position get one assigned. When
     * disabling, positions are cleared to null to match the backend
     * (which nulls them), so UI and DB stay in sync.
     */
    toggleCollectionOrder(collection: Collection) {
      collection.order = !collection.order
      if (collection.order) {
        collection.items.forEach((item, index) => {
          if (item.position == null) {
            item.position = index + 1
          }
        })
      } else {
        // Backend setzt die Positionen auf NULL, wenn die Reihenfolge
        // deaktiviert wird — lokal nachziehen, damit UI und DB nicht divergieren.
        collection.items.forEach((item) => {
          item.position = null
        })
      }
      if (collection.collectionId) {
        _adapter?.toggleCollectionOrder(collection.collectionId, { order: collection.order })
          .catch((e) => this._notifyError(e))
      }
      this._syncOrderedCollectionItems(collection)
    },

    // ── Validator Actions ────────────────────────────────────────────────────

    async loadValidators() {
      try {
        this.allValidators = await _adapter!.getValidators()
      } catch (e) {
        this._notifyError(e)
      }
    },

    async createValidator(description: string, rule: string): Promise<ValidatorDTO | null> {
      try {
        const dto = await _adapter!.createValidator({ description, validator: rule })
        this.allValidators.push(dto)
        return dto
      } catch (e) {
        this._notifyError(e)
        return null
      }
    },

    async linkValidatorToSelectedItem(validatorId: string) {
      const inner = this.selectedInnerItem
      if (!inner) return
      if (inner.validators.includes(validatorId)) return
      inner.validators.push(validatorId)
      try {
        await _adapter!.addValidatorToItem(inner.id, validatorId)
      } catch (e) {
        inner.validators = inner.validators.filter((v: string) => v !== validatorId)
        this._notifyError(e)
      }
    },

    async unlinkValidatorFromSelectedItem(validatorId: string) {
      const inner = this.selectedInnerItem
      if (!inner) return
      inner.validators = inner.validators.filter((v: string) => v !== validatorId)
      try {
        await _adapter!.removeValidatorFromItem(inner.id, validatorId)
      } catch (e) {
        if (!inner.validators.includes(validatorId)) {
          inner.validators.push(validatorId)
        }
        this._notifyError(e)
      }
    },

    // ── Content Actions ───────────────────────────────────────────────────────

    addContentToSelectedItem() {
      const inner = this.selectedInnerItem
      if (!inner) return
      const now = Date.now().toString()
      const licenseId = inner.licenseId ?? this.defaultLicenseId
      const authorId = inner.authorId ?? this.defaultAuthorId
      const contentTypeId = this.defaultContentTypeId
      const content: Content = {
        id: 'content-' + now,
        license: inner.license ?? null,
        contentType: this.contentTypes.find((c) => c.id === contentTypeId)?.name ?? 'text',
        author: inner.author ?? 'author',
        tags: [],
        purpose: 'Neuer Inhalt',
        jsonContent: { text: '' },
        blobContent: '',
        authorId,
        licenseId,
        contentTypeId
      }
      inner.contents.push(content)
      this._ensurePurposeInTemplateXml(content.purpose)
      _adapter?.createContent(inner.id, {
        licenseId,
        itemContentTypeId: contentTypeId,
        authorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).then((dto) => {
        if (dto) content.id = dto.itemContentId
      }).catch((e) => this._notifyError(e))
    },

    removeContentFromSelectedItem(index: number) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const removedPurpose = inner.contents[index].purpose
      const removedId = inner.contents[index].id
      inner.contents.splice(index, 1)
      this._removePurposeFromTemplateXml(removedPurpose)
      if (removedId) {
        _adapter?.deleteContent(removedId).catch((e) => this._notifyError(e))
      }
    },

    updateContentText(index: number, text: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      content.jsonContent.text = text
      _adapter?.updateContent(content.id ?? '', {
        licenseId: content.licenseId ?? this.defaultLicenseId,
        itemContentTypeId: content.contentTypeId ?? this.defaultContentTypeId,
        authorId: content.authorId ?? this.defaultAuthorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    updateContentPurpose(index: number, purpose: string) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      const oldPurpose = content.purpose
      content.purpose = purpose
      const xml = this._getTemplateXml()
      if (xml && xml.includes(`<purpose>${oldPurpose}</purpose>`)) {
        this._saveTemplateXml(xml.replace(
          new RegExp(`<purpose>${escapeRegex(oldPurpose)}</purpose>`, 'g'),
          `<purpose>${purpose}</purpose>`
        ))
      }
      _adapter?.updateContent(content.id ?? '', {
        licenseId: content.licenseId ?? this.defaultLicenseId,
        itemContentTypeId: content.contentTypeId ?? this.defaultContentTypeId,
        authorId: content.authorId ?? this.defaultAuthorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    /**
     * Update a content block's type / license from the editor dropdowns.
     * Updates local labels and persists via PUT /contents/{id}.
     */
    updateContentMeta(index: number, meta: { contentTypeId?: string; licenseId?: string }) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      if (meta.contentTypeId !== undefined) {
        content.contentTypeId = meta.contentTypeId
        content.contentType =
          this.contentTypes.find((c) => c.id === meta.contentTypeId)?.name ?? content.contentType
      }
      if (meta.licenseId !== undefined) {
        content.licenseId = meta.licenseId
        content.license = this.licenses.find((l) => l.id === meta.licenseId)?.name ?? content.license
      }
      _adapter?.updateContent(content.id ?? '', {
        licenseId: content.licenseId ?? this.defaultLicenseId,
        itemContentTypeId: content.contentTypeId ?? this.defaultContentTypeId,
        authorId: content.authorId ?? this.defaultAuthorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    async uploadBlob(index: number, file: File) {
      const inner = this.selectedInnerItem
      if (!inner || !inner.contents[index]) return
      const content = inner.contents[index]
      const contentId = content.id
      if (!contentId) return
      try {
        await _adapter!.uploadBlob(contentId, file)
        content.blobMimeType = file.type
        content.blobContent = file.type.startsWith('image/') ? '(image)' : '(binary)'
      } catch (e) {
        this._notifyError(e)
      }
    },

    getBlobUrl(contentId: string): string {
      if (!_adapter) return ''
      return _adapter.getBlobUrl(contentId)
    },

    // ── Template XML Actions ─────────────────────────────────────────────────

    _getTemplateXml(): string | null {
      if (this.liveTemplateXml) return this.liveTemplateXml
      const item = this.selectedInnerItem
      if (!item) return null
      const stored = this.templateById(item.representationTemplate)
      if (stored) return stored
      if (item.contents.length === 0) return '<layout>\n</layout>'
      const purposes = item.contents.map(c => `  <purpose>${escapeXml(c.purpose)}</purpose>`).join('\n')
      return `<layout>\n${purposes}\n</layout>`
    },

    async _saveTemplateXml(xml: string) {
      const item = this.selectedInnerItem
      if (!item) return

      // Local save — update in-place to keep templateById lookup consistent
      const localId = item.representationTemplate ?? '_' + Date.now()
      const existing = this.templates.find(t => t.id === item.representationTemplate)
      if (existing) {
        existing.template = xml
      } else {
        this.templates.push({ id: localId, template: xml })
      }
      item.representationTemplate = localId

      // Backend persist
      try {
        if (localId.startsWith('_')) {
          const created = await _adapter!.createRepresentationTemplate({ template: xml })
          // Update in-place so templateById never sees a gap
          const temp = this.templates.find(t => t.id === localId)
          if (temp) {
            temp.id = created.id
            temp.template = created.template
          } else {
            this.templates.push({ id: created.id, template: created.template })
          }
          this.updateItemMeta(item, { itemTemplateId: created.id })
        } else {
          await _adapter!.updateRepresentationTemplate(localId, { template: xml })
        }
      } catch (e) {
        this._notifyError(e)
      }
    },

    _ensurePurposeInTemplateXml(purpose: string) {
      const xml = this._getTemplateXml()
      if (!xml) return
      this._saveTemplateXml(ensurePurposeInXml(xml, purpose))
    },

    _removePurposeFromTemplateXml(purpose: string) {
      const xml = this._getTemplateXml()
      if (!xml) return
      this._saveTemplateXml(removePurposeFromXml(xml, purpose))
    },

    _splitPurposeInTemplateXml(purpose: string) {
      const xml = this._getTemplateXml()
      if (!xml) return
      this._saveTemplateXml(splitPurposeInXml(xml, purpose))
    },

    _unsplitPurposeFromTemplateXml(purpose: string) {
      const xml = this._getTemplateXml()
      if (!xml) return
      this._saveTemplateXml(unsplitPurposeFromXml(xml, purpose))
    },

    saveEditedTemplateXml(xml: string) {
      const item = this.selectedInnerItem
      if (!item) return
      this._saveTemplateXml(xml)
    },

    // ── Variants ────────────────────────────────────────────────────────────────

    /**
     * Zählt die Varianten eines Items (Items mit root_item_id === itemId),
     * ohne den State zu verändern. Für die Warnung vor dem Umwandeln
     * in eine Collection.
     */
    async getVariantCount(itemId: string): Promise<number> {
      try {
        const dtos = await _adapter!.getItemsByRootId(itemId)
        return dtos.length
      } catch (e) {
        this._notifyError(e)
        return 0
      }
    },

    async loadVariants(baseItemId: string) {
      this.variants = []
      try {
        const dtos = await _adapter!.getItemsByRootId(baseItemId)
        this.variants = dtos.map(toItem)
        await Promise.all(this.variants.map(async (v) => {
          const contentDtos = await _adapter!.getContents(v.id)
          if (contentDtos.length > 0) {
            v.contents = contentDtos.map(toFullContent)
          }
        }))
      } catch (e) {
        this._notifyError(e)
      }
    },

    async createVariant(baseItemId: string) {
      const variant = this._createItemData(baseItemId)
      this.variants.push(variant)
      try {
        const dto = await _adapter!.createItem({
          authorId: this.authorId,
          licenseId: this.licenseId,
          itemTypeId: this.itemTypeId,
          rootItemId: baseItemId
        })
        variant.id = dto.itemId
        if (variant.contents.length > 0) {
          const contentDto = await _adapter!.createContent(dto.itemId, {
            licenseId: this.licenseId,
            itemContentTypeId: this.contentTypeId,
            authorId: this.authorId,
            purpose: variant.contents[0].purpose,
            jsonSerializedContent: JSON.stringify(variant.contents[0].jsonContent)
          })
          if (contentDto && variant.contents[0]) {
            variant.contents[0].id = contentDto.itemContentId
          }
        }
      } catch (e) {
        this._notifyError(e)
        this.variants = this.variants.filter((v) => v !== variant)
        // Konvergiert zur DB-Wahrheit: falls Stufe 1 (createItem) doch
        // durchlief und erst createContent scheiterte, taucht das Item hier
        // wieder auf, statt als Waise zu verschwinden.
        await this.loadVariants(baseItemId)
      }
    },

    updateVariantText(variantIndex: number, contentIndex: number, text: string) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const content = variant.contents[contentIndex]
      content.jsonContent.text = text
      _adapter?.updateContent(content.id ?? '', {
        licenseId: this.licenseId,
        itemContentTypeId: this.contentTypeId,
        authorId: this.authorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    updateVariantPurpose(variantIndex: number, contentIndex: number, purpose: string) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const content = variant.contents[contentIndex]
      content.purpose = purpose
      _adapter?.updateContent(content.id ?? '', {
        licenseId: this.licenseId,
        itemContentTypeId: this.contentTypeId,
        authorId: this.authorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).catch((e) => this._notifyError(e))
    },

    addVariantContent(variantIndex: number) {
      const variant = this.variants[variantIndex]
      if (!variant) return
      const now = Date.now().toString()
      const content: Content = {
        id: 'content-' + now,
        license: null,
        contentType: 'text',
        author: variant.author ?? 'author',
        tags: [],
        purpose: 'Neuer Inhalt',
        jsonContent: { text: '' },
        blobContent: ''
      }
      variant.contents.push(content)
      _adapter?.createContent(variant.id, {
        licenseId: this.licenseId,
        itemContentTypeId: this.contentTypeId,
        authorId: this.authorId,
        purpose: content.purpose,
        jsonSerializedContent: JSON.stringify(content.jsonContent)
      }).then((dto) => {
        if (dto) content.id = dto.itemContentId
      }).catch((e) => this._notifyError(e))
    },

    removeVariantContent(variantIndex: number, contentIndex: number) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const removedId = variant.contents[contentIndex].id
      variant.contents.splice(contentIndex, 1)
      if (removedId) {
        _adapter?.deleteContent(removedId).catch((e) => this._notifyError(e))
      }
    },

    async uploadVariantBlob(variantIndex: number, contentIndex: number, file: File) {
      const variant = this.variants[variantIndex]
      if (!variant || !variant.contents[contentIndex]) return
      const content = variant.contents[contentIndex]
      const contentId = content.id
      if (!contentId) return
      try {
        await _adapter!.uploadBlob(contentId, file)
        content.blobMimeType = file.type
        content.blobContent = file.type.startsWith('image/') ? '(image)' : '(binary)'
      } catch (e) {
        this._notifyError(e)
      }
    },
    // ── Tree helpers ──

    /**
     * Remove an item from all collections and from rootItems.
     * @returns The ID of the parent collection the item was removed from, or null.
     */
    _detachItem(itemId: string, excludeId?: string): string | null {
      let parentId: string | null = null
      const removeFromCollections = (collections: Collection[]) => {
        collections.forEach((coll) => {
          const hadItem = coll.items.some((ci) => ci.item.id === itemId)
          if (hadItem) parentId = coll.id
          coll.items = coll.items.filter((ci) => ci.item.id !== itemId)
          const nested = coll.items.map((ci) => ci.item).filter(checkIsCollection)
          if (nested.length > 0) removeFromCollections(nested)
        })
      }
      removeFromCollections(this.rootItems.filter(checkIsCollection) as Collection[])
      this.rootItems = this.rootItems.filter((ri) => ri.id !== itemId || ri.id === excludeId)
      return parentId
    },

    /**
     * Walk the tree to find a collection by ID.
     */
    _findCollectionById(id: string, items?: Item[]): Collection | null {
      const searchItems = items ?? this.rootItems
      for (const item of searchItems) {
        if (item.id === id && checkIsCollection(item)) return item as Collection
        if (checkIsCollection(item)) {
          const coll = item as Collection
          const found = this._findCollectionById(id, coll.items.map((ci) => ci.item))
          if (found) return found
        }
      }
      return null
    },

    /**
     * Nach einem fehlgeschlagenen optimistischen Write den betroffenen
     * Teilbaum wieder mit dem DB-Stand abgleichen. Bevorzugt der schmale
     * Reload einer Kollektion; Fallback: kompletter Baum-Reload.
     */
    _resyncAggregate(target?: Collection | null): Promise<void> {
      if (target?.collectionId) return this._loadChildrenRecursively([target])
      return this.loadTree()
    },

    /** Build a minimal Item object with a unique ID and single Content block. */
    _createItemData(rootItemId: string | null = null): Item {
      const now = Date.now().toString()
      const authorId = this.defaultAuthorId
      const licenseId = this.defaultLicenseId
      const itemTypeId = this.defaultItemTypeId
      const contentTypeId = this.defaultContentTypeId
      const authorName = this.authors.find((a) => a.id === authorId)?.descriptor ?? 'author'
      const licenseName = this.licenses.find((l) => l.id === licenseId)?.name ?? null
      const typeName = this.itemTypes.find((t) => t.id === itemTypeId)?.name ?? null
      const contentTypeName = this.contentTypes.find((c) => c.id === contentTypeId)?.name ?? 'text'
      return {
        id: 'item-' + now,
        item_type: 'exercise',
        author: authorName,
        representationTemplate: null,
        license: licenseName,
        tags: [],
        validators: [],
        modifiers: [],
        rootItemId,
        authorId,
        licenseId,
        itemTypeId,
        itemTypeName: typeName,
        contents: [
          {
            id: 'content-' + now,
            license: licenseName,
            contentType: contentTypeName,
            author: authorName,
            tags: [],
            purpose: 'Neuer Inhalt',
            jsonContent: { text: '' },
            blobContent: '',
            authorId,
            licenseId,
            contentTypeId
          }
        ]
      }
    },

    /**
     * After a mutation to an ordered collection, diff the current
     * positions against their expected sequential order and push
     * any changes to the API.
     *
     * Only fires when `collection.order === true`.
     */
    /**
     * Persist positions for an ordered collection.
     *
     * @param collection - The collection to sync.
     * @param force - When `true`, call the API for every item regardless
     * of whether its local position already matches. Used after DnD reorder
     * where positions are pre-set in `.map()` but still need to be persisted.
     */
    _syncOrderedCollectionItems(collection: Collection, force = false) {
      if (!collection.order || !_adapter || !collection.collectionId) return
      const collectionId = collection.collectionId
      collection.items.forEach((item, index) => {
        const expected = index + 1
        if (force || item.position !== expected) {
          _adapter!.updateCollectionItemPosition(collectionId, item.item.id, expected)
            .catch((e) => this._notifyError(e))
          item.position = expected
        }
      })
    },

    // ── CRUD Actions ──

    /**
     * Update an item's metadata (author / license / item-type) from the
     * editor dropdowns. Updates the local item (incl. display labels) and
     * persists via PUT /items/{id}.
     */
    updateItemMeta(item: Item, meta: { authorId?: string; licenseId?: string; itemTypeId?: string; itemTemplateId?: string }) {
      if (meta.authorId !== undefined) item.authorId = meta.authorId
      if (meta.licenseId !== undefined) item.licenseId = meta.licenseId
      if (meta.itemTypeId !== undefined) item.itemTypeId = meta.itemTypeId
      if (meta.itemTemplateId !== undefined) item.representationTemplate = meta.itemTemplateId

      const author = this.authors.find((a) => a.id === item.authorId)
      if (author) item.author = author.descriptor
      const license = this.licenses.find((l) => l.id === item.licenseId)
      item.license = license ? license.name : item.license
      const type = this.itemTypes.find((t) => t.id === item.itemTypeId)
      if (type) item.itemTypeName = type.name

      _adapter?.updateItem(item.id, {
        authorId: item.authorId ?? this.defaultAuthorId,
        licenseId: item.licenseId ?? this.defaultLicenseId,
        itemTypeId: item.itemTypeId ?? this.defaultItemTypeId,
        itemTemplateId: item.representationTemplate ?? null,
        rootItemId: item.rootItemId ?? null
      }).catch((e) => this._notifyError(e))
    },

    createItem(rootItemId: string | null = null, addToRoot = true, onCreated?: (realId: string) => void): Item {
      const item = this._createItemData(rootItemId)
      if (addToRoot) this.rootItems.push(item)
      void (async () => {
        try {
          const dto = await _adapter!.createItem({
            authorId: item.authorId ?? this.defaultAuthorId,
            licenseId: item.licenseId ?? this.defaultLicenseId,
            itemTypeId: item.itemTypeId ?? this.defaultItemTypeId,
            rootItemId: item.rootItemId ?? null
          })
          item.id = dto.itemId
          onCreated?.(dto.itemId)
          if (item.contents.length > 0) {
            const c0 = item.contents[0]
            const contentDto = await _adapter!.createContent(dto.itemId, {
              licenseId: c0.licenseId ?? this.defaultLicenseId,
              itemContentTypeId: c0.contentTypeId ?? this.defaultContentTypeId,
              authorId: c0.authorId ?? this.defaultAuthorId,
              purpose: c0.purpose,
              jsonSerializedContent: JSON.stringify(c0.jsonContent)
            })
            if (contentDto && item.contents[0]) {
              item.contents[0].id = contentDto.itemContentId
            }
          }
        } catch (e) {
          // Bei Teilfehler den Baum wieder mit dem DB-Stand abgleichen,
          // damit kein Geister-Item mit Temp-ID zurückbleibt.
          this._notifyError(e)
          await this.loadTree()
        }
      })()
      this.validate()
      return item
    },

    /**
     * Create an item from the creation form. Uses the chosen Typ/Autor/Lizenz
     * (or defaults when left empty — the form is not strict) and the entered
     * task text as the first content. Selects the new item afterwards.
     */
    createItemFromForm(
      form: { itemTypeId?: string; authorId?: string; licenseId?: string; text?: string },
      target: Collection | null = null
    ): Item {
      const item = this._createItemData(target?.rootItemId ?? null)
      item.authorId = form.authorId ?? this.defaultAuthorId
      item.licenseId = form.licenseId ?? this.defaultLicenseId
      item.itemTypeId = form.itemTypeId ?? this.defaultItemTypeId
      item.author = this.authors.find((a) => a.id === item.authorId)?.descriptor ?? item.author
      item.license = this.licenses.find((l) => l.id === item.licenseId)?.name ?? item.license
      item.itemTypeName = this.itemTypes.find((t) => t.id === item.itemTypeId)?.name ?? item.itemTypeName

      const c0 = item.contents[0]
      c0.authorId = item.authorId
      c0.licenseId = item.licenseId
      c0.license = item.license
      c0.purpose = 'Aufgabenstellung'
      c0.jsonContent = { text: form.text ?? '' }

      // In eine Collection einsortieren oder auf Top-Level legen
      let collectionItem: CollectionItem | null = null
      if (target) {
        collectionItem = {
          id: 'coll-item-' + Date.now().toString(),
          collectionId: target.id,
          item,
          position: target.order ? target.items.length + 1 : null
        }
        target.items.push(collectionItem)
      } else {
        this.rootItems.push(item)
      }

      void (async () => {
        try {
          const dto = await _adapter!.createItem({
            authorId: item.authorId ?? this.defaultAuthorId,
            licenseId: item.licenseId ?? this.defaultLicenseId,
            itemTypeId: item.itemTypeId ?? this.defaultItemTypeId,
            rootItemId: item.rootItemId ?? null
          })
          item.id = dto.itemId
          const contentDto = await _adapter!.createContent(dto.itemId, {
            licenseId: c0.licenseId ?? this.defaultLicenseId,
            itemContentTypeId: c0.contentTypeId ?? this.defaultContentTypeId,
            authorId: c0.authorId ?? this.defaultAuthorId,
            purpose: c0.purpose,
            jsonSerializedContent: JSON.stringify(c0.jsonContent)
          })
          if (contentDto) c0.id = contentDto.itemContentId
          if (target?.collectionId) {
            await _adapter!.addItemToCollection(target.collectionId, dto.itemId)
          }
          this.selectItem(collectionItem ?? item)
        } catch (e) {
          // Schlägt eine der Stufen fehl, den betroffenen Aggregat-Teilbaum
          // (Ziel-Kollektion oder Baum) wieder mit dem DB-Stand abgleichen.
          this._notifyError(e)
          await this._resyncAggregate(target)
        }
      })()

      if (target) this._syncOrderedCollectionItems(target)
      this.validate()
      return item
    },

    // ── Erstellungs-Dialog (geteilter Zustand) ──
    openCreateDialog(target: Collection | null = null) {
      this.createDialogTarget = target
      this.createDialogOpen = true
    },

    closeCreateDialog() {
      this.createDialogOpen = false
      this.createDialogTarget = null
    },

    createCollection(): Collection {
      const now = Date.now().toString()
      const authorId = this.defaultAuthorId
      const licenseId = this.defaultLicenseId
      const itemTypeId = this.defaultItemTypeId
      const collection: Collection = {
        id: 'coll-' + now,
        item_type: 'collection',
        author: this.authors.find((a) => a.id === authorId)?.descriptor ?? 'author',
        representationTemplate: null,
        license: this.licenses.find((l) => l.id === licenseId)?.name ?? null,
        tags: [],
        validators: [],
        modifiers: [],
        authorId,
        licenseId,
        itemTypeId,
        itemTypeName: this.itemTypes.find((t) => t.id === itemTypeId)?.name ?? null,
        // Eine Kollektion ist im Backend eine Item ohne eigenen Content.
        contents: [],
        items: [],
        order: false
      }
      this.rootItems.push(collection)
      // Backend-Modell: erst eine Item anlegen, dann zur Kollektion machen
      // (POST /items → POST /items/{id}/collection). So taucht die Kollektion
      // beim Neuladen über GET /items?root=true wieder auf.
      void (async () => {
        try {
          const dto = await _adapter!.createItem({
            authorId,
            licenseId,
            itemTypeId,
            rootItemId: null
          })
          collection.id = dto.itemId
          // item_collection_id merken — nötig für alle weiteren Collection-Calls
          const collDto = await _adapter!.convertItemToCollection(dto.itemId)
          collection.collectionId = collDto.collectionId ?? null
        } catch (e) {
          // Schlägt das Anlegen oder das Umwandeln fehl, bleibt collectionId
          // null (alle weiteren Collection-Calls würden still übersprungen) —
          // daher den Baum wieder mit dem DB-Stand abgleichen.
          this._notifyError(e)
          await this.loadTree()
        }
      })()
      this.validate()
      return collection
    },

    addItemToCollection(collection: Collection): CollectionItem {
      const rootId = collection.rootItemId ?? null
      const item = this.createItem(rootId, false, (realId) => {
        if (collection.collectionId) {
          _adapter?.addItemToCollection(collection.collectionId, realId)
            .catch(async (e) => {
              // Verknüpfung fehlgeschlagen → Kollektion mit dem DB-Stand
              // abgleichen (das lokal eingefügte Item wieder entfernen).
              this._notifyError(e)
              await this._resyncAggregate(collection)
            })
        }
      })
      const collectionItem: CollectionItem = {
        id: 'coll-item-' + Date.now().toString(),
        collectionId: collection.id,
        item,
        position: collection.order ? collection.items.length + 1 : null
      }
      collection.items.push(collectionItem)
      this._syncOrderedCollectionItems(collection)
      this.validate()
      return collectionItem
    },

    makeItemACollection(item: Item): Collection {
      if (checkIsCollection(item)) return item as Collection
      item.item_type = 'collection'
      item.items = []
      item.order = false
      this.validate()
      _adapter?.convertItemToCollection(item.id)
        .then((dto) => {
          if (dto) item.collectionId = dto.collectionId ?? null
        })
        .catch((e) => this._notifyError(e))
      return item as Collection
    },

    deleteItem(itemToDelete: Item) {
      const itemId = itemToDelete.id
      const parentId = this._detachItem(itemId)
      if (this.selectedItem && getInnerItem(this.selectedItem).id === itemId) {
        this.selectedItem = null
      }
      // Parent VOR dem async-Gap merken (nach _detachItem ist das Item schon
      // aus dem lokalen Baum entfernt).
      const parentForResync = parentId ? this._findCollectionById(parentId) : null
      _adapter?.deleteItem(itemId).catch(async (e) => {
        // Löschen fehlgeschlagen → das Item wieder aus dem DB-Stand herstellen.
        this._notifyError(e)
        await this._resyncAggregate(parentForResync)
      })
      if (parentId) {
        const parent = this._findCollectionById(parentId)
        if (parent) this._syncOrderedCollectionItems(parent)
      }
      this.validate()
    },

    deleteCollection(collectionToDelete: Collection) {
      // Kinder merken, BEVOR die Kollektion aus dem Baum entfernt wird.
      const children = collectionToDelete.items.map((ci) => ci.item)
      // Nur die Kollektion selbst loeschen. Das Backend entfernt dabei die
      // ItemCollection und ihre Sub-Item-Verknuepfungen; die enthaltenen
      // Aufgaben bleiben bestehen (sie koennen in anderen Kollektionen liegen).
      this.deleteItem(collectionToDelete)
      // Enthaltene Aufgaben nicht loeschen, sondern auf die Root-Ebene heben.
      children.forEach((child) => {
        // rootItemId NICHT anfassen: das ist der Varianten-Bezug (horizontal),
        // orthogonal zur Kollektions-Zugehoerigkeit. Nullen wuerde die
        // Varianten-Beziehung einer Aufgabe zerstoeren.
        if (!this.rootItems.some((ri) => ri.id === child.id)) {
          this.rootItems.push(child)
        }
      })
      this.validate()
    },

    // ── DnD / Reorder Actions ──

    updateCollectionItems(collection: Collection, newItems: TreeItem[]) {
      collection.items = newItems.map((item, index) => {
        const inner = getInnerItem(item)
        if (inner.id !== collection.id) {
          const oldParentId = this._detachItem(inner.id, collection.id)
          if (oldParentId) {
            if (oldParentId !== collection.id) {
              // Cross-collection move: remove from source, add to target
              // (Collection-Calls über item_collection_id, nicht item_id)
              const oldParent = this._findCollectionById(oldParentId)
              if (oldParent?.collectionId) {
                _adapter?.removeItemFromCollection(oldParent.collectionId, inner.id)
                  .catch((e) => this._notifyError(e))
              }
              if (collection.collectionId) {
                _adapter?.addItemToCollection(collection.collectionId, inner.id)
                  .catch((e) => this._notifyError(e))
              }
              if (oldParent) this._syncOrderedCollectionItems(oldParent)
            }
            // oldParentId === collection.id → item stayed, skip API (reorder only)
          } else {
            // Root-to-collection move: add to target
            if (collection.collectionId) {
              _adapter?.addItemToCollection(collection.collectionId, inner.id)
                .catch((e) => this._notifyError(e))
            }
          }
        }
        if (isCollectionItem(item)) {
          return { ...item, collectionId: collection.id, position: collection.order ? index + 1 : null } as CollectionItem
        }
        return {
          id: 'coll-item-' + Date.now().toString() + '-' + index,
          collectionId: collection.id,
          item: inner,
          position: collection.order ? index + 1 : null
        } as CollectionItem
      })
      this._syncOrderedCollectionItems(collection, true)
      this.validate()
    },

    updateRootItems(newItems: TreeItem[]) {
      const mapped = newItems.map((item) => {
        const inner = getInnerItem(item)
        const parentId = this._detachItem(inner.id)
        // Moved from a collection to root: notify backend
        if (parentId) {
          const oldParent = this._findCollectionById(parentId)
          if (oldParent?.collectionId) {
            _adapter?.removeItemFromCollection(oldParent.collectionId, inner.id)
              .catch((e) => this._notifyError(e))
          }
          if (oldParent) this._syncOrderedCollectionItems(oldParent)
        }
        return inner as Item
      })
      this.rootItems = mapped
      this.validate()
    },

    // ── Validation ──

    validate() {
      const issues = validateTreeData(this.rootItems)
      if (issues.length === 0) return
      const notifStore = useNotificationStore()
      for (const issue of issues) {
        notifStore.push(issue.message, 'error', 8000)
      }
    }
  }
})
