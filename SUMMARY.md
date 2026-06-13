# PWI-Aufgabendatenbank — Zusammenfassung

## Projektziel

Konzeption und Implementierung einer zentralen Web-Plattform zur Verwaltung von
Übungsaufgaben an der TH Mittelhessen. Ein bestehendes, hochschulweit abgestimmtes
Datenschema wird in eine praktisch nutzbare Anwendung überführt.

---

## 1. Datenbank (PostgreSQL + Flyway)

### Migrationen (`database/migrations/`)

| Migration | Inhalt |
|---|---|
| `V1_001__Level_1_Basis.sql` | Referenz-/Lookup-Tabellen: `item_content_type`, `license`, `tag` (hierarchisch), `author`, `item_type`, `item_representation_template`, `validator`, `modifier` |
| `V1_002__Level_2_Main.sql` | Kerntabellen: `item_content` (JSONB + BYTEA), `item` (zentrale Aufgaben-Entität mit self-ref `root_item_id`), `item_collection` (Sammlungen mit `order`-Flag) |
| `V1_003__Level_3_Joins.sql` | 7 Join-Tabellen: `item_content_types`, `item_contents`, `item_content_tags`, `item_tags`, `item_validator`, `item_modifier`, `item_collection_sub_item` (mit `position`) |
| `V1_004_Testdaten.sql` | Umfassende Testdaten: 6 Content-Typen, 4 Lizenzen, hierarchische Tags (SQL → Joins → INNER JOIN), 3 Autoren, 4 Aufgabentypen, 5 Items + 1 Variante, 1 Collection mit 4 Sub-Items |
| `V1_005.sql` | **Änderung:** `item_collection.order` von `INTEGER` auf `BOOLEAN` migriert (geordnet/ungeordnet) |

### Zentrale DB-Konzepte

- **Item** — zentrale Aufgaben-Entität; `root_item_id` als Self-Reference für Varianten
- **ItemContent** — Inhaltsbausteine getrennt von der Item-Struktur (JSONB für strukturierte Daten, BYTEA für Binärdateien)
- **ItemCollection** — Sammlungen von Aufgaben; `order` (BOOLEAN) steuert, ob Positionen vergeben werden
- **ItemCollectionSubItem** — Many-to-Many zwischen Collection und Item mit `position` für Sequenzierung
- **Tag** — hierarchisch (Self-Reference `parent_tag_id`)

---

## 2. Backend (Spring Boot 3.2.5, Java 21, Maven)

### Architektur

```
controller/ → service/ → repository/ → entity/
                           ↓
                         dto/
```

### Entities (15 Stück)

- **Author, License, ItemType, ItemContentType, ItemRepresentationTemplate** — Referenzdaten
- **Tag** — hierarchisch, mit Self-Reference
- **Validator, Modifier** — Prüf- und Transformationslogik
- **Item** — Kern-Entität mit @ManyToOne zu Author/License/ItemType/Template/RootItem und @ManyToMany zu Tag/Validator/Modifier
- **ItemContent** — JSONB + BYTEA Felder, @ManyToMany zu Tag
- **ItemContents** — Join-Tabelle Item ↔ ItemContent mit `purpose` (EmbeddedId)
- **ItemCollection** — parent_item_id (optional), order (BOOLEAN), @OneToMany zu SubItems
- **ItemCollectionSubItem / ItemCollectionSubItemId** — Join mit position (EmbeddedId)

### Controller & Endpunkte

**ItemController** (`/api/items`)
- `GET /api/items` — alle Items; Query-Params: `root=true` (root Items), `rootItemId={id}` (Kinder)
- `GET /api/items/{id}` — einzelnes Item mit Contents
- `POST /api/items` — Item anlegen
- `PUT /api/items/{id}` — Item aktualisieren
- `DELETE /api/items/{id}` — Item löschen

**ItemContentController** (`/api/contents`)
- `GET /api/contents` — alle Contents
- `GET /api/contents/{id}` — einzelner Content
- `GET /api/contents/{id}/blob` — BLOB-Daten abrufen
- `POST /api/contents` — Content anlegen
- `POST /api/contents/{id}/blob` — BLOB hochladen (MultipartFile)
- `PUT /api/contents/{id}` — Content aktualisieren
- `DELETE /api/contents/{id}` — Content löschen

