# Architektur & Vorführszenarien — PWI-Aufgabendatenbank

## 1. Was ich gemacht habe (und warum)

### Problem: 404/400 Fehler beim Anlegen von Items

Der `POST /api/items`-Endpoint wurde **gefunden**, aber der Service hat eine
`ResponseStatusException(NOT_FOUND)` geworfen, weil die vom Frontend gesendeten
UUIDs in der Datenbank nicht existierten.

**Frontend (exerciseStore.ts)** sendete vorher:
```typescript
authorId: '00000000-0000-0000-0000-000000000000'  // existiert nicht in DB
licenseId: '00000000-0000-0000-0000-000000000000'  // existiert nicht in DB
itemTypeId: '00000000-0000-0000-0000-000000000000'  // existiert nicht in DB
```

**Backend (ItemService.java:181)** versucht via `authorRepository.findById()` den
Author zu laden — Fehlschlag → `ResponseStatusException(HttpStatus.NOT_FOUND)`.

**Lösung:** Die UUIDs durch die tatsächlichen Seed-Daten aus
`database/init/init.sql` ersetzt (Zeile 24-27 in exerciseStore.ts):
```typescript
const SEED_AUTHOR_ID = 'd0000000-0000-0000-0000-000000000001'      // Prof. Siepermann
const SEED_LICENSE_ID = 'b0000000-0000-0000-0000-000000000001'      // CC-BY-4.0
const SEED_ITEM_TYPE_ID = 'e0000000-0000-0000-0000-000000000001'    // SQL-Abfrage
const SEED_CONTENT_TYPE_ID = 'a0000000-0000-0000-0000-000000000003' // application/json
```

---

## 2. Gesamtarchitektur (3-Schichten)

### Frontend (Vue 3 + Vite + Pinia) — Port 8085
```
┌──────────────────────────────────────────────────────────┐
│  Adb.vue (Root)                                          │
│  ├─ AdbToolbar.vue     — "Aufgabe erstellen" etc.        │
│  ├─ AdbStructure.vue   — Baumstruktur (linke Sidebar)   │
│  │  └─ AdbTreeItem.vue — Rekursive Tree-Node            │
│  │     ├─ AdbTreeFolder.vue  — Kollektion (Ordner)       │
│  │     └─ AdbTreeFile.vue    — Aufgabe (Datei)           │
│  └─ AdbEditor.vue      — Detailansicht (rechts)          │
│     └─ AdbContentList.vue — Inhaltsblöcke bearbeiten     │
│        └─ AdbContentEditor.vue — Texteditor              │
│                                                            │
│  exerciseStore.ts (Pinia) — zentrale State-Verwaltung     │
│       │                                                    │
│       ▼                                                    │
│  ApiAdapter (Interface)                                    │
│  ├─ AdbApiService      — echte HTTP-Aufrufe (axios)       │
│  └─ DevAdbApiService   — lokale Mock-Daten (Dummy-Modus)  │
└──────────────────────┬───────────────────────────────────┘
                       │ HTTP (axios, via Vite-Proxy)
                       ▼
```

**Adapter-Auswahl** in `main.ts:20`:
```typescript
const adapter = import.meta.env.VITE_ADB_API_MODE === "dummy"
  ? devAdbApi    // npm run dev:dummy → lokale Mock-Daten
  : adbApi;      // npm run dev       → echte API-Aufrufe
```

### Backend (Spring Boot) — Port 8080
```
┌─────────────────────────────────────────────────────┐
│  Controller-Ebene (REST)                             │
│  ├─ ItemController.java         /api/items          │
│  ├─ ItemCollectionController.java /api/collections  │
│  └─ ItemContentController.java   /api/contents      │
│         │                                            │
│         ▼                                            │
│  Service-Ebene (Geschäftslogik)                      │
│  ├─ ItemService.java                                 │
│  ├─ ItemCollectionService.java                       │
│  └─ ItemContentService.java                          │
│         │                                            │
│         ▼                                            │
│  Repository-Ebene (Spring Data JPA)                  │
│  ├─ ItemRepository.java         → item Tabelle      │
│  ├─ ItemCollectionRepository.java → item_collection  │
│  ├─ ItemContentRepository.java  → item_content       │
│  └─ ... (13 Repositories total)                     │
└──────────────────┬──────────────────────────────────┘
                   │ JDBC
                   ▼
```

