-- -----------------------------------------------------------------------------
-- Table: item_content_type
-- Definiert die möglichen Typen von Item-Inhalten (z.B. Text, JSON, Image, etc.)
-- -----------------------------------------------------------------------------
CREATE TABLE item_content_type (
    item_content_type_id    SERIAL PRIMARY KEY,
    item_content_type_name  TEXT NOT NULL,
    description             TEXT
);

COMMENT ON TABLE item_content_type IS 'Typen von Item-Inhalten (Text, JSON, Image, etc.)';

-- -----------------------------------------------------------------------------
-- Table: license
-- Definiert die möglichen Lizenzen für Items und Item-Contents
-- -----------------------------------------------------------------------------
CREATE TABLE license (
    license_id  SERIAL PRIMARY KEY,
    license     TEXT NOT NULL UNIQUE
);

COMMENT ON TABLE license IS 'Lizenzen (CC-BY, MIT, GPL, etc.)';

-- -----------------------------------------------------------------------------
-- Table: tag
-- Hierarchische Tags (parent_tag_id ermöglicht eine Baumstruktur)
-- z.B. SQL → Joins → INNER JOIN
-- -----------------------------------------------------------------------------
CREATE TABLE tag (
    tag_id          SERIAL PRIMARY KEY,
    parent_tag_id   INTEGER,
    tag             TEXT NOT NULL,
    description     TEXT,
    CONSTRAINT fk_tag_parent_tag_id
        FOREIGN KEY (parent_tag_id)
        REFERENCES tag(tag_id)
        ON DELETE SET NULL
);

CREATE INDEX idx_tag_parent_tag_id ON tag(parent_tag_id);

COMMENT ON TABLE tag IS 'Hierarchische Tags zur thematischen Kategorisierung';
COMMENT ON COLUMN tag.parent_tag_id IS 'Self-Reference für hierarchische Tags';

-- -----------------------------------------------------------------------------
-- Table: author
-- Autoren der Aufgaben
-- -----------------------------------------------------------------------------
CREATE TABLE author (
    author_id   SERIAL PRIMARY KEY,
    descriptor  TEXT NOT NULL,
    mail        TEXT
);

COMMENT ON TABLE author IS 'Autoren der Aufgaben (Lehrende und Administratoren)';

-- -----------------------------------------------------------------------------
-- Table: item_type
-- Typen von Items (z.B. SQL-Aufgabe, Modellierungs-Aufgabe, QCM, etc.)
-- -----------------------------------------------------------------------------
CREATE TABLE item_type (
    item_type_id    SERIAL PRIMARY KEY,
    item_type_name  TEXT NOT NULL,
    description     TEXT
);

COMMENT ON TABLE item_type IS 'Typen von Aufgaben (SQL, Modellierung, QCM, etc.)';

-- -----------------------------------------------------------------------------
-- Table: item_representation_template
-- Templates zur Darstellung der Items in der Oberfläche
-- -----------------------------------------------------------------------------
CREATE TABLE item_representation_template (
    item_template_id    SERIAL PRIMARY KEY,
    template            TEXT NOT NULL
);

COMMENT ON TABLE item_representation_template IS 'Templates zur Darstellung der Aufgaben';
COMMENT ON COLUMN item_representation_template.template IS 'Template-Inhalt (HTML, Markdown, etc.)';

-- -----------------------------------------------------------------------------
-- Table: validator
-- Validatoren zur Prüfung von Benutzereingaben oder Eigenschaften
-- z.B. „muss INNER JOIN enthalten", „muss ORDER BY enthalten"
-- -----------------------------------------------------------------------------
CREATE TABLE validator (
    validator_id    SERIAL PRIMARY KEY,
    description     TEXT NOT NULL,
    validator       TEXT NOT NULL
);

COMMENT ON TABLE validator IS 'Validatoren für Aufgaben (z.B. SQL-Restriktionen)';
COMMENT ON COLUMN validator.validator IS 'Identifier oder Code des Validators';

-- -----------------------------------------------------------------------------
-- Table: modifier
-- Modifikatoren zur Anpassung/Transformation von Aufgaben
-- z.B. „ohne verschachtelte Subqueries", „mit GROUP BY"
-- -----------------------------------------------------------------------------
CREATE TABLE modifier (
    modifier_id     SERIAL PRIMARY KEY,
    description     TEXT NOT NULL,
    modifier        TEXT NOT NULL
);

COMMENT ON TABLE modifier IS 'Modifikatoren für Aufgaben-Varianten';
COMMENT ON COLUMN modifier.modifier IS 'Identifier oder Code des Modifiers';