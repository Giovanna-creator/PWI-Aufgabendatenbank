-- =============================================================================
-- V1_006 — UUID-Migration: Alle Primary Keys und Foreign Keys auf UUID umstellen
-- =============================================================================
-- Strategie:
--   Phase 1: UUID-Spalten zu allen Tabellen hinzufuegen
--   Phase 2: UUIDs fuer bestehende Daten generieren
--   Phase 3: Join-Tabellen auf UUID-Fremdschluessel umstellen
--   Phase 4: Alte Spalten/Constraints entfernen, neue setzen
-- =============================================================================

-- pgcrypto fuer gen_random_uuid() aktivieren
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- PHASE 1: UUID-Spalten zu Referenz-Tabellen hinzufuegen
-- =============================================================================

ALTER TABLE author ADD COLUMN author_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE license ADD COLUMN license_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE tag ADD COLUMN tag_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE item_type ADD COLUMN item_type_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE item_content_type ADD COLUMN item_content_type_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE item_representation_template ADD COLUMN item_template_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE validator ADD COLUMN validator_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE modifier ADD COLUMN modifier_id_new UUID DEFAULT gen_random_uuid();

-- =============================================================================
-- PHASE 2: UUID-Spalten zu Kern-Tabellen hinzufuegen
-- =============================================================================

ALTER TABLE item ADD COLUMN item_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE item_content ADD COLUMN item_content_id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE item_collection ADD COLUMN item_collection_id_new UUID DEFAULT gen_random_uuid();

-- =============================================================================
-- PHASE 3: UUIDs generieren (alle Zeilen bekommen eine UUID)
-- =============================================================================

UPDATE author SET author_id_new = gen_random_uuid();
UPDATE license SET license_id_new = gen_random_uuid();
UPDATE tag SET tag_id_new = gen_random_uuid();
UPDATE item_type SET item_type_id_new = gen_random_uuid();
UPDATE item_content_type SET item_content_type_id_new = gen_random_uuid();
UPDATE item_representation_template SET item_template_id_new = gen_random_uuid();
UPDATE validator SET validator_id_new = gen_random_uuid();
UPDATE modifier SET modifier_id_new = gen_random_uuid();
UPDATE item SET item_id_new = gen_random_uuid();
UPDATE item_content SET item_content_id_new = gen_random_uuid();
UPDATE item_collection SET item_collection_id_new = gen_random_uuid();

-- =============================================================================
-- PHASE 4: Self-Reference FKs aktualisieren (tag.parent_tag_id, item.root_item_id)
-- =============================================================================

-- tag.parent_tag_id: alten int-Wert durch UUID ersetzen
ALTER TABLE tag ADD COLUMN parent_tag_id_new UUID;

UPDATE tag t
SET parent_tag_id_new = t2.tag_id_new
FROM tag t2
WHERE t.parent_tag_id = t2.tag_id;

-- item.root_item_id: alten int-Wert durch UUID ersetzen
ALTER TABLE item ADD COLUMN root_item_id_new UUID;

UPDATE item i
SET root_item_id_new = i2.item_id_new
FROM item i2
WHERE i.root_item_id = i2.item_id;

-- =============================================================================
-- PHASE 5: Fremdschluessel in Kern-Tabellen aktualisieren
-- =============================================================================

-- item: author_id, license_id, item_type_id, item_template_id
ALTER TABLE item ADD COLUMN author_id_new UUID;
ALTER TABLE item ADD COLUMN license_id_new UUID;
ALTER TABLE item ADD COLUMN item_type_id_new UUID;
ALTER TABLE item ADD COLUMN item_template_id_new UUID;

UPDATE item i SET author_id_new = a.author_id_new FROM author a WHERE i.author_id = a.author_id;
UPDATE item i SET license_id_new = l.license_id_new FROM license l WHERE i.license_id = l.license_id;
UPDATE item i SET item_type_id_new = it.item_type_id_new FROM item_type it WHERE i.item_type_id = it.item_type_id;
UPDATE item i SET item_template_id_new = irt.item_template_id_new FROM item_representation_template irt WHERE i.item_template_id = irt.item_template_id;

