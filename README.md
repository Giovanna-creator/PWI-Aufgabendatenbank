# PWI-Aufgabendatenbank

Webanwendung zum Beschreiben, Strukturieren und Speichern von Übungsaufgaben.
Das Projekt überführt ein vorgegebenes relationales Datenschema in ein
nutzbares Autorenwerkzeug für Lehrende.

Die Anwendung ist **kein Lern-, Abgabe- oder Korrektursystem**. Sie verwaltet
Aufgaben, Inhalte, Metadaten, Sammlungen, Sequenzen und Varianten. Validatoren
werden definiert und gespeichert; ihre Ausführung ist Aufgabe eines externen
Dienstes.

## Funktionsumfang

- Aufgaben mit Autor, Lizenz und Typ erstellen und bearbeiten
- mehrere Inhaltsbausteine pro Aufgabe verwalten
- Text/JSON, Bilder und PDF-Dateien speichern und darstellen
- geordnete Lernpfade und ungeordnete Sammlungen bilden
- Drag-and-Drop innerhalb und zwischen Collections
- Varianten einer Ausgangsaufgabe verwalten
- Validatoren erstellen und Aufgaben zuordnen
- hierarchische Tags anlegen, zuweisen und filtern
- Suche nach Inhalt, Autor, Typ und Tags
- XML-basierte Darstellungstemplates mit Live-Vorschau
- Dummy-Modus für die Frontend-Entwicklung ohne Backend
- Docker-Images automatisiert nach GHCR veröffentlichen

## Fachliches Modell

### Item

Ein Item ist die zentrale Aufgabenentität. Es besitzt einen Autor, eine Lizenz,
einen Typ und optional ein Darstellungstemplate.

**root_item_id ist ausschließlich der Bezug einer Variante zu ihrer
Ausgangsaufgabe.** Die Spalte bildet keine Collection-Hierarchie ab.

### ItemContent

Inhalte werden unabhängig vom Item gespeichert:

- json_serialized_content für strukturierte Inhalte,
- blob_serialized_content für Bilder, PDF und andere Binärdateien,
- eigene Metadaten wie Autor, Lizenz und Inhaltstyp.

Die Join-Tabelle item_contents verbindet Item und Content. Ihr Feld purpose
beschreibt die Rolle des Inhalts, beispielsweise Aufgabenstellung, Hinweis oder
Lösung.

### Collections

Eine ItemCollection gruppiert Items und besitzt eine eigene Collection-ID. Über
parent_item_id ist ihr ein Item als fachlicher Träger zugeordnet.

- order = false: ungeordnete Sammlung, Positionen sind null
- order = true: geordnete Sequenz, Positionen sind 1, 2, 3, ...

Die Mitgliedschaft wird ausschließlich über item_collection_sub_item
abgebildet. Sie verändert root_item_id nicht.

### Tags, Validatoren und Modifier

- Tags bilden über parent_tag_id eine Hierarchie.
- Validatoren beschreiben Prüf- oder Restriktionsregeln.
- Modifier sind im Schema für spätere Transformationen vorgesehen.

Validatoren und Modifier werden in dieser Anwendung nicht ausgeführt.

## Architektur

~~~text
Vue-Komponenten
      |
Pinia Store
      |
ApiAdapter
      |
REST/JSON
      |
Spring Controller -> Service -> Repository -> PostgreSQL
~~~

| Bereich | Technologie |
|---|---|
| Frontend | Vue 3, TypeScript, Vuetify, Pinia, Vite |
| Backend | Java 21, Spring Boot 3.2.5, Spring Data JPA |
| Datenbank | PostgreSQL 16 |
| Tests | Vitest, JUnit, Mockito, MockMvc |
| Betrieb | Docker Compose, GitHub Actions, GHCR, Kubernetes |

Der ApiAdapter besitzt zwei Implementierungen:

- AdbApiService: Kommunikation mit der realen REST-API
- DevAdbApiService: lokale Daten für den Dummy-Modus

## Repository-Struktur

~~~text
backend/            Spring-Boot-Service
frontend/           Vue-/Vite-Client
database/           SQL-Schema, Migrationen und Initialdaten
docs/               Konzepte, Testplan und Präsentationsunterlagen
Lastenpflichtheft/  Lasten- und Pflichtenheft
docker-compose.yml  lokaler Gesamtstart
~~~

## Voraussetzungen

### Docker-Variante

