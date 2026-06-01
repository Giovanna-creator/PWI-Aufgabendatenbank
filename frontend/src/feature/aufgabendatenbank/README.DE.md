# Adb (Aufgabendatenbank) Feature

UI für die Aufgabendatenbank zum Erstellen, Bearbeiten und Organisieren von
Übungen (Items) und Sammlungen (Collections).

## Verzeichnisstruktur

```
aufgabendatenbank/
├── __tests__/                       (entfernt – Tests liegen in tests/ im Projektroot)
├── composables/
│   └── useSidebarResizer.ts         Ziehbarer Sidebar-Splitter
├── editor/
│   ├── AdbEditor.vue                Rechtes Eigenschaften-Panel
│   └── components/
│       ├── AdbContentEditor.vue     Einzelnen Content-Block bearbeiten
│       └── AdbContentList.vue       Liste der Content-Blöcke eines Items
├── structure/
│   ├── AdbStructure.vue             Baumwurzel – rendert alle rootItems
│   ├── components/
│   │   ├── AdbTreeItem.vue          Rekursiver DnD-Knoten-Wrapper
│   │   └── tree/
│   │       ├── AdbTreeFile.vue      Blattknoten (Übung oder Nicht-Collection-Item)
│   │       └── AdbTreeFolder.vue    Collection-Knoten (aufklappbarer Ordner)
├── toolbar/
│   └── AdbToolbar.vue               Obere Leiste mit Erstell-Buttons
├── Adb.vue                          Root-Layout (Sidebar + Resizer + Editor)
├── dummy-data.ts                    Seed-Daten für die Entwicklung
├── validation.ts                    Validierungsregeln für die Baumdaten
├── README.md
└── README.DE.md
```

## State (Zustand)

Der gesamte Zustand lebt in `src/stores/exerciseStore.ts` (Pinia). Komponenten
rufen Store-Aktionen direkt auf und verwalten Items oder Selektion nie lokal.

## Item-Organisation

### Kern-Typen (definiert in `src/lib/types.ts`)

| Typ | Rolle |
|---|---|
| `Item` | Basis-Entität – kann eine Übung oder eine Sammlung sein (unterschieden durch `item_type`) |
| `Collection` | Ein `Item` mit `item_type = 'collection'`, das Kind-Items enthalten kann |
| `CollectionItem` | Ummantelt ein `Item` innerhalb einer Sammlung mit Metadaten (`position`) |
| `Content` | Ein Inhaltsblock, der an ein Item gehängt ist (Titel, Beschreibung, Lösung usw.) |

### Zwei Organisationsmechanismen

#### 1. Collections (der einzige Hierarchiemechanismus)

Nur Items mit `item_type = 'collection'` können Kinder haben. Kinder werden im
`items: CollectionItem[]`-Array der Sammlung gespeichert. Ein Item, das **keine**
Sammlung ist, ist immer ein Blatt im Baum.

- **Ungeordnete Sammlung** (`order: false`): Kinder haben `position: null`.
  Wird mit einem Ordner-Icon dargestellt.
- **Geordnete Sammlung** (`order: true`): Kinder haben fortlaufende Positionen
  (`1, 2, 3...`). Wird mit einem nummerierten-Liste-Icon dargestellt.

Sammlungen können verschachtelt werden: Ein `CollectionItem` kann selbst eine
`Collection` ummanteln, was einen Baum beliebiger Tiefe erzeugt. Jede
verschachtelte Sammlung hat ihr eigenes `items`-Array und `order`-Flag.

#### 2. `rootItemId` (Baum-Eigentümer-Referenz, **NICHT** Eltern-Kind)

`rootItemId` ist eine Metadaten-Referenz auf den **ultimativen Wurzel-Eigentümer**
des Baums, zu dem ein Item gehört. Es ist **kein** Eltern-Kind-Zeiger und
beeinflusst **nicht** die Baumdarstellung oder -hierarchie.

- Wenn ein Item die Wurzel seines eigenen Baums ist, ist `rootItemId` `null`.
- Wenn ein Item innerhalb einer Sammlung lebt, zeigt sein `rootItemId` auf die
  oberste Wurzel dieses Baums (die eigene Wurzel der Sammlung oder die Sammlung
  selbst, wenn sie die Wurzel ist).

Beispiel aus `dummy-data.ts`:

```
rootItems (alle Knoten der obersten Ebene):
├── "SQL Grundlagen"           [collection, rootItemId: null]
│   ├── "SELECT Abfragen"      [exercise,   rootItemId: "coll-sql-basics"]
│   ├── "WHERE Klausel"        [exercise,   rootItemId: "coll-sql-basics"]
│   └── "NULL Werte"           [exercise,   rootItemId: "coll-sql-basics"]
│
├── "SQL Fortgeschritten"      [collection, rootItemId: null, order: true]
│   ├── (1) "JOINs"            [exercise,   rootItemId: "coll-sql-advanced"]
│   ├── (2) "GROUP BY..."      [exercise,   rootItemId: "coll-sql-advanced"]
│   ├── (3) "Subqueries"       [exercise,   rootItemId: "coll-sql-advanced"]
│   └── (4) "Window Functions" [exercise,   rootItemId: "coll-sql-advanced"]
│
├── "Datenbankentwurf"         [collection, rootItemId: null]
│   ├── "Datenmodellierung"    [collection, rootItemId: "coll-db-design"]
│   │   ├── "ER-Diagramme"     [exercise,   rootItemId: "coll-db-design"]
│   │   └── "Normalisierung"   [exercise,   rootItemId: "coll-db-design"]
│   │
│   └── "Transaktionen"        [collection, rootItemId: "coll-db-design", order: true]
│       ├── (1) "ACID"         [exercise,   rootItemId: "coll-db-design"]
│       ├── (2) "Isolation..." [exercise,   rootItemId: "coll-db-design"]
│       └── (3) "LOCKs..."     [exercise,   rootItemId: "coll-db-design"]
│
├── "Indizes"                  [exercise,   rootItemId: null]    (eigenständig)
└── "Views"                    [exercise,   rootItemId: null]    (eigenständig)
```

