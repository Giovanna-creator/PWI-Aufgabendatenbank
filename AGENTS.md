# AGENTS

## Purpose
This repository is a mono repo for the PWI task database project. Use this file to align tooling and
workflow choices when making changes.

## Repo layout
- backend/: Spring Boot service (Java, Maven wrapper present)
- frontend/: Vue/Vite client
- database/: SQL migrations and related docs
- docs/: project documentation and requirements
- Lastenpflichtheft/: project specs (MD/PDF)

## General guidance
- Keep changes scoped to the relevant package (backend, frontend, database, docs).
- Prefer minimal, well-named commits and avoid refactors unless requested.
- If unsure about commands or versions, ask before running destructive operations.
- Database schema changes are constrained: only datatype changes are allowed unless explicitly
  requested; do not add new tables or columns.

## Coding Style & Maintainability
- Keep files small and focused. Aim for a maximum of 200-300 lines per file (Vue components/Java records/classes).
- Extract complex UI components or business logic into smaller, reusable pieces (e.g., separate services or smaller components).
- Follow clean code principles: descriptive variable names, concise comments explaining "why" instead of "what", and eliminate dead code.
- Group related features and leverage modularity. Don't let views or controllers become bloated.
- Consistency is key. Follow the established formatting (Prettier/ESLint for frontend, IDE default styling for backend).

## Running and testing
- backend: use the Maven wrapper in `backend/` (`mvnw` or `mvnw.cmd`) if you need to build or test.
- frontend: use npm scripts from `frontend/package.json` (Node.js 22 expected per README).
- database: migrations live in `database/migrations/`; add new migrations only when required.

## Docs
- Keep README files concise and update them when behavior or scripts change.
- Use ASCII text unless the surrounding file already uses Unicode.

## Database Aggregation Strategy (First Iteration)
- As noted in the frontend types, the properties of an Exercise (Item) do not influence the creation of items themselves or their organization into differently ordered collections.
- To reduce complexity for the first iteration, exercise logic can be implemented independently from advanced schema relations.
- During this iteration, simplified aggregate types will rely on core fields only:
  - Common `item_type`, `author`, `license` (optional/null), and default `representationTemplate` (optional/null).
  - No `validators`, `modifiers`, or `tags` will be attached.
  - Only core dependencies and related `contents` are loaded initially.

### Data Flow & Operations of Creations of Exercises
Based on the defined types, the creation and management of exercises and collections follows specific workflows:

1. **Creating a single Exercise (No collection/root item):**
   - User creates an `Item`, then adds `Content` blocks mapped by `purpose` (e.g., description, task) with `jsonContent` or `blobContent`.

2. **Unordered Collections (Groups of exercises):**
   - Create a Collection: System creates a parent `Item` (with no content), then a `Collection` referencing it (`order = false`). (`POST /collections`)
   - Add Items: Create a new `Item`, then add to collection via `CollectionItem` with `position = null`. (`POST /collections/{collectionId}/items`)

3. **Ordered Collections (Sequences of exercises):**
   - Via `Collection.order = true` and `CollectionItem.position`s (1, 2, 3...).
   - Via `rootItemId`: Items referencing the same root are grouped visually in the tree.

4. **Extending and Modifying Collections:**
   - *Horizontal Vector:* An existing exercise can be extended by attaching a new collection to it. (`POST /items/{id}/collections` -> `POST /collections/{collectionId}/items`)
   - *Toggling Order:* Updating a collection's `order` property to `true`/`false` will automatically assign sequential positions or remove them (`position = null`) across its children. (`PUT /collections/{collectionId}`)
   - *Reordering:* Moving an item changes its `position` on the `CollectionItem`. The backend will automatically recalculate the positions of sibling items. (`PUT /collections/{collectionId}/items/{itemId}`)
   - *Moving/Extending:* Items can be moved or copied to another collection. If the target is ordered, positions are assigned automatically.
