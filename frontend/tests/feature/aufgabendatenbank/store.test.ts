import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useExerciseStore, setApiAdapter } from '@/stores/exerciseStore'
import type { ApiAdapter, ItemDTO, AuthorDTO, ItemTypeDTO, ContentTypeDTO, ValidatorDTO } from '@/feature/aufgabendatenbank/api-adapter.types'

function createMockAdapter(): ApiAdapter {
  return {
    getRootItems: vi.fn<() => Promise<ItemDTO[]>>().mockResolvedValue([]),
    searchItems: vi.fn<() => Promise<ItemDTO[]>>().mockResolvedValue([]),
    getCollectionItems: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    createItem: vi.fn<() => Promise<ItemDTO>>().mockResolvedValue({ itemId: 'mock-item' } as ItemDTO),
    updateItem: vi.fn<() => Promise<ItemDTO>>().mockResolvedValue({} as ItemDTO),
    createCollection: vi.fn<() => Promise<ItemDTO>>().mockResolvedValue({} as ItemDTO),
    convertItemToCollection: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    addItemToCollection: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    removeItemFromCollection: vi.fn<() => Promise<void>>().mockResolvedValue(),
    deleteItem: vi.fn<() => Promise<void>>().mockResolvedValue(),
    updateCollection: vi.fn<() => Promise<ItemDTO>>(),
    toggleCollectionOrder: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    updateCollectionItemPosition: vi.fn<() => Promise<void>>().mockResolvedValue(),
    getContents: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    createContent: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    updateContent: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    deleteContent: vi.fn<() => Promise<void>>().mockResolvedValue(),
    uploadBlob: vi.fn<() => Promise<void>>().mockResolvedValue(),
    getBlobUrl: vi.fn<() => string>().mockReturnValue(''),
    loadFullTree: vi.fn<() => Promise<ItemDTO[]>>().mockResolvedValue([]),
    getAuthors: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    getLicenses: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    getItemTypes: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    getContentTypes: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    createAuthor: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    createLicense: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    createItemType: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    createContentType: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    getItemsByRootId: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    getValidators: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    createValidator: vi.fn<() => Promise<ValidatorDTO>>(),
    updateValidator: vi.fn<() => Promise<any>>().mockResolvedValue({}),
    deleteValidator: vi.fn<() => Promise<void>>().mockResolvedValue(),
    getValidatorsForItem: vi.fn<() => Promise<any[]>>().mockResolvedValue([]),
    addValidatorToItem: vi.fn<() => Promise<ItemDTO>>().mockResolvedValue({} as ItemDTO),
    removeValidatorFromItem: vi.fn<() => Promise<void>>().mockResolvedValue()
  }
}

const DEFAULT_AUTHOR: AuthorDTO = {
  id: 'd0000000-0000-0000-0000-000000000001',
  descriptor: 'Default Author',
  mail: null
}
const DEFAULT_ITEM_TYPE: ItemTypeDTO = {
  id: 'e0000000-0000-0000-0000-000000000001',
  name: 'Standard',
  description: null
}
const DEFAULT_CONTENT_TYPE: ContentTypeDTO = {
  id: 'a0000000-0000-0000-0000-000000000003',
  name: 'text',
  description: null
}

let mock: ReturnType<typeof createMockAdapter>

beforeEach(() => {
  setActivePinia(createPinia())
  mock = createMockAdapter()
  setApiAdapter(mock)
})

