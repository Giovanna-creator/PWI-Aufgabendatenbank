-- -----------------------------------------------------------------------------
-- Table: item_content
-- Inhalte einer Aufgabe (getrennt von der Aufgabenstruktur)
-- Ein Item kann mehrere Item_Content haben (über item_contents Join-Tabelle)
-- -----------------------------------------------------------------------------
CREATE TABLE item_content (
    item_content_id         SERIAL PRIMARY KEY,
    license_id              INTEGER NOT NULL,
    item_content_type_id    INTEGER NOT NULL,
    author_id               INTEGER NOT NULL,
    json_serialized_content JSONB,
    blob_serialized_content BYTEA,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_content_license
        FOREIGN KEY (license_id)
        REFERENCES license(license_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_item_content_type
        FOREIGN KEY (item_content_type_id)
        REFERENCES item_content_type(item_content_type_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_item_content_author
        FOREIGN KEY (author_id)
        REFERENCES author(author_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_item_content_license     ON item_content(license_id);
CREATE INDEX idx_item_content_type        ON item_content(item_content_type_id);
CREATE INDEX idx_item_content_author      ON item_content(author_id);
CREATE INDEX idx_item_content_json        ON item_content USING GIN (json_serialized_content);

COMMENT ON TABLE item_content IS 'Inhaltsbausteine der Aufgaben (Text, JSON, Binärdaten)';
COMMENT ON COLUMN item_content.json_serialized_content IS 'Strukturierter Inhalt als JSONB (für Performance-Abfragen)';
COMMENT ON COLUMN item_content.blob_serialized_content IS 'Binärinhalt (Bilder, PDFs, Diagramme)';

-- -----------------------------------------------------------------------------
-- Table: item
-- Die zentrale Aufgaben-Entität
-- root_item_id ermöglicht die Verwaltung von Aufgaben-Varianten
-- -----------------------------------------------------------------------------
CREATE TABLE item (
    item_id             SERIAL PRIMARY KEY,
    author_id           INTEGER NOT NULL,
    license_id          INTEGER NOT NULL,
    item_type_id        INTEGER NOT NULL,
    item_template_id    INTEGER,
    root_item_id        INTEGER,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_author
        FOREIGN KEY (author_id)
        REFERENCES author(author_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_item_license
        FOREIGN KEY (license_id)
        REFERENCES license(license_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_item_type
        FOREIGN KEY (item_type_id)
        REFERENCES item_type(item_type_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_item_template
        FOREIGN KEY (item_template_id)
        REFERENCES item_representation_template(item_template_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_item_root_item
        FOREIGN KEY (root_item_id)
        REFERENCES item(item_id)
        ON DELETE SET NULL
);

CREATE INDEX idx_item_author        ON item(author_id);
CREATE INDEX idx_item_license       ON item(license_id);
CREATE INDEX idx_item_type          ON item(item_type_id);
CREATE INDEX idx_item_template      ON item(item_template_id);
CREATE INDEX idx_item_root_item     ON item(root_item_id);

COMMENT ON TABLE item IS 'Zentrale Aufgaben-Entität';
COMMENT ON COLUMN item.root_item_id IS 'Self-Reference: Verweist auf die Ursprungs-Aufgabe (für Varianten)';

-- -----------------------------------------------------------------------------
-- Table: item_collection
-- Sammlungen von Aufgaben (z.B. Lernpfade, Übungssets)
-- parent_item_id erlaubt eine optionale Verbindung zu einem übergeordneten Item
-- -----------------------------------------------------------------------------
CREATE TABLE item_collection (
    item_collection_id  SERIAL PRIMARY KEY,
    parent_item_id      INTEGER,
    "order"             BOOLEAN DEFAULT false,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_collection_parent_item
        FOREIGN KEY (parent_item_id)
        REFERENCES item(item_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_item_collection_parent_item ON item_collection(parent_item_id);

COMMENT ON TABLE item_collection IS 'Sammlungen von Aufgaben (Lernpfade, Sequenzen)';
COMMENT ON COLUMN item_collection."order" IS 'true=geordnete Sequenz (Positionen 1,2,3...), false=ungeordnete Gruppe (order ist ein reserviertes SQL-Wort, daher in Anführungszeichen)';