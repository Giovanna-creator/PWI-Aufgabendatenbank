interface ExerciseItem {
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
  rootItem: ExerciseItem | null

  contents: Content[]
}

interface Content {
  license: 'todo'
  contentType: 'todo'
  author: 'todo'

  jsonContent: Record<string, any>
  blobContent: string // base64 string
}
