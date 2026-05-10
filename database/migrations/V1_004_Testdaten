-- -----------------------------------------------------------------------------
-- 1. Item_Content_Type — Typen von Inhalten
-- -----------------------------------------------------------------------------
INSERT INTO item_content_type (item_content_type_name, description) VALUES
    ('text/plain',          'Einfacher Text'),
    ('text/markdown',       'Markdown-formatierter Text'),
    ('application/json',    'Strukturierter JSON-Inhalt'),
    ('image/png',           'PNG-Bilder'),
    ('image/jpeg',          'JPEG-Bilder'),
    ('application/pdf',     'PDF-Dokumente');

-- -----------------------------------------------------------------------------
-- 2. License — Lizenzen
-- -----------------------------------------------------------------------------
INSERT INTO license (license) VALUES
    ('CC-BY-4.0'),
    ('CC-BY-SA-4.0'),
    ('MIT'),
    ('Internal-THM');

-- -----------------------------------------------------------------------------
-- 3. Tag — Hierarchische Tags
-- -----------------------------------------------------------------------------
-- Wurzel-Tags (ohne parent)
INSERT INTO tag (tag, description, parent_tag_id) VALUES
    ('SQL',             'SQL-Programmierung',        NULL),
    ('Modellierung',    'Datenmodellierung',         NULL),
    ('Wirtschaftsinformatik', 'Allgemeine WI-Themen', NULL);

-- Sub-Tags von SQL (tag_id 1)
INSERT INTO tag (tag, description, parent_tag_id) VALUES
    ('Joins',           'SQL Joins',                 1),
    ('Aggregation',     'GROUP BY, COUNT, SUM',      1),
    ('Subqueries',      'Verschachtelte Abfragen',   1);

-- Sub-Tags von Joins (tag_id 4)
INSERT INTO tag (tag, description, parent_tag_id) VALUES
    ('INNER JOIN',      'Inner Join Operationen',    4),
    ('LEFT JOIN',       'Left Outer Join',           4),
    ('OUTER JOIN',      'Outer Joins allgemein',     4);

-- Sub-Tags von Modellierung (tag_id 2)
INSERT INTO tag (tag, description, parent_tag_id) VALUES
    ('ERM',             'Entity-Relationship-Modell', 2),
    ('SERM',            'Strukturiertes ERM',         2);

-- -----------------------------------------------------------------------------
-- 4. Author — Autoren
-- -----------------------------------------------------------------------------
INSERT INTO author (descriptor, mail) VALUES
    ('Prof. Dr. Markus Siepermann',  'markus.siepermann@mni.thm.de'),
    ('Johannes Kunz',                 'johannes.kunz@mni.thm.de'),
    ('Joelle Kamwa Mokam',            'joelle.kamwa@mni.thm.de');

-- -----------------------------------------------------------------------------
-- 5. Item_Type — Aufgabentypen
-- -----------------------------------------------------------------------------
INSERT INTO item_type (item_type_name, description) VALUES
    ('SQL-Abfrage',         'Aufgaben, die das Schreiben einer SQL-Abfrage erfordern'),
    ('Modellierung',        'Erstellung eines Datenmodells'),
    ('Multiple-Choice',     'Auswahl der richtigen Antwort(en) aus mehreren Optionen'),
    ('Freitext',            'Offene Textantwort');

-- -----------------------------------------------------------------------------
-- 6. Item_Representation_Template — Darstellungs-Templates
-- -----------------------------------------------------------------------------
INSERT INTO item_representation_template (template) VALUES
    ('default-text-template'),
    ('sql-task-template'),
    ('mc-task-template');

-- -----------------------------------------------------------------------------
-- 7. Validator — Validatoren
-- -----------------------------------------------------------------------------
INSERT INTO validator (description, validator) VALUES
    ('Muss INNER JOIN enthalten',               'must_contain_inner_join'),
    ('Muss ORDER BY enthalten',                  'must_contain_order_by'),
    ('Keine verschachtelten Subqueries erlaubt', 'no_nested_subqueries'),
    ('Mindestens 2 Tabellen referenzieren',     'min_two_tables');

-- -----------------------------------------------------------------------------
-- 8. Modifier — Modifikatoren
-- -----------------------------------------------------------------------------
INSERT INTO modifier (description, modifier) VALUES
    ('Variante mit INNER JOIN-Pflicht',          'variant_inner_join'),
    ('Variante mit Sortierung',                  'variant_with_sorting'),
    ('Vereinfachte Variante',                    'variant_simplified');

