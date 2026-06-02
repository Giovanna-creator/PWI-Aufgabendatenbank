# Treeview Interactions for Exercise Organization

## 1. Overview
This document outlines how the creation, management, and structuring of exercises (and collections) described in the `AGENTS.md` data flow can be visualized and interacted with using a tree-based user interface. The UI behaves similarly to a file explorer.

## 2. Component Stack
- **Vuetify Treeview (`v-treeview` or similar Vuetify components):** Used for rendering structured hierachical data, nested collections, and items.
- **vuedraggable:** Used for drag-and-drop operations, allowing users to intuitively reorder exercises, move them across collections, and build nested structures.

## 3. Visual Mapping
To translate the backend data models into a tree view:

- **Unified Item Architecture:** Each item can be a collection. An item acts as a "folder" (expandable node) when it has child items (either via `rootItemId` or via a collection's `items`), or as a "file" (leaf node) when it does not.
- **Inheritance:** The `Collection` type inherits from `Item`. This ensures that a collection is always an item and can be edited, moved, or deleted like any other task.
- **Naming Convention:** All entities in the tree view (both files and folders) should display their title from the first `Content` block.
- **Single Exercises (Leaf Items):** Represented as "files" (leaf nodes) when they currently have no child items or collection items attached.
- **Collections (Items with children):** When an item contains other items, it is represented as a "folder" (expandable node). 
  - **Unordered Collections:** Indicated by a standard folder icon.
  - **Ordered Collections:** Indicated by a specific "sequence" folder icon. Child items display their sequential `position`.


## 4. User Interactions & Data Flow Integration

### Primary Tree Navigation (Node Click for Expansion)
- **Expanding Collections:** Clicking the **node body** (the item title or background) should expand the item if it has children.
  - Clicking the node body also selects the item, opening its details (contents and metadata) in the editor.
  - The **chevron/arrow icon** serves as a visual indicator of the expansion state but is no longer the exclusive trigger for expansion.

### A. Drag-and-Drop Reordering (Inside an Ordered Collection)
- **Action:** User drags an exercise up or down within the same ordered collection.
- **vuedraggable:** Detects the sortable interaction and calculates the new index.
- **Backend Flow:** Triggers `PUT /collections/{collectionId}/items/{itemId}` with the newly calculated `position`. The backend recalculates the positions of sibling items automatically.

### B. Moving Items Between Collections
- **Action:** User drags an item from Collection A and drops it into Collection B.
- **vuedraggable:** Detects the drop event over a different droppable container (folder).
- **Backend Flow:** Triggers `PUT /collections/items/{itemId}` with the target `collectionId`. If Collection B is ordered, the backend assigns a position automatically or based on the drop index.

### C. Extending Exercises (Horizontal Vector via Drag & Drop)
- **Action:** User drops an exercise directly onto another existing base exercise (not a folder).
- **Frontend Logic:**
  1. Prompts or automatically groups them into a newly created Collection.
  2. `POST /items/{id}/collections` (creates a collection attached to the base item).
  3. `POST /collections/{collectionId}/items` (moves the dragged item into the new collection).

### D. Toggling Collection Order
- **Action:** A switch, checkbox, or context-menu option on a "Collection" node to toggle its sequential nature.
- **Backend Flow:** Calls `PUT /collections/{collectionId}` with `{ order: true/false }`.
- **UI Update:** The UI dynamically hides or reveals positional numbers for all child items based on the new state.

### E. Creating New Entries
- **New Exercise:** Action opens a dialog to define Content blocks (`purpose`, `jsonContent`). Fires `POST /items` and appends it to the tree root, or to the currently active collection via `POST /collections/{collectionId}/items`.
- **New Collection:** Action fires `POST /collections`, creating a new folder node in the tree.

## 5. Implementation Considerations
- **Draggable vs. Droppable:** Differentiate between sortable contexts (within the same collection) and cross-container drops (moving to a new collection). vuedraggable handles nested droppable zones via its group configuration.
- **Optimistic UI Updates:** Because backend operations (especially position recalculations) might take time, the Vue state (via Pinia or local component state) should optimisticly update the tree view structure immediately upon drop, reverting only if the API call fails.
- **Root Items (rootItemId):** Items linked via `rootItemId` are grouped visually under their parent. Dragging an item to link it to another fires `PUT /items/{id}` setting the new `rootItemId`.