describe('store getters', () => {
  it('authorId returns selectedAuthorId or fallback', () => {
    const store = useExerciseStore()
    expect(store.authorId).toBe('d0000000-0000-0000-0000-000000000001')
    store.selectedAuthorId = 'custom-id'
    expect(store.authorId).toBe('custom-id')
  })

  it('defaultAuthorId returns first author or fallback', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    expect(store.defaultAuthorId).toBe(DEFAULT_AUTHOR.id)
    store.authors = []
    expect(store.defaultAuthorId).toBe('d0000000-0000-0000-0000-000000000001')
  })

  it('defaultItemTypeId returns first type or fallback', () => {
    const store = useExerciseStore()
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    expect(store.defaultItemTypeId).toBe(DEFAULT_ITEM_TYPE.id)
    store.itemTypes = []
    expect(store.defaultItemTypeId).toBe('e0000000-0000-0000-0000-000000000001')
  })

  it('defaultContentTypeId returns first type or fallback', () => {
    const store = useExerciseStore()
    store.contentTypes = [DEFAULT_CONTENT_TYPE]
    expect(store.defaultContentTypeId).toBe(DEFAULT_CONTENT_TYPE.id)
    store.contentTypes = []
    expect(store.defaultContentTypeId).toBe('a0000000-0000-0000-0000-000000000003')
  })

  it('selectedInnerItem returns null when nothing selected', () => {
    const store = useExerciseStore()
    expect(store.selectedInnerItem).toBeNull()
  })

  it('isCollectionSelected returns false for exercise', () => {
    const store = useExerciseStore()
    store.selectedItem = { id: 'ex-1', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }
    expect(store.isCollectionSelected).toBe(false)
  })

  it('isCollectionSelected returns true for collection', () => {
    const store = useExerciseStore()
    store.selectedItem = { id: 'coll-1', item_type: 'collection', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null, items: [], order: false }
    expect(store.isCollectionSelected).toBe(true)
  })

  it('selectedCollection returns null for exercise', () => {
    const store = useExerciseStore()
    store.selectedItem = { id: 'ex-1', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }
    expect(store.selectedCollection).toBeNull()
  })

  it('selectedCollection returns the collection object', () => {
    const store = useExerciseStore()
    const coll = { id: 'coll-1', item_type: 'collection' as const, contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null, items: [], order: false }
    store.selectedItem = coll
    expect(store.selectedCollection?.id).toBe('coll-1')
    expect(store.selectedCollection?.item_type).toBe('collection')
  })

  it('isOrdered returns false for exercise', () => {
    const store = useExerciseStore()
    store.selectedItem = { id: 'ex-1', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }
    expect(store.isOrdered).toBe(false)
  })

  it('isOrdered returns true for ordered collection', () => {
    const store = useExerciseStore()
    store.selectedItem = { id: 'coll-1', item_type: 'collection', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null, items: [], order: true }
    expect(store.isOrdered).toBe(true)
  })

  it('hasActiveFilters returns false initially', () => {
    const store = useExerciseStore()
    expect(store.hasActiveFilters).toBe(false)
  })

  it('hasActiveFilters returns true when searchQuery is set', () => {
    const store = useExerciseStore()
    store.searchQuery = 'test'
    expect(store.hasActiveFilters).toBe(true)
  })

  it('hasActiveFilters returns true when filterAuthorId is set', () => {
    const store = useExerciseStore()
    store.filterAuthorId = 'some-author'
    expect(store.hasActiveFilters).toBe(true)
  })

  it('hasActiveFilters returns true when filterItemTypeId is set', () => {
    const store = useExerciseStore()
    store.filterItemTypeId = 'some-type'
    expect(store.hasActiveFilters).toBe(true)
  })

  it('hasActiveFilters returns false for whitespace-only search', () => {
    const store = useExerciseStore()
    store.searchQuery = '   '
    expect(store.hasActiveFilters).toBe(false)
  })
})

describe('createItem', () => {
  it('creates an item and adds it to rootItems', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    const item = store.createItem()

    expect(item).toBeDefined()
    expect(item.id).toMatch(/^item-/)
    expect(item.item_type).toBe('exercise')
    expect(item.contents).toHaveLength(1)
    expect(item.contents[0].purpose).toBe('Neuer Inhalt')
    expect(store.rootItems.length).toBe(1)
    expect(store.rootItems[0].id).toBe(item.id)
  })

  it('passes rootItemId to the item', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    const item = store.createItem('parent-id')
    expect(item.rootItemId).toBe('parent-id')
  })

  it('calls adapter.createItem', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    store.createItem(null, true)

    expect(mock.createItem).toHaveBeenCalledOnce()
    const payload = (mock.createItem as any).mock.calls[0][0]
    expect(payload.authorId).toBe(DEFAULT_AUTHOR.id)
    expect(payload.itemTypeId).toBe(DEFAULT_ITEM_TYPE.id)
  })

  it('does not add to rootItems when addToRoot is false', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    const item = store.createItem(null, false)
    expect(store.rootItems).toHaveLength(0)
  })
})

