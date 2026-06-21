# Gesamtprojekt-Plan: Aufgabendatenbank

> Dieses Dokument beschreibt den aktuellen Stand, alle inkonsistenten Stellen und den
> vollständigen Fahrplan zur Fertigstellung des Projekts — von der Datenbank über Java
> bis zum Vue-Frontend.

---

## 1. Aktueller Stand

### 1.1 Datenbank (`database/migrations/`)

| Datei | Status |
|---|---|
| `V1_001__Level_1_Basis.sql` | Fertig — Referenz-Tabellen (Author, License, ItemType, Tag, Validator, Modifier, ItemContentType, ItemRepresentationTemplate) |
| `V1_002__Level_2_Main.sql` | Fertig — Kerntabellen (Item, ItemContent, ItemCollection) |
| `V1_003__Level_3_Joins.sql` | Fertig — Join-Tabellen (Item_Contents, Item_Tags, Item_Content_Tags, Item_Content_Types, Item_Validator, Item_Modifier, Item_Collection_Sub_Item) |
| `V1_004_Testdaten.sql` | Fertig — Testdaten für manuelle Prüfung |
| `V1_005.sql` | Fertig — `item_collection.order` von INTEGER auf BOOLEAN migriert |

**Known Issues:**
- `position` in `item_collection_sub_item` ist `NOT NULL`, aber ungeordnete Collections brauchen `NULL`
- Alle Tabellen nutzen `SERIAL` (Integer), wir möchten **UUIDs**
- Keine `updated_at`-Spalte in `item_collection`

### 1.2 Backend (Spring Boot)

| Datei | Status |
|---|---|
| 15 Entities | Vorhanden, aber alle mit Integer-IDs |
| 8 DTOs | Vorhanden (`ItemCreateDto`, `ItemResponseDto`, `ItemContentCreateDto`, `ItemContentResponseDto`, `ItemCollectionCreateDto`, `ItemCollectionResponseDto`, `CollectionSubItemDto`, `ContentSummaryDto`) |
| 3 Controller | Vorhanden (Item, ItemContent, ItemCollection) |
| 3 Services | Vorhanden |
| 13 Repositories | Vorhanden, teilweise erweitert |

**Wesentliche Änderungen seit Letztem Stand:**
- `ItemCollectionController`: **OrderToggle + PositionUpdate Endpunkte entfernt** — nur noch GET/POST/PUT/DELETE + GET /{id}/items
- `ItemCollectionResponseDto.collectionOrder` ist jetzt `Integer` (nicht `Boolean`) — **Widerspruch zu DB (BOOLEAN)**
- `PositionUpdateDto` und `OrderToggleDto` existieren nicht mehr im Code (noch aber im alten Plan referenziert)
- `ItemRepository`: Neue Methoden `findByRootItemIsNull()`, `findByRootItem_ItemId()`
- `ItemContentsRepository`: Neue Methode `findByItem_ItemId()`
- `ItemCollectionSubItemRepository`: Neue Methode `findByCollection_ItemCollectionIdOrderByPositionAsc()`
- `ItemCollectionRepository`: Neue Methoden `findByParentItemIsNull()`, `existsByParentItem_ItemId()`
- `ItemResponseDto`: Felder `isCollection` und `contents` (List<ContentSummaryDto>) hinzugefügt
- `CollectionSubItemDto`: Eingebettetes `ItemResponseDto` für optionale vollständige Item-Daten

**Known Issues:**
- Alle IDs sind `Integer` statt `UUID`
- `ItemCollectionCreateDto.SubItemDto.position` ist `@NotNull` — ungeordnete Collections erlauben kein `null`
- `ItemCollectionResponseDto.collectionOrder` ist `Integer` — DB hat `BOOLEAN` (V1_005). **Typ-Muss synchronisiert werden.**
- Keine Endpunkte für: Contents pro Item abrufen, Items zwischen Collections verschieben, Items aus Collection entfernen
- `ItemService` prüft `isCollection` über `collectionRepository.existsByParentItem_ItemId()` — funktioniert, aber fragil

### 1.3 Frontend (Vue 3)