Alle tief verschachtelten Items teilen sich `rootItemId: "coll-db-design"` (die
ultimative Wurzel dieses Baums), nicht die Zwischensammlung, in der sie sitzen.

### Was es **nicht** gibt

- Items **können keine** Kinder über `rootItemId` haben – dies war eine frühere
  Fehlinterpretation und wurde entfernt. Die Baumvalidierung (`validation.ts`)
  markiert jedes Nicht-Collection-Item, das unerwartet ein `items`-Array besitzt.
- Items **können nicht** sowohl in `rootItems[]` als auch im `items[]` einer
  Sammlung auftauchen. Die Validierung markiert Duplikate.

## Dummy-Daten (`dummy-data.ts`)

Enthält SQL-bezogene Seed-Daten, die vier Strukturierungs-Anwendungsfälle
demonstrieren:

1. **Ungeordnete Sammlung** – "SQL Grundlagen" (themengruppiert, keine Reihenfolge)
2. **Geordnete Sammlung** – "SQL Fortgeschritten" (steigender Schwierigkeitsgrad)
3. **Verschachtelte Unter-Sammlungen** – "Datenbankentwurf" mit zwei inneren
   Sammlungen (eine ungeordnet, eine geordnet)
4. **Eigenständige Root-Items** – "Indizes" und "Views" als Blatt-Übungen

## Baumdarstellung (Tree rendering)

Der Baum ist eine benutzerdefinierte rekursive Implementierung mit **vuedraggable**
(SortableJS).

| Komponente | Rolle |
|---|---|
| `AdbStructure.vue` | Rendert alle `store.rootItems` auf oberster Ebene |
| `AdbTreeItem.vue` | Iteriert über `TreeItem[]`, rendert Ordner oder Dateien |
| `AdbTreeFolder.vue` | Sammlungsknoten – aufklappbar, zeigt Kinder via `<slot>`, Kontextmenü |
| `AdbTreeFile.vue` | Blattknoten – kein Sammlungs-Item, anklickbar, Kontextmenü |

Das `items`-Array einer Sammlung wird rekursiv an `<AdbTreeItem>` übergeben,
das für jedes Element mit `isCollection()` prüft, ob es als Ordner oder Datei
dargestellt werden soll.

## Drag & Drop

Verwendet **vuedraggable** (SortableJS-Wrapper).
- Neuanordnung innerhalb derselben Liste löst `updateCollectionItems` oder
  `updateRootItems` im Store aus.
- Ein Ablegen in einer Sammlung verschiebt das Item – `_detachItem` entfernt es
  zuerst aus seinem alten Elternkontext, dann wird es zur Ziel-Sammlung
  hinzugefügt.
- Ein Ablegen in einer geordneten Sammlung weist die nächste freie Position zu.

## Validierung (`validation.ts`)

Eine reine Funktion `validateTreeData(rootItems)` durchläuft den gesamten Baum
und gibt eine Liste von `ValidationIssue`-Objekten zurück. Drei Regeln werden
geprüft:

| Regel | Beispielmeldung |
|---|---|
| Nur Sammlungen dürfen Kinder haben | `"SELECT Abfragen" ist kein Collection-Item, hat aber 3 Kind-Elemente.` |
| Keine doppelten Items in root + collection | `"dup" (dup) ist sowohl in rootItems als auch in einer Kollektion enthalten.` |
| Keine verwaisten `rootItemId`-Referenzen | `"orphan" verweist auf rootItemId "non-existent", das nicht existiert.` |

Die Validierung läuft automatisch nach jeder Store-Mutation (`createItem`,
`deleteItem`, `updateCollectionItems`, usw.) und beim Seitenaufruf via `Adb.vue`.
Probleme werden an den globalen Benachrichtigungs-Store übergeben und als
Toast-Meldungen angezeigt (siehe Globale Benachrichtigungen).

## Globale Benachrichtigungen (`useNotificationStore`)

**Store:** `src/stores/useNotificationStore.ts`

Ein wiederverwendbarer Pinia-Store für Toast-Meldungen:

```typescript
import { useNotificationStore } from '@/stores/useNotificationStore'

const notif = useNotificationStore()
notif.push('Item gelöscht', 'success', 5000)
notif.push('Validierung fehlgeschlagen', 'error', 8000)
```

| Parameter | Typ | Standard | Beschreibung |
|---|---|---|---|
| `message` | `string` | — | Anzuzeigender Text |
| `type` | `'success'\|'error'\|'warning'\|'info'` | `'info'` | Meldungsfarbe / Icon |
| `timeout` | `number \| null` | `5000` | Auto-Ausblenden in ms (`null` = bleibt sichtbar) |

Die Komponente `<NotificationToast />` ist in `App.vue` platziert und rendert
alle aktiven Benachrichtigungen als fixierte Alert-Boxen mit einem
Schließen-Button.

## Tests

Tests liegen in `frontend/tests/` und spiegeln die `src/`-Struktur:

```
tests/
  feature/
    aufgabendatenbank/
      validation.test.ts    14 Testfälle für validateTreeData
```

Ausführen mit `npm run test` (vitest).

## API

Backend-Endpunkte sind noch nicht angebunden. Das geplante Service-Interface
befindet sich in `src/services/exerciseApiService.ts` mit vollständigem TSDoc.
