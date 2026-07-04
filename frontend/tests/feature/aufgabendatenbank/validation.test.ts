import { describe, it, expect } from 'vitest'
import {
  validateTreeData,
  checkOnlyCollectionsHaveChildren,
  checkNoDuplicateItems,
  checkNoDanglingRootItemId
} from '@/feature/aufgabendatenbank/validation'
import type { Item, Collection, CollectionItem } from '@/lib/types'

function item(overrides: Partial<Item> & { id: string }): Item {
  return {
    item_type: 'exercise',
    author: 'test',
    representationTemplate: null,
    license: null,
    tags: [],
    validators: [],
    modifiers: [],
    rootItemId: null,
    contents: [
      {
        id: 'c-' + overrides.id,
        license: null,
        contentType: 'text',
        author: 'test',
        tags: [],
        purpose: 'Titel',
        jsonContent: { text: overrides.id },
        blobContent: ''
      }
    ],
    ...overrides
  }
}

function collection(overrides: Partial<Collection> & { id: string; items: CollectionItem[] }): Collection {
  return {
    ...item({ ...overrides, item_type: 'collection' }),
    item_type: 'collection',
    items: overrides.items,
    order: false,
    ...overrides
  } as Collection
}

function collItem(overrides: Partial<CollectionItem> & { id: string; item: Item }): CollectionItem {
  return {
    collectionId: '',
    position: null,
    ...overrides
  }
}

describe('validateTreeData', () => {
  it('returns no issues for an empty tree', () => {
    expect(validateTreeData([])).toEqual([])
  })

  it('returns no issues for standalone exercises', () => {
    const data = [item({ id: 'a' }), item({ id: 'b' })]
    expect(validateTreeData(data)).toEqual([])
  })

  it('returns no issues for a valid unordered collection', () => {
    const data = [
      collection({
        id: 'coll-1',
        items: [
          collItem({ id: 'ci-1', collectionId: 'coll-1', item: item({ id: 'ex-1', rootItemId: 'coll-1' }) }),
          collItem({ id: 'ci-2', collectionId: 'coll-1', item: item({ id: 'ex-2', rootItemId: 'coll-1' }) })
        ]
      })
    ]
    expect(validateTreeData(data)).toEqual([])
  })

  it('returns no issues for a valid ordered collection', () => {
    const data = [
      collection({
        id: 'coll-1',
        order: true,
        items: [
          collItem({ id: 'ci-1', collectionId: 'coll-1', position: 1, item: item({ id: 'ex-1', rootItemId: 'coll-1' }) }),
          collItem({ id: 'ci-2', collectionId: 'coll-1', position: 2, item: item({ id: 'ex-2', rootItemId: 'coll-1' }) })
        ]
      })
    ]
    expect(validateTreeData(data)).toEqual([])
  })

  it('returns no issues for nested collections', () => {
    const data = [
      collection({
        id: 'coll-root',
        items: [
          collItem({
            id: 'ci-sub',
            collectionId: 'coll-root',
            item: collection({
              id: 'coll-sub',
              rootItemId: 'coll-root',
              items: [
                collItem({ id: 'ci-sub-1', collectionId: 'coll-sub', item: item({ id: 'ex-sub-1', rootItemId: 'coll-root' }) })
              ]
            })
          })
        ]
      })
    ]
    expect(validateTreeData(data)).toEqual([])
  })

  it('flags an exercise that has children', () => {
    const bad = item({
      id: 'bad-ex',
      item_type: 'exercise',
      items: [
        collItem({ id: 'ci-orphan', collectionId: 'bad-ex', item: item({ id: 'orphan' }) })
      ]
    }) as Item & { items: CollectionItem[] }
    const data = [bad]
    const issues = validateTreeData(data)
    expect(issues).toHaveLength(1)
    expect(issues[0].message).toContain('bad-ex')
    expect(issues[0].message).toContain('kein Collection-Item')
  })

  it('does not flag an item appearing in rootItems and inside a collection', () => {
    // Sub-Items behalten root_item_id=null und tauchen daher regulär in beiden
    // Listen auf — das ist kein Fehler und darf keine Meldung erzeugen.
    const dup = item({ id: 'dup', rootItemId: null })
    const data = [
      dup,
      collection({
        id: 'coll-1',
        items: [
          collItem({ id: 'ci-1', collectionId: 'coll-1', item: dup })
        ]
      })
    ]
    const issues = validateTreeData(data)
    const dupIssue = issues.find((i) => i.message.includes('sowohl in rootItems als auch in einer Kollektion'))
    expect(dupIssue).toBeUndefined()
  })

  it('flags a dangling rootItemId reference', () => {
    const data = [item({ id: 'orphan', rootItemId: 'non-existent' })]
    const issues = validateTreeData(data)
    expect(issues).toHaveLength(1)
    expect(issues[0].message).toContain('orphan')
    expect(issues[0].message).toContain('non-existent')
  })

  it('flags multiple issues at once', () => {
    const badItem = item({
      id: 'bad',
      item_type: 'exercise',
      items: [
        collItem({ id: 'ci-bad', collectionId: 'bad', item: item({ id: 'child' }) })
      ]
    }) as Item & { items: CollectionItem[] }

    const data = [
      badItem,
      item({ id: 'dangling', rootItemId: 'nowhere' })
    ]

    const issues = validateTreeData(data)
    expect(issues.length).toBeGreaterThanOrEqual(2)
  })

  it('does not flag an exercise with an empty items array', () => {
    const ex = item({
      id: 'ex',
      items: []
    }) as Item & { items: CollectionItem[] }
    expect(validateTreeData([ex])).toEqual([])
  })

  it('flags a dangling rootItemId on a deeply nested item', () => {
    const data = [
      collection({
        id: 'coll-1',
        items: [
          collItem({
            id: 'ci-1',
            collectionId: 'coll-1',
            item: item({ id: 'deep', rootItemId: 'missing-root' })
          })
        ]
      })
    ]
    const issues = validateTreeData(data)
    expect(issues).toHaveLength(1)
    expect(issues[0].message).toContain('deep')
    expect(issues[0].message).toContain('missing-root')
  })

  it('does not flag self-referencing rootItemId', () => {
    const data = [item({ id: 'self', rootItemId: 'self' })]
    expect(validateTreeData(data)).toEqual([])
  })

  it('does not flag rootItemId pointing to a valid collection', () => {
    const data = [
      collection({
        id: 'coll-1',
        items: [
          collItem({ id: 'ci-1', collectionId: 'coll-1', item: item({ id: 'ex-1', rootItemId: 'coll-1' }) })
        ]
      })
    ]
    expect(validateTreeData(data)).toEqual([])
  })

  it('does not flag nested collection with valid structure', () => {
    const data = [
      collection({
        id: 'root',
        items: [
          collItem({
            id: 'ci-sub',
            collectionId: 'root',
            item: collection({
              id: 'sub',
              rootItemId: 'root',
              items: [
                collItem({ id: 'ci-leaf', collectionId: 'sub', item: item({ id: 'leaf', rootItemId: 'root' }) })
              ]
            })
          })
        ]
      })
    ]
    expect(validateTreeData(data)).toEqual([])
  })
})

