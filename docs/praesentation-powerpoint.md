# PowerPoint-Storyboard: Aufgabendatenbank

Ziel: 10--12 Minuten Präsentation plus 4--5 Minuten Live-Demo.
Die Folien sollen visuell bleiben: höchstens 4 kurze Aussagen pro Folie,
keine Codeblöcke außer dem vereinfachten Architekturfluss.

---

## Folie 1 -- Aufgabendatenbank

**Untertitel:** Vom abstrakten Datenschema zum nutzbaren Autorenwerkzeug

- Praktikum Wirtschaftsinformatik, SS 2026
- Projektteam: [Namen]
- Betreuung: Prof. Dr. Markus Siepermann, Johannes Kunz

**Visual:** ruhiger Ausschnitt der Hauptansicht oder THM-/Projektlogo.

**Sprechtext (20 s):**
Unser Ausgangspunkt war kein fertiges Produkt, sondern ein hochschulweit
abgestimmtes Datenschema. Unsere Aufgabe war, daraus eine verständliche und
nutzbare Anwendung für Lehrende zu entwickeln.

---

## Folie 2 -- Ausgangsproblem und Ziel

**Problem**

- Aufgaben und Beziehungen lagen nur als abstraktes Schema vor.
- Collections, Varianten und Inhalte waren nicht unmittelbar bedienbar.
- Eine praktische Oberfläche und Anwendungslogik fehlten.

**Ziel**

- Aufgaben beschreiben, strukturieren, kombinieren und wiederfinden.
- Kein Lern- oder Korrektursystem, sondern ein Autorenwerkzeug.

**Visual:** links ein Ausschnitt des ER-Diagramms, rechts die Anwendung.

**Sprechtext (50 s):**
Die zentrale Herausforderung war die Übersetzung fachlicher Beziehungen in
Interaktionen. Nutzende sollen nicht über Fremdschlüssel nachdenken, sondern
Aufgaben, Inhalte, Lernpfade und Varianten direkt bearbeiten können.

---

## Folie 3 -- Fachliches Kernmodell

- **Item:** Aufgabe mit Autor, Lizenz, Typ und Template
- **ItemContent:** Text/JSON oder Datei, verbunden über einen Zweck
- **Collection:** geordnete Sequenz oder ungeordnete Sammlung
- **Variante:** horizontale Beziehung über `root_item_id`

**Visual:** vereinfachtes Diagramm mit vier Boxen und Pfeilen.

**Kernaussage:** Collection-Mitgliedschaft und Variantenbezug sind orthogonal.

**Sprechtext (70 s):**
Inhalte sind bewusst von Aufgaben getrennt. Der Purpose beschreibt, ob ein
Baustein beispielsweise Aufgabenstellung, Hinweis oder Lösung ist. Collections
organisieren verschiedene Aufgaben. RootItemId wird dagegen ausschließlich für
Varianten derselben Ausgangsaufgabe verwendet.

---

## Folie 4 -- Architektur und Technologien

```text
Vue 3 + Pinia
      |
   REST/JSON
      |
Spring Boot: Controller -> Service -> Repository
      |
PostgreSQL
```

- TypeScript und Java 21
- JSONB für strukturierte Inhalte, BYTEA für Dateien
- API-Adapter für echte API und Dummy-Modus
- Docker Compose für den lokalen Gesamtstart

**Visual:** horizontales Architekturdiagramm, keine Klassenliste.

**Sprechtext (60 s):**
Der Adapter entkoppelt den Store vom konkreten Datenzugriff. Dadurch kann die
Oberfläche mit dem realen Backend oder mit lokalen Demonstrationsdaten laufen.
Im Backend liegen HTTP-Vertrag, Geschäftslogik und Persistenz in getrennten
Schichten.

---

## Folie 5 -- Inhalte und Darstellung

- Mehrere Content-Blöcke pro Aufgabe
- Text, Bild und PDF
- eigene Metadaten und Purpose je Inhalt
- XML-basiertes Template mit Live-Vorschau

**Visual:** Screenshot `02-inhalte-und-vorschau.png`.

**Sprechtext (55 s):**
Das Schema besitzt kein einfaches großes Textfeld für eine Aufgabe. Stattdessen
besteht sie aus wiederverwendbaren Inhaltsbausteinen. Das Template legt fest,
in welcher Reihenfolge diese Zwecke in der Vorschau erscheinen.

---

## Folie 6 -- Strukturieren und kombinieren

- ungeordnet: thematische Sammlung
- geordnet: Lernpfad mit Positionen
- Drag-and-Drop und serverseitige Neuberechnung
- Varianten mit eigenen Validatoren

**Visual:** Screenshot `03-kollektionen.png` und kleiner Ausschnitt
`04-varianten-validatoren.png`.