-- -----------------------------------------------------------------------------
-- 9. Item_Content — Inhaltsbausteine
-- -----------------------------------------------------------------------------
-- Aufgabenstellung 1: SQL-Konzept entwerfen
INSERT INTO item_content (license_id, item_content_type_id, author_id, json_serialized_content) VALUES
    (4, 2, 1, '{"title": "Datenbank-Konzept für ein Bibliothekssystem", "instruction": "Entwerfen Sie ein Konzept für eine Bibliotheksdatenbank mit Büchern, Lesern und Ausleihen."}');

-- Aufgabenstellung 2: SERM-Diagramm zeichnen
INSERT INTO item_content (license_id, item_content_type_id, author_id, json_serialized_content) VALUES
    (4, 2, 1, '{"title": "SERM-Diagramm Bibliothek", "instruction": "Zeichnen Sie das SERM-Diagramm zu Ihrem Konzept aus Aufgabe 1."}');

-- Aufgabenstellung 3: SQL-Tabellen anlegen
INSERT INTO item_content (license_id, item_content_type_id, author_id, json_serialized_content) VALUES
    (4, 2, 1, '{"title": "SQL-DDL für Bibliotheksdatenbank", "instruction": "Schreiben Sie die CREATE TABLE-Statements für Ihr Datenmodell."}');

-- Aufgabenstellung 4: SQL-Abfrage formulieren
INSERT INTO item_content (license_id, item_content_type_id, author_id, json_serialized_content) VALUES
    (4, 2, 1, '{"title": "SQL-Abfrage Bibliothek", "instruction": "Geben Sie alle Bücher und deren aktuelle Ausleiher aus."}');

-- -----------------------------------------------------------------------------
-- 10. Item — Aufgaben (mit Item-Content über item_contents verlinkt)
-- -----------------------------------------------------------------------------
-- Item 1: Konzept entwerfen
INSERT INTO item (author_id, license_id, item_type_id, item_template_id, root_item_id) VALUES
    (1, 4, 2, 1, NULL);

-- Item 2: SERM-Diagramm
INSERT INTO item (author_id, license_id, item_type_id, item_template_id, root_item_id) VALUES
    (1, 4, 2, 1, NULL);

-- Item 3: SQL-DDL
INSERT INTO item (author_id, license_id, item_type_id, item_template_id, root_item_id) VALUES
    (1, 4, 1, 2, NULL);

-- Item 4: SQL-Abfrage (Basisversion)
INSERT INTO item (author_id, license_id, item_type_id, item_template_id, root_item_id) VALUES
    (1, 4, 1, 2, NULL);

-- Item 5: SQL-Abfrage VARIANTE — muss INNER JOIN verwenden (root_item_id = 4)
INSERT INTO item (author_id, license_id, item_type_id, item_template_id, root_item_id) VALUES
    (1, 4, 1, 2, 4);

-- -----------------------------------------------------------------------------
-- 11. Item_Contents — Verknüpfung Item ↔ Item_Content
-- -----------------------------------------------------------------------------
INSERT INTO item_contents (item_id, item_content_id, purpose) VALUES
    (1, 1, 'Aufgabenstellung'),
    (2, 2, 'Aufgabenstellung'),
    (3, 3, 'Aufgabenstellung'),
    (4, 4, 'Aufgabenstellung'),
    (5, 4, 'Aufgabenstellung');  -- Variante nutzt dieselbe Aufgabenstellung

-- -----------------------------------------------------------------------------
-- 12. Item_Tags — Tags pro Aufgabe
-- -----------------------------------------------------------------------------
-- Item 1 (Konzept) → Tags: Modellierung, ERM
INSERT INTO item_tags (item_id, tag_id) VALUES (1, 2), (1, 10);

-- Item 2 (SERM) → Tags: Modellierung, SERM
INSERT INTO item_tags (item_id, tag_id) VALUES (2, 2), (2, 11);

-- Item 3 (DDL) → Tags: SQL
INSERT INTO item_tags (item_id, tag_id) VALUES (3, 1);

-- Item 4 (Abfrage) → Tags: SQL, Joins
INSERT INTO item_tags (item_id, tag_id) VALUES (4, 1), (4, 4);

-- Item 5 (Variante INNER JOIN) → Tags: SQL, Joins, INNER JOIN
INSERT INTO item_tags (item_id, tag_id) VALUES (5, 1), (5, 4), (5, 7);