describe('createCollection', () => {
  it('creates a collection and adds to rootItems', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'new-item-id' } as ItemDTO)
    mock.convertItemToCollection = vi.fn().mockResolvedValue({ collectionId: 'new-coll-id' })

    const coll = store.createCollection()

    expect(coll).toBeDefined()
    expect(coll.item_type).toBe('collection')
    expect(coll.items).toEqual([])
    expect(coll.order).toBe(false)
    expect(coll.contents).toEqual([])
    expect(store.rootItems.length).toBe(1)
    expect(store.rootItems[0].id).toBe(coll.id)
  })

  it('calls adapter.createItem then convertItemToCollection', async () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'new-item-id' } as ItemDTO)
    mock.convertItemToCollection = vi.fn().mockResolvedValue({ collectionId: 'new-coll-id' })

    store.createCollection()

    expect(mock.createItem).toHaveBeenCalledOnce()
    await vi.waitFor(() => {
      expect(mock.convertItemToCollection).toHaveBeenCalledWith('new-item-id')
    })
  })
})

describe('createItemFromForm', () => {
  it('creates an item with form data on root level', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]
    store.licenses = [{ id: 'b0000000-0000-0000-0000-000000000001', name: 'CC0' }]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'form-item' } as ItemDTO)

    const item = store.createItemFromForm({
      text: 'Testaufgabe',
      authorId: DEFAULT_AUTHOR.id,
      itemTypeId: DEFAULT_ITEM_TYPE.id
    })

    expect(item).toBeDefined()
    expect(item.contents[0].jsonContent.text).toBe('Testaufgabe')
    expect(item.contents[0].purpose).toBe('Aufgabenstellung')
    expect(store.rootItems.length).toBe(1)
    expect(store.rootItems[0].id).toBe(item.id)
  })

  it('creates an item inside a collection', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'form-item' } as ItemDTO)

    const target = {
      id: 'coll-1',
      item_type: 'collection' as const,
      author: 'test',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      contents: [],
      items: [],
      order: false
    }
    store.rootItems.push(target)

    const item = store.createItemFromForm({ text: 'In Collection' }, target)

    expect(target.items).toHaveLength(1)
    expect(target.items[0].item.id).toBe(item.id)
  })

  it('assigns sequential position for ordered collection', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'form-item' } as ItemDTO)

    const target = {
      id: 'coll-1',
      item_type: 'collection' as const,
      author: 'test',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      contents: [],
      items: [],
      order: true
    }
    store.rootItems.push(target)
    const item1 = store.createItemFromForm({ text: 'First' }, target)
    const item2 = store.createItemFromForm({ text: 'Second' }, target)

    expect(target.items[0].position).toBe(1)
    expect(target.items[1].position).toBe(2)
  })
})

describe('addItemToCollection', () => {
  it('creates an item and adds it to the collection', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'new-item' } as ItemDTO)

    const coll: any = {
      id: 'coll-1',
      collectionId: 'coll-backend-1',
      item_type: 'collection',
      items: [],
      order: false,
      author: 'test',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      contents: [],
      rootItemId: null
    }
    store.rootItems.push(coll)

    const ci = store.addItemToCollection(coll)

    expect(coll.items).toHaveLength(1)
    expect(ci.collectionId).toBe('coll-1')
    expect(ci.item).toBeDefined()
    expect(ci.position).toBeNull()
  })

  it('assigns position for ordered collection', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'new-item' } as ItemDTO)

    const coll: any = {
      id: 'coll-1',
      collectionId: 'coll-backend-1',
      item_type: 'collection',
      items: [],
      order: true,
      author: 'test',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      contents: [],
      rootItemId: null
    }
    store.rootItems.push(coll)

    store.addItemToCollection(coll)
    expect(coll.items[0].position).toBe(1)
  })
})

