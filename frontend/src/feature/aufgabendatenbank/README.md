# Adb (Aufgabendatenbank) Feature

Task database UI for creating, editing and organising exercises (Items) and
collections.

## Folder structure

```
aufgabendatenbank/
├── __tests__/                       (removed — tests live in tests/ at project root)
├── composables/
│   └── useSidebarResizer.ts         Draggable sidebar splitter
├── editor/
│   ├── AdbEditor.vue                Right-hand properties panel
│   └── components/
│       ├── AdbContentEditor.vue     Edit a single content block
│       └── AdbContentList.vue       List of content blocks on an item
├── structure/
│   ├── AdbStructure.vue             Tree root — renders all rootItems
│   ├── components/
│   │   ├── AdbTreeItem.vue          Recursive DnD node wrapper
│   │   └── tree/
│   │       ├── AdbTreeFile.vue      Leaf node (exercise or non-collection item)
│   │       └── AdbTreeFolder.vue    Collection node (expandable folder)
├── toolbar/
│   └── AdbToolbar.vue               Top bar with create buttons
├── Adb.vue                          Root layout (sidebar + resizer + editor)
├── dummy-data.ts                    Seed data used in development
├── validation.ts                    Tree data validation rules
└── README.md
```

## State

All state lives in `src/stores/exerciseStore.ts` (Pinia). Components call store
actions directly and never manage items or selection locally.

## Item organisation

### Core types (defined in `src/lib/types.ts`)

| Type | Role |
|---|---|
| `Item` | Base entity — can be an exercise or a collection (discriminated by `item_type`) |
| `Collection` | An `Item` with `item_type = 'collection'` that may hold child items |
| `CollectionItem` | Wraps an `Item` inside a collection with metadata (`position`) |
| `Content` | A content block attached to an item (title, description, solution, etc.) |

### Two organising mechanisms

#### 1. Collections (the only hierarchy mechanism)

Only items with `item_type = 'collection'` can have children. Children are
stored in the `items: CollectionItem[]` array of the collection. An item
that is **not** a collection is always a leaf in the tree.

- **Unordered collection** (`order: false`): Children have `position: null`.
  Displayed with a folder icon.
- **Ordered collection** (`order: true`): Children have sequential positions
  (`1, 2, 3...`). Displayed with a numbered-list icon.

Collections can be nested: a `CollectionItem` may itself wrap a `Collection`,
creating a tree of arbitrary depth. Each nested collection has its own `items`
array and `order` flag.

#### 2. `rootItemId` (tree-owner reference, **NOT** parent-child)

`rootItemId` is a metadata reference to the **ultimate root owner** of the
tree that an item belongs to. It is **not** a parent-child pointer and does
**not** influence tree rendering or hierarchy.

- If an item is a root of its own tree, `rootItemId` is `null`.
- If an item lives inside a collection, its `rootItemId` points to the
  top-most root of that tree (the collection's own root or the collection
  itself if it is the root).

Example from `dummy-data.ts`:

```
rootItems (all top-level nodes):
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
├── "Indizes"                  [exercise,   rootItemId: null]    (standalone)
└── "Views"                    [exercise,   rootItemId: null]    (standalone)
```

All deeply nested items share `rootItemId: "coll-db-design"` (the ultimate
root of that tree), not the intermediate collection they sit in.

### What does **not** exist

- Items **cannot** have children via `rootItemId` — this was an earlier
  misinterpretation and has been removed. The tree validation (`validation.ts`)
  flags any non-collection item that unexpectedly has an `items` array.
- Items **cannot** appear in both `rootItems[]` and inside a collection's
  `items[]`. The validation flags duplicates.

## Dummy data (`dummy-data.ts`)

Contains SQL-themed seed data that demonstrates four structuring use cases:

1. **Unordered collection** — "SQL Grundlagen" (topic grouping, no sequence)
2. **Ordered collection** — "SQL Fortgeschritten" (progressive difficulty)
3. **Nested sub-collections** — "Datenbankentwurf" with two inner collections
   (one unordered, one ordered)
4. **Standalone root items** — "Indizes" and "Views" as leaf exercises

## Tree rendering

The tree is a custom recursive implementation built with `vuedraggable`
(SortableJS).

| Component | Role |
|---|---|
| `AdbStructure.vue` | Renders all `store.rootItems` at the top level |
| `AdbTreeItem.vue` | Iterates over `TreeItem[]`, renders folders or files |
| `AdbTreeFolder.vue` | Collection node — expandable, shows children via `<slot>`, context menu |
| `AdbTreeFile.vue` | Leaf node — non-collection item, clickable, context menu |

A collection's `items` array is passed recursively to `<AdbTreeItem>`, which
checks `isCollection()` on each element to decide folder or file rendering.

## Drag & drop

Uses [vuedraggable](https://github.com/SortableJS/vue.draggable.next).
- Reordering within the same list triggers `updateCollectionItems` or
  `updateRootItems` in the store.
- Dropping into a collection moves the item — `_detachItem` removes it from
  its old parent first, then it is added to the target collection.
- Dropping into an ordered collection assigns the next sequential position.

## Validation (`validation.ts`)

A pure function `validateTreeData(rootItems)` walks the entire tree and
returns a list of `ValidationIssue` objects. Three rules are checked:

| Rule | Example message |
|---|---|
| Only collections can have children | `"SELECT Abfragen" ist kein Collection-Item, hat aber 3 Kind-Elemente.` |
| No duplicate items in root + collection | `"dup" (dup) ist sowohl in rootItems als auch in einer Kollektion enthalten.` |
| No dangling `rootItemId` references | `"orphan" verweist auf rootItemId "non-existent", das nicht existiert.` |

Validation runs automatically after every store mutation (`createItem`,
`deleteItem`, `updateCollectionItems`, etc.) and on page mount via `Adb.vue`.
Issues are pushed to the global notification store and displayed as
toast alerts (see Global notifications).

## Global notifications (`useNotificationStore`)

**Store:** `src/stores/useNotificationStore.ts`

A reusable Pinia store for toast-style messages:

```typescript
import { useNotificationStore } from '@/stores/useNotificationStore'

const notif = useNotificationStore()
notif.push('Item deleted', 'success', 5000)
notif.push('Validation failed', 'error', 8000)
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `message` | `string` | — | Display text |
| `type` | `'success'\|'error'\|'warning'\|'info'` | `'info'` | Alert colour / icon |
| `timeout` | `number \| null` | `5000` | Auto-dismiss in ms (`null` = sticky) |

The `<NotificationToast />` component is placed in `App.vue` and renders
all active notifications as fixed-position alerts with a close button.

## Tests

Tests live in `frontend/tests/` mirroring the `src/` structure:

```
tests/
  feature/
    aufgabendatenbank/
      validation.test.ts    14 test cases for validateTreeData
```

Run with `npm run test` (vitest).

## API

Backend endpoints are not yet connected. The planned service interface is at
`src/services/exerciseApiService.ts` with full TSDoc.