-- -----------------------------------------------------------------------------
-- 13. Item_Validator + Item_Modifier — Restriktionen für die Variante
-- -----------------------------------------------------------------------------
-- Item 5 (Variante INNER JOIN) hat:
--   - Validator 1: muss INNER JOIN enthalten
--   - Modifier 1: Variante mit INNER JOIN-Pflicht
INSERT INTO item_validator (item_id, validator_id) VALUES (5, 1);
INSERT INTO item_modifier  (item_id, modifier_id)  VALUES (5, 1);

-- -----------------------------------------------------------------------------
-- 14. Item_Collection — Lernpfad
-- -----------------------------------------------------------------------------
INSERT INTO item_collection (parent_item_id, "order") VALUES (NULL, 1);

-- -----------------------------------------------------------------------------
-- 15. Item_Collection_Sub_Item — Aufgaben im Lernpfad
-- -----------------------------------------------------------------------------
-- Lernpfad „SQL-Grundlagen": Items 1 → 2 → 3 → 4
INSERT INTO item_collection_sub_item (item_collection_id, subitem_id, position) VALUES
    (1, 1, 1),  -- Konzept entwerfen
    (1, 2, 2),  -- SERM-Diagramm
    (1, 3, 3),  -- SQL-DDL
    (1, 4, 4);  -- SQL-Abfrage

-- -----------------------------------------------------------------------------
-- 16. Item_Content_Tags — Tags auf Content-Ebene (optional)
-- -----------------------------------------------------------------------------
INSERT INTO item_content_tags (item_content_id, tag_id) VALUES
    (1, 2),  -- Content „Konzept" → Modellierung
    (2, 11), -- Content „SERM"   → SERM
    (3, 1),  -- Content „DDL"    → SQL
    (4, 4);  -- Content „Abfrage" → Joins

-- -----------------------------------------------------------------------------
-- 17. Item_Content_Types — Erlaubte Content-Typen pro Item-Typ
-- -----------------------------------------------------------------------------
-- SQL-Abfrage erlaubt: text/plain, markdown, JSON
INSERT INTO item_content_types (item_type_id, item_content_type_id) VALUES
    (1, 1), (1, 2), (1, 3);

-- Modellierung erlaubt: markdown, JSON, image
INSERT INTO item_content_types (item_type_id, item_content_type_id) VALUES
    (2, 2), (2, 3), (2, 4);

-- =============================================================================
-- Verifikations-Abfragen (auskommentiert — manuell ausführen zum Testen)
-- =============================================================================

-- 1. Alle Aufgaben mit ihren Tags
-- SELECT i.item_id, ic.json_serialized_content->>'title' AS title,
--        string_agg(t.tag, ', ') AS tags
-- FROM item i
-- JOIN item_contents ics ON i.item_id = ics.item_id
-- JOIN item_content ic ON ics.item_content_id = ic.item_content_id
-- LEFT JOIN item_tags it ON i.item_id = it.item_id
-- LEFT JOIN tag t ON it.tag_id = t.tag_id
-- GROUP BY i.item_id, ic.json_serialized_content
-- ORDER BY i.item_id;

-- 2. Hierarchischer Tag-Baum (rekursiv)
-- WITH RECURSIVE tag_tree AS (
--     SELECT tag_id, tag, parent_tag_id, 0 AS level, tag AS path
--     FROM tag WHERE parent_tag_id IS NULL
--     UNION ALL
--     SELECT t.tag_id, t.tag, t.parent_tag_id, tt.level + 1, tt.path || ' → ' || t.tag
--     FROM tag t JOIN tag_tree tt ON t.parent_tag_id = tt.tag_id
-- )
-- SELECT level, path FROM tag_tree ORDER BY path;

-- 3. Aufgaben mit ihren Varianten (root_item_id)
-- SELECT i.item_id, i.root_item_id,
--        CASE WHEN i.root_item_id IS NULL THEN 'Original' ELSE 'Variante' END AS typ
-- FROM item i ORDER BY COALESCE(i.root_item_id, i.item_id), i.item_id;

-- 4. Lernpfad „SQL-Grundlagen" in Reihenfolge
-- SELECT icsi.position, i.item_id, ic.json_serialized_content->>'title' AS title
-- FROM item_collection_sub_item icsi
-- JOIN item i ON icsi.subitem_id = i.item_id
-- JOIN item_contents ics ON i.item_id = ics.item_id
-- JOIN item_content ic ON ics.item_content_id = ic.item_content_id
-- WHERE icsi.item_collection_id = 1
-- ORDER BY icsi.position;