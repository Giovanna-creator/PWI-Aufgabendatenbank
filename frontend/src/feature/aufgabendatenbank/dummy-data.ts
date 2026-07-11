import type { Item } from '@/lib/types'

/**
 * Hard-coded seed data for development and testing.
 * Organized around SQL exercises to demonstrate different structuring use cases:
 *
 * 1. Unordered collection     — "SQL Grundlagen" (topic grouping, no sequence)
 * 2. Ordered collection       — "SQL Fortgeschritten" (progressive difficulty)
 * 3. Nested sub-collections   — "Datenbankentwurf" (collection containing sub-collections)
 * 4. Standalone items         — root-level exercises without a collection parent
 *
 * Only items with item_type='collection' can have children (via items: CollectionItem[]).
 * rootItemId is a reference to the root owner of the tree (not a parent-child pointer).
 */
export const dummyData: { rootItems: Item[] } = {
  rootItems: [
    // 1. Unordered collection
    {
      id: 'coll-sql-basics',
      item_type: 'collection',
      author: 'admin',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: null,
      contents: [
        {
          id: 'content-coll-sql-basics',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'Titel',
          jsonContent: { text: 'SQL Grundlagen' },
          blobContent: ''
        }
      ],
      items: [
        {
          id: 'ci-sql-select',
          collectionId: 'coll-sql-basics',
          item: {
            id: 'item-sql-select',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-basics',
            contents: [
              {
                id: 'content-sql-select',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'SELECT Abfragen' },
                blobContent: ''
              }
            ]
          },
          position: null
        },
        {
          id: 'ci-sql-where',
          collectionId: 'coll-sql-basics',
          item: {
            id: 'item-sql-where',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-basics',
            contents: [
              {
                id: 'content-sql-where',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'WHERE Klausel' },
                blobContent: ''
              }
            ]
          },
          position: null
        },
        {
          id: 'ci-sql-null',
          collectionId: 'coll-sql-basics',
          item: {
            id: 'item-sql-null',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-basics',
            contents: [
              {
                id: 'content-sql-null',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'NULL Werte' },
                blobContent: ''
              }
            ]
          },
          position: null
        }
      ],
      order: false
    },

    // 2. Ordered collection (progressive difficulty)
    {
      id: 'coll-sql-advanced',
      item_type: 'collection',
      author: 'admin',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: null,
      contents: [
        {
          id: 'content-coll-sql-advanced',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'Titel',
          jsonContent: { text: 'SQL Fortgeschritten' },
          blobContent: ''
        }
      ],
      items: [
        {
          id: 'ci-sql-joins',
          collectionId: 'coll-sql-advanced',
          item: {
            id: 'item-sql-joins',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-advanced',
            contents: [
              {
                id: 'content-sql-joins',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'JOINs' },
                blobContent: ''
              }
            ]
          },
          position: 1
        },
        {
          id: 'ci-sql-groupby',
          collectionId: 'coll-sql-advanced',
          item: {
            id: 'item-sql-groupby',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-advanced',
            contents: [
              {
                id: 'content-sql-groupby',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'GROUP BY und Aggregatfunktionen' },
                blobContent: ''
              }
            ]
          },
          position: 2
        },
        {
          id: 'ci-sql-subqueries',
          collectionId: 'coll-sql-advanced',
          item: {
            id: 'item-sql-subqueries',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-advanced',
            contents: [
              {
                id: 'content-sql-subqueries',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'Subqueries' },
                blobContent: ''
              }
            ]
          },
          position: 3
        },
        {
          id: 'ci-sql-window',
          collectionId: 'coll-sql-advanced',
          item: {
            id: 'item-sql-window',
            item_type: 'exercise',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-sql-advanced',
            contents: [
              {
                id: 'content-sql-window',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'Window Functions' },
                blobContent: ''
              }
            ]
          },
          position: 4
        }
      ],
      order: true
    },

    // 3. Nested sub-collections
    {
      id: 'coll-db-design',
      item_type: 'collection',
      author: 'admin',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: null,
      contents: [
        {
          id: 'content-coll-db-design',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'Titel',
          jsonContent: { text: 'Datenbankentwurf' },
          blobContent: ''
        }
      ],
      items: [
        // 3a. Unordered sub-collection
        {
          id: 'ci-coll-modeling',
          collectionId: 'coll-db-design',
          item: {
            id: 'coll-modeling',
            item_type: 'collection',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-db-design',
            contents: [
              {
                id: 'content-coll-modeling',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'Datenmodellierung' },
                blobContent: ''
              }
            ],
            items: [
              {
                id: 'ci-modeling-er',
                collectionId: 'coll-modeling',
                item: {
                  id: 'item-modeling-er',
                  item_type: 'exercise',
                  author: 'admin',
                  representationTemplate: null,
                  license: null,
                  tags: [],
                  validators: [],
                  modifiers: [],
                  rootItemId: 'coll-db-design',
                  contents: [
                    {
                      id: 'content-modeling-er',
                      license: null,
                      contentType: 'text',
                      author: 'admin',
                      tags: [],
                      purpose: 'Titel',
                      jsonContent: { text: 'ER-Diagramme' },
                      blobContent: ''
                    }
                  ]
                },
                position: null
              },
              {
                id: 'ci-modeling-normalization',
                collectionId: 'coll-modeling',
                item: {
                  id: 'item-modeling-normalization',
                  item_type: 'exercise',
                  author: 'admin',
                  representationTemplate: null,
                  license: null,
                  tags: [],
                  validators: [],
                  modifiers: [],
                  rootItemId: 'coll-db-design',
                  contents: [
                    {
                      id: 'content-modeling-normalization',
                      license: null,
                      contentType: 'text',
                      author: 'admin',
                      tags: [],
                      purpose: 'Titel',
                      jsonContent: { text: 'Normalisierung' },
                      blobContent: ''
                    }
                  ]
                },
                position: null
              }
            ],
            order: false
          },
          position: null
        },
        // 3b. Ordered sub-collection
        {
          id: 'ci-coll-transactions',
          collectionId: 'coll-db-design',
          item: {
            id: 'coll-transactions',
            item_type: 'collection',
            author: 'admin',
            representationTemplate: null,
            license: null,
            tags: [],
            validators: [],
            modifiers: [],
            rootItemId: 'coll-db-design',
            contents: [
              {
                id: 'content-coll-transactions',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'Titel',
                jsonContent: { text: 'Transaktionen' },
                blobContent: ''
              }
            ],
            items: [
              {
                id: 'ci-transactions-acid',
                collectionId: 'coll-transactions',
                item: {
                  id: 'item-transactions-acid',
                  item_type: 'exercise',
                  author: 'admin',
                  representationTemplate: null,
                  license: null,
                  tags: [],
                  validators: [],
                  modifiers: [],
                  rootItemId: 'coll-db-design',
                  contents: [
                    {
                      id: 'content-transactions-acid',
                      license: null,
                      contentType: 'text',
                      author: 'admin',
                      tags: [],
                      purpose: 'Titel',
                      jsonContent: { text: 'ACID' },
                      blobContent: ''
                    }
                  ]
                },
                position: 1
              },
              {
                id: 'ci-transactions-isolation',
                collectionId: 'coll-transactions',
                item: {
                  id: 'item-transactions-isolation',
                  item_type: 'exercise',
                  author: 'admin',
                  representationTemplate: null,
                  license: null,
                  tags: [],
                  validators: [],
                  modifiers: [],
                  rootItemId: 'coll-db-design',
                  contents: [
                    {
                      id: 'content-transactions-isolation',
                      license: null,
                      contentType: 'text',
                      author: 'admin',
                      tags: [],
                      purpose: 'Titel',
                      jsonContent: { text: 'Isolation Levels' },
                      blobContent: ''
                    }
                  ]
                },
                position: 2
              },
              {
                id: 'ci-transactions-locks',
                collectionId: 'coll-transactions',
                item: {
                  id: 'item-transactions-locks',
                  item_type: 'exercise',
                  author: 'admin',
                  representationTemplate: null,
                  license: null,
                  tags: [],
                  validators: [],
                  modifiers: [],
                  rootItemId: 'coll-db-design',
                  contents: [
                    {
                      id: 'content-transactions-locks',
                      license: null,
                      contentType: 'text',
                      author: 'admin',
                      tags: [],
                      purpose: 'Titel',
                      jsonContent: { text: 'LOCKs und Deadlocks' },
                      blobContent: ''
                    }
                  ]
                },
                position: 3
              }
            ],
            order: true
          },
          position: null
        }
      ],
      order: false
    },

    // 4. Standalone root-level exercises
    {
      id: 'item-sql-indexes',
      item_type: 'exercise',
      author: 'admin',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: null,
      contents: [
        {
          id: 'content-sql-indexes',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'Titel',
          jsonContent: { text: 'Indizes' },
          blobContent: ''
        }
      ]
    },
    {
      id: 'item-sql-views',
      item_type: 'exercise',
      author: 'admin',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: null,
      contents: [
        {
          id: 'content-sql-views',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'Titel',
          jsonContent: { text: 'Views' },
          blobContent: ''
        }
      ]
    }
  ]
}
