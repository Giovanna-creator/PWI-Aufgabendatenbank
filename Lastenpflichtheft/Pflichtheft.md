# Pflichtheft: Implementierung einer Aufgabendatenbank

## 1. Zielbestimmung

Dieses Dokument beschreibt die konkrete technische und funktionale Umsetzung der im Lastenheft definierten Anforderungen an die Aufgabendatenbank. Der Fokus liegt auf der technischen Architektur, den Datenstrukturen und der Realisierung des User Interfaces.

### 1.1. Muss-Kriterien

- Persistierung des vorgegebenen, hochgradig flexiblen Datenmodells (Item, Content, Tags, Collections).
- REST-API für CRUD-Operationen der Aufgaben, Inhalte und Metadaten.
- Grafische Benutzeroberfläche zur Visualisierung und Manipulation von horizontalen (Varianten, Restriktionen) und vertikalen (Sequenzen) Aufgabenabhängigkeiten.
- Möglichkeit zur Speicherung flexibler Inhaltsformate (JSON, strukturierter Text) für Aufgaben in `Item_Content`.

### 1.2. Abgrenzungskriterien

- Das System ist kein automatisiertes Bewertungssystem (Autograder) für Lösungsabgaben der Studierenden; es dient primär der Modellierung, Erstellung und Organisation der Aufgaben durch Lehrende.

## 2. Systemarchitektur & Technologie-Stack

### 2.1. Gewählter primärer Tech-Stack

Die gewählte Architektur folgt einem klassischen Client-Server-Muster (Single Page Application + RESTful Backend).

- **Frontend: Vue.js mit Vuetify**
    - _Warum?_ Vue.js bietet eine exzellente Reaktivität und einfache Zustandsverwaltung. Vuetify bringt als UI-Bibliothek fertige, gut getestete Material-Design-Komponenten (z.B. Data Tables, Tree Views für Sequenzen, Cards) mit, was die Entwicklung einer komplexen aber aufgeräumten Lehrenden-Oberfläche extrem beschleunigt.
- **Backend: Java mit Spring Boot**
    - _Warum?_ Spring Boot liefert mit Spring Data JPA eine bewährte, robuste Möglichkeit, eine derart komplexe relationale Datenbankstruktur (ERM) über Entity-Relationships zu verwalten. Transaktionssicherheit bei der Erstellung komplexer Aufgabennetzwerke (Aufgabe, Inhalt, Modifikator, Tag gleichzeitig speichern) ist out-of-the-box gewährleistet.
- **Datenbank: PostgreSQL**
    - _Warum?_ PostgreSQL bietet exzellenten Support für relationale Strukturen als auch für JSON-Felder (`JSONB`). Da laut Schema z.B. bei `Item_Content` Inhalte als `json_serialized_content` persistiert werden, ist PostgreSQL die ideale Wahl.

### 2.2. Alternative Technologie-Optionen (Evaluierung)

Sollte der gewählte Stack nicht eingesetzt werden können, sind folgende Alternativen ähnlich gut geeignet:

- **Frontend Alternativen:**
    - **React + Material UI (MUI):** Größte Community und enormes Ökosystem. Ähnliche Vorteile wie Vuetify durch MUI, tendenziell jedoch etwas steilere Lernkurve bei der Zustandsverwaltung komplexer UI-Graphen.
    - **Angular + Angular Material:** Sehr strikte Architektur (TypeScript-First). Hervorragend für Enterprise-Anwendungen geeignet, erzwingt ein sauberes Pattern, hat jedoch etwas mehr Boilerplate-Code als Vue.

- **Backend Alternativen:**
    - **Node.js mit NestJS (TypeScript):** Moderne Alternative. Da das Frontend in TypeScript/JavaScript geschrieben ist, könnte so eine einheitliche Sprache über den gesamten Stack genutzt werden. NestJS nutzt ein Architekturmodell stark angelehnt an Angular/Spring.
    - **Python mit FastAPI oder Django:** Besonders interessant, falls in der Aufgabenplattform langfristig KI-Komponenten, Machine Learning für Aufgabenvorschläge oder Script-Auswertungen eingebunden werden sollen.

## 3. Umsetzung der Kernfunktionalitäten (Use Cases)

### 3.1. Abbildung von vertikalen und horizontalen Abhängigkeiten

Das System muss das abstrakte Schema benutzbar machen.

- **Frontend (UI-Darstellung):**
    - _Vertikale Sequenzen (`Item_Collection`)_ werden im Frontend als visuelle "Lernpfade" (Trees oder Pipelines) mittels Vuetify `v-treeview` oder Drag & Drop Listen dargestellt.
    - _Horizontale Varianten (`Modifier`, `Validator`)_ werden bei einer Einzelaufgabe (`Item`) als Konfigurationsreiter ("Erweiterte Restriktionen") implementiert (z.B. "Muss INNER JOIN enthalten").

- **Backend (Datenfluss & Mapping):**
    - In Spring Boot werden die Entitäten hierarchisch gemappt (`@OneToMany`, `@ManyToMany`).
    - Eine spezielle Service-Logik (`ItemRelationshipService`) aggregiert diese komplexen Datenbankabfragen in strukturierte und tief verschachtelte JSON-DTOs (Data Transfer Objects), sodass das Vuex/Pinia-System in Vue.js diese einfach konsumieren kann.

