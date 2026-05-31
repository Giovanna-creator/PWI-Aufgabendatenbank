import axios, { type AxiosInstance } from 'axios'
import type { Item, Collection, CollectionItem, Content } from '@/lib/types'

/**
 * ## Aufgabendatenbank API Service
 *
 * Geplante REST-Kommunikation zwischen Frontend und Spring-Boot-Backend.
 * Der Service bildet alle aktuellen Operationen aus `useAdbActions` auf API-Aufrufe ab.
 *
 * ### Datenmodell (Backend → Frontend Mapping)
 *
 * | DB-Tabelle              | Frontend-Typ      | Bemerkung                                      |
 * |-------------------------|-------------------|-------------------------------------------------|
 * | `item`                  | `Item`            | item_type_id → item_type (String im Frontend)  |
 * | `item_content`          | `Content`         | json_serialized_content → jsonContent           |
 * | `item_contents`         | — (Join-Tabelle)  | purpose, verknüpft Item ↔ Content               |
 * | `item_collection`       | `Collection`      | `Item` mit item_type='collection'               |
 * | `item_collection_sub_item` | `CollectionItem` | position, verknüpft Collection ↔ Item          |
 * | `item` → root_item_id   | `Item.rootItemId` | Self-Referenz für Varianten (Implementation B)  |
 *
 * ### ID-Konvention
 * Das Backend verwendet **Integer**-IDs (SERIAL). Das Frontend arbeitet derzeit mit
 * **String**-IDs. Der Service erwartet, dass das Backend entsprechende DTOs mit
 * String-IDs ausliefert oder eine bidirektionale Konvertierung stattfindet.
 *
 * ### Aggregierte Endpunkte
 * Das Backend sollte aggregierte DTOs anbieten, die verschachtelte Objekte
 * (z. B. Item mit Contents, Collection mit Children) in einem Request liefern,
 * um die Anzahl der HTTP-Roundtrips zu minimieren.
 */
export class ExerciseApiService {
  private http: AxiosInstance

  constructor(baseURL = '/api') {
    this.http = axios.create({
      baseURL,
      headers: { 'Content-Type': 'application/json' }
    })
  }

  // ──────────────────────────────────────────────
  //  Items (Aufgaben / Übungen)
  // ──────────────────────────────────────────────

  /**
   * Gibt alle root-Items zurück (Items ohne rootItemId).
   * Dies sind die Einstiegspunkte des Übungsbaums.
   *
   * **GET** `/api/items?root=true`
   *
   * Das Backend liefert jedes Item als aggregiertes DTO mit:
   * - eingebetteten `contents` (über item_contents-Join)
   * - `item_type` aufgelöst aus item_type_id
   * - `author` aufgelöst aus author_id
   * - `license` aufgelöst aus license_id
   *
   * @returns Liste aller root-Items (Items ohne rootItemId)
   */
  async getRootItems(): Promise<Item[]> {
    const { data } = await this.http.get<Item[]>('/items', { params: { root: true } })
    return data
  }

  /**
   * Gibt ein einzelnes Item mit allen verschachtelten Inhalten zurück.
   *
   * **GET** `/api/items/{id}`
   *
   * @param id - Item-ID
   * @returns Das Item-DTO mit Contents
   */
  async getItem(id: string): Promise<Item> {
    const { data } = await this.http.get<Item>(`/items/${id}`)
    return data
  }

  /**
   * Gibt die Implementation-B-Kinder eines Items zurück
   * (Items, deren rootItemId auf die übergebene ID zeigt).
   *
   * **GET** `/api/items?rootItemId={id}`
   *
   * @param parentId - ID des Eltern-Items
   * @returns Liste der Kinder-Items
   */
  async getItemChildren(parentId: string): Promise<Item[]> {
    const { data } = await this.http.get<Item[]>('/items', { params: { rootItemId: parentId } })
    return data
  }