**ItemCollectionController** (`/api/collections`)
- `GET /api/collections` — alle Collections
- `GET /api/collections/roots` — Root-Collections (ohne parentItem)
- `GET /api/collections/{id}` — einzelne Collection
- `GET /api/collections/{id}/items` — Sub-Items einer Collection
- `POST /api/collections` — Collection anlegen
- `PUT /api/collections/{id}` — Collection aktualisieren
- `PUT /api/collections/{id}/order` — Order togglen (`true` → Positionen vergeben, `false` → Positionen entfernen)
- `PUT /api/collections/{id}/items/{itemId}/position` — Position eines Sub-Items ändern
- `DELETE /api/collections/{id}` — Collection löschen

### Order-Management (aktuelle Branch `feat/itemcollection-order-management`)

- **OrderToggleDto** — `{ order: boolean }` zum Toggeln
- **PositionUpdateDto** — `{ position: integer (>= 1) }` zum Setzen einer Position
- `toggleOrder()` — setzt bei `order=true` sequenzielle Positionen (1,2,3…), bei `false` alle auf null
- `updateSubItemPosition()` — aktualisiert Position eines Sub-Items und berechnet Geschwister-Positionen neu

### Wichtige Service-Details

- **ItemService** — CRUD + Filter + automatische Erkennung ob Item eine Collection ist (`collectionRepository.existsByParentItem_ItemId()`) + ContentSummaries
- **ItemContentService** — CRUD + BLOB-Upload/Download
- **ItemCollectionService** — CRUD + Order-Toggle + Position-Updates

---

## 3. Frontend (Vue 3 + Vuetify + Tailwind CSS + TypeScript)

### Technologie-Stack

- **Vue 3** (Composition API, `<script setup>`)
- **Vuetify 4** — UI-Komponenten
- **Tailwind CSS 4** — Styling
- **Pinia** — State Management
- **Vue Router** (Hash-History)
- **vuedraggable** — Drag & Drop
- **Axios** — HTTP-Client (für geplante Backend-Anbindung)
- **Vitest** — Testing
- **ESLint + Prettier** — Linting/Formatting

### Routen

| Pfad | View | Beschreibung |
|---|---|---|
| `/` | ViewIntroduction | Einführung |
| `/home` | ViewHome | Startseite |
| `/login` | ViewLogin | Login |
| `/profile` | ViewProfile | Benutzerprofil |
| `/course` | ViewCourses | Kursübersicht |
| `/course/:id` | ViewCourse | Kursdetails |
| `/course/:id/signup` | ViewCourseSignup | Kurseinschreibung |
| `/course/:courseId/members` | ViewMembers | Mitgliederverwaltung |
| `/adb` | ViewAdb | **Aufgabendatenbank (Kernfeature)** |
| `/impressum` | ViewImpressum | Impressum |
| `/datenschutz` | ViewDatenschutz | Datenschutz |

### Feature: Aufgabendatenbank (`src/feature/aufgabendatenbank/`)

**Struktur:**
```
feature/aufgabendatenbank/
├── Adb.vue                      — Hauptkomponente (Split-View: Struktur + Editor)
├── AdbEditor.vue                — Editor für ausgewähltes Item
├── AdbToolbar.vue               — Symbolleiste (Neu, Löschen, etc.)
├── editor/
│   ├── AdbContentEditor.vue     — Content-Block-Editor
│   ├── AdbContentList.vue       — Liste aller Content-Blöcke
│   └── AdbDeleteDialog.vue      — Lösch-Dialog
├── structure/
│   ├── AdbStructure.vue         — Baumansicht (Tree-View)
│   └── components/
│       ├── AdbTreeItem.vue      — Baumknoten (Wrapper)
│       └── tree/
│           ├── AdbTreeFile.vue  — Blattknoten (normales Item)
│           └── AdbTreeFolder.vue— Ordnerknoten (Collection)
├── composables/
│   └── useSidebarResizer.ts     — Sidebar-Größenänderung
├── dummy-data.ts                — Beispiel-Daten
├── validation.ts                — Baum-Validierung
```