-- item_content: license_id, item_content_type_id, author_id
ALTER TABLE item_content ADD COLUMN license_id_new UUID;
ALTER TABLE item_content ADD COLUMN item_content_type_id_new UUID;
ALTER TABLE item_content ADD COLUMN author_id_new UUID;

UPDATE item_content ic SET license_id_new = l.license_id_new FROM license l WHERE ic.license_id = l.license_id;
UPDATE item_content ic SET item_content_type_id_new = ict.item_content_type_id_new FROM item_content_type ict WHERE ic.item_content_type_id = ict.item_content_type_id;
UPDATE item_content ic SET author_id_new = a.author_id_new FROM author a WHERE ic.author_id = a.author_id;

-- item_collection: parent_item_id
ALTER TABLE item_collection ADD COLUMN parent_item_id_new UUID;

UPDATE item_collection ic
SET parent_item_id_new = i.item_id_new
FROM item i
WHERE ic.parent_item_id = i.item_id;

-- =============================================================================
-- PHASE 6: Join-Tabellen auf UUID umstellen
-- =============================================================================

-- item_contents (item ↔ item_content)
ALTER TABLE item_contents ADD COLUMN item_id_new UUID;
ALTER TABLE item_contents ADD COLUMN item_content_id_new UUID;

UPDATE item_contents ic SET item_id_new = i.item_id_new FROM item i WHERE ic.item_id = i.item_id;
UPDATE item_contents ic SET item_content_id_new = ic2.item_content_id_new FROM item_content ic2 WHERE ic.item_content_id = ic2.item_content_id;

-- item_tags (item ↔ tag)
ALTER TABLE item_tags ADD COLUMN item_id_new UUID;
ALTER TABLE item_tags ADD COLUMN tag_id_new UUID;

UPDATE item_tags it SET item_id_new = i.item_id_new FROM item i WHERE it.item_id = i.item_id;
UPDATE item_tags it SET tag_id_new = t.tag_id_new FROM tag t WHERE it.tag_id = t.tag_id;

-- item_content_tags (item_content ↔ tag)
ALTER TABLE item_content_tags ADD COLUMN item_content_id_new UUID;
ALTER TABLE item_content_tags ADD COLUMN tag_id_new UUID;

UPDATE item_content_tags ict SET item_content_id_new = ic.item_content_id_new FROM item_content ic WHERE ict.item_content_id = ic.item_content_id;
UPDATE item_content_tags ict SET tag_id_new = t.tag_id_new FROM tag t WHERE ict.tag_id = t.tag_id;

-- item_content_types (item_type ↔ item_content_type)
ALTER TABLE item_content_types ADD COLUMN item_type_id_new UUID;
ALTER TABLE item_content_types ADD COLUMN item_content_type_id_new UUID;

UPDATE item_content_types ict SET item_type_id_new = it.item_type_id_new FROM item_type it WHERE ict.item_type_id = it.item_type_id;
UPDATE item_content_types ict SET item_content_type_id_new = ict2.item_content_type_id_new FROM item_content_type ict2 WHERE ict.item_content_type_id = ict2.item_content_type_id;

-- item_validator (item ↔ validator)
ALTER TABLE item_validator ADD COLUMN item_id_new UUID;
ALTER TABLE item_validator ADD COLUMN validator_id_new UUID;

UPDATE item_validator iv SET item_id_new = i.item_id_new FROM item i WHERE iv.item_id = i.item_id;
UPDATE item_validator iv SET validator_id_new = v.validator_id_new FROM validator v WHERE iv.validator_id = v.validator_id;

-- item_modifier (item ↔ modifier)
ALTER TABLE item_modifier ADD COLUMN item_id_new UUID;
ALTER TABLE item_modifier ADD COLUMN modifier_id_new UUID;