- Docker Engine bzw. Docker Desktop
- Docker Compose

### Lokale Entwicklung

- Java 21
- Node.js 22
- npm
- PostgreSQL 16 oder eine über Docker gestartete Datenbank

## Schnellstart mit Docker

~~~bash
docker compose up --build
~~~

Danach:

- Frontend: <http://localhost:8085>
- Backend-API: <http://localhost:8080/api>
- PostgreSQL: localhost:5432

Die Datenbank wird beim ersten Start über database/init/init.sql initialisiert.
Das Volume pgdata erhält die Daten über Neustarts hinweg.

Stoppen:

~~~bash
docker compose down
~~~

Das Datenbank-Volume wird dabei nicht gelöscht.

## Lokale Entwicklung

### 1. Datenbank

~~~bash
docker compose up -d db
~~~

### 2. Backend

PowerShell:

~~~powershell
cd backend
$env:DB_URL = "jdbc:postgresql://localhost:5432/pwi_aufgabendatenbank"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
.\mvnw.cmd spring-boot:run
~~~

Linux/macOS:

~~~bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/pwi_aufgabendatenbank
export DB_USER=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run
~~~

### 3. Frontend

~~~bash
cd frontend
npm ci
npm run dev
~~~

### Dummy-Modus

Der Dummy-Modus benötigt weder Backend noch Datenbank:

~~~bash
cd frontend
npm ci
npm run dev:dummy
~~~

## Tests und Build

### Frontend

~~~bash
cd frontend
npm ci
npm test
npm run build
~~~

Aktueller geprüfter Stand:

- 107 Frontend-Tests
- TypeScript-Prüfung erfolgreich
- Produktionsbuild erfolgreich

### Backend

Windows:

~~~powershell
cd backend
.\mvnw.cmd test
~~~

Linux/macOS:

~~~bash
cd backend
./mvnw test
~~~

Aktueller geprüfter Stand: **92 Backend-Tests**. Die Tests verwenden Mocks und
benötigen keine laufende PostgreSQL-Instanz.

## Wichtige API-Bereiche

| Basis-Pfad | Zweck |
|---|---|
| /api/items | Items, Suche, Tags, Varianten und Validator-Zuordnung |
| /api/contents | Inhalte und Datei-Uploads |
| /api/collections | Collections, Mitglieder und Positionen |
| /api/tags | hierarchische Tags |
| /api/validators | Validatoren |
| /api/representation-templates | Darstellungstemplates |
| /api/authors | Autoren |
| /api/licenses | Lizenzen |
| /api/item-types | Aufgabentypen |
| /api/content-types | Inhaltstypen |

## Konsistenzregeln

- Collection-Mitgliedschaft und Variantenbezug werden nicht vermischt.
- Beim Deaktivieren einer Reihenfolge werden Positionen auf null gesetzt.
- Das Verschieben eines Items verändert rootItemId nicht.
- Die Umwandlung eines Items in eine Collection ist idempotent.
- Das Löschen einer Collection löscht nicht ihre enthaltenen Items.
- Nach partiell fehlgeschlagenen Schreibvorgängen lädt das Frontend den
  betroffenen Aggregatbereich erneut.

## Bekannte Grenzen

- Die erste UI-Iteration stellt Items in einer eindeutigen Baumposition dar,
  obwohl das Schema Mehrfachzuordnungen zu Collections ermöglicht.
- Wiederverwendung eines ItemContent in mehreren Items ist im Schema
  vorbereitet, aber noch kein eigener UI-Workflow.
- Authentifizierung und rollenbasierte Rechte werden von der Plattform
  erwartet und sind noch nicht integriert.
- CORS ist für die Entwicklungs- und Integrationsphase offen konfiguriert.
- Validatoren und Modifier werden gespeichert, jedoch extern ausgeführt.
- Vollständiges Kubernetes-Deployment hängt von Plattformdiensten ab.

## Dokumentation und Präsentation



## Projektstatus

Die Kernfunktionen der ersten Iteration sind implementiert. Der Schwerpunkt vor
der Abgabe liegt auf Dokumentation, Präsentation, reproduzierbarer Demonstration
und abschließender Bereinigung.

## Lizenz

Für das Repository ist derzeit keine eigenständige Softwarelizenz hinterlegt.
Die in der Anwendung verwalteten Aufgaben und Inhalte besitzen jeweils eigene
Lizenzangaben.
