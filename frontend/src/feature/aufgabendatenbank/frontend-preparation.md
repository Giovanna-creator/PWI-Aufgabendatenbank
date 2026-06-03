# Frontend Preparation — API Adapter & Store Refactor

## Session Overview

Complete refactor of the frontend communication layer for the Aufgabendatenbank feature.
The old `ExerciseApiService` is deprecated. A new strategy-pattern architecture was built:

**ApiAdapter interface** → two implementations:
- `AdbApiService` — real HTTP (axios, for production)
- `DevAdbApiService` — logs + seed dummy data (for dev without backend)

---

## Files Created

| File | Purpose |
|---|---|
| `api-adapter.types.ts` | `ApiAdapter` interface, all DTOs (`ItemDTO`, `ContentDTO`, `CollectionItemDTO`), request payloads |
| `adbApi.service.ts` | Real HTTP adapter — singleton axios, paths per `frontend-backend-communication.md` |
| `dev-adb-api.service.ts` | Dev adapter — console.logs each request, returns `dummy-data.ts` seed data |
| `.env.dummy` | Env file setting `VITE_ADB_API_MODE=dummy` |

## Files Modified

| File | Changes |
|---|---|
| `exerciseStore.ts` | Injected `ApiAdapter` via `setApiAdapter()`; added `loading`/`error` state; progressive loading; auto-sync reorder positions; notification-store for API errors |
| `main.ts` | Adapter selection based on `VITE_ADB_API_MODE`: `adbApi` (real) vs `devAdbApi` (dummy) |
| `Adb.vue` | Calls `store.loadTree()` on mount instead of `store.validate()` |
| `AdbTreeFolder.vue` | Shows `<v-progress-circular>` spinner while collection children load in background |
| `exerciseApiService.ts` | Added `@deprecated` JSDoc pointing to new `AdbApiService` |
| `package.json` | Added `"dev:dummy": "vite --mode dummy"` script |

---

## Architecture

```
main.ts
  └─ setApiAdapter(devAdbApi | adbApi)       ← env-based selection
       │
exerciseStore.ts  (depends on ApiAdapter interface)
  │  ├─ loadTree()         → adapter.getRootItems() + _loadChildrenRecursively()
  │  ├─ createItem()       → local + fire-and-forget adapter.createItem()
  │  ├─ createCollection() → local + fire-and-forget adapter.createCollection()
  │  ├─ addItemToCollection() → local + adapter.addItemToCollection()
  │  ├─ deleteItem()       → local + adapter.deleteItem() + _syncOrderedCollectionItems()
  │  ├─ updateCollectionItems() → local + cross-collection API calls + auto-reorder
  │  ├─ updateRootItems()  → local + adapter.removeItemFromCollection()
  │  ├─ toggleCollectionOrder() → adapter.updateCollection(id, { order })  (single endpoint)
  │  └─ _syncOrderedCollectionItems() → diffs positions, calls adapter.updateCollectionItemPosition()
  │
  ├── AdbApiService (adbApi.service.ts)       ← real HTTP, for production
  └── DevAdbApiService (dev-adb-api.service.ts) ← logs + dummy data, for dev
```

## Endpoint Mapping (per communication doc)

| Doc Path | adapter method | Notes |
|---|---|---|
| `GET /api/items?root=true` | `getRootItems()` | Returns roots w/o children |
| `GET /api/collections/{id}/items` | `getCollectionItems(id)` | Returns children w/ position |
| `POST /api/items` | `createItem(payload)` | |
| `POST /api/collections` | `createCollection(payload)` | Creates Item + Collection record |
| `POST /api/items/{id}/collection` | `convertItemToCollection(id)` | |
| `POST /api/collection/{id}/items` | `addItemToCollection(id, itemId)` | Note singular "collection" |
| `DELETE /api/collections/{id}/items/{itemId}` | `removeItemFromCollection(id, itemId)` | |
| `DELETE /api/items/{id}` | `deleteItem(id)` | Cascade delete |
| `PUT /api/collections/{id}` | `updateCollection(id, { order })` | Single endpoint for both on/off |
| `PUT /api/collection/{id}/items/{itemId}` | `updateCollectionItemPosition(id, itemId, pos)` | Note singular "collection" |
| `GET /api/items/{itemId}/contents` | `getContents(itemId)` | Returns all content blocks for an item |
| `POST /api/items/{itemId}/contents` | `createContent(itemId, payload)` | Creates a new content block |
| `PUT /api/contents/{id}` | `updateContent(contentId, payload)` | Updates a content block's fields |
| `DELETE /api/contents/{id}` | `deleteContent(contentId)` | Deletes a content block |

## Progressive Loading Strategy

1. **Phase 1** — `loadTree()` calls `getRootItems()`, roots appear in UI immediately
2. **Phase 2** — `_loadChildrenRecursively()` fires in background for each collection
   - Each collection gets flagged in `loadingChildrenIds[]`
   - `AdbTreeFolder.vue` shows spinner while loading
   - Sub-collections trigger their own fetch recursively
   - All siblings at same depth load in parallel via `Promise.all`

## Auto-Reorder Strategy

`_syncOrderedCollectionItems(collection, force?)` is called automatically after:
- `updateCollectionItems()` — DnD reorder (calls with `force=true`)
- `addItemToCollection()` — new item added
- `deleteItem()` — item removed from ordered collection
- `toggleCollectionOrder()` — order enabled
- `updateRootItems()` — item moved from collection to root

**Normal mode** (`force=false`): diffs each item's position against its index + 1, then calls `updateCollectionItemPosition()` only for changed ones.

**Force mode** (`force=true`): always calls `updateCollectionItemPosition()` for every item regardless of current position. Used after DnD reorder (`updateCollectionItems`) because positions are pre-assigned in `.map()` before the sync runs, so a plain diff would find nothing to persist.

## Cross-Collection DnD API Calls

`updateCollectionItems()` distinguishes three cases via `oldParentId` (returned by `_detachItem`):

- `oldParentId === collection.id` → item stayed in same collection → no API calls (reorder only)
- `oldParentId` is different ID → cross-collection move → `removeItemFromCollection(old)` + `addItemToCollection(new)` + reorder old parent
- `oldParentId === null` → root-to-collection move → `addItemToCollection(new)`

## Item Content Sync

Content CRUD follows the same fire-and-forget pattern as item/collection mutations:

| Store Action | API Call | Frequency |
|---|---|---|
| `addContentToSelectedItem()` | `adapter.createContent(itemId, payload)` | On user click (add button) |
| `removeContentFromSelectedItem(index)` | `adapter.deleteContent(contentId)` | On user click (delete X button) |
| `updateContentText(index, text)` | `adapter.updateContent(contentId, payload)` | On every keystroke (`@input`) |
| `updateContentPurpose(index, purpose)` | `adapter.updateContent(contentId, payload)` | On every keystroke (`@input`) |

All four fire the API call after applying the local state change, using `.catch(this._notifyError)` per the standard pattern. Text and purpose updates sync on every keystroke (no debounce) to match the direct fire-and-forget convention used elsewhere — the backend is expected to handle frequent PATCH-like updates gracefully.

Content IDs are generated locally (`'content-' + timestamp`) for create, identical to the `createItem` / `createCollection` pattern. The backend-assigned ID from the response is ignored (fire-and-forget).

## Running