describe('toggleCollectionOrder', () => {
  it('enables order and assigns positions', () => {
    const store = useExerciseStore()
    const coll: any = {
      id: 'coll-1',
      collectionId: 'coll-backend-1',
      item_type: 'collection',
      items: [
        { id: 'ci-1', collectionId: 'coll-1', item: { id: 'a' }, position: null },
        { id: 'ci-2', collectionId: 'coll-1', item: { id: 'b' }, position: null }
      ],
      order: false
    }

    store.toggleCollectionOrder(coll)

    expect(coll.order).toBe(true)
    expect(coll.items[0].position).toBe(1)
    expect(coll.items[1].position).toBe(2)
  })

  it('disables order (keeps positions intact)', () => {
    const store = useExerciseStore()
    const coll: any = {
      id: 'coll-1',
      collectionId: 'coll-backend-1',
      item_type: 'collection',
      items: [
        { id: 'ci-1', collectionId: 'coll-1', item: { id: 'a' }, position: 1 },
        { id: 'ci-2', collectionId: 'coll-1', item: { id: 'b' }, position: 2 }
      ],
      order: true
    }

    store.toggleCollectionOrder(coll)

    expect(coll.order).toBe(false)
    expect(coll.items[0].position).toBe(1)
  })

  it('calls adapter.toggleCollectionOrder when collectionId is set', () => {
    const store = useExerciseStore()
    mock.toggleCollectionOrder = vi.fn().mockResolvedValue({})
    const coll: any = {
      id: 'coll-1',
      collectionId: 'coll-backend-1',
      item_type: 'collection',
      items: [],
      order: false
    }

    store.toggleCollectionOrder(coll)
    expect(mock.toggleCollectionOrder).toHaveBeenCalledWith('coll-backend-1', { order: true })
  })
})

describe('deleteItem and deleteCollection', () => {
  it('deleteItem removes item from rootItems', () => {
    const store = useExerciseStore()
    const item: any = { id: 'del-item', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }
    store.rootItems.push(item)

    store.deleteItem(item)

    expect(store.rootItems.length).toBe(0)
  })

  it('deleteItem clears selection when deleted item is selected', () => {
    const store = useExerciseStore()
    const item: any = { id: 'del-item', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }
    store.rootItems.push(item)
    store.selectedItem = item

    store.deleteItem(item)

    expect(store.selectedItem).toBeNull()
  })

  it('deleteCollection deletes parent and all children', () => {
    const store = useExerciseStore()
    const child: any = { id: 'child', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }
    const coll: any = {
      id: 'coll-del',
      item_type: 'collection',
      contents: [],
      tags: [],
      validators: [],
      modifiers: [],
      author: 'a',
      representationTemplate: null,
      license: null,
      items: [{ id: 'ci-del', collectionId: 'coll-del', item: child, position: null }],
      order: false
    }
    store.rootItems.push(coll)

    store.deleteCollection(coll)

    expect(store.rootItems.length).toBe(0)
  })
})

describe('makeItemACollection', () => {
  it('converts an exercise to a collection', () => {
    const store = useExerciseStore()
    const item: any = { id: 'ex-to-coll', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }

    const coll = store.makeItemACollection(item)

    expect(coll.item_type).toBe('collection')
    expect(coll.items).toEqual([])
    expect(coll.order).toBe(false)
  })

  it('is idempotent for existing collections', () => {
    const store = useExerciseStore()
    const coll: any = { id: 'already-coll', item_type: 'collection', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null, items: [], order: false }

    const result = store.makeItemACollection(coll)

    expect(result.id).toBe(coll.id)
  })
})

describe('updateItemMeta', () => {
  it('updates author, license, and type on the item', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR, { id: 'author-2', descriptor: 'New Author', mail: null }]
    store.licenses = [{ id: 'lic-1', name: 'MIT' }]
    store.itemTypes = [DEFAULT_ITEM_TYPE, { id: 'type-2', name: 'Advanced', description: null }]

    const item: any = { id: 'meta-item', authorId: DEFAULT_AUTHOR.id, licenseId: null, itemTypeId: DEFAULT_ITEM_TYPE.id, author: DEFAULT_AUTHOR.descriptor, license: null, itemTypeName: DEFAULT_ITEM_TYPE.name }

    store.updateItemMeta(item, { authorId: 'author-2', licenseId: 'lic-1', itemTypeId: 'type-2' })

    expect(item.authorId).toBe('author-2')
    expect(item.licenseId).toBe('lic-1')
    expect(item.itemTypeId).toBe('type-2')
    expect(item.author).toBe('New Author')
    expect(item.license).toBe('MIT')
    expect(item.itemTypeName).toBe('Advanced')
  })

  it('calls adapter.updateItem with correct payload', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR, { id: 'author-2', descriptor: 'New Author', mail: null }]
    store.licenses = [{ id: 'lic-1', name: 'MIT' }]
    store.itemTypes = [DEFAULT_ITEM_TYPE, { id: 'type-2', name: 'Advanced', description: null }]

    const item: any = { id: 'meta-item', authorId: DEFAULT_AUTHOR.id, licenseId: null, itemTypeId: DEFAULT_ITEM_TYPE.id, author: DEFAULT_AUTHOR.descriptor, license: null, itemTypeName: DEFAULT_ITEM_TYPE.name }

    store.updateItemMeta(item, { authorId: 'author-2', licenseId: 'lic-1', itemTypeId: 'type-2' })

    expect(mock.updateItem).toHaveBeenCalledOnce()
  })
})

