# AGENTS

## Purpose
This folder contains the frontend for the PWI task database project. Use this file to align tooling and
workflow choices when making changes here.

## Scope
- Limit changes to `frontend/` unless explicitly requested.
- Keep refactors minimal and scoped to the task at hand.

## Folder Structure
- `src/feature/`: Feature-based folder structure. Each distinct feature (e.g., `aufgabendatenbank`) gets its own subfolder containing its specific components, composables, and logic.
- `src/views/`: Strict routing wrappers. These components should generally just import and render the top-level feature components.
- Other folders (`src/components/`, `src/services/`, `src/stores/`, etc.): Reserved for globally shared logic, shared UI components, and common utilities used across multiple features.

## Coding Style & Maintainability
- Component Architecture: Keep all complex logic, templating, and composition inside the `src/feature/<feature-name>/` directory. The `src/views/` folder should be reserved solely for routing structural wrappers and entry points that import feature components.
- Keep Vue components small and focused on a single responsibility. Aim for under 200-300 lines of `<template>` and `<script>` combined.
- Extract complex logic into composables (Vue 3 Composition API) or Pinia stores.
- Extract large UI segments into smaller child components.
- Use distinct naming for variables and components, following standard Vue/TS conventions.
- Make liberal use of ESLint and Prettier to automatically enforce styles.

## Stack and packages in use
- Framework: Vue 3, Vite
- UI: Vuetify, Bootstrap, Tailwind CSS
- State and routing: Pinia, Vue Router
- Drag and drop: Vue DnD Kit
- HTTP: Axios
- Build/test: TypeScript, Vitest, ESLint, Prettier

## Running and testing
- Use npm scripts from `frontend/package.json`.
- Frontend README states Node.js 22 is expected.

## Data and backend
- The frontend currently uses local mock services; backend integration is optional and can be added
  incrementally.

## Project purpose
- Build a central platform to manage exercise tasks across the institution.
- Transform an agreed but abstract schema into a usable application and interface.
- Model tasks, attributes, relationships, and collections so the data is both rigorous and usable.
- Support task variants, task relationships, and structured task sequences.
- Provide a practical web interface for creating, editing, organizing, and combining tasks.
- Explore requirements for data model, interaction, UI, and logic to fit the larger platform concept.

## Docs
- Keep README files concise and update them when scripts or behavior change.
- Use ASCII text unless the surrounding file already uses Unicode.

## Exercise management model
- Exercises are stored as items with an author, license, item type, and optional representation
  template.
- Items can reference a root item to model variants; a missing root means the item is a root.
- Exercise content lives in separate item_content records with license, content type, and author.
- Items and contents are connected via a join table with a purpose (statement, hint, solution).
- Tags, validators, and modifiers are attached via join tables for filtering and constraints.
- Item collections group exercises into ordered sequences; sub-items store a position for sequencing.
- Frontend types should follow this split between item metadata and content blocks.

## Database Aggregation Strategy (First Iteration)
- The inherent properties of an Exercise (Item) do not influence the creation of the items themselves or their organization into differently ordered collections.
- To reduce complexity for the first iteration, exercise logic can be implemented independently from advanced schema relations.
- During this iteration, simplified aggregate types will have:
  - Common `item_type`
  - Common `author`
  - Common `license` (can be null)
  - Common default `representationTemplate` (can be null)
  - No `validators` (null)
  - No `modifiers` (null)
  - No `tags`
  - Only core dependencies and related `contents` are loaded.

### Data Flow & Operations
Based on the defined types, the creation and management of exercises and collections follows specific workflows:

1. **Creating a single Exercise (No collection/root item):**
   - User creates an `Item`, then adds `Content` blocks mapped by `purpose` (e.g., description, task) with `jsonContent` or `blobContent`.

2. **Unordered Collections (Groups of exercises):**
   - Create a Collection: System creates a parent `Item` (with no content), then a `Collection` referencing it (`order = false`). (`POST /collections`)
   - Add Items: Create a new `Item`, then add to collection via `CollectionItem` with `position = null`. (`POST /collections/{collectionId}/items`)

3. **Ordered Collections (Sequences of exercises):**
   - *Implementation A (Via Collections):* Same as unordered, but `Collection` has `order = true` and `CollectionItem`s are added with sequential `position`s (1, 2, 3...).
   - *Implementation B (Via Root Items):* Create an `Item`, then subsequent `Item`s are created setting `rootItem` to the first item's ID.