describe('checkOnlyCollectionsHaveChildren', () => {
  it('returns no issues when all items follow the rule', () => {
    const data = [item({ id: 'a' }), collection({ id: 'c', items: [] })]
    expect(checkOnlyCollectionsHaveChildren(data)).toEqual([])
  })

  it('flags an exercise with children', () => {
    const bad = item({ id: 'ex', items: [collItem({ id: 'ci', collectionId: 'ex', item: item({ id: 'child' }) })] }) as Item & { items: CollectionItem[] }
    expect(checkOnlyCollectionsHaveChildren([bad])).toHaveLength(1)
  })

  it('does not flag an exercise with empty items', () => {
    const ex = item({ id: 'ex', items: [] }) as Item & { items: CollectionItem[] }
    expect(checkOnlyCollectionsHaveChildren([ex])).toEqual([])
  })
})

describe('checkNoDuplicateItems', () => {
  it('returns no issues when there are no duplicates', () => {
    const data = [item({ id: 'a' })]
    expect(checkNoDuplicateItems(data)).toEqual([])
  })

  it('flags an item appearing in rootItems and inside a collection', () => {
    const dup = item({ id: 'dup' })
    const data = [
      dup,
      collection({ id: 'c', items: [collItem({ id: 'ci', collectionId: 'c', item: dup })] })
    ]
    expect(checkNoDuplicateItems(data)).toHaveLength(1)
  })
})

describe('checkNoDanglingRootItemId', () => {
  it('returns no issues when all references are valid', () => {
    const data = [item({ id: 'a', rootItemId: 'b' }), item({ id: 'b' })]
    expect(checkNoDanglingRootItemId(data)).toEqual([])
  })

  it('flags a reference to a non-existent ID', () => {
    const data = [item({ id: 'orphan', rootItemId: 'missing' })]
    expect(checkNoDanglingRootItemId(data)).toHaveLength(1)
  })

  it('does not flag null rootItemId', () => {
    expect(checkNoDanglingRootItemId([item({ id: 'a' })])).toEqual([])
  })
})