  /**
   * Erzeugt ein neues Item.
   *
   * **POST** `/api/items`
   *
   * Request-Body (Beispiel):
   * ```json
   * {
   *   "item_type": "exercise",
   *   "author": "admin",
   *   "rootItemId": null,
   *   "contents": [
   *     {
   *       "purpose": "title",
   *       "contentType": "text",
   *       "jsonContent": { "text": "Neue Aufgabe" }
   *     }
   *   ]
   * }
   * ```
   *
   * @param item - Item-Daten (ohne id, da serverseitig vergeben)
   * @returns Das gespeicherte Item mit generierter ID
   */
  async createItem(item: Partial<Item>): Promise<Item> {
    const { data } = await this.http.post<Item>('/items', item)
    return data
  }

  /**
   * Aktualisiert ein bestehendes Item (Metadaten, item_type, etc.).
   *
   * **PUT** `/api/items/{id}`
   *
   * @param id - Item-ID
   * @param item - Aktualisierte Item-Daten
   * @returns Das aktualisierte Item
   */
  async updateItem(id: string, item: Partial<Item>): Promise<Item> {
    const { data } = await this.http.put<Item>(`/items/${id}`, item)
    return data
  }

  /**
   * Ändert die rootItemId eines Items (Implementation B: Verschieben
   * eines Items als Kind eines anderen).
   *
   * **PATCH** `/api/items/{id}/root`
   *
   * @param id - Item-ID
   * @param rootItemId - Neue Eltern-ID oder null (um es wieder zum root zu machen)
   * @returns Das aktualisierte Item
   */
  async moveItem(id: string, rootItemId: string | null): Promise<Item> {
    const { data } = await this.http.patch<Item>(`/items/${id}/root`, { rootItemId })
    return data
  }

  /**
   * Löscht ein Item. Die Cascade-Logik (DB: ON DELETE SET NULL für
   * root_item_id, ON DELETE CASCADE für Join-Tabellen) sorgt für
   * die Bereinigung abhängiger Daten.
   *
   * **DELETE** `/api/items/{id}`
   *
   * @param id - Item-ID
   */
  async deleteItem(id: string): Promise<void> {
    await this.http.delete(`/items/${id}`)
  }

  // ──────────────────────────────────────────────
  //  Contents (Inhaltsbausteine)
  // ──────────────────────────────────────────────

  /**
   * Gibt alle Contents eines Items zurück.
   *
   * **GET** `/api/items/{itemId}/contents`
   *
   * @param itemId - Item-ID
   * @returns Liste der Content-Blöcke
   */
  async getContents(itemId: string): Promise<Content[]> {
    const { data } = await this.http.get<Content[]>(`/items/${itemId}/contents`)
    return data
  }

  /**
   * Erzeugt einen neuen Content-Block und verknüpft ihn via purpose
   * mit dem angegebenen Item.
   *
   * **POST** `/api/items/{itemId}/contents`
   *
   * Das Backend legt je nach contentTyp automatisch einen passenden
   * Eintrag in item_content und item_contents (mit purpose) an.
   *
   * @param itemId - ID des zugehörigen Items
   * @param content - Content-Daten
   * @returns Der gespeicherte Content mit generierter ID
   */
  async createContent(itemId: string, content: Partial<Content>): Promise<Content> {
    const { data } = await this.http.post<Content>(`/items/${itemId}/contents`, content)
    return data
  }

  /**
   * Aktualisiert einen bestehenden Content-Block.
   *
   * **PUT** `/api/contents/{id}`
   *
   * @param id - Content-ID
   * @param content - Aktualisierte Content-Daten
   * @returns Der aktualisierte Content
   */
  async updateContent(id: string, content: Partial<Content>): Promise<Content> {
    const { data } = await this.http.put<Content>(`/contents/${id}`, content)
    return data
  }

