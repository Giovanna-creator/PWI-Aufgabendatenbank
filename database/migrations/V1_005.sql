-- =============================================================
-- Migration V1_005: order-Spalte in item_collection auf BOOLEAN
-- =============================================================
--
-- Begründung:
-- Die Spalte "order" in item_collection war ursprünglich als INTEGER
-- definiert (zur Sortierung mehrerer Kollektionen). Nach Abstimmung
-- mit dem Frontend wird "order" jetzt als BOOLEAN verwendet:
--  - true:  Kollektion ist geordnet (SubItems haben Positionen 1, 2, 3...)
--  - false: Kollektion ist ungeordnet (Positionen werden nicht angezeigt)
--
-- Bestehende Daten werden als FALSE migriert (ungeordnet als Default).
-- =============================================================

ALTER TABLE item_collection
    ADD COLUMN order_new BOOLEAN NOT NULL DEFAULT FALSE;


ALTER TABLE item_collection
    DROP COLUMN "order";

ALTER TABLE item_collection
    RENAME COLUMN order_new TO "order";