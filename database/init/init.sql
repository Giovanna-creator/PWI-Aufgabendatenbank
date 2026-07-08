-- PWI-Aufgabendatenbank — Schema + minimale Referenzdaten
-- Keine Demo-Items/Kollektionen — alles wird übers Frontend erstellt.

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

-- ── Minimale Referenzdaten ────────────────────────────────────────────────────
-- Nur Einträge, die das Frontend via hartcodierter UUIDs referenziert.
-- Alles andere (Items, Kollektionen, Content) wird über die UI erstellt.

INSERT INTO item_content_type (item_content_type_id, item_content_type_name, description) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'text/plain',          'Einfacher Text'),
    ('a0000000-0000-0000-0000-000000000002', 'text/markdown',       'Markdown-formatierter Text'),
    ('a0000000-0000-0000-0000-000000000003', 'application/json',    'Strukturierter JSON-Inhalt'),
    ('a0000000-0000-0000-0000-000000000004', 'image/png',           'PNG-Bilder'),
    ('a0000000-0000-0000-0000-000000000005', 'image/jpeg',          'JPEG-Bilder'),
    ('a0000000-0000-0000-0000-000000000006', 'application/pdf',     'PDF-Dokumente');

INSERT INTO license (license_id, license) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'CC-BY-4.0'),
    ('b0000000-0000-0000-0000-000000000002', 'CC-BY-SA-4.0'),
    ('b0000000-0000-0000-0000-000000000003', 'MIT'),
    ('b0000000-0000-0000-0000-000000000004', 'Internal-THM');

INSERT INTO author (author_id, descriptor, mail) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'Prof. Dr. Markus Siepermann', 'markus.siepermann@mni.thm.de'),
    ('d0000000-0000-0000-0000-000000000002', 'Johannes Kunz',               'johannes.kunz@mni.thm.de'),
    ('d0000000-0000-0000-0000-000000000003', 'Joelle Kamwa Mokam',          'joelle.kamwa@mni.thm.de');

INSERT INTO item_type (item_type_id, item_type_name, description) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'SQL-Abfrage',      'Aufgaben, die das Schreiben einer SQL-Abfrage erfordern'),
    ('e0000000-0000-0000-0000-000000000002', 'Modellierung',     'Erstellung eines Datenmodells'),
    ('e0000000-0000-0000-0000-000000000003', 'Multiple-Choice',  'Auswahl der richtigen Antwort(en) aus mehreren Optionen'),
    ('e0000000-0000-0000-0000-000000000004', 'Freitext',         'Offene Textantwort');

INSERT INTO item_representation_template (template) VALUES
  ('<layout><name>Standard-Reihenfolge</name><purpose>Aufgabenstellung</purpose><purpose>Hinweis</purpose><purpose>L\u00f6sung</purpose></layout>'),
  ('<layout><name>Aufgabe+L\u00f6sung</name><purpose>Aufgabenstellung</purpose><purpose>L\u00f6sung</purpose></layout>'),
  ('<layout><name>L\u00f6sung zuerst</name><purpose>L\u00f6sung</purpose><purpose>Aufgabenstellung</purpose></layout>');

INSERT INTO item_content_types (item_type_id, item_content_type_id) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'),
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'),
    ('e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003'),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002'),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003'),
    ('e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000004');