### 3.2. Speicherung von Aufgabeninhalten (`Item_Content`) und Zusatzmaterialien

Da Inhaltsbausteine getrennt vom Root-Item sind, wird eine geteilte Strategie für formatierte Texte und Binärdaten verfolgt:

- **Texte / Strukturierte Daten:** Das Vue-Frontend rendert Eingabemasken basierend auf der `item_content_type_id`. Diese Daten erfolgen als JSON in der Datenbank (`json_serialized_content`) und werden über `Map<String, Object>` im Backend dynamisch verarbeitet.
- **Zusatzmaterialien (Binärdaten/Bilder):** Lehrende können über das Frontend (z.B. über die Vue-Komponente `v-file-input`) direkt Dateien (Bilder, Diagramme, PDFs etc.) als Zusatzmaterial anhängen.
    - _Backend:_ In Spring Boot wird dies über Endpunkte für Dateiuploads via `MultipartFile` entgegengenommen.
    - _Datenbank:_ Die hochgeladenen Dateien werden physikalisch als `BLOB`-Datentyp (bzw. `bytea` in PostgreSQL) im Feld `blob_serialized_content` in der Tabelle `Item_Content` gespeichert, ganz nach dem bereitgestellten ER-Modell.

### 3.3. Zusatzfeature (Gimmick): KI-gestützte Generierung von Aufgaben-Bäumen (Prompt-to-Tree)

Um den Dozierenden die Erstellung komplexer Sequenzen zu erleichtern, wird ein KI-Feature integriert, das durch natürlichsprachliche Prompts komplette Aufgaben-Bäume (inklusive vertikaler und horizontaler Abhängigkeiten) generiert.

- **Szenario:** Ein Nutzer gibt ein: _"Erstelle eine 3-stufige SQL-Übung zu JOINs. Stufe 1: Konzept erstellen, Stufe 2: Tabellen anlegen, Stufe 3: Abfrage zwingend mit INNER JOIN."_
- **Frontend-Implementierung (Vue.js):**
    - Eine Eingabemaske ("AI Tree Builder") im _Collection Manager_ erlaubt die Eingabe des Prompts.
    - Das Resultat wird nicht direkt gespeichert, sondern als flüchtiger Zustand in _Pinia_ geladen und im `v-treeview` visuell als Entwurf (`Draft`) dargestellt. Der Nutzer kann Modifikatoren und Texte manuell anpassen, bevor der Baum final persistiert wird.
- **Backend-Implementierung (Spring Boot):**
    - Ein neuer Endpunkt (`/api/collections/generate`) nimmt den Prompt entgegen.
    - Spring Boot kommuniziert über das _Spring AI Framework_ (oder direkte HTTP-Clients) mit einem LLM (z.B. OpenAI GPT-4).
    - Hierbei wird ein robuster **System Prompt** gesendet, der das LLM zwingt, ein reines JSON-Objekt zurückzugeben, das _exakt_ dem DTO-Schema (Data Transfer Object) des Backends entspricht.
    - Spring Boot validiert das empfangene JSON, wandelt es in DTOs um und sendet die fertige Struktur (ohne sie sofort in PostgreSQL zu speichern) an das Frontend zurück.

## 4. Softwarekomponenten & Module

### 4.1 Frontend-Architektur (Vue.js)

1.  **Item Editor Component:** Zum Anlegen von Struktur und Metadaten.
2.  **Content Builder Component:** Dynamischer Editor zur Eingabe fachlicher Inhalte.
3.  **Collection Manager Component:** Grafischer Editor für Aufgabenfolgen (Pipelines).
4.  **State Management (Pinia/Vuex):** Hält den aktuellen Aufgabenbaum lokal im Browser-Speicher, um unnötige Ladezeiten bei komplexen Konfigurationen zu vermeiden, bevor auf "Speichern" geklickt wird.

### 4.2 Backend-Architektur (Spring Boot)

1.  **ItemController, TagController, CollectionController:** REST-Endpoints.
2.  **DTO-Layer:** Trennung von internem Datenbankmodell (über 10 Tabellen) und der JSON-Antwort an das Frontend, um die Komplexität für die Vue-App zu reduzieren.
3.  **Repositories (Spring Data JPA):** ORM-Mapping des bereitgestellten ER-Modells (z.B. `ItemRepository`, `ValidatorRepository`).

## 5. Nicht-funktionale Anforderungen & Qualitätssicherung

- **Transaktionen:** Beim Speichern einer neuen Aufgabe samt Sequenz und Tags in Spring Boot ist `@Transactional` konsequent zu verwenden, um inkonsistente Zustände bei Fehlern zu vermeiden.
- **Validierung:** `javax.validation` im Backend stellt sicher, dass Datensätze, die das Frontend verlassen (z.B. leere Contents, fehlende Autoren), abgelehnt werden.
- **REST-Kommunikation:** Konsequente Nutzung von korrekten HTTP-Status-Codes (200, 201, 400, 404, 500).