UPDATE item_modifier im SET item_id_new = i.item_id_new FROM item i WHERE im.item_id = i.item_id;
UPDATE item_modifier im SET modifier_id_new = m.modifier_id_new FROM modifier m WHERE im.modifier_id = m.modifier_id;

-- item_collection_sub_item (item_collection ↔ item)
ALTER TABLE item_collection_sub_item ADD COLUMN item_collection_id_new UUID;
ALTER TABLE item_collection_sub_item ADD COLUMN subitem_id_new UUID;

UPDATE item_collection_sub_item icsi
SET item_collection_id_new = ic.item_collection_id_new
FROM item_collection ic
WHERE icsi.item_collection_id = ic.item_collection_id;

UPDATE item_collection_sub_item icsi
SET subitem_id_new = i.item_id_new
FROM item i
WHERE icsi.subitem_id = i.item_id;

-- =============================================================================
-- PHASE 7: Alte Constraints und Spalten entfernen
-- =============================================================================

-- --- Join-Tabellen: alte Spalten entfernen ---

-- item_contents
ALTER TABLE item_contents DROP CONSTRAINT IF EXISTS fk_item_contents_item;
ALTER TABLE item_contents DROP CONSTRAINT IF EXISTS fk_item_contents_content;
ALTER TABLE item_contents DROP CONSTRAINT IF EXISTS item_contents_pkey;
ALTER TABLE item_contents DROP COLUMN item_id;
ALTER TABLE item_contents DROP COLUMN item_content_id;
ALTER TABLE item_contents RENAME COLUMN item_id_new TO item_id;
ALTER TABLE item_contents RENAME COLUMN item_content_id_new TO item_content_id;
ALTER TABLE item_contents ADD PRIMARY KEY (item_id, item_content_id);

-- item_tags
ALTER TABLE item_tags DROP CONSTRAINT IF EXISTS fk_item_tags_item;
ALTER TABLE item_tags DROP CONSTRAINT IF EXISTS fk_item_tags_tag;
ALTER TABLE item_tags DROP CONSTRAINT IF EXISTS item_tags_pkey;
ALTER TABLE item_tags DROP COLUMN item_id;
ALTER TABLE item_tags DROP COLUMN tag_id;
ALTER TABLE item_tags RENAME COLUMN item_id_new TO item_id;
ALTER TABLE item_tags RENAME COLUMN tag_id_new TO tag_id;
ALTER TABLE item_tags ADD PRIMARY KEY (item_id, tag_id);

-- item_content_tags
ALTER TABLE item_content_tags DROP CONSTRAINT IF EXISTS fk_item_content_tags_content;
ALTER TABLE item_content_tags DROP CONSTRAINT IF EXISTS fk_item_content_tags_tag;
ALTER TABLE item_content_tags DROP CONSTRAINT IF EXISTS item_content_tags_pkey;
ALTER TABLE item_content_tags DROP COLUMN item_content_id;
ALTER TABLE item_content_tags DROP COLUMN tag_id;
ALTER TABLE item_content_tags RENAME COLUMN item_content_id_new TO item_content_id;
ALTER TABLE item_content_tags RENAME COLUMN tag_id_new TO tag_id;
ALTER TABLE item_content_tags ADD PRIMARY KEY (item_content_id, tag_id);

-- item_content_types
ALTER TABLE item_content_types DROP CONSTRAINT IF EXISTS fk_item_content_types_item_type;
ALTER TABLE item_content_types DROP CONSTRAINT IF EXISTS fk_item_content_types_content_type;
ALTER TABLE item_content_types DROP CONSTRAINT IF EXISTS item_content_types_pkey;
ALTER TABLE item_content_types DROP COLUMN item_type_id;
ALTER TABLE item_content_types DROP COLUMN item_content_type_id;
ALTER TABLE item_content_types RENAME COLUMN item_type_id_new TO item_type_id;
ALTER TABLE item_content_types RENAME COLUMN item_content_type_id_new TO item_content_type_id;
ALTER TABLE item_content_types ADD PRIMARY KEY (item_type_id, item_content_type_id);

