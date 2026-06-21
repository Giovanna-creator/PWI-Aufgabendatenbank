-- =============================================================================
-- V1_007 — Position nullable + updated_at fuer item_collection
-- =============================================================================
-- Aenderungen:
--   1. position in item_collection_sub_item wird NULLABLE (fuer ungeordnete Collections)
--   2. updated_at Spalte wird zu item_collection hinzugefuegt
-- =============================================================================

-- position NULLABLE erlauben (vorher: NOT NULL)
ALTER TABLE item_collection_sub_item
    ALTER COLUMN position DROP NOT NULL;

-- updated_at zu item_collection hinzufuegen
ALTER TABLE item_collection
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
