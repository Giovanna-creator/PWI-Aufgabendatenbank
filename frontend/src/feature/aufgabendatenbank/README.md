# Adb (Aufgabendatenbank) Feature

Task database UI for creating, editing and organising exercises (Items) and
collections.

## Folder structure

```
aufgabendatenbank/
├── composables/
│   └── useSidebarResizer.ts   Draggable sidebar splitter
├── editor/
│   └── AdbEditor.vue          Right-hand properties panel
├── structure/
│   ├── AdbStructure.vue        Tree root + filter
│   ├── components/
│   │   ├── AdbTreeItem.vue     Recursive DnD node wrapper
│   │   └── tree/
│   │       ├── AdbTreeFile.vue Leaf node (exercise or collapsible item)
│   │       └── AdbTreeFolder.vue  Collection node (expandable folder)
├── toolbar/
│   └── AdbToolbar.vue          Top bar with create buttons
├── Adb.vue                     Root layout (sidebar + resizer + editor)
├── dummy-data.ts               Seed data used in development
└── README.md
```

## State

All state lives in `src/stores/exerciseStore.ts` (Pinia). Components call store
actions directly and never manage items or selection locally.

## Item organisation

Items are organised in two ways:

- **Tree hierarchy (rootItemId):** Each item can reference a parent via
  `rootItemId`. Items live in `rootItems[]` and the tree groups them by
  matching parent IDs. This determines the tree display.

- **Collections:** A collection is an item with `item_type = 'collection'`
  and an `items: CollectionItem[]` array. It holds wrapped items and may
  be ordered (sequential positions) or unordered (positions are null).
  Collections are themselves items, so they appear in the tree too and
  may also have a `rootItemId`.

## Drag & drop

Uses [vuedraggable](https://github.com/SortableJS/vue.draggable.next)
(SortableJS wrapper). DnD within a list reorders; dropping into a collection
node moves the item into that collection.

## API

Backend endpoints are not yet connected. The planned service interface is at
`src/services/exerciseApiService.ts` with full TSDoc.