4. **Extending and Modifying Collections:**
   - *Horizontal Vector:* An existing exercise can be extended by attaching a new collection to it. (`POST /items/{id}/collections` -> `POST /collections/{collectionId}/items`)
   - *Toggling Order:* Updating a collection's `order` property to `true`/`false` will automatically assign sequential positions or remove them (`position = null`) across its children. (`PUT /collections/{collectionId}`)
   - *Reordering:* Moving an item changes its `position` on the `CollectionItem`. The backend will automatically recalculate the positions of sibling items. (`PUT /collections/{collectionId}/items/{itemId}`)
   - *Moving/Extending:* Items can be moved or copied to another collection. If the target is ordered, positions are assigned automatically.


# Interaction design for Items creation and management:

# Treeview Interactions for Exercise Organization

## 1. Overview
This document outlines how the creation, management, and structuring of exercises (and collections) described in the `AGENTS.md` data flow can be visualized and interacted with using a tree-based user interface. The UI behaves similarly to a file explorer.

## 2. Component Stack
- **Vuetify Treeview (`v-treeview` or similar Vuetify components):** Used for rendering structured hierachical data, nested collections, and items.
- **Vue DnD Kit:** Used for drag-and-drop operations, allowing users to intuitively reorder exercises, move them across collections, and build nested structures.

## 3. Visual Mapping
To translate the backend data models into a tree view:

- **Unified Item Architecture:** It must be absolutely clear that **each item can be a collection, and a collection is always an item as well**. There is no distinct "folder" vs. "file" entity in the core schema—an item merely acts as a "folder" (expandable node) when it has child items, or as a "file" (leaf node) when it does not.
- **Naming Convention:** All entities in the tree view (both files and folders) should simply be labeled generically as "Item" (or display their `item_type` / ID) since items lack a direct "title" field in the root database schema (titles are stored as Content blocks).
- **Single Exercises (Leaf Items):** Represented as "files" (leaf nodes) when they currently have no child items attached.
- **Collections (Parent Items):** When an item acts as a parent (contains other items), it is represented as a "folder" (expandable node).
    - **Unordered Collections:** Indicated by a standard folder icon or bullet points. Child items have no visible sequence numbers (since `position = null`).
    - **Ordered Collections:** Indicated by a specific "sequence" folder icon (e.g., list-numbered). Child items display their sequential `position` (1, 2, 3...) prominently next to their names.
    - **Default State (Unordered):** Items placed inside a collection of another item are **unordered by default**. They are indicated by a standard folder icon or bullet points, with no visible sequence numbers (`position = null`).
    - **Changing to Ordered:** An unordered collection can be explicitly changed into an ordered sequence **only via a context menu option** on the parent node. Once ordered (e.g., indicated by a list-numbered icon), the child items display their sequential `position` (1, 2, 3...) prominently next to their names.


## 4. User Interactions & Data Flow Integration

### Primary Tree Navigation (Chevron vs. Node Click)
- **Expanding Collections:** Because every item can potentially act as a collection and inherently contains its own content/data, clicking the **node body** itself should NOT expand the item.
    - Clicking the node body is reserved for selecting, opening, or previewing the item's details (its contents and metadata).
    - Expanding the collection to reveal child tree items must strictly be triggered by clicking the **chevron/arrow icon** next to the node.

### A. Drag-and-Drop Reordering (Inside an Ordered Collection)
- **Action:** User drags an exercise up or down within the same ordered collection.
- **Vue DnD Kit:** Detects the sortable interaction and calculates the new index.
- **Backend Flow:** Triggers `PUT /collections/{collectionId}/items/{itemId}` with the newly calculated `position`. The backend recalculates the positions of sibling items automatically.

### B. Moving Items Between Collections
- **Action:** User drags an item from Collection A and drops it into Collection B.
- **Vue DnD Kit:** Detects the drop event over a different droppable container (folder).
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
- **Draggable vs. Droppable:** Differentiate between sortable contexts (within the same collection) and cross-container drops (moving to a new collection). Vue DnD Kit sensors should be configured to handle nested droppable zones (folders).
- **Optimistic UI Updates:** Because backend operations (especially position recalculations) might take time, the Vue state (via Pinia or local component state) should optimisticly update the tree view structure immediately upon drop, reverting only if the API call fails.
- **Root Items (Implementation B for Sequences):** If sequences are managed via `rootItem` instead of explicit Collections, dragging an item to link it to another should fire `PUT /items/{id}` setting the new `rootItemId`. Displaying these might involve grouping items that share a common `rootItem` under a virtual folder node in the UI.
