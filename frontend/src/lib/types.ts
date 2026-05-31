export interface Content {
  id?: string
  license: string | null
  contentType: string
  author: string
  tags: string[]

  // content itself
  purpose: string; // e.g. "description", "task", "title"
  jsonContent: Record<string, any>; // content (text)
  blobContent: string; // images
}

export interface Item {
  id: string;
  item_type: string
  author: string
  representationTemplate: string | null
  license: string | null
  tags: string[]
  validators: any[]
  modifiers: any[]
  rootItemId?: string | null
  contents: Content[]
  // Collection properties (optional, if item_type is 'collection')
  items?: CollectionItem[];
  order?: boolean;
}

export type Collection = Item & {
  item_type: 'collection';
  items: CollectionItem[];
  order: boolean;
}

export function isItem(object: any): object is Item {
  return object && typeof object.id === 'string' && 'item_type' in object && !('collectionId' in object);
}

export function isCollection(object: any): object is Collection {
  return isItem(object) && object.item_type === 'collection';
}

export function isCollectionItem(object: any): object is CollectionItem {
  return object && typeof object.id === 'string' && 'collectionId' in object && 'item' in object;
}

export function getInnerItem(element: Item | Collection | CollectionItem): Item {
  if (isCollectionItem(element)) {
    return element.item;
  }
  return element;
}
export interface CollectionItem {
    id: string;
    collectionId: string;
    item: Item;
    position: number | null;
}

/**
 * Creating New Exercise (No collenction, no root item):
 * 
 * User creates new Item ->
 * User adds Content:
 * 
 * Exercise Title (purpose)
 * Exercise Discription: JSON {"text": "This is a description of the exercise"} (jsonContent)
 * BLOB: image (blobContent)
 * 
 */

/**
 * Creation unordered group if exercises:
 * 
 * 1. User creates Collection (Collection itself is an item with collection is-relation):
 * Item with no content is created
 * 
 * 2. Collection with the item as parent is created order = false
 * 
 * 3. User adds item to the collection x times:
 * Item is being created
 * Items is beind added to the collection by creating CollectionItem with position = null
 * 
 * 1. // POST /collections
 * 
 * creates item and then creates collection
 * 
 * 2. // POST /collections/{collectionId}/items
 * 
 *  creates item and then creates collectionItem with position = null
 * 
 */

/**
 * Creation ordered list of exercises (first possible implementation):
 * 
 * User creates first Item is-Collection as from 
 * "Creation of unordered group of exercises" 
 * and adds first item to the collection with position = 1, 2, 3
 * 
 * 1. // POST /collections mit order = true
 * 
 * creates item and then creates collection
 * 
 * 2. // POST /collections/{collectionId}/items
 * 
 *  creates item and then creates collectionItem with position 1, 2, 3 ...
 */

/**
 * Creation ordered list of exercises (second possible implementation):
 * 
 * 1. User Creates Item with no root item
 * 2. User adds another Item with the first item as rootItem
 * 3. repeat
 * 
 * 1. POST /items for root item
 * 2. POST /items with rootItemId = id of the first item
 * 3. repeat
 * 
 */

/**
 * Extending existed ordered List of Exercises addind Horizontal vector:
 * 
 * Existing Exercise get collections that is goint to be appended with unordered items
 * 
 * 1. POST /items/{id}/collections
 * 2. POST /collections/{collectionId}/items
 * 3. repeat 2.
 * 
 * 
 */

/**
 * make from unordered List of Exercises ordered List of Exercises:
 * 
 * 1. Update collection with order = true (items get order automatically)
 * 
 * 1. PUT /collections/{collectionId} to change order to true (items get positions automatically)
 * 
 */

/**
 * make from ordered List of Exercises unordered List of Exercises:
 * 
 * 1. Update collection with order = false (items get order = null)
 * 1. PUT /collections/{collectionId} to change order to false (items get position = null)
 * 
 */

/**
 * 
 * Reodering strategy (in Collection):
 * 
 * Item no7 to Item no2:
 * 
 * PUT /collections/{collectionId}/items/{itemId} with position = 2
 * Another items should be recalculated in backend
 * 
 */

/**
 * Extend existing collection with items from another collection:
 * 
 * 1. PUT /collections/items/{itemId} with collectionId = id of the collection to be added
 * 
 * If collection was ordered, items get positions automatically
 * 
 * 
 */

/**
 * 
 * 
 */