**Neue Dateien (seit PR #52):**

| Datei | Beschreibung |
|---|---|
| `feature/aufgabendatenbank/api-adapter.types.ts` | API-Adapter-Pattern mit DTOs (`ItemDTO`, `ContentDTO`, `CollectionItemDTO`) für Frontend-Backend-Kommunikation |
| `feature/aufgabendatenbank/adbApi.service.ts` | Echter HTTP-Adapter (axios) — implementiert `ApiAdapter`-Interface |
| `feature/aufgabendatenbank/dev-adb-api.service.ts` | Dev-Adapter mit Dummy-Daten + Konsolen-Logging |
| `feature/aufgabendatenbank/frontend-preparation.md` | Architektur-Doku, Endpoint-Mapping, Loading-Strategien |
| `feature/aufgabendatenbank/validation.ts` | Baum-Validierung (keine Duplikate, gültige Parents, keine dangling rootItemId) |
| `feature/aufgabendatenbank/validation.test.ts` | Tests für Validierung |
| `feature/aufgabendatenbank/editor/components/AdbContentEditor.vue` | Neuer Inline-Content-Editor |
| `feature/aufgabendatenbank/editor/components/AdbContentList.vue` | Neue Content-Liste mit "Hinzufügen"-Button |
| `feature/aufgabendatenbank/editor/components/AdbDeleteDialog.vue` | Neuer Vuetify-Lösch-Dialog |
| `frontend/frontend-backend-communication.md` | API-Vertrags-Doku (deutsch) |
| `src/components/NotificationToast.vue` | Globale Benachrichtigungen |

**Geänderte Dateien:**

| Datei | Änderung |
|---|---|
| `exerciseStore.ts` | Adapter-Injection, progressive Loading, CRUD, DnD-Reorder — deutlich erweitert (547 Zeilen) |
| `dummy-data.ts` | Neue Testdaten (ungeordnete + geordnete Collections, verschachtelte Sub-Collections) |
| `exerciseApiService.ts` | Als `@deprecated` markiert — wird durch `adbApi.service.ts` ersetzt |
| `AdbEditor.vue` | Erweitert: Item-Typ-Label, Order-Toggle, Delete-Button, Content-Liste |
| `AdbTreeFile.vue` | Kontextmenü mit "Zu Collection konvertieren" |
| `AdbTreeFolder.vue` | Lade-Spinner, Kontextmenü mit "Order togglen", "Item hinzufügen" |
| `AdbTreeItem.vue` | Vereinfacht — nur noch Wrapper für vuedraggable |
| `AdbStructure.vue` | Vereinfacht — `rootItems` an `AdbTreeItem` übergeben |

**Architektur-Entscheidung (PR #52):**
- Frontend nutzt jetzt **Adapter-Pattern** (`ApiAdapter` Interface)
- Im Dev-Modus: `DevAdbApiService` (lokal, ohne Backend)
- Für Echtbetrieb: `AdbApiService` (axios gegen `/api`)
- Store injiziert den Adapter — kein hartcodierter Service

**Known Issues:**
- `api-adapter.types.ts` definiert eigene DTOs — müssen mit Backend-DTOs synchronisiert werden
- `exerciseApiService.ts` ist deprecated aber noch vorhanden — kann entfernt werden
- Frontend-Types in `lib/types.ts` sind veraltet — `api-adapter.types.ts` ist die neue Quelle
- Dummy-Daten in `dummy-data.ts` strukturell anders als echte Backend-Antworten
- `Collection.order` im Frontend ist `boolean` — Backend liefert `Integer` (`collectionOrder`)

---

## 2. Inkonsistenzen im Detail

### 2.1 ID-Typen

| Schicht | Aktuell | Gewünscht |
|---|---|---|
| Datenbank | `SERIAL` (Integer) | `UUID` |
| Java Entities | `Integer` | `UUID` |
| Java DTOs | `Integer` | `String` (UUID-Serialisierung) |
| Frontend (alt) | `string` (Dummy-IDs) | `string` (UUIDs) |
| Frontend (neu) | `string` (via `api-adapter.types.ts`) | `string` (UUIDs) — passt schon |

### 2.2 Collection-Modell

| Schicht | Aktuell | Korrektur |
|---|---|---|
| Frontend (alt) | `Collection` erweitert `Item` (`item_type: 'collection'`) | Collection ist eine separate Entität |
| Frontend (neu) | `api-adapter.types.ts` hat eigenes `CollectionItemDTO` | Besser, aber nicht vollständig mit Backend synchronisiert |
| Backend | `ItemCollection` ist separate Entity mit `parentItemId` FK | Korrekt |
| DB | Eigene Tabelle `item_collection` | Korrekt |

**Entscheidung:** Wir halten das Backend-Modell (separate Entität) und passen das Frontend daran an.

### 2.3 `position` für ungeordnete Collections

| Schicht | Aktuell | Problem |
|---|---|---|
| DB | `position INTEGER NOT NULL` | `NULL` nicht erlaubt |
| Java | `Integer position` (nullable) | DB-Constraint verhindert null |
| Service | Setzt `position = null` bei `order=false` | SQL-Fehler |

**Lösung:** DB-Migration V1_007 erstellt (Branch `feature/db-position-nullable`).

### 2.4 Fehlende API-Endpunkte

Der alte `exerciseApiService.ts` (jetzt deprecated) und der neue `adbApi.service.ts` dokumentieren Endpunkte, die im Backend nicht existieren:

| Fehlender Endpunkt | Beschreibung | Priorität |
|---|---|---|
| `GET /api/contents/by-item/{itemId}` | Contents eines Items abrufen | Hoch |
| `POST /api/contents/by-item/{itemId}` | Content zu Item hinzufügen (mit Purpose) | Hoch |
| `POST /api/collections/{id}/items` | Item zu Collection hinzufügen | Hoch |
| `DELETE /api/collections/{collId}/items/{itemId}` | Item aus Collection entfernen | Hoch |
| `PUT /api/collections/{id}/order` | Order-Toggle (wurde aus Controller entfernt) | Mittel |
| `PUT /api/collections/{id}/items/{itemId}/position` | Position ändern (wurde aus Controller entfernt) | Mittel |

### 2.5 DTO-Konflikte

| Feld | Frontend (api-adapter.types.ts) | Backend-DTO |
|---|---|---|
| `Item.author` | `authorId: string` | `authorId: int` + `authorDescriptor: string` |
| `Item.item_type` | `itemTypeId: string` | `itemTypeId: int` + `itemTypeName: string` |
| `Item.license` | `licenseId: string \| null` | `licenseId: int` + `licenseName: string` |
| `Item.contents` | `ContentDTO[]` | `ContentSummaryDto[]` (nur IDs + Typ-Name) |
| `Content.jsonContent` | `jsonContent: Record<string, any>` | `jsonSerializedContent: string` (JSON-String) |
| `Content.blobContent` | `hasBlobContent: boolean` | `hasBlobContent: boolean` — passt |
| `Content.contentType` | `contentTypeId: string` | `itemContentTypeId: int` + `itemContentTypeName: string` |
| `Collection.order` | `order: boolean` | `collectionOrder: Integer` — **Typ-Mismatch** |

### 2.6 `collectionOrder` Typ-Mismatch (NEU)

**Kritischer Fund:** `ItemCollectionResponseDto.collectionOrder` ist `Integer`, aber:
- DB-V1_005 definiert `order` als `BOOLEAN`
- Frontend `api-adapter.types.ts` erwartet `order: boolean`
- Entity `ItemCollection.java` hat `Boolean collectionOrder`

**Entscheidung:** DTO muss auf `Boolean` geändert werden (passt zu DB und Frontend).

---

## 3. Gesamtänderungsplan

### Phase 1: Datenbank

#### 1a. UUID-Integration
Alle Primary Keys von `SERIAL` auf `UUID` umstellen.

**Betroffene Tabellen (alle außer Join-Tabellen):**
- `author` — `author_id: UUID`
- `license` — `license_id: UUID`
- `tag` — `tag_id: UUID`
- `item_type` — `item_type_id: UUID`
- `item_content_type` — `item_content_type_id: UUID`
- `item_representation_template` — `item_template_id: UUID`
- `validator` — `validator_id: UUID`
- `modifier` — `modifier_id: UUID`
- `item` — `item_id: UUID`
- `item_content` — `item_content_id: UUID`
- `item_collection` — `item_collection_id: UUID`

**Join-Tabellen — Fremdschlüssel werden ebenfalls zu UUIDs:**
- `item_contents` — `(item_id UUID, item_content_id UUID)`
- `item_tags` — `(item_id UUID, tag_id UUID)`
- `item_content_tags` — `(item_content_id UUID, tag_id UUID)`
- `item_content_types` — `(item_type_id UUID, item_content_type_id UUID)`
- `item_validator` — `(item_id UUID, validator_id UUID)`
- `item_modifier` — `(item_id UUID, modifier_id UUID)`
- `item_collection_sub_item` — `(item_collection_id UUID, subitem_id UUID)`

**Migration-Vorgehen:**
1. Neue Spalten mit `DEFAULT gen_random_uuid()` hinzufügen
2. Bestehende Daten übertragen
3. Alte Spalten und Constraints entfernen
4. Neue Primary Keys setzen

**Beispiel-Migration:**
```sql
-- 1. UUID-Extension aktivieren
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Neue UUID-Spalten hinzufügen
ALTER TABLE author ADD COLUMN author_id_new UUID DEFAULT gen_random_uuid();

-- 3. Daten übertragen
UPDATE author SET author_id_new = gen_random_uuid();

-- 4. Alte Spalte entfernen, neue umbenennen
ALTER TABLE author DROP COLUMN author_id;
ALTER TABLE author RENAME COLUMN author_id_new TO author_id;
ALTER TABLE author ADD PRIMARY KEY (author_id);
```

**Wichtig:** Bei Join-Tabellen müssen zuerst die FK-Spalten geändert werden, bevor die PKs der referenzierten Tabellen geändert werden.

#### 1b. `position` nullable machen
```sql
ALTER TABLE item_collection_sub_item
    ALTER COLUMN position DROP NOT NULL;
```

#### 1c. `updated_at` zu `item_collection` hinzufügen
```sql
ALTER TABLE item_collection
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
```

---

### Phase 2: Backend (Java)

#### 2a. UUID in Entities

**Vorgehen:** Alle Entities von `Integer` auf `UUID` umstellen.

**Beispiel Item.java:**
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "item_id", updatable = false, nullable = false)
private UUID itemId;
```

**Gilt für alle Entities:**
- `Author` — `UUID authorId`
- `License` — `UUID licenseId`
- `Tag` — `UUID tagId`
- `ItemType` — `UUID itemTypeId`
- `ItemContentType` — `UUID itemContentTypeId`
- `ItemRepresentationTemplate` — `UUID itemTemplateId`
- `Validator` — `UUID validatorId`
- `Modifier` — `UUID modifierId`
- `Item` — `UUID itemId`
- `ItemContent` — `UUID itemContentId`
- `ItemCollection` — `UUID itemCollectionId`

**Join-Tabellen — EmbeddedIds ebenfalls zu UUIDs:**
- `ItemContentsId` — `(UUID itemId, UUID itemContentId)`
- `ItemCollectionSubItemId` — `(UUID itemCollectionId, UUID subitemId)`

**@ManyToMany Join-Tabellen (Item ↔ Tag, Item ↔ Validator, etc.):**
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "item_tags",
    joinColumns = @JoinColumn(name = "item_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private Set<Tag> tags = new HashSet<>();
```
Die `@JoinColumn`-Namen bleiben gleich — Hibernate erstellt die Spalten automatisch als UUID.

#### 2b. DTOs auf UUID umstellen

Alle DTOs bekommen `String`-Felder für UUIDs (JSON-kompatibel):

**Beispiel ItemResponseDto.java:**
```java
private String itemId;
private String authorId;
private String authorDescriptor;
private String licenseId;
private String licenseName;
private String itemTypeId;
private String itemTypeName;
private String itemTemplateId;
private String rootItemId;
private Set<String> tagIds = new HashSet<>();
// ...
```

**Beispiel ItemCreateDto.java:**
```java
private String authorId;
private String licenseId;
private String itemTypeId;
private String itemTemplateId;
private String rootItemId;
private Set<String> tagIds = new HashSet<>();
private Set<String> validatorIds = new HashSet<>();
private Set<String> modifierIds = new HashSet<>();
```

#### 2c. Position-Validierung anpassen

**ItemCollectionCreateDto.SubItemDto:**
```java
// VORHER:
@NotNull(message = "Position ist Pflicht")
private Integer position;

// NACHHER:
private Integer position;  // null erlaubt für ungeordnete Collections
```

**PositionUpdateDto wiederherstellen (wurde aus Code entfernt, wird aber braucht):**
```java
public class PositionUpdateDto {
    private Integer position;  // null erlaubt
    // getter/setter
}
```

#### 2d. `collectionOrder` Typ korrigieren (NEU)

**Problem:** `ItemCollectionResponseDto.collectionOrder` ist `Integer`, aber DB und Entity nutzen `Boolean`.

**Lösung:**
```java
// VORHER (ItemCollectionResponseDto.java):
private Integer collectionOrder;
public Integer getCollectionOrder() { return collectionOrder; }
public void setCollectionOrder(Integer o) { this.collectionOrder = o; }

// NACHHER:
private Boolean order;
public Boolean getOrder() { return order; }
public void setOrder(Boolean o) { this.order = o; }
```

**Auch in `ItemCollectionCreateDto` prüfen:** `getOrder()` / `setOrder()` muss `Boolean` verwenden.

#### 2e. Fehlende Endpunkte implementieren

**1. Contents eines Items abrufen:**
```java
// ItemContentController
@GetMapping("/by-item/{itemId}")
public ResponseEntity<List<ItemContentResponseDto>> getContentsByItemId(
        @PathVariable Integer itemId) {
    return ResponseEntity.ok(contentService.getContentsByItemId(itemId));
}
```

**2. Content zu Item hinzufügen (mit Purpose):**
```java
// ItemContentController
@PostMapping("/by-item/{itemId}")
public ResponseEntity<ItemContentResponseDto> createForItem(
        @PathVariable Integer itemId,
        @Valid @RequestBody ItemContentCreateDto dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(contentService.createForItem(itemId, dto));
}
```

**3. Item aus Collection entfernen:**
```java
// ItemCollectionController
@DeleteMapping("/{collectionId}/items/{itemId}")
public ResponseEntity<Void> removeItemFromCollection(
        @PathVariable Integer collectionId,
        @PathVariable Integer itemId) {
    collectionService.removeItemFromCollection(collectionId, itemId);
    return ResponseEntity.noContent().build();
}
```

**4. Order-Toggle wiederherstellen:**
```java
// ItemCollectionController
@PutMapping("/{id}/order")
public ResponseEntity<ItemCollectionResponseDto> toggleOrder(
        @PathVariable Integer id,
        @Valid @RequestBody OrderToggleDto dto) {
    return ResponseEntity.ok(collectionService.toggleOrder(id, dto.getOrder()));
}
```

**5. Position-Update wiederherstellen:**
```java
// ItemCollectionController
@PutMapping("/{id}/items/{itemId}/position")
public ResponseEntity<Void> updateSubItemPosition(
        @PathVariable Integer id,
        @PathVariable Integer itemId,
        @Valid @RequestBody PositionUpdateDto dto) {
    collectionService.updateSubItemPosition(id, itemId, dto.getPosition());
    return ResponseEntity.noContent().build();
}
```

#### 2f. `ItemCollectionService` Korrekturen

**`toggleOrder()` muss `position` auf `NULL` setzen können:**
```java
@Transactional
public ItemCollectionResponseDto toggleOrder(Integer collectionId, Boolean newOrder) {
    ItemCollection collection = findCollectionOrThrow(collectionId);
    collection.setCollectionOrder(newOrder);

    List<ItemCollectionSubItem> subItems = subItemRepository
        .findByCollection_ItemCollectionIdOrderByPositionAsc(collectionId);

    for (int i = 0; i < subItems.size(); i++) {
        subItems.get(i).setPosition(newOrder ? i + 1 : null);
    }

    subItemRepository.saveAll(subItems);
    return convertToResponseDto(collectionRepository.save(collection));
}
```

**`updateSubItemPosition()` muss Recalculation der Geschwister übernehmen:**
```java
@Transactional
public void updateSubItemPosition(Integer collectionId, Integer itemId, Integer newPosition) {
    ItemCollection collection = findCollectionOrThrow(collectionId);

    List<ItemCollectionSubItem> subItems = subItemRepository
        .findByCollection_ItemCollectionIdOrderByPositionAsc(collectionId);

    // Altes Element finden und entfernen
    ItemCollectionSubItem moved = subItems.stream()
        .filter(s -> s.getSubItem().getItemId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SubItem nicht gefunden"));

    subItems.remove(moved);
    moved.setPosition(newPosition);

    // An neue Position einfügen
    int targetIndex = newPosition - 1;
    if (targetIndex >= 0 && targetIndex <= subItems.size()) {
        subItems.add(targetIndex, moved);
    } else {
        subItems.add(moved);
    }

    // Alle Positionen neu berechnen
    for (int i = 0; i < subItems.size(); i++) {
        subItems.get(i).setPosition(i + 1);
    }

    subItemRepository.saveAll(subItems);
}
```

#### 2g. `ItemService` — isCollection-Erkennung

**Aktuell (funktioniert):**
```java
dto.setCollection(
    collectionRepository.existsByParentItem_ItemId(item.getItemId())
);
```

**Bleibt so — ist korrekt und getestet.**

---

### Phase 3: Frontend (Vue 3)

#### 3a. API-Adapter-Pattern (NEU — stand by PR #52)

Das Frontend nutzt jetzt ein Adapter-Pattern zur Backend-Kommunikation:

```
ApiAdapter (Interface)
├── AdbApiService (axios, für Echtbetrieb)
└── DevAdbApiService (Dummy-Daten, für Entwicklung)
```

**`api-adapter.types.ts`** definiert die DTOs:
```typescript
interface ItemDTO {
  itemId: string
  authorId: string
  authorDescriptor: string
  licenseId: string | null
  licenseName: string | null
  itemTypeId: string
  itemTypeName: string
  rootItemId: string | null
  isCollection: boolean
  contents: ContentSummaryDTO[]
  tagIds: string[]
  validatorIds: string[]
  modifierIds: string[]
}
```

**Todo:** Diese DTOs müssen mit den Backend-DTOs synchronisiert werden (UUIDs, `collectionOrder` als Boolean).

#### 3b. `api-adapter.types.ts` anpassen

**Aktuell:**
```typescript
order: boolean  // im CollectionItemDTO
```

**Nachher (UUID + Boolean-Korrektur):**
```typescript
// Alle IDs sind UUID-Strings — passt schon
// collectionOrder muss Boolean sein (nicht Integer)
order: boolean  // bleibt boolean — DTO muss zum Backend passen
```

#### 3c. `exerciseStore.ts` — Adapter-Injection beibehalten

Der Store nutzt jetzt den injizierten Adapter:
```typescript
const store = useExerciseStore(adapter)  // adapter = AdbApiService oder DevAdbApiService
```

**Änderungen:**
1. `loadTree()` — lädt rootItems + Collections vom Adapter
2. `createItem()` — ruft `adapter.createItem()` auf
3. `deleteItem()` — ruft `adapter.deleteItem()` auf
4. `toggleCollectionOrder()` — ruft `adapter.updateCollection()` auf
5. `updateCollectionItems()` — ruft `adapter.reorderCollectionItems()` auf

**Bleibt so — funktioniert mit dem Adapter-Pattern.**

#### 3d. `lib/types.ts` entfernen/ersetzen

Die alten Types in `lib/types.ts` sind veraltet:
```typescript
// ALT (veraltet):
interface Item { id: string; item_type: string; author: string; ... }

// NEU (in api-adapter.types.ts):
interface ItemDTO { itemId: string; authorId: string; ... }
```

**Entscheidung:** `lib/types.ts` kann entfernt werden, wenn alle Komponenten `api-adapter.types.ts` nutzen.

#### 3e. Baumstruktur — funktioniert bereits

Die Baumstruktur ist mit PR #52 aktualisiert:
- `AdbTreeFile.vue` — Kontextmenü mit "Zu Collection konvertieren"
- `AdbTreeFolder.vue` — Lade-Spinner, Kontextmenü mit "Order togglen"
- `AdbTreeItem.vue` — vuedraggable-Wrapper

**Bleibt so — keine Änderungen nötig.**

#### 3f. Editor — funktioniert bereits

Der Editor ist mit PR #52 erweitert:
- `AdbEditor.vue` — Item-Typ-Label, Order-Toggle, Delete-Button
- `AdbContentEditor.vue` — Inline-Content-Editor
- `AdbContentList.vue` — Content-Liste mit "Hinzufügen"
- `AdbDeleteDialog.vue` — Lösch-Dialog

**Bleibt so — keine Änderungen nötig.**

#### 3g. Alten `exerciseApiService.ts` entfernen

Der alte Service ist als `@deprecated` markiert:
```typescript
/**
 * @deprecated — wird durch adbApi.service.ts ersetzt
 */
export class ExerciseApiService { ... }
```

**Todo:** Datei entfernen, nachdem alle Referenzen migriert sind.

---

## 4. Implementierungs-Reihenfolge

### Bereits erledigt (auf separaten Branches)

| Branch | Aufgabe | Status |
|---|---|---|
| `feature/db-uuid-migration` | DB-Migration V1_006: UUIDs für alle Tabellen | Erstellt |
| `feature/db-position-nullable` | DB-Migration V1_007: `position` nullable + `updated_at` | Erstellt |

### Nächste Schritte

| Phase | Aufgabe | Priorität | Geschätzt | Abhängigkeit |
|---|---|---|---|---|
| **2d** | `collectionOrder` Typ auf Boolean korrigieren | **Hoch** | 30min | — |
| **2c** | Position-Validierung anpassen + DTOs wiederherstellen | **Hoch** | 1h | — |
| **2e** | Fehlende Backend-Endpunkte (6 Stück) | **Hoch** | 2-3h | — |
| **2a** | Java Entities auf UUID umstellen | **Hoch** | 2-3h | DB-Migration |
| **2b** | Java DTOs auf UUID umstellen | **Hoch** | 1-2h | 2a |
| **3a** | `api-adapter.types.ts` mit Backend synchronisieren | **Hoch** | 1h | 2b |
| **3b** | `lib/types.ts` entfernen | Niedrig | 15min | 3a |
| **3g** | Alten `exerciseApiService.ts` entfernen | Niedrig | 15min | 3a |

### Optional / Später

| Phase | Aufgabe | Priorität | Geschätzt |
|---|---|---|---|
| **3d** | Baumstruktur优化 | Niedrig | — |
| **3e** | Editor优化 | Niedrig | — |

---

## 5. Zusammenfassung der Änderungen

### Was ist bereits passiert:

1. **DB-Migration V1_006** (Branch `feature/db-uuid-migration`): UUIDs für alle Tabellen
2. **DB-Migration V1_007** (Branch `feature/db-position-nullable`): `position` nullable + `updated_at`
3. **Frontend PR #52**: Adapter-Pattern, neue Komponenten, erweiterter Store

### Was muss noch geändert werden:

1. **Backend `collectionOrder`**: `Integer` → `Boolean` in `ItemCollectionResponseDto`
2. **Backend DTOs**: `PositionUpdateDto` und `OrderToggleDto` wiederherstellen
3. **Backend Endpunkte**: 6 fehlende Endpunkte implementieren
4. **Backend Entities**: `Integer` → `UUID` (nach DB-Migration)
5. **Backend DTOs**: `Integer` → `String` für UUIDs
6. **Frontend `api-adapter.types.ts`**: Mit Backend-DTOs synchronisieren
7. **Frontend**: Alte Dateien entfernen (`lib/types.ts`, `exerciseApiService.ts`)

### Was muss neu implementiert werden:

1. **Backend:** `GET /api/contents/by-item/{itemId}`, `POST /api/contents/by-item/{itemId}`
2. **Backend:** `POST /api/collections/{id}/items`, `DELETE /api/collections/{collId}/items/{itemId}`
3. **Backend:** `PUT /api/collections/{id}/order`, `PUT /api/collections/{id}/items/{itemId}/position`
4. **Frontend:** Synchronisation der DTOs mit Backend