describe('validator actions', () => {
  it('createValidator adds the validator and returns it', async () => {
    const store = useExerciseStore()
    const dto: ValidatorDTO = { validatorId: 'v-1', description: 'Range check', validator: 'range(1,100)' }
    mock.createValidator = vi.fn().mockResolvedValue(dto)

    const result = await store.createValidator('Range check', 'range(1,100)')

    expect(result).toEqual(dto)
    expect(store.allValidators.length).toBe(1)
    expect(store.allValidators[0].validatorId).toBe('v-1')
  })

  it('linkValidatorToSelectedItem adds validator to item', async () => {
    const store = useExerciseStore()
    const inner: any = { id: 'val-item', validators: [] }
    store.selectedItem = inner

    await store.linkValidatorToSelectedItem('v-1')

    expect(inner.validators).toContain('v-1')
    expect(mock.addValidatorToItem).toHaveBeenCalledWith('val-item', 'v-1')
  })

  it('unlinkValidatorFromSelectedItem removes validator from item', async () => {
    const store = useExerciseStore()
    const inner: any = { id: 'val-item', validators: ['v-1', 'v-2'] }
    store.selectedItem = inner

    await store.unlinkValidatorFromSelectedItem('v-1')

    expect(inner.validators).toEqual(['v-2'])
    expect(mock.removeValidatorFromItem).toHaveBeenCalledWith('val-item', 'v-1')
  })

  it('linkValidatorToSelectedItem is no-op when nothing is selected', async () => {
    const store = useExerciseStore()
    await store.linkValidatorToSelectedItem('v-1')
    expect(mock.addValidatorToItem).not.toHaveBeenCalled()
  })
})

describe('variant actions', () => {
  it('createVariant creates a variant with rootItemId', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.itemTypes = [DEFAULT_ITEM_TYPE]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]

    store.variants = []
    mock.createItem = vi.fn().mockResolvedValue({ itemId: 'variant-id' } as ItemDTO)
    mock.createContent = vi.fn().mockResolvedValue({ itemContentId: 'content-id' })

    store.createVariant('base-item')

    expect(store.variants).toHaveLength(1)
    expect(store.variants[0].rootItemId).toBe('base-item')
  })

  it('loadVariants sets variants from adapter', async () => {
    const store = useExerciseStore()
    mock.getItemsByRootId = vi.fn().mockResolvedValue([
      { itemId: 'v-1', authorDescriptor: 'test', tagIds: [], validatorIds: [], modifierIds: [], isCollection: false, contents: [], createdAt: '', updatedAt: '', authorId: DEFAULT_AUTHOR.id, itemTypeId: DEFAULT_ITEM_TYPE.id, itemTypeName: 'Standard', itemTemplateId: null, rootItemId: 'base', licenseId: null, licenseName: null }
    ])
    mock.getContents = vi.fn().mockResolvedValue([])

    await store.loadVariants('base')

    expect(store.variants).toHaveLength(1)
    expect(store.variants[0].id).toBe('v-1')
  })

  it('getVariantCount returns the count', async () => {
    const store = useExerciseStore()
    mock.getItemsByRootId = vi.fn().mockResolvedValue([{}, {}])

    const count = await store.getVariantCount('base')

    expect(count).toBe(2)
  })
})

