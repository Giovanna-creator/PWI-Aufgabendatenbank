import type { Item } from '@/lib/types'

/**
 * Hard-coded seed data for development and testing.
 * Contains a mix of collections (ordered / unordered), stand-alone items,
 * and items linked via rootItemId.
 */
export const dummyData: { rootItems: Item[] } = {
  rootItems: [
    {
      id: 'coll-js-basics',
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
          id: 'content-coll-js',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'title',
          jsonContent: { text: 'JavaScript Grundlagen' },
          blobContent: ''
        }
      ],
      items: [
        {
          id: 'ci-js-vars',
          collectionId: 'coll-js-basics',
          item: {
            id: 'item-js-vars',
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
                id: 'content-js-vars',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'title',
                jsonContent: { text: 'Variablen und Konstanten' },
                blobContent: ''
              }
            ]
          },
          position: null
        },
        {
          id: 'ci-js-types',
          collectionId: 'coll-js-basics',
          item: {
            id: 'item-js-types',
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
                id: 'content-js-types',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'title',
                jsonContent: { text: 'Datentypen' },
                blobContent: ''
              }
            ]
          },
          position: null
        }
      ],
      order: false
    },
    {
      id: 'coll-tasks',
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
          id: 'content-coll-tasks',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'title',
          jsonContent: { text: 'Aufgaben' },
          blobContent: ''
        }
      ],
      items: [
        {
          id: 'ci-task1',
          collectionId: 'coll-tasks',
          item: {
            id: 'item-task1',
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
                id: 'content-task1',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'title',
                jsonContent: { text: 'Aufgabe 1: Hello World' },
                blobContent: ''
              }
            ]
          },
          position: 1
        },
        {
          id: 'ci-task2',
          collectionId: 'coll-tasks',
          item: {
            id: 'item-task2',
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
                id: 'content-task2',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'title',
                jsonContent: { text: 'Aufgabe 2: Funktionen' },
                blobContent: ''
              }
            ]
          },
          position: 2
        },
        {
          id: 'ci-task3',
          collectionId: 'coll-tasks',
          item: {
            id: 'item-task3',
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
                id: 'content-task3',
                license: null,
                contentType: 'text',
                author: 'admin',
                tags: [],
                purpose: 'title',
                jsonContent: { text: 'Aufgabe 3: Arrays' },
                blobContent: ''
              }
            ]
          },
          position: 3
        }
      ],
      order: true
    },
    {
      id: 'item-css-intro',
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
          id: 'content-css-intro',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'title',
          jsonContent: { text: 'Grundlagen CSS' },
          blobContent: ''
        }
      ]
    },
    {
      id: 'item-css-selectors',
      item_type: 'exercise',
      author: 'admin',
      representationTemplate: null,
      license: null,
      tags: [],
      validators: [],
      modifiers: [],
      rootItemId: 'item-css-intro',
      contents: [
        {
          id: 'content-css-selectors',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'title',
          jsonContent: { text: 'CSS Selektoren' },
          blobContent: ''
        }
      ]
    },
    {
      id: 'item-html-intro',
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
          id: 'content-html-intro',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'title',
          jsonContent: { text: 'HTML Grundstruktur' },
          blobContent: ''
        }
      ]
    },
    {
      id: 'item-vue-basics',
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
          id: 'content-vue-basics',
          license: null,
          contentType: 'text',
          author: 'admin',
          tags: [],
          purpose: 'title',
          jsonContent: { text: 'Vue.js Grundlagen' },
          blobContent: ''
        }
      ]
    }
  ]
}