### Datenbank (PostgreSQL 16) — Port 5432
```
┌──────────────────────────────────────────────────────────┐
│  Tabellen (17 Stück in 3 Ebenen)                          │
│                                                            │
│  Level 1: Referenztabellen (Lookup-Listen)                │
│  ├─ author, license, item_type, item_content_type         │
│  ├─ item_representation_template, tag, validator, modifier│
│                                                            │
│  Level 2: Kerntabellen                                    │
│  ├─ item            — Die Aufgabe an sich                  │
│  ├─ item_content    — Inhaltsbausteine (JSON/Blob)        │
│  └─ item_collection — Gruppierung von Aufgaben            │
│                                                            │
│  Level 3: Join-Tabellen (Verknüpfungen)                   │
│  ├─ item_contents        — Item ↔ Content (mit purpose)   │
│  ├─ item_collection_sub_item — Kollektion ↔ Items (pos.)  │
│  ├─ item_tags, item_validator, item_modifier              │
│  ├─ item_content_tags, item_content_types                 │
└──────────────────────────────────────────────────────────┘
```

---

## 3. Datenbank-Schema im Detail

### 3.1 Zentrale Konzepte

**Item (Aufgabe)**
- Jede Aufgabe ist ein `item` mit author, license und item_type
- `root_item_id` (selbstreferenzierend) → für **Varianten** einer Aufgabe
- Kein eigenes "title"-Feld → der Titel lebt als `Content`-Block (über Join-Tabelle `item_contents` mit `purpose`)
- `isCollection` wird dynamisch via `EXISTS(item_collection WHERE parent_item_id = item_id)` ermittelt

**ItemContent (Inhaltsbaustein)**
- Ein Content kann JSON (`json_serialized_content` als JSONB) oder Binärdaten (`blob_serialized_content` als BYTEA) enthalten
- Beispiel-JSON: `{"title": "SQL-Abfrage", "instruction": "Schreiben Sie..."}`
- Content wird über `item_contents`-Join-Tabelle mit einem Item und einem `purpose` verknüpft (z.B. "Aufgabenstellung", "Hinweis", "Lösung")

**ItemCollection (Kollektion)**
- Eine Kollektion gruppiert mehrere Items
- `"order"` (BOOLEAN) steuert, ob die Items eine **Reihenfolge** haben oder nicht
- Bei `order=true` haben Sub-Items eine `position` (1, 2, 3...) → Sequenz
- Bei `order=false` ist die `position` der Sub-Items `null` → ungeordnete Sammlung
- `parent_item_id` — optionaler FK auf Item: Eine Kolleption kann an ein Item "angehängt" werden (horizontale Erweiterung)

### 3.2 Wichtige FK-Beziehungen

```
item
├── author_id ──────────→ author (RESTRICT)
├── license_id ─────────→ license (RESTRICT)
├── item_type_id ───────→ item_type (RESTRICT)
├── item_template_id ───→ item_representation_template (SET NULL)
└── root_item_id ───────→ item (SET NULL) — Selbstreferenz

item_content
├── license_id ─────────→ license (RESTRICT)
├── item_content_type_id → item_content_type (RESTRICT)
└── author_id ──────────→ author (RESTRICT)

item_collection
└── parent_item_id ─────→ item (CASCADE)

item_collection_sub_item
├── item_collection_id ─→ item_collection (CASCADE)
└── subitem_id ─────────→ item (CASCADE)
```

---

## 4. Präsentationsszenarien (für den Dozenten)

### Szenario 1: Aufgabe anlegen und Inhalt hinzufügen

**Was passiert:**
1. Klick "Aufgabe erstellen" in der Toolbar
2. `POST /api/items` → neues Item in der DB
3. `POST /api/contents/by-item/{id}` → Content-Block + Verknüpfung

**Zeigen in der App:**
- Aufgabe erscheint sofort im Baum (optimistisches Update)
- Rechts im Editor: Content-Block mit Text kann bearbeitet werden

**DB-Effekt:** 1× `item`, 1× `item_content`, 1× `item_contents` (Join)

**Code-Pfad:** `AdbToolbar.vue` → `exerciseStore.createItem()` → `AdbApiService.createItem()` → `ItemController.create()` → `ItemService.createItem()` → `ItemRepository.save()`

### Szenario 2: Kollektion anlegen (ungeordnet)

**Was passiert:**
1. Klick "Kollektion erstellen"
2. `POST /api/collections` → `order: false`