  /**
   * Löscht einen Content-Block.
   *
   * **DELETE** `/api/contents/{id}`
   *
   * @param id - Content-ID
   */
  async deleteContent(id: string): Promise<void> {
    await this.http.delete(`/contents/${id}`)
  }

  // ──────────────────────────────────────────────
  //  Collections (Aufgaben-Sammlungen)
  // ──────────────────────────────────────────────

  /**
   * Gibt alle Kollektionen zurück.
   *
   * **GET** `/api/collections`
   *
   * Jede Kollektion wird als aggregiertes DTO ausgeliefert:
   * - Basis-Item-Daten (author, license, item_type = 'collection')
   * - `order`-Flag
   * - Eingebettete `items` als CollectionItem[] (mit Item-DTOs und position)
   *
   * @returns Liste aller Kollektionen
   */
  async getCollections(): Promise<Collection[]> {
    const { data } = await this.http.get<Collection[]>('/collections')
    return data
  }

  /**
   * Gibt eine einzelne Kollektion mit allen Sub-Items zurück.
   *
   * **GET** `/api/collections/{id}`
   *
   * @param id - Kollektions-ID
   * @returns Die Kollektion mit Items
   */
  async getCollection(id: string): Promise<Collection> {
    const { data } = await this.http.get<Collection>(`/collections/${id}`)
    return data
  }

  /**
   * Erzeugt eine neue Kollektion.
   *
   * **POST** `/api/collections`
   *
   * Das Backend erzeugt:
   * 1. Ein Item mit item_type = 'collection'
   * 2. Einen Eintrag in item_collection (mit order = false)
   * und gibt das fertig aggregierte Collection-DTO zurück.
   *
   * @param collection - Kollektions-Daten (title/contents via eingebettetem Item)
   * @returns Die erstellte Kollektion
   */
  async createCollection(collection: Partial<Collection>): Promise<Collection> {
    const { data } = await this.http.post<Collection>('/collections', collection)
    return data
  }

  /**
   * Aktualisiert eine Kollektion (z. B. Toggle des order-Flags).
   *
   * **PUT** `/api/collections/{id}`
   *
   * Beim Toggle von `order` auf `true` weist das Backend automatisch
   * sequenzielle Positionen (1, 2, 3, …) zu. Bei `false` werden alle
   * Positionen auf null gesetzt.
   *
   * @param id - Kollektions-ID
   * @param collection - Aktualisierte Kollektions-Daten
   * @returns Die aktualisierte Kollektion
   */
  async updateCollection(id: string, collection: Partial<Collection>): Promise<Collection> {
    const { data } = await this.http.put<Collection>(`/collections/${id}`, collection)
    return data
  }

  /**
   * Löscht eine Kollektion. CASCADE in der DB entfernt auch die
   * Einträge in item_collection_sub_item. Das zugehörige Item bleibt
   * jedoch erhalten (es muss separat gelöscht werden, falls gewünscht).
   *
   * **DELETE** `/api/collections/{id}`
   *
   * @param id - Kollektions-ID
   */
  async deleteCollection(id: string): Promise<void> {
    await this.http.delete(`/collections/${id}`)
  }

  // ──────────────────────────────────────────────
  //  Collection Items (Sub-Items einer Kollektion)
  // ──────────────────────────────────────────────

  /**
   * Gibt alle Items einer Kollektion zurück.
   *
   * **GET** `/api/collections/{collectionId}/items`
   *
   * @param collectionId - Kollektions-ID
   * @returns Liste der CollectionItems mit position und Item-DTO
   */
  async getCollectionItems(collectionId: string): Promise<CollectionItem[]> {
    const { data } = await this.http.get<CollectionItem[]>(`/collections/${collectionId}/items`)
    return data
  }