-- item_validator
ALTER TABLE item_validator DROP CONSTRAINT IF EXISTS fk_item_validator_item;
ALTER TABLE item_validator DROP CONSTRAINT IF EXISTS fk_item_validator_validator;
ALTER TABLE item_validator DROP CONSTRAINT IF EXISTS item_validator_pkey;
ALTER TABLE item_validator DROP COLUMN item_id;
ALTER TABLE item_validator DROP COLUMN validator_id;
ALTER TABLE item_validator RENAME COLUMN item_id_new TO item_id;
ALTER TABLE item_validator RENAME COLUMN validator_id_new TO validator_id;
ALTER TABLE item_validator ADD PRIMARY KEY (item_id, validator_id);

-- item_modifier
ALTER TABLE item_modifier DROP CONSTRAINT IF EXISTS fk_item_modifier_item;
ALTER TABLE item_modifier DROP CONSTRAINT IF EXISTS fk_item_modifier_modifier;
ALTER TABLE item_modifier DROP CONSTRAINT IF EXISTS item_modifier_pkey;
ALTER TABLE item_modifier DROP COLUMN item_id;
ALTER TABLE item_modifier DROP COLUMN modifier_id;
ALTER TABLE item_modifier RENAME COLUMN item_id_new TO item_id;
ALTER TABLE item_modifier RENAME COLUMN modifier_id_new TO modifier_id;
ALTER TABLE item_modifier ADD PRIMARY KEY (item_id, modifier_id);

-- item_collection_sub_item
ALTER TABLE item_collection_sub_item DROP CONSTRAINT IF EXISTS fk_item_collection_sub_item_collection;
ALTER TABLE item_collection_sub_item DROP CONSTRAINT IF EXISTS fk_item_collection_sub_item_item;
ALTER TABLE item_collection_sub_item DROP CONSTRAINT IF EXISTS item_collection_sub_item_pkey;
ALTER TABLE item_collection_sub_item DROP COLUMN item_collection_id;
ALTER TABLE item_collection_sub_item DROP COLUMN subitem_id;
ALTER TABLE item_collection_sub_item RENAME COLUMN item_collection_id_new TO item_collection_id;
ALTER TABLE item_collection_sub_item RENAME COLUMN subitem_id_new TO subitem_id;
ALTER TABLE item_collection_sub_item ADD PRIMARY KEY (item_collection_id, subitem_id);

-- --- Referenz-Tabellen: alte PK-Spalten entfernen ---

-- tag: parent_tag_id zuerst aktualisieren, dann alte Spalten entfernen
ALTER TABLE tag DROP CONSTRAINT IF EXISTS fk_tag_parent_tag_id;
ALTER TABLE tag DROP COLUMN parent_tag_id;
ALTER TABLE tag DROP COLUMN tag_id;
ALTER TABLE tag RENAME COLUMN tag_id_new TO tag_id;
ALTER TABLE tag RENAME COLUMN parent_tag_id_new TO parent_tag_id;
ALTER TABLE tag ADD PRIMARY KEY (tag_id);

-- tag: FK fuer parent_tag_id neu setzen
ALTER TABLE tag ADD CONSTRAINT fk_tag_parent_tag_id
    FOREIGN KEY (parent_tag_id) REFERENCES tag(tag_id) ON DELETE SET NULL;

-- author
ALTER TABLE author DROP COLUMN author_id;
ALTER TABLE author RENAME COLUMN author_id_new TO author_id;
ALTER TABLE author ADD PRIMARY KEY (author_id);

-- license
ALTER TABLE license DROP COLUMN license_id;
ALTER TABLE license RENAME COLUMN license_id_new TO license_id;
ALTER TABLE license ADD PRIMARY KEY (license_id);