describe('filter / search actions', () => {
  it('setSearchQuery updates state', () => {
    const store = useExerciseStore()
    vi.useFakeTimers()
    store.setSearchQuery('test query')
    expect(store.searchQuery).toBe('test query')
    vi.useRealTimers()
  })

  it('setFilterAuthorId updates state', () => {
    const store = useExerciseStore()
    store.setFilterAuthorId('author-1')
    expect(store.filterAuthorId).toBe('author-1')
  })

  it('setFilterItemTypeId updates state', () => {
    const store = useExerciseStore()
    store.setFilterItemTypeId('type-1')
    expect(store.filterItemTypeId).toBe('type-1')
  })

  it('clearFilters resets all filter state', () => {
    const store = useExerciseStore()
    store.searchQuery = 'test'
    store.filterAuthorId = 'author-1'
    store.filterItemTypeId = 'type-1'
    store.filterTag = 'tag'
    store.filteredItems = [{ itemId: '1' } as any]

    store.clearFilters()

    expect(store.searchQuery).toBe('')
    expect(store.filterAuthorId).toBeNull()
    expect(store.filterItemTypeId).toBeNull()
    expect(store.filterTag).toBe('')
    expect(store.filteredItems).toBeNull()
  })
})

describe('selectItem', () => {
  it('sets selectedItem and clears variants', () => {
    const store = useExerciseStore()
    store.variants = [{ id: 'old-variant' } as any]
    const item: any = { id: 'select-me', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 'a', representationTemplate: null, license: null }

    store.selectItem(item)

    expect(store.selectedItem?.id).toBe('select-me')
    expect(store.selectedItem?.item_type).toBe('exercise')
    expect(store.variants).toEqual([])
  })
})

describe('content actions', () => {
  it('addContentToSelectedItem adds content and calls adapter', () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]
    store.contentTypes = [DEFAULT_CONTENT_TYPE]
    mock.createContent = vi.fn().mockResolvedValue({ itemContentId: 'new-content-id' })
    const inner: any = { id: 'content-item', contents: [], authorId: DEFAULT_AUTHOR.id, author: 'test', license: null, licenseId: null }
    store.selectedItem = inner

    store.addContentToSelectedItem()

    expect(inner.contents).toHaveLength(1)
    expect(inner.contents[0].purpose).toBe('Neuer Inhalt')
    expect(mock.createContent).toHaveBeenCalled()
  })

  it('removeContentFromSelectedItem removes content', () => {
    const store = useExerciseStore()
    const inner: any = { id: 'rm-item', contents: [{ id: 'c-1', purpose: 'test' }] }
    store.selectedItem = inner

    store.removeContentFromSelectedItem(0)

    expect(inner.contents).toHaveLength(0)
  })
})

describe('updateCollectionItems (DnD reorder)', () => {
  it('reorders items within the same collection', () => {
    const store = useExerciseStore()
    const itemA: any = { id: 'a', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 't', representationTemplate: null, license: null }
    const itemB: any = { id: 'b', item_type: 'exercise', contents: [], tags: [], validators: [], modifiers: [], author: 't', representationTemplate: null, license: null }
    const coll: any = {
      id: 'coll-1',
      collectionId: 'coll-backend-1',
      item_type: 'collection',
      items: [
        { id: 'ci-a', collectionId: 'coll-1', item: itemA, position: 1 },
        { id: 'ci-b', collectionId: 'coll-1', item: itemB, position: 2 }
      ],
      order: true,
      rootItemId: null,
      author: 't',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      contents: []
    }

    store.updateCollectionItems(coll, [coll.items[1], coll.items[0]])

    expect(coll.items[0].item.id).toBe('b')
    expect(coll.items[1].item.id).toBe('a')
    expect(coll.items[0].position).toBe(1)
    expect(coll.items[1].position).toBe(2)
  })
})

describe('createReference', () => {
  it('creates an author via adapter and appends to list', async () => {
    const store = useExerciseStore()
    const dto: AuthorDTO = { id: 'new-author', descriptor: 'New Author', mail: null }
    mock.createAuthor = vi.fn().mockResolvedValue(dto)

    const result = await store.createReference('author', 'New Author')

    expect(result).toEqual(dto)
    expect(store.authors.length).toBe(1)
    expect(store.authors[0].id).toBe('new-author')
  })

  it('rejects duplicate names', async () => {
    const store = useExerciseStore()
    store.authors = [DEFAULT_AUTHOR]

    const result = await store.createReference('author', DEFAULT_AUTHOR.descriptor)

    expect(result).toBeNull()
    expect(mock.createAuthor).not.toHaveBeenCalled()
  })
})
