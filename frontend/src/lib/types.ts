interface Item {
  // TODO: Define the properties of the Exercise interface
  /**
   * The properties of Exercise (Item) do not influence the creation of items itself
   * and their organisation in diffrently ordered collections. Therefore it is possible to
   * implement the exercises logic independently to avoid complexity for fitst interation.
   *
   * Such exercises will have:
   *
   * common item_type,
   * common license ?null
   * common global author
   * common default representation template ?null
   * no validators, ?null
   * no modifiers, ?null
   * no tags,
   */
  // dependencies
  item_type: 'todo'
  author: 'todo'
  representationTemplate: 'todo'
  license: 'todo'

  // connceted to the item with relation table
  tags: 'todo'
  validators: 'todo'
  modifiers: 'todo'

  // ------------------------------------------------------------------------------
  // Exercise can have no root item
  rootItem: Item | null

  contents: Content[]
}

interface Content {
  license: 'todo'
  contentType: 'todo'
  author: 'todo'
  tags: 'todo'

  // content itself
  purpose: string; // e.g. "description", "task", "title"
  jsonContent: Record<string, any>; // content (text)
  blobContent: string; // images
}

interface Collection {
    parent: Item | null;
    items: CollectionItem[];
    order: boolean;
}
interface CollectionItem {
    collection: Collection;
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
 * 1. // POST /collections
 * 
 * creates item and then creates collection
 * 
 * 2. // POST /collections/{collectionId}/items
 * 
 *  creates item and then creates collectionItem with position 1, 2, 3 ...
 * 
 */

/**
 * Creation ordered list of exercises (second possible implementation):
 * 
 * 1. User Creates Item with no root item
 * 2. User adds another Item with the first item as root item
 * 3. repeat
 * 
 * 1. POST /items for root item
 * 2. POST /items with rootItemId = id of the first item
 * 3. repeat
 * 
 */

/**
 * Extending existed ordered List of Exercises addind Horzontal vector:
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