**Sprechtext (65 s):**
Beim Umschalten auf geordnet weist das Backend fortlaufende Positionen zu.
Beim Ausschalten werden sie auf null gesetzt. Varianten bleiben davon getrennt:
Das Verschieben in eine Collection verändert den Variantenbezug nicht.

---

## Folie 7 -- Metadaten, Tags und Suche

- Autoren, Lizenzen und Typen auswählbar und neu anlegbar
- hierarchische Tags statt flacher Schlagwortliste
- kombinierte Suche nach Inhalt, Autor, Typ und Tag
- Validatoren werden gespeichert, extern ausgeführt

**Visual:** Screenshot `05-tags-und-suche.png`.

**Sprechtext (55 s):**
Speichern allein genügt nicht; Aufgaben müssen wiedergefunden werden. Deshalb
haben wir den Tag-Baum mit Volltext- und Attributfiltern in einer gemeinsamen
Filterleiste kombiniert.

---

## Folie 8 -- Qualität und Robustheit

- **107 Frontend-Tests**
- **92 Backend-Tests**
- Frontend-Produktionsbuild erfolgreich
- Resynchronisation nach partiellen API-Fehlern

**Zusatz klein:** Pull-Requests, Branch-Schutz, CI-Build nach GHCR.

**Visual:** vier große Kennzahlen, optional Screenshot eines grünen Testlaufs.

**Sprechtext (60 s):**
Mehrstufige Vorgänge können teilweise fehlschlagen, beispielsweise wenn ein
Item angelegt wird, aber sein Content nicht. In solchen Fällen lädt das
Frontend den betroffenen Teilbaum erneut und konvergiert zum Datenbankstand.
Die Tests decken neben Erfolgsfällen auch diese Fehlerpfade ab.

---

## Folie 9 -- Projektverlauf und Abweichungen

- Team von fünf auf drei Personen reduziert
- Aufgaben neu priorisiert und schichtübergreifend verteilt
- unklare Konzepte zuerst fachlich geklärt
- Cluster und Authentifizierung von Plattformdiensten abhängig

**Visual:** kompakte Zeitleiste April--Juli.

**Sprechtext (55 s):**
Die größte organisatorische Abweichung war die Verkleinerung des Teams.
Gleichzeitig mussten wir Begriffe wie Varianten und Templates präzisieren.
Diese Klärungen haben spätere Fehlinterpretationen und Dateninkonsistenzen
verhindert.

---

## Folie 10 -- Ergebnis und Ausblick

**Ergebnis**

- abstraktes Schema als nutzbares Autorenwerkzeug umgesetzt
- zentrale Workflows durchgängig integriert
- getestet, containerisiert und für die Plattform vorbereitet

**Ausblick**

- Ende-zu-Ende-Tests mit PostgreSQL
- weitere Modularisierung des Frontend-Stores
- zentrale Authentifizierung und vollständiges Cluster-Deployment

**Sprechtext (45 s):**
Unser wichtigstes Ergebnis ist nicht nur ein CRUD-System. Die Anwendung macht
die fachlichen Beziehungen des Schemas tatsächlich bedienbar. Gleichzeitig
bleiben bekannte Grenzen transparent und die Architektur ist für weitere
Iterationen vorbereitet.

---

# Live-Demo (4--5 Minuten)

Die Demo sollte mit vorbereiteten Daten beginnen und exakt diese Reihenfolge
verwenden:

1. Aufgabe erstellen, Autor/Lizenz/Typ wählen.
2. Textinhalt und Bild oder PDF hinzufügen.
3. Template-Vorschau zeigen.
4. Tags zuweisen und Aufgabe über Filter wiederfinden.
5. Aufgabe in eine geordnete Collection einfügen und verschieben.
6. Variante mit Validator zeigen.
7. Seite neu laden, um Persistenz zu belegen.

Nicht während der Demo:

- neue Infrastruktur starten,
- lange Freitexte eingeben,
- Datenbankkonsole öffnen,
- mehrere ähnliche CRUD-Dialoge demonstrieren,
- einen ungeprüften Sonderfall improvisieren.

# Screenshot-Liste

Die gleichen Bilder können in Dokumentation und PowerPoint verwendet werden:

- `docs/screenshots/01-aufgabe-erstellen.png`
- `docs/screenshots/02-inhalte-und-vorschau.png`
- `docs/screenshots/03-kollektionen.png`
- `docs/screenshots/04-varianten-validatoren.png`
- `docs/screenshots/05-tags-und-suche.png`

# Rollen für drei Personen

- **Person 1:** Problem, Ziel, Datenmodell, Projektverlauf
- **Person 2:** Architektur, Inhalte, Tags und Suche
- **Person 3:** Collections, Varianten, Tests, Live-Demo, Fazit

Jede Person sollte mindestens einen fachlichen und einen technischen Aspekt
erklären. Übergaben vorher als vollständigen Satz üben.

