-- PWI-Aufgabendatenbank — Schema + Seeddaten für frisches PostgreSQL
-- Alle Tabellen mit UUID-Primärschlüsseln (keine Migration nötig)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Level 1: Referenztabellen ─────────────────────────────────────────────────

CREATE TABLE item_content_type (
    item_content_type_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    item_content_type_name  TEXT NOT NULL,
    description             TEXT
);

CREATE TABLE license (
    license_id  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    license     TEXT NOT NULL UNIQUE
);

CREATE TABLE tag (
    tag_id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    parent_tag_id   UUID,
    tag             TEXT NOT NULL,
    description     TEXT,
    CONSTRAINT fk_tag_parent_tag_id
        FOREIGN KEY (parent_tag_id) REFERENCES tag(tag_id) ON DELETE SET NULL
);
CREATE INDEX idx_tag_parent_tag_id ON tag(parent_tag_id);

CREATE TABLE author (
    author_id   UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    descriptor  TEXT NOT NULL,
    mail        TEXT
);

CREATE TABLE item_type (
    item_type_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    item_type_name  TEXT NOT NULL,
    description     TEXT
);

CREATE TABLE item_representation_template (
    item_template_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    template            TEXT NOT NULL
);

CREATE TABLE validator (
    validator_id    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    description     TEXT NOT NULL,
    validator       TEXT NOT NULL
);

CREATE TABLE modifier (
    modifier_id     UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    description     TEXT NOT NULL,
    modifier        TEXT NOT NULL
);

-- ── Level 2: Kerntabellen ─────────────────────────────────────────────────────