-- item_type
ALTER TABLE item_type DROP COLUMN item_type_id;
ALTER TABLE item_type RENAME COLUMN item_type_id_new TO item_type_id;
ALTER TABLE item_type ADD PRIMARY KEY (item_type_id);

-- item_content_type
ALTER TABLE item_content_type DROP COLUMN item_content_type_id;
ALTER TABLE item_content_type RENAME COLUMN item_content_type_id_new TO item_content_type_id;
ALTER TABLE item_content_type ADD PRIMARY KEY (item_content_type_id);

-- item_representation_template
ALTER TABLE item_representation_template DROP COLUMN item_template_id;
ALTER TABLE item_representation_template RENAME COLUMN item_template_id_new TO item_template_id;
ALTER TABLE item_representation_template ADD PRIMARY KEY (item_template_id);

-- validator
ALTER TABLE validator DROP COLUMN validator_id;
ALTER TABLE validator RENAME COLUMN validator_id_new TO validator_id;
ALTER TABLE validator ADD PRIMARY KEY (validator_id);

-- modifier
ALTER TABLE modifier DROP COLUMN modifier_id;
ALTER TABLE modifier RENAME COLUMN modifier_id_new TO modifier_id;
ALTER TABLE modifier ADD PRIMARY KEY (modifier_id);

-- --- Kern-Tabellen: alte Spalten und Constraints entfernen ---

-- item: alte FKs entfernen
ALTER TABLE item DROP CONSTRAINT IF EXISTS fk_item_author;
ALTER TABLE item DROP CONSTRAINT IF EXISTS fk_item_license;
ALTER TABLE item DROP CONSTRAINT IF EXISTS fk_item_type;
ALTER TABLE item DROP CONSTRAINT IF EXISTS fk_item_template;
ALTER TABLE item DROP CONSTRAINT IF EXISTS fk_item_root_item;

ALTER TABLE item DROP COLUMN author_id;
ALTER TABLE item DROP COLUMN license_id;
ALTER TABLE item DROP COLUMN item_type_id;
ALTER TABLE item DROP COLUMN item_template_id;
ALTER TABLE item DROP COLUMN root_item_id;
ALTER TABLE item DROP COLUMN item_id;

ALTER TABLE item RENAME COLUMN item_id_new TO item_id;
ALTER TABLE item RENAME COLUMN author_id_new TO author_id;
ALTER TABLE item RENAME COLUMN license_id_new TO license_id;
ALTER TABLE item RENAME COLUMN item_type_id_new TO item_type_id;
ALTER TABLE item RENAME COLUMN item_template_id_new TO item_template_id;
ALTER TABLE item RENAME COLUMN root_item_id_new TO root_item_id;

ALTER TABLE item ADD PRIMARY KEY (item_id);

-- item: neue FKs setzen
ALTER TABLE item ADD CONSTRAINT fk_item_author
    FOREIGN KEY (author_id) REFERENCES author(author_id) ON DELETE RESTRICT;
ALTER TABLE item ADD CONSTRAINT fk_item_license
    FOREIGN KEY (license_id) REFERENCES license(license_id) ON DELETE RESTRICT;
ALTER TABLE item ADD CONSTRAINT fk_item_type
    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id) ON DELETE RESTRICT;
ALTER TABLE item ADD CONSTRAINT fk_item_template
    FOREIGN KEY (item_template_id) REFERENCES item_representation_template(item_template_id) ON DELETE SET NULL;
ALTER TABLE item ADD CONSTRAINT fk_item_root_item
    FOREIGN KEY (root_item_id) REFERENCES item(item_id) ON DELETE SET NULL;

-- item_content: alte FKs entfernen
ALTER TABLE item_content DROP CONSTRAINT IF EXISTS fk_item_content_license;
ALTER TABLE item_content DROP CONSTRAINT IF EXISTS fk_item_content_type;
ALTER TABLE item_content DROP CONSTRAINT IF EXISTS fk_item_content_author;