**Zeigen in der App:**
- Ordner-Symbol im Baum
- Items per Kontextmenü oder Drag-and-Drop in die Kollektion ziehen

**Code-Pfad:** `AdbToolbar.vue` → `exerciseStore.createCollection()` → `AdbApiService.createCollection()` → `ItemCollectionController.create()` → `ItemCollectionService.create()`

### Szenario 3: Reihenfolge einschalten (ordered ↔ unordered)

**Was passiert:**
1. Kollektion auswählen
2. Klick auf nummerierte-Liste-Button im Editor
3. `PUT /api/collections/{id}/order` → `{ order: true/false }`

**Zeigen in der App:**
- Vorher: Items ohne Nummern
- Nachher: Items mit 1., 2., 3. … vor den Namen
- Die Backend-Logik setzt `position` auf 1,2,3 (beim Einschalten) oder `null` (beim Ausschalten)

**Code:** `exerciseStore.toggleCollectionOrder()` (Zeile 202)

### Szenario 4: Varianten (rootItemId)

**Was passiert:**
1. Aufgabe anlegen mit `rootItemId` = ID einer existierenden Aufgabe
2. In der DB: `root_item_id` zeigt auf die "Original"-Aufgabe

**Zeigen in der App:**
- Items mit selber `rootItemId` werden visuell grupiert

**Seed-Beispiel in der DB:** Item 5 (`40000000-...-000004`) hat `root_item_id = 40000000-...-000004` (ist eine Variante von Item 4)

### Szenario 5: Horizontale Erweiterung (Kollektion an ein Item anhängen)

**Was passiert:**
1. Auf einem Item im Kontextmenü "In Kollektion umwandeln"
2. `POST /api/items/{id}/collection` → erstellt `item_collection` mit `parent_item_id = item_id`
3. Danach können weitere Items in diese Kollektion verschoben werden

**Zeigen in der App:**
- Item wird zu einem Ordner (erweiterbar im Baum)
- Neue Items können per Drag-and-Drop hinein

### Szenario 6: Drag-and-Drop neu anordnen

**Was passiert:**
1. Innerhalb einer geordneten Kollektion: Item ziehen
2. `PUT /api/collections/{id}/items/{itemId}/position` → neue Position
3. Backend berechnet alle Geschwister-Positionen neu

**Zeigen in der App:**
- Nummern passen sich sofort an

**Code:** `vuedraggable` in `AdbTreeItem.vue` → `exerciseStore.updateCollectionItems()` → `_adapter.updateCollectionItemPosition()`

---

## 5. Wichtige Endpunkte (Übersicht für die Präsentation)

| Methode | Pfad | Zweck |
|---------|------|-------|
| GET | `/api/items?root=true` | Alle Root-Items laden |
| POST | `/api/items` | Aufgabe erstellen |
| POST | `/api/items/{id}/collection` | Item → Kollektion konvertieren |
| POST | `/api/collections` | Kollektion erstellen |
| PUT | `/api/collections/{id}` | Kollektion updaten (z.B. order togglen) |
| PUT | `/api/collections/{id}/order` | Reihenfolge ein-/ausschalten |
| POST | `/api/collections/{id}/items` | Item zu Kollektion hinzufügen |
| DELETE | `/api/collections/{id}/items/{itemId}` | Item aus Kollektion entfernen |
| PUT | `/api/collections/{id}/items/{itemId}/position` | Position eines Items ändern |
| GET | `/api/contents/by-item/{itemId}` | Alle Inhalte einer Aufgabe laden |
| POST | `/api/contents/by-item/{itemId}` | Neuen Inhalt für Aufgabe erstellen |

---

## 6. Starten der App (für die Vorführung)

### Production-ähnlich (Docker):
```bash
docker compose up --build
```
→ Frontend: http://localhost:8085
→ Backend:  http://localhost:8080 (nur intern)
→ Datenbank: Port 5432

### Entwicklung (getrennt):
```bash
# 1. Datenbank starten
docker compose up db -d

# 2. Backend starten (braucht DB_URL, DB_USER, DB_PASSWORD)
cd backend
$env:DB_URL="jdbc:postgresql://localhost:5432/pwi_aufgabendatenbank"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run

# 3. Frontend starten (in einem zweiten Terminal)
cd frontend
npm run dev
```

### Dummy-Modus (nur Frontend, keine DB nötig):
```bash
cd frontend
npm run dev:dummy
```
→ Alle Daten sind lokal in `dummy-data.ts`, kein Backend nötig
