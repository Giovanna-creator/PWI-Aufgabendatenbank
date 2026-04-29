# Lastenheft: Konzeption und Implementierung einer Aufgabendatenbank

## 1. Zielsetzung

Ziel des Projekts ist der Aufbau einer zentralen Plattform zur Verwaltung von Übungsaufgaben. Es soll ein bereits hochschulweit abgestimmtes, aber stark abstrahiertes Datenschema in eine nutzbare, verständliche und praktikable Web-Anwendung überführt werden. Die Plattform soll es Lehrenden (Professoren) ermöglichen, Aufgaben, deren Attribute, Beziehungen untereinander sowie Einbettungen in größere Sammlungen flexibel und strukturiert zu organisieren.

## 2. Ist-Zustand

Derzeit existiert ein theoretisch ausgearbeitetes und hochschulweit diskutiertes Datenschema (vgl. Konzept-Diagramm) zur Abbildung von Aufgaben. Bislang wurde dieses Schema jedoch nicht in eine konkret nutzbare Softwareanwendung überführt. Es fehlt ein Werkzeug, um die hochgradig flexiblen und komplexen Beziehungen zwischen Aufgaben (z.B. horizontale und vertikale Abhängigkeiten) praktisch zu verwalten und für Lernende nutzbar zu machen.

## 3. Soll-Zustand

Es soll ein webbasiertes Interface zur Erstellung, Bearbeitung und Organisation von Aufgaben entwickelt werden. Die stark abstrahierte fachliche Struktur muss in eine intuitive Bedienoberfläche übersetzt werden. Das System muss es ermöglichen, Aufgaben nicht nur statisch abzulegen, sondern strukturiert auszuwählen, zu konfigurieren und zu kombinieren.

## 4. Funktionale Anforderungen

### 4.1. Verwaltung von Aufgaben (Items)

- **Erstellung und Bearbeitung:** Anlegen neuer Aufgaben mit Metadaten (Autor, Lizenz, Tags, Aufgabentyp).
- **Inhaltsverwaltung (Item Content):** Trennung von Aufgabenstruktur und konkretem Inhalt. Textbasierte Inhalte müssen als strukturierte Daten (z.B. JSON) gespeichert werden.
- **Zusatzmaterialien (Binärdaten):** Es muss möglich sein, zusätzliche Materialien (z.B. Bilder, Diagramme oder sonstige Dateianhänge) direkt zu einer Aufgabe hinzuzufügen. Diese sollen als Binärdaten (Blob) in der Datenbank hinterlegt werden.
- **Templates:** Zuweisung von Repräsentations-Templates (`Item_Representation_Template`) zur Darstellung der Aufgaben.

### 4.2. Modellierung von Aufgabenbeziehungen (Kernherausforderung)

Das System muss die komplexe, abstrakte Struktur von Aufgabenbeziehungen abbilden:

**Vertikale Abhängigkeiten (Sequenzierung):**
Abbildung aufeinander aufbauender Aufgabenfolgen (z.B. als `Item_Collection` / `Item_Collection_Sub_Item`).
_Beispiel-Pipeline:_

1. Datenbank-Konzept entwerfen.
2. SERM-Diagramm zeichnen.
3. SQL-DDL für die Tabellen schreiben.
4. Komplexe SQL-Abfrage formulieren.

**Horizontale Abhängigkeiten (Variationen & Restriktionen):**
Abbildung von Varianten und methodischen Vorgaben für dieselbe Problemstellung (z.B. durch `Modifier` und `Validator`).
_Beispiel-Variationen einer SQL-Abfrage:_

- Variante A: Umsetzung zwingend mit `INNER JOIN`.
- Variante B: Umsetzung ohne verschachtelte Subqueries (Nested Requests).
- Variante C: Das Ergebnis muss sortiert sein (`ORDER BY`).

### 4.3. Strukturierung und Organisation

- **Kollektionen:** Gruppierung von Aufgaben in hierarchischen Strukturen (`Item_Collection`).
- **Tagging-System:** Hierarchische Tags (`Tag`, `parent_tag_id`) zur thematischen Kategorisierung.
- **Validatoren & Modifikatoren:** Zuweisung von `Validator`- und `Modifier`-Logiken an Aufgaben, um Benutzereingaben zu prüfen oder zu transformieren.

## 5. Nicht-funktionale Anforderungen

- **Usability:** Das komplexe Datenmodell muss im Frontend für Lehrende einfach und intuitiv bedienbar sein.
- **Skalierbarkeit:** Die Datenbankstruktur muss performant mit tiefen Verschachtelungen und Abhängigkeiten umgehen können.
- **Erweiterbarkeit:** Neue Aufgabentypen und Beziehungsarten müssen leicht in das bestehende Schema integrierbar sein.

## 6. Datenmodell & Strukturmerkmale

Das System basiert auf dem vorgegebenen Entity-Relationship-Modell (ERM):

- **Item / Item_Content:** Trennung von Aufgabe (Item) und den tatsächlichen Inhaltsbausteinen (Item_Content).
- **Item_Tags / Item_Content_Tags:** Granulare Verschlagwortung sowohl auf Aufgaben- als auch auf Inhaltsebene.
- **Item_Collection / Item_Collection_Sub_Item:** Umsetzung von Aufgabenfolgen mit Positionsangaben (Sequenzierung für vertikale Abhängigkeiten).
- **Item_Validator / Item_Modifier:** Anbindung von Prüf- und Anpassungslogiken (für horizontale Restriktionen/Variationen).

## 7. Lieferumfang

- Anforderungsanalyse hinsichtlich Datenmodell, Interaktion, Bedienoberfläche und Logik.
- Funktionierende Datenbankstruktur basierend auf dem vorgegebenen Schema.
- Web-Interface für Lehrende zur Aufgabenorganisation und -erstellung.
- Dokumentation der Implementierung und der Lösungsansätze für die Abbildung horizontaler und vertikaler Abhängigkeiten.
