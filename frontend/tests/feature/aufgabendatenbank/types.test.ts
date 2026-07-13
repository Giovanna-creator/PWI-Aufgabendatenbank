import { describe, it, expect } from 'vitest'
import { getInnerItem, isCollection, isCollectionItem } from '@/lib/types'
import type { Item, Collection, CollectionItem } from '@/lib/types'

function makeItem(overrides: Partial<Item> = {}): Item {
  return {
    id: 'item-1',
    item_type: 'exercise',
    author: 'test',
    representationTemplate: null,
    license: null,
    tags: [],
    validators: [],
    modifiers: [],
    contents: [],
    ...overrides
  }
}

function makeCollection(overrides: Partial<Collection> = {}): Collection {
  return {
    id: 'coll-1',
    item_type: 'collection',
    author: 'test',
    representationTemplate: null,
    license: null,
    tags: [],
    validators: [],
    modifiers: [],
    contents: [],
    items: [],
    order: false,
    ...overrides
  }
}

function makeCollectionItem(overrides: Partial<CollectionItem> = {}): CollectionItem {
  return {
    id: 'ci-1',
    collectionId: 'coll-1',
    item: makeItem({ id: 'child-1' }),
    position: null,
    ...overrides
  }
}

describe('getInnerItem', () => {
  it('returns the same item for a plain Item', () => {
    const item = makeItem()
    expect(getInnerItem(item)).toBe(item)
  })

  it('returns the nested item for a CollectionItem', () => {
    const child = makeItem({ id: 'child-1' })
    const ci = makeCollectionItem({ item: child })
    expect(getInnerItem(ci)).toBe(child)
  })

  it('returns the same item for a Collection (which is also an Item)', () => {
    const coll = makeCollection()
    expect(getInnerItem(coll)).toBe(coll)
  })
})

describe('isCollection', () => {
  it('returns true for a collection', () => {
    expect(isCollection(makeCollection())).toBe(true)
  })

  it('returns false for a plain item', () => {
    expect(isCollection(makeItem())).toBe(false)
  })

  it('returns false for a CollectionItem', () => {
    expect(isCollection(makeCollectionItem())).toBe(false)
  })

  it('returns false for null/undefined', () => {
    expect(isCollection(null)).toBe(false)
    expect(isCollection(undefined)).toBe(false)
  })

  it('returns true for item_type collection even without items', () => {
    expect(isCollection(makeItem({ item_type: 'collection' }))).toBe(true)
  })
})

describe('isCollectionItem', () => {
  it('returns true for a CollectionItem', () => {
    expect(isCollectionItem(makeCollectionItem())).toBe(true)
  })

  it('returns false for a plain item', () => {
    expect(isCollectionItem(makeItem())).toBe(false)
  })

  it('returns false for a Collection', () => {
    expect(isCollectionItem(makeCollection())).toBe(false)
  })

  it('returns false for null/undefined', () => {
    expect(isCollectionItem(null)).toBe(false)
    expect(isCollectionItem(undefined)).toBe(false)
  })

  it('returns false for an object missing item property', () => {
    expect(isCollectionItem({ id: 'x', collectionId: 'y' })).toBe(false)
  })
})