  /**
   * Fügt ein Item zu einer Kollektion hinzu.
   *
   * **POST** `/api/collections/{collectionId}/items`
   *
   * Request-Body:
   * ```json
   * {
   *   "item": { "id": "item-123" },
   *   "position": null
   * }
   * ```
   *
   * Bei geordneter Kollektion (order = true) setzt das Backend die
   * Position automatisch ans Ende.
   *
   * @param collectionId - Kollektions-ID
   * @param item - Das hinzuzufügende Item (oder nur item.id)
   * @returns Das neu erstellte CollectionItem
   */
  async addItemToCollection(collectionId: string, item: Partial<CollectionItem>): Promise<CollectionItem> {
    const { data } = await this.http.post<CollectionItem>(`/collections/${collectionId}/items`, item)
    return data
  }

  /**
   * Aktualisiert die Position eines Items in einer Kollektion
   * (z. B. nach Drag-and-Drop Reorder).
   *
   * **PUT** `/api/collections/{collectionId}/items/{itemId}`
   *
   * Das Backend aktualisiert die Position des angegebenen Items und
   * berechnet die Positionen aller Geschwister-Items neu.
   *
   * @param collectionId - Kollektions-ID
   * @param itemId - ID des CollectionItem-Eintrags (nicht die Item-ID)
   * @param position - Neue Position
   * @returns Das aktualisierte CollectionItem
   */
  async updateCollectionItemPosition(
    collectionId: string,
    itemId: string,
    position: number | null
  ): Promise<CollectionItem> {
    const { data } = await this.http.put<CollectionItem>(
      `/collections/${collectionId}/items/${itemId}`,
      { position }
    )
    return data
  }

  /**
   * Verschiebt ein Item von einer Kollektion in eine andere
   * (oder ändert Position und Zielkollektion in einem Schritt).
   *
   * **PUT** `/api/collections/items/{itemId}`
   *
   * Request-Body:
   * ```json
   * {
   *   "collectionId": "neue-collection-id",
   *   "position": 1
   * }
   * ```
   *
   * @param itemId - ID des CollectionItem-Eintrags
   * @param targetCollectionId - Ziel-Kollektions-ID
   * @param position - Optionale neue Position
   * @returns Das aktualisierte CollectionItem
   */
  async moveCollectionItem(
    itemId: string,
    targetCollectionId: string,
    position?: number | null
  ): Promise<CollectionItem> {
    const { data } = await this.http.put<CollectionItem>(`/collections/items/${itemId}`, {
      collectionId: targetCollectionId,
      position
    })
    return data
  }

  /**
   * Entfernt ein Item aus einer Kollektion.
   *
   * **DELETE** `/api/collections/{collectionId}/items/{itemId}`
   *
   * @param collectionId - Kollektions-ID
   * @param itemId - ID des CollectionItem-Eintrags
   */
  async removeItemFromCollection(collectionId: string, itemId: string): Promise<void> {
    await this.http.delete(`/collections/${collectionId}/items/${itemId}`)
  }

  // ──────────────────────────────────────────────
  //  Convenience: Aggregierte Lade-Operationen
  // ──────────────────────────────────────────────

  /**
   * Lädt den gesamten Übungsbaum in einem Rutsch.
   *
   * Diese Methode kombiniert:
   * 1. `getRootItems()` – alle root-Items
   * 2. `getCollections()` – alle Kollektionen (enthalten bereits ihre Sub-Items)
   *
   * Die Collections werden als `Collection`-Typ (Item mit items[])
   * ausgeliefert. Root-Items ohne item_type='collection' haben ggf.
   * eigene Implementation-B-Kinder, die via `getItemChildren()` separat
   * geladen werden müssen (oder das Backend liefert sie direkt im DTO mit).
   *
   * @returns Kombinierte Liste aus root-Items und Kollektionen
   */
  async loadFullTree(): Promise<{ rootItems: Item[]; collections: Collection[] }> {
    const [rootItems, collections] = await Promise.all([
      this.getRootItems(),
      this.getCollections()
    ])
    return { rootItems, collections }
  }
}

export default new ExerciseApiService()