ALTER TABLE item_content DROP COLUMN license_id;
ALTER TABLE item_content DROP COLUMN item_content_type_id;
ALTER TABLE item_content DROP COLUMN author_id;
ALTER TABLE item_content DROP COLUMN item_content_id;

ALTER TABLE item_content RENAME COLUMN item_content_id_new TO item_content_id;
ALTER TABLE item_content RENAME COLUMN license_id_new TO license_id;
ALTER TABLE item_content RENAME COLUMN item_content_type_id_new TO item_content_type_id;
ALTER TABLE item_content RENAME COLUMN author_id_new TO author_id;

ALTER TABLE item_content ADD PRIMARY KEY (item_content_id);

-- item_content: neue FKs setzen
ALTER TABLE item_content ADD CONSTRAINT fk_item_content_license
    FOREIGN KEY (license_id) REFERENCES license(license_id) ON DELETE RESTRICT;
ALTER TABLE item_content ADD CONSTRAINT fk_item_content_type
    FOREIGN KEY (item_content_type_id) REFERENCES item_content_type(item_content_type_id) ON DELETE RESTRICT;
ALTER TABLE item_content ADD CONSTRAINT fk_item_content_author
    FOREIGN KEY (author_id) REFERENCES author(author_id) ON DELETE RESTRICT;

-- item_collection: alte FKs entfernen
ALTER TABLE item_collection DROP CONSTRAINT IF EXISTS fk_item_collection_parent_item;

ALTER TABLE item_collection DROP COLUMN parent_item_id;
ALTER TABLE item_collection DROP COLUMN item_collection_id;

ALTER TABLE item_collection RENAME COLUMN item_collection_id_new TO item_collection_id;
ALTER TABLE item_collection RENAME COLUMN parent_item_id_new TO parent_item_id;

ALTER TABLE item_collection ADD PRIMARY KEY (item_collection_id);

-- item_collection: neue FK setzen
ALTER TABLE item_collection ADD CONSTRAINT fk_item_collection_parent_item
    FOREIGN KEY (parent_item_id) REFERENCES item(item_id) ON DELETE CASCADE;

-- =============================================================================
-- PHASE 8: Neue FK-Constraints fuer Join-Tabellen setzen
-- =============================================================================

ALTER TABLE item_contents ADD CONSTRAINT fk_item_contents_item
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE;
ALTER TABLE item_contents ADD CONSTRAINT fk_item_contents_content
    FOREIGN KEY (item_content_id) REFERENCES item_content(item_content_id) ON DELETE CASCADE;

ALTER TABLE item_tags ADD CONSTRAINT fk_item_tags_item
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE;
ALTER TABLE item_tags ADD CONSTRAINT fk_item_tags_tag
    FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON DELETE CASCADE;

ALTER TABLE item_content_tags ADD CONSTRAINT fk_item_content_tags_content
    FOREIGN KEY (item_content_id) REFERENCES item_content(item_content_id) ON DELETE CASCADE;
ALTER TABLE item_content_tags ADD CONSTRAINT fk_item_content_tags_tag
    FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON DELETE CASCADE;

ALTER TABLE item_content_types ADD CONSTRAINT fk_item_content_types_item_type
    FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id) ON DELETE CASCADE;
ALTER TABLE item_content_types ADD CONSTRAINT fk_item_content_types_content_type
    FOREIGN KEY (item_content_type_id) REFERENCES item_content_type(item_content_type_id) ON DELETE CASCADE;

ALTER TABLE item_validator ADD CONSTRAINT fk_item_validator_item
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE;
ALTER TABLE item_validator ADD CONSTRAINT fk_item_validator_validator
    FOREIGN KEY (validator_id) REFERENCES validator(validator_id) ON DELETE CASCADE;