CREATE TABLE item_content (
    item_content_id         UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    license_id              UUID NOT NULL,
    item_content_type_id    UUID NOT NULL,
    author_id               UUID NOT NULL,
    json_serialized_content JSONB,
    blob_serialized_content BYTEA,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_content_license
        FOREIGN KEY (license_id) REFERENCES license(license_id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_content_type
        FOREIGN KEY (item_content_type_id) REFERENCES item_content_type(item_content_type_id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_content_author
        FOREIGN KEY (author_id) REFERENCES author(author_id) ON DELETE RESTRICT
);
CREATE INDEX idx_item_content_license     ON item_content(license_id);
CREATE INDEX idx_item_content_type        ON item_content(item_content_type_id);
CREATE INDEX idx_item_content_author      ON item_content(author_id);
CREATE INDEX idx_item_content_json        ON item_content USING GIN (json_serialized_content);

CREATE TABLE item (
    item_id             UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    author_id           UUID NOT NULL,
    license_id          UUID NOT NULL,
    item_type_id        UUID NOT NULL,
    item_template_id    UUID,
    root_item_id        UUID,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_author
        FOREIGN KEY (author_id) REFERENCES author(author_id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_license
        FOREIGN KEY (license_id) REFERENCES license(license_id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_type
        FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_template
        FOREIGN KEY (item_template_id) REFERENCES item_representation_template(item_template_id) ON DELETE SET NULL,
    CONSTRAINT fk_item_root_item
        FOREIGN KEY (root_item_id) REFERENCES item(item_id) ON DELETE SET NULL
);
CREATE INDEX idx_item_author        ON item(author_id);
CREATE INDEX idx_item_license       ON item(license_id);
CREATE INDEX idx_item_type          ON item(item_type_id);
CREATE INDEX idx_item_template      ON item(item_template_id);
CREATE INDEX idx_item_root_item     ON item(root_item_id);

CREATE TABLE item_collection (
    item_collection_id  UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    parent_item_id      UUID,
    "order"             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_item_collection_parent_item
        FOREIGN KEY (parent_item_id) REFERENCES item(item_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_collection_parent_item ON item_collection(parent_item_id);

-- ── Level 3: Join-Tabellen ────────────────────────────────────────────────────

CREATE TABLE item_content_types (
    item_type_id            UUID NOT NULL,
    item_content_type_id    UUID NOT NULL,
    PRIMARY KEY (item_type_id, item_content_type_id),
    CONSTRAINT fk_item_content_types_item_type
        FOREIGN KEY (item_type_id) REFERENCES item_type(item_type_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_content_types_content_type
        FOREIGN KEY (item_content_type_id) REFERENCES item_content_type(item_content_type_id) ON DELETE CASCADE
);

CREATE TABLE item_contents (
    item_id         UUID NOT NULL,
    item_content_id UUID NOT NULL,
    purpose         TEXT,
    PRIMARY KEY (item_id, item_content_id),
    CONSTRAINT fk_item_contents_item
        FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_contents_content
        FOREIGN KEY (item_content_id) REFERENCES item_content(item_content_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_contents_item     ON item_contents(item_id);
CREATE INDEX idx_item_contents_content  ON item_contents(item_content_id);

CREATE TABLE item_content_tags (
    item_content_id UUID NOT NULL,
    tag_id          UUID NOT NULL,
    PRIMARY KEY (item_content_id, tag_id),
    CONSTRAINT fk_item_content_tags_content
        FOREIGN KEY (item_content_id) REFERENCES item_content(item_content_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_content_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_content_tags_content ON item_content_tags(item_content_id);
CREATE INDEX idx_item_content_tags_tag     ON item_content_tags(tag_id);

CREATE TABLE item_tags (
    item_id UUID NOT NULL,
    tag_id  UUID NOT NULL,
    PRIMARY KEY (item_id, tag_id),
    CONSTRAINT fk_item_tags_item
        FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tag(tag_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_tags_item ON item_tags(item_id);
CREATE INDEX idx_item_tags_tag  ON item_tags(tag_id);

CREATE TABLE item_validator (
    item_id       UUID NOT NULL,
    validator_id  UUID NOT NULL,
    PRIMARY KEY (item_id, validator_id),
    CONSTRAINT fk_item_validator_item
        FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_validator_validator
        FOREIGN KEY (validator_id) REFERENCES validator(validator_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_validator_item       ON item_validator(item_id);
CREATE INDEX idx_item_validator_validator  ON item_validator(validator_id);

CREATE TABLE item_modifier (
    item_id     UUID NOT NULL,
    modifier_id UUID NOT NULL,
    PRIMARY KEY (item_id, modifier_id),
    CONSTRAINT fk_item_modifier_item
        FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_modifier_modifier
        FOREIGN KEY (modifier_id) REFERENCES modifier(modifier_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_modifier_item     ON item_modifier(item_id);
CREATE INDEX idx_item_modifier_modifier ON item_modifier(modifier_id);

CREATE TABLE item_collection_sub_item (
    item_collection_id  UUID NOT NULL,
    subitem_id          UUID NOT NULL,
    position            INTEGER,
    PRIMARY KEY (item_collection_id, subitem_id),
    CONSTRAINT fk_item_collection_sub_item_collection
        FOREIGN KEY (item_collection_id) REFERENCES item_collection(item_collection_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_collection_sub_item_item
        FOREIGN KEY (subitem_id) REFERENCES item(item_id) ON DELETE CASCADE
);
CREATE INDEX idx_item_collection_sub_item_collection ON item_collection_sub_item(item_collection_id);
CREATE INDEX idx_item_collection_sub_item_item       ON item_collection_sub_item(subitem_id);

-- ── Seed-Daten ─────────────────────────────────────────────────────────────────

-- 1. Item_Content_Type
INSERT INTO item_content_type (item_content_type_id, item_content_type_name, description) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'text/plain',          'Einfacher Text'),
    ('a0000000-0000-0000-0000-000000000002', 'text/markdown',       'Markdown-formatierter Text'),
    ('a0000000-0000-0000-0000-000000000003', 'application/json',    'Strukturierter JSON-Inhalt'),
    ('a0000000-0000-0000-0000-000000000004', 'image/png',           'PNG-Bilder'),
    ('a0000000-0000-0000-0000-000000000005', 'image/jpeg',          'JPEG-Bilder'),
    ('a0000000-0000-0000-0000-000000000006', 'application/pdf',     'PDF-Dokumente');

-- 2. License
INSERT INTO license (license_id, license) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'CC-BY-4.0'),
    ('b0000000-0000-0000-0000-000000000002', 'CC-BY-SA-4.0'),
    ('b0000000-0000-0000-0000-000000000003', 'MIT'),
    ('b0000000-0000-0000-0000-000000000004', 'Internal-THM');

-- 3. Tag (hierarchisch)
INSERT INTO tag (tag_id, tag, description, parent_tag_id) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'SQL',                   'SQL-Programmierung',      NULL),
    ('c0000000-0000-0000-0000-000000000002', 'Modellierung',          'Datenmodellierung',       NULL),
    ('c0000000-0000-0000-0000-000000000003', 'Wirtschaftsinformatik', 'Allgemeine WI-Themen',    NULL);

INSERT INTO tag (tag_id, tag, description, parent_tag_id) VALUES
    ('c0000000-0000-0000-0000-000000000004', 'Joins',       'SQL Joins',                'c0000000-0000-0000-0000-000000000001'),
    ('c0000000-0000-0000-0000-000000000005', 'Aggregation', 'GROUP BY, COUNT, SUM',     'c0000000-0000-0000-0000-000000000001'),
    ('c0000000-0000-0000-0000-000000000006', 'Subqueries',  'Verschachtelte Abfragen',  'c0000000-0000-0000-0000-000000000001');

INSERT INTO tag (tag_id, tag, description, parent_tag_id) VALUES
    ('c0000000-0000-0000-0000-000000000007', 'INNER JOIN', 'Inner Join Operationen',  'c0000000-0000-0000-0000-000000000004'),
    ('c0000000-0000-0000-0000-000000000008', 'LEFT JOIN',  'Left Outer Join',         'c0000000-0000-0000-0000-000000000004'),
    ('c0000000-0000-0000-0000-000000000009', 'OUTER JOIN', 'Outer Joins allgemein',   'c0000000-0000-0000-0000-000000000004');

INSERT INTO tag (tag_id, tag, description, parent_tag_id) VALUES
    ('c0000000-0000-0000-0000-00000000000a', 'ERM',  'Entity-Relationship-Modell',  'c0000000-0000-0000-0000-000000000002'),
    ('c0000000-0000-0000-0000-00000000000b', 'SERM', 'Strukturiertes ERM',          'c0000000-0000-0000-0000-000000000002');

-- 4. Author
INSERT INTO author (author_id, descriptor, mail) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'Prof. Dr. Markus Siepermann', 'markus.siepermann@mni.thm.de'),
    ('d0000000-0000-0000-0000-000000000002', 'Johannes Kunz',               'johannes.kunz@mni.thm.de'),
    ('d0000000-0000-0000-0000-000000000003', 'Joelle Kamwa Mokam',          'joelle.kamwa@mni.thm.de');

-- 5. Item_Type
INSERT INTO item_type (item_type_id, item_type_name, description) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'SQL-Abfrage',      'Aufgaben, die das Schreiben einer SQL-Abfrage erfordern'),
    ('e0000000-0000-0000-0000-000000000002', 'Modellierung',     'Erstellung eines Datenmodells'),
    ('e0000000-0000-0000-0000-000000000003', 'Multiple-Choice',  'Auswahl der richtigen Antwort(en) aus mehreren Optionen'),
    ('e0000000-0000-0000-0000-000000000004', 'Freitext',         'Offene Textantwort');

-- 6. Item_Representation_Template
INSERT INTO item_representation_template (item_template_id, template) VALUES
    ('f0000000-0000-0000-0000-000000000001', 'default-text-template'),
    ('f0000000-0000-0000-0000-000000000002', 'sql-task-template'),
    ('f0000000-0000-0000-0000-000000000003', 'mc-task-template');

-- 7. Validator
INSERT INTO validator (validator_id, description, validator) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Muss INNER JOIN enthalten',               'must_contain_inner_join'),
    ('10000000-0000-0000-0000-000000000002', 'Muss ORDER BY enthalten',                  'must_contain_order_by'),
    ('10000000-0000-0000-0000-000000000003', 'Keine verschachtelten Subqueries erlaubt', 'no_nested_subqueries'),
    ('10000000-0000-0000-0000-000000000004', 'Mindestens 2 Tabellen referenzieren',     'min_two_tables');

-- 8. Modifier
INSERT INTO modifier (modifier_id, description, modifier) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Variante mit INNER JOIN-Pflicht', 'variant_inner_join'),
    ('20000000-0000-0000-0000-000000000002', 'Variante mit Sortierung',         'variant_with_sorting'),
    ('20000000-0000-0000-0000-000000000003', 'Vereinfachte Variante',           'variant_simplified');

-- 9. Item_Content
INSERT INTO item_content (item_content_id, license_id, item_content_type_id, author_id, json_serialized_content) VALUES
    ('30000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001',
     '{"title": "Datenbank-Konzept für ein Bibliothekssystem", "instruction": "Entwerfen Sie ein Konzept für eine Bibliotheksdatenbank mit Büchern, Lesern und Ausleihen."}'),
    ('30000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001',
     '{"title": "SERM-Diagramm Bibliothek", "instruction": "Zeichnen Sie das SERM-Diagramm zu Ihrem Konzept aus Aufgabe 1."}'),
    ('30000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001',
     '{"title": "SQL-DDL für Bibliotheksdatenbank", "instruction": "Schreiben Sie die CREATE TABLE-Statements für Ihr Datenmodell."}'),
    ('30000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001',
     '{"title": "SQL-Abfrage Bibliothek", "instruction": "Geben Sie alle Bücher und deren aktuelle Ausleiher aus."}');

-- 10. Item
INSERT INTO item (item_id, author_id, license_id, item_type_id, item_template_id, root_item_id) VALUES
    ('40000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', NULL),
    ('40000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000001', NULL),
    ('40000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000002', NULL),
    ('40000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000002', NULL),
    ('40000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000004');

-- 11. Item_Contents
INSERT INTO item_contents (item_id, item_content_id, purpose) VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Aufgabenstellung'),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'Aufgabenstellung'),
    ('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', 'Aufgabenstellung'),
    ('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000004', 'Aufgabenstellung'),
    ('40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000004', 'Aufgabenstellung');

-- 12. Item_Tags
INSERT INTO item_tags (item_id, tag_id) VALUES
    ('40000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002'),
    ('40000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-00000000000a'),
    ('40000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002'),
    ('40000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-00000000000b'),
    ('40000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004'),
    ('40000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000001'),
    ('40000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000004'),
    ('40000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000007');

-- 13. Item_Validator + Item_Modifier
INSERT INTO item_validator (item_id, validator_id) VALUES
    ('40000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001');
INSERT INTO item_modifier (item_id, modifier_id) VALUES
    ('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000001');

-- 14. Item_Collection
INSERT INTO item_collection (item_collection_id, parent_item_id, "order") VALUES
    ('50000000-0000-0000-0000-000000000001', NULL, TRUE);

-- 15. Item_Collection_Sub_Item
INSERT INTO item_collection_sub_item (item_collection_id, subitem_id, position) VALUES
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 1),
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000002', 2),
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003', 3),
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000004', 4);

-- 16. Item_Content_Tags
INSERT INTO item_content_tags (item_content_id, tag_id) VALUES
    ('30000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002'),
    ('30000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-00000000000b'),
    ('30000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000001'),
    ('30000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004');

-- 17. Item_Content_Types
INSERT INTO item_content_types (item_type_id, item_content_type_id) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'),
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'),
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003'),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002'),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003'),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000004');