### Pinia Store: exerciseStore

Zentraler Store für die gesamte Aufgabendatenbank-Logik:

- **State:** `rootItems[]`, `selectedItem`
- **Getter:** `selectedInnerItem`, `isCollectionSelected`, `selectedCollection`, `isOrdered`
- **Actions:**
  - `selectItem()` — Knoten auswählen
  - `toggleCollectionOrder()` — Order togglen inkl. Positions-Neuvergabe
  - Content CRUD: `addContentToSelectedItem()`, `removeContentFromSelectedItem()`, `updateContentText()`, `updateContentPurpose()`
  - Item CRUD: `createItem()`, `createCollection()`, `addItemToCollection()`, `makeItemACollection()`, `deleteItem()`, `deleteCollection()`
  - DnD: `updateCollectionItems()`, `updateRootItems()`
  - Hilfsfunktionen: `_detachItem()`, `_createItemData()`
  - `validate()` — Validierung mit Benachrichtigungen

### TypeScript-Typen (`src/lib/types.ts`)

```typescript
interface Content { id, license, contentType, author, tags, purpose, jsonContent, blobContent }
interface Item { id, item_type, author, representationTemplate, license, tags, validators, modifiers, rootItemId, contents, items?, order? }
interface Collection extends Item { item_type: 'collection', items: CollectionItem[], order }
interface CollectionItem { id, collectionId, item, position }
```

### API-Service (`exerciseApiService.ts`)

Geplante REST-Kommunikation zum Backend mit dokumentierten Endpunkten für Items, Contents, Collections und CollectionItems. Derzeit dokumentiert aber noch nicht aktiv angebunden (Frontend arbeitet mit `dummy-data.ts`).

---

## 4. Dokumentation

| Datei | Inhalt |
|---|---|
| `Lastenpflichtheft/Lastenheft.md` | Anforderungsanalyse (Ziele, Ist/Soll, funktionale/nicht-funktionale Anforderungen, Datenmodell) |
| `Lastenpflichtheft/Pflichtheft.md` | Technische Spezifikation (Tech-Stack-Entscheidungen, Use Cases, Architektur, KI-Feature-Idee) |
| `docs/README.md` | Projektbeschreibung und allgemeine Anforderungen |
| `README.md` | Kurzvorstellung des Projekts |
| `AGENTS.md` | Arbeitsanweisungen für KI-Assistenten (Repo-Struktur, Konventionen, Datenbank-Aggregationsstrategie) |
| `CONTRINUTING.md` | Beitragsrichtlinien |

---

## 5. Git-Historie (wichtige Branches & Features)

| Branch | Feature |
|---|---|
| `feat/itemcollection-order-management` | **Aktuell.** Order-Management (toggle order, position update mit sibling-Neuberechnung) + Migration V1_005 |
| `feat/Backend-itemCollection-erweitern` | Collection-API um SubItems-Endpunkt erweitert (GET /collections/{id}/items) |
| `feat/Backend-itemApi-erweitern` | Item-API: Filter (`root`, `rootItemId`), `isCollection`-Feld, `contents`-Summary |
| `feat/adb-editor` | Frontend: ADB-Editor mit Baumstruktur, Drag & Drop, Content-Editor |
| `feat/itemContent-crud-endpoints` | Backend: ItemContent CRUD mit BLOB-Support |
| `feat/itemCollection-crud-endpoints` | Backend: ItemCollection CRUD (inkl. SubItems) |

---

## 6. Nächste Schritte / Offene Punkte

1. **Frontend-Backend-Integration:** Der `exerciseApiService` ist dokumentiert aber nicht aktiv angebunden; dummy-data.ts wird aktuell genutzt
2. **Erweiterte Validierung:** Validatoren und Modifier sind im Backend modelliert, aber noch nicht vollständig im Frontend nutzbar
3. **Tag/Validator/Modifier-Management** im Frontend
4. **Drag & Drop zwischen Collections** optimieren (Backend-Endpunkte für Move/Copy existieren)
5. **Tests** ausbauen (Backend-Tests, Frontend-Vitest-Tests)