ALTER TABLE item_modifier ADD CONSTRAINT fk_item_modifier_item
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE;
ALTER TABLE item_modifier ADD CONSTRAINT fk_item_modifier_modifier
    FOREIGN KEY (modifier_id) REFERENCES modifier(modifier_id) ON DELETE CASCADE;

ALTER TABLE item_collection_sub_item ADD CONSTRAINT fk_item_collection_sub_item_collection
    FOREIGN KEY (item_collection_id) REFERENCES item_collection(item_collection_id) ON DELETE CASCADE;
ALTER TABLE item_collection_sub_item ADD CONSTRAINT fk_item_collection_sub_item_item
    FOREIGN KEY (subitem_id) REFERENCES item(item_id) ON DELETE CASCADE;

-- =============================================================================
-- PHASE 9: Indizes neu erstellen
-- =============================================================================

DROP INDEX IF EXISTS idx_tag_parent_tag_id;
CREATE INDEX idx_tag_parent_tag_id ON tag(parent_tag_id);

DROP INDEX IF EXISTS idx_item_author;
CREATE INDEX idx_item_author ON item(author_id);
DROP INDEX IF EXISTS idx_item_license;
CREATE INDEX idx_item_license ON item(license_id);
DROP INDEX IF EXISTS idx_item_type;
CREATE INDEX idx_item_type ON item(item_type_id);
DROP INDEX IF EXISTS idx_item_template;
CREATE INDEX idx_item_template ON item(item_template_id);
DROP INDEX IF EXISTS idx_item_root_item;
CREATE INDEX idx_item_root_item ON item(root_item_id);

DROP INDEX IF EXISTS idx_item_content_license;
CREATE INDEX idx_item_content_license ON item_content(license_id);
DROP INDEX IF EXISTS idx_item_content_type;
CREATE INDEX idx_item_content_type ON item_content(item_content_type_id);
DROP INDEX IF EXISTS idx_item_content_author;
CREATE INDEX idx_item_content_author ON item_content(author_id);

DROP INDEX IF EXISTS idx_item_contents_item;
CREATE INDEX idx_item_contents_item ON item_contents(item_id);
DROP INDEX IF EXISTS idx_item_contents_content;
CREATE INDEX idx_item_contents_content ON item_contents(item_content_id);

DROP INDEX IF EXISTS idx_item_content_tags_content;
CREATE INDEX idx_item_content_tags_content ON item_content_tags(item_content_id);
DROP INDEX IF EXISTS idx_item_content_tags_tag;
CREATE INDEX idx_item_content_tags_tag ON item_content_tags(tag_id);

DROP INDEX IF EXISTS idx_item_tags_item;
CREATE INDEX idx_item_tags_item ON item_tags(item_id);
DROP INDEX IF EXISTS idx_item_tags_tag;
CREATE INDEX idx_item_tags_tag ON item_tags(tag_id);

DROP INDEX IF EXISTS idx_item_validator_item;
CREATE INDEX idx_item_validator_item ON item_validator(item_id);
DROP INDEX IF EXISTS idx_item_validator_validator;
CREATE INDEX idx_item_validator_validator ON item_validator(validator_id);

DROP INDEX IF EXISTS idx_item_modifier_item;
CREATE INDEX idx_item_modifier_item ON item_modifier(item_id);
DROP INDEX IF EXISTS idx_item_modifier_modifier;
CREATE INDEX idx_item_modifier_modifier ON item_modifier(modifier_id);

DROP INDEX IF EXISTS idx_item_collection_parent_item;
CREATE INDEX idx_item_collection_parent_item ON item_collection(parent_item_id);

DROP INDEX IF EXISTS idx_item_collection_sub_item_collection;
CREATE INDEX idx_item_collection_sub_item_collection ON item_collection_sub_item(item_collection_id);
DROP INDEX IF EXISTS idx_item_collection_sub_item_item;
CREATE INDEX idx_item_collection_sub_item_item ON item_collection_sub_item(subitem_id);
