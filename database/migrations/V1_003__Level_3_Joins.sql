-- -----------------------------------------------------------------------------
-- Table: item_content_types
-- Many-to-many: Item_Type ↔ Item_Content_Type
-- Definiert, welche Content-Typen für welchen Item-Typ erlaubt sind
-- -----------------------------------------------------------------------------
CREATE TABLE item_content_types (
    item_type_id            INTEGER NOT NULL,
    item_content_type_id    INTEGER NOT NULL,
    PRIMARY KEY (item_type_id, item_content_type_id),
    CONSTRAINT fk_item_content_types_item_type
        FOREIGN KEY (item_type_id)
        REFERENCES item_type(item_type_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_content_types_content_type
        FOREIGN KEY (item_content_type_id)
        REFERENCES item_content_type(item_content_type_id)
        ON DELETE CASCADE
);

COMMENT ON TABLE item_content_types IS 'Erlaubte Content-Typen pro Item-Typ';

-- -----------------------------------------------------------------------------
-- Table: item_contents
-- Many-to-many: Item ↔ Item_Content
-- Ein Item kann mehrere Item_Content haben (mit Zweck/Purpose)
-- -----------------------------------------------------------------------------
CREATE TABLE item_contents (
    item_id         INTEGER NOT NULL,
    item_content_id INTEGER NOT NULL,
    purpose         TEXT,
    PRIMARY KEY (item_id, item_content_id),
    CONSTRAINT fk_item_contents_item
        FOREIGN KEY (item_id)
        REFERENCES item(item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_contents_content
        FOREIGN KEY (item_content_id)
        REFERENCES item_content(item_content_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_contents_item     ON item_contents(item_id);
CREATE INDEX idx_item_contents_content  ON item_contents(item_content_id);

COMMENT ON TABLE item_contents IS 'Verknüpfung Aufgabe ↔ Inhaltsbaustein';
COMMENT ON COLUMN item_contents.purpose IS 'Zweck dieses Contents (z.B. „Aufgabenstellung", „Hinweis", „Lösung")';

-- -----------------------------------------------------------------------------
-- Table: item_content_tags
-- Many-to-many: Item_Content ↔ Tag
-- Tagging auf Content-Ebene (z.B. „SQL", „JOIN")
-- -----------------------------------------------------------------------------
CREATE TABLE item_content_tags (
    item_content_id INTEGER NOT NULL,
    tag_id          INTEGER NOT NULL,
    PRIMARY KEY (item_content_id, tag_id),
    CONSTRAINT fk_item_content_tags_content
        FOREIGN KEY (item_content_id)
        REFERENCES item_content(item_content_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_content_tags_tag
        FOREIGN KEY (tag_id)
        REFERENCES tag(tag_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_content_tags_content  ON item_content_tags(item_content_id);
CREATE INDEX idx_item_content_tags_tag      ON item_content_tags(tag_id);

COMMENT ON TABLE item_content_tags IS 'Tags auf Content-Ebene';

-- -----------------------------------------------------------------------------
-- Table: item_tags
-- Many-to-many: Item ↔ Tag
-- Tagging auf Aufgaben-Ebene
-- -----------------------------------------------------------------------------
CREATE TABLE item_tags (
    item_id INTEGER NOT NULL,
    tag_id  INTEGER NOT NULL,
    PRIMARY KEY (item_id, tag_id),
    CONSTRAINT fk_item_tags_item
        FOREIGN KEY (item_id)
        REFERENCES item(item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_tags_tag
        FOREIGN KEY (tag_id)
        REFERENCES tag(tag_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_tags_item ON item_tags(item_id);
CREATE INDEX idx_item_tags_tag  ON item_tags(tag_id);

COMMENT ON TABLE item_tags IS 'Tags auf Aufgaben-Ebene';

-- -----------------------------------------------------------------------------
-- Table: item_validator
-- Many-to-many: Item ↔ Validator
-- Welche Validatoren gelten für welche Aufgabe?
-- -----------------------------------------------------------------------------
CREATE TABLE item_validator (
    item_id         INTEGER NOT NULL,
    validator_id    INTEGER NOT NULL,
    PRIMARY KEY (item_id, validator_id),
    CONSTRAINT fk_item_validator_item
        FOREIGN KEY (item_id)
        REFERENCES item(item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_validator_validator
        FOREIGN KEY (validator_id)
        REFERENCES validator(validator_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_validator_item        ON item_validator(item_id);
CREATE INDEX idx_item_validator_validator   ON item_validator(validator_id);

COMMENT ON TABLE item_validator IS 'Zuweisung von Validatoren zu Aufgaben (horizontale Restriktionen)';

-- -----------------------------------------------------------------------------
-- Table: item_modifier
-- Many-to-many: Item ↔ Modifier
-- Welche Modifier gelten für welche Aufgabe?
-- -----------------------------------------------------------------------------
CREATE TABLE item_modifier (
    item_id     INTEGER NOT NULL,
    modifier_id INTEGER NOT NULL,
    PRIMARY KEY (item_id, modifier_id),
    CONSTRAINT fk_item_modifier_item
        FOREIGN KEY (item_id)
        REFERENCES item(item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_modifier_modifier
        FOREIGN KEY (modifier_id)
        REFERENCES modifier(modifier_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_modifier_item     ON item_modifier(item_id);
CREATE INDEX idx_item_modifier_modifier ON item_modifier(modifier_id);

COMMENT ON TABLE item_modifier IS 'Zuweisung von Modifiers zu Aufgaben (horizontale Variationen)';

-- -----------------------------------------------------------------------------
-- Table: item_collection_sub_item
-- Many-to-many: Item_Collection ↔ Item (als Sub-Item)
-- Mit Positionsangabe für die Reihenfolge (vertikale Sequenzen)
-- -----------------------------------------------------------------------------
CREATE TABLE item_collection_sub_item (
    item_collection_id  INTEGER NOT NULL,
    subitem_id          INTEGER NOT NULL,
    position            INTEGER NOT NULL,
    PRIMARY KEY (item_collection_id, subitem_id),
    CONSTRAINT fk_item_collection_sub_item_collection
        FOREIGN KEY (item_collection_id)
        REFERENCES item_collection(item_collection_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_collection_sub_item_item
        FOREIGN KEY (subitem_id)
        REFERENCES item(item_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_collection_sub_item_collection    ON item_collection_sub_item(item_collection_id);
CREATE INDEX idx_item_collection_sub_item_item          ON item_collection_sub_item(subitem_id);

COMMENT ON TABLE item_collection_sub_item IS 'Aufgaben innerhalb einer Collection (mit Reihenfolge)';
COMMENT ON COLUMN item_collection_sub_item.position IS 'Position der Aufgabe in der Sequenz (1, 2, 3, ...)';