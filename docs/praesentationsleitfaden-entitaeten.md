# Präsentationsleitfaden: Demo, Datenmodell und typische Fragen

Dieses Dokument dient als fachlicher und technischer Spickzettel für die
Projektpräsentation. Es erklärt:

1. welche Demonstrationen sinnvoll sind,
2. wie das vorgegebene Schema verstanden wurde,
3. wie jede Entität in Datenbank, Backend und Frontend behandelt wird,
4. welche Entscheidungen bewusst getroffen wurden,
5. welche Grenzen offen kommuniziert werden sollten,
6. wie typische Rückfragen beantwortet werden können.

## 1. Kernbotschaft des Projekts

Die wichtigste Aussage lautet:

> Wir haben nicht nur CRUD-Endpunkte für ein vorhandenes Schema gebaut, sondern
> die abstrakten Beziehungen des Schemas in verständliche Arbeitsabläufe für
> Lehrende übersetzt.

Die Anwendung ist ein **Autoren- und Verwaltungswerkzeug**. Sie ist kein
Lernsystem und führt weder Validatoren noch Modifier aus. Ein externer Dienst
kann diese gespeicherten Regeln später interpretieren.

## 2. Empfohlene Gesamtdemonstration

Die vollständige Demo dauert etwa fünf bis sieben Minuten. Vorher sollten
Backend, Frontend und Datenbank laufen und vorbereitete Beispieldaten vorhanden
sein.

### Demo 1: Aufgabe erstellen

**Aktion**

1. Auf „Aufgabe erstellen“ klicken.
2. Autor, Lizenz und Aufgabentyp wählen.
3. Einen kurzen Aufgabentext eingeben.
4. Erstellung bestätigen.

**Was damit gezeigt wird**

- Item als zentrale Aufgabenentität
- Referenzdaten aus der Datenbank
- bestätigungspflichtiger Workflow
- optimistische Oberfläche
- persistente Speicherung

**Technischer Datenfluss**

~~~text
Create-Dialog
 -> exerciseStore.createItemFromForm()
 -> POST /api/items
 -> ItemController
 -> ItemService
 -> ItemRepository
 -> Tabelle item

anschließend:
 -> POST /api/contents/by-item/{itemId}
 -> item_content + item_contents
~~~

**Erklärung**

Das Item wird zuerst angelegt, weil der Content für die Verknüpfung eine reale
Item-ID benötigt. Schlägt eine spätere Stufe fehl, lädt das Frontend den
betroffenen Bereich erneut aus dem Backend.

### Demo 2: Mehrere Inhalte und Dateiformate

**Aktion**

1. Einen zweiten Content-Block hinzufügen.
2. Unterschiedliche Zwecke zeigen, zum Beispiel Aufgabenstellung und Hinweis.
3. Optional ein Bild oder PDF hochladen.
4. Vorschau öffnen.

**Was damit gezeigt wird**

- Trennung zwischen Item und ItemContent
- purpose als Attribut der Verknüpfung
- JSONB für Text
- BYTEA und Multipart für Dateien
- Template-basierte Darstellung

**Wichtige Antwort**

Der Zweck gehört nicht zum Content selbst, sondern zur Beziehung
item_contents. Dadurch könnte derselbe Content in unterschiedlichen Kontexten
mit unterschiedlichem Zweck verwendet werden.

### Demo 3: Darstellungstemplate

**Aktion**

1. Template-Editor öffnen.
2. Reihenfolge der Purpose-Platzhalter ändern.
3. Live-Vorschau vergleichen.
4. Template speichern.

**Was damit gezeigt wird**

- item_representation_template
- Trennung zwischen Inhalt und Darstellung
- XML-Parsing im Frontend
- persistente Template-Verwaltung im Backend

**Warum diese Lösung**

Die Inhalte bleiben strukturiert und unabhängig von einer festen Oberfläche.
Das Template bestimmt, wo die Content-Blöcke dargestellt werden.

### Demo 4: Ungeordnete Collection

**Aktion**

1. Collection erstellen.
2. Zwei Aufgaben hineinziehen.
3. Zeigen, dass keine Nummerierung vorhanden ist.

**Was damit gezeigt wird**

- Item als fachlicher Träger der Collection
- eigene ItemCollection-ID für Collection-Endpunkte
- item_collection_sub_item als Mitgliedschaft
- position = null bei ungeordneten Collections

### Demo 5: Geordnete Collection und Drag-and-Drop

**Aktion**

1. Reihenfolge aktivieren.
2. Positionen 1, 2, 3 zeigen.
3. Aufgabe verschieben.
4. Reihenfolge deaktivieren.

**Was damit gezeigt wird**

- order-Flag
- serverseitige Positionsberechnung
- Neuordnung der Geschwister
- Positionen werden beim Deaktivieren auf null gesetzt

**Wichtige Antwort**

Die Reihenfolge liegt nicht in root_item_id. Sie existiert ausschließlich in
item_collection_sub_item.position.

### Demo 6: Horizontale Variante

**Aktion**

1. Eine Ausgangsaufgabe auswählen.
2. Variante erstellen.
3. Geänderten Inhalt oder Validator hinzufügen.
4. Variantenbereich zeigen.

**Was damit gezeigt wird**

- Self-Reference über root_item_id
- gleiche Problemfamilie, aber unterschiedliche Ausprägungen
- klare Trennung zur Collection-Mitgliedschaft

**Wichtige Antwort**

Das Verschieben einer Variante in eine Collection verändert rootItemId nicht.
Collection-Zugehörigkeit und Variantenbezug sind orthogonal.

### Demo 7: Validatoren

**Aktion**

1. Validator anlegen, zum Beispiel „muss INNER JOIN enthalten“.
2. Regeltext eintragen.
3. Validator der Aufgabe zuordnen.

**Was damit gezeigt wird**

- Validator als wiederverwendbare Regel
- Many-to-many-Verknüpfung item_validator
- bewusste Systemgrenze

**Wichtige Antwort**

Die Anwendung speichert die Regel, führt sie aber nicht aus. Die eigentliche
Prüfung gehört zu einem externen Laufzeitdienst.

### Demo 8: Hierarchische Tags

**Aktion**

1. Pfad wie Datenbanken/SQL/Joins anlegen.
2. Blatt-Tag der Aufgabe zuweisen.
3. Breadcrumb oder Chip zeigen.
4. Eltern-Tag als Filter auswählen.

**Was damit gezeigt wird**

- Self-Reference parent_tag_id
- Aufbau des Baums im Frontend aus einer flachen DTO-Liste
- item_tags als Zuordnung
- Filter über Hierarchie

### Demo 9: Kombinierte Suche

**Aktion**

1. Textsuche ausführen.
2. Autor oder Typ ergänzen.
3. Tag-Filter kombinieren.
4. Filter zurücksetzen.

**Was damit gezeigt wird**

- dynamische JPA Specifications
- Suche in JSONB-Inhalten
- Kombination optionaler Kriterien
- praktische Wiederauffindbarkeit

### Demo 10: Fehlerrobustheit und Persistenz

In der Live-Demo keinen Fehler künstlich provozieren. Stattdessen erklären:

- temporäre IDs für optimistische Darstellung,
- Resynchronisation nach Teilfehlern,
- Reload der Seite als Nachweis der Persistenz,
- 107 Frontend- und 92 Backend-Tests.

## 3. Architektur über alle Ebenen

### Datenbank

PostgreSQL bildet das vorgegebene Schema ab. Primär- und Fremdschlüssel verwenden
UUIDs. JSONB speichert strukturierte Inhalte, BYTEA binäre Dateien.
Join-Tabellen bilden Many-to-many-Beziehungen und zusätzliche
Beziehungsattribute wie purpose oder position ab.

### Backend

~~~text
Controller -> Service -> Repository -> Entity -> PostgreSQL
               |
               -> DTO-Mapping und Geschäftsregeln
~~~

- Controller definieren HTTP-Vertrag und Statuscodes.
- Services enthalten Transaktionen, Validierung und Geschäftslogik.
- Repositories kapseln Spring Data JPA.
- Entities spiegeln Tabellen und Beziehungen.
- DTOs verhindern die direkte Veröffentlichung von JPA-Entities.

### Frontend

~~~text
Vue-Komponente -> Pinia Store -> ApiAdapter -> Axios -> REST
                                  |
                                  -> Dummy-Adapter
~~~

- Vue-Komponenten bilden Dialoge, Editor, Baum und Filter.
- Der Store koordiniert Zustand und fachliche Aktionen.
- ApiAdapter entkoppelt Store und Transport.
- DTOs werden in UI-Modelle übersetzt.
- Optimistische Aktionen werden bei Fehlern mit dem Backend resynchronisiert.

## 4. Erklärung aller Schema-Entitäten

### 4.1 author

**Verständnis**

Beschreibt die verantwortliche Person eines Items oder Contents. Ein Autor ist
kein Benutzerkonto und enthält deshalb nur Descriptor und optional Mail.

**Datenbank**

Referenztabelle mit UUID, Descriptor und Mail. Item und ItemContent verweisen
über Fremdschlüssel darauf.

**Backend**

Author-Entity und AuthorRepository. ReferenceController liefert und erstellt
Autoren. ItemService und ItemContentService prüfen, ob die übergebene ID
existiert.

**Frontend**

Auswahlliste in den Metadaten. Neue Autoren können direkt angelegt werden. Bis
zur zentralen Authentifizierung dient ein vorhandener Standardautor als
Fallback.

**Status**

Vollständig für die erste Iteration umgesetzt.

### 4.2 license

**Verständnis**

Beschreibt die Nutzungsrechte. Sowohl Item als auch ItemContent besitzen im
vorgegebenen Schema eine Lizenz.

**Datenbank**

Referenztabelle. Fremdschlüssel sind gegen das Löschen verwendeter Lizenzen
geschützt.

**Backend**

License-Entity, Repository und ReferenceController. IDs werden beim Schreiben
validiert.

**Frontend**

Auswahl und Neuanlage in den Metadaten. Der Content kann eine eigene Lizenz
tragen.

**Status**

Umgesetzt. Die fachliche Frage, ob die Item-Lizenz langfristig zusätzlich zur
Content-Lizenz notwendig ist, bleibt eine Vorgabeentscheidung.

### 4.3 item_type

**Verständnis**

Klassifiziert eine Aufgabe, zum Beispiel SQL-Aufgabe oder Modellierungsaufgabe.

**Datenbank**

Referenztabelle mit Name und Beschreibung. Über item_content_types kann
festgelegt werden, welche Content-Typen kompatibel sind.

**Backend**

ItemType-Entity mit Many-to-many-Beziehung zu ItemContentType. Referenz-Endpunkt
für Lesen und Erstellen.

**Frontend**

Auswahl und Neuanlage. Zusätzlich als Suchfilter verfügbar.

**Status**

Typverwaltung umgesetzt. Die automatische Einschränkung erlaubter Content-Typen
über item_content_types ist technisch modelliert, aber noch kein vollständiger
UI-Workflow.

### 4.4 item_content_type

**Verständnis**

Beschreibt das Format bzw. die fachliche Art eines Inhaltsblocks.

**Datenbank**

Referenztabelle mit Name und Beschreibung.

**Backend**

ItemContentType-Entity und Repository. ItemContentService prüft die Referenz.

**Frontend**

Auswahl beim Erstellen oder Bearbeiten eines Contents; neue Typen können
angelegt werden.

**Status**

Umgesetzt.

### 4.5 item_representation_template

**Verständnis**

Trennt die gespeicherten Inhalte von ihrer Anordnung in der Darstellung.

**Datenbank**

Speichert den Template-Text und wird optional vom Item referenziert.

**Backend**

Entity, Repository, Service und vollständiger CRUD-Controller.

**Frontend**

XML-Editor, Purpose-Platzhalter, Reihenfolgenlogik, Speichern und Live-Vorschau.

**Status**

Umgesetzt. Die Template-Sprache ist eine projektspezifische erste Iteration und
kein allgemeiner Rendering-Standard.

### 4.6 tag

**Verständnis**

Hierarchisches Schlagwort. parent_tag_id bildet einen Baum.

**Datenbank**

Self-Reference mit ON DELETE SET NULL. Beim Löschen eines Eltern-Tags bleiben
Kinder erhalten und werden zu Wurzel-Tags.

**Backend**

Tag-Entity und TagController. Die API liefert eine flache Liste mit
parentTagId; Duplikate werden verhindert.

**Frontend**

Aufbau eines rekursiven Baums, Pfadanzeige, Anlage von Tag-Pfaden, Zuordnung,
Entfernung und Filter. Die Oberfläche bevorzugt den spezifischsten Tag und
entfernt gleichzeitig zugeordnete Vorfahren oder Nachfahren.

**Status**

Item-Tags vollständig demonstrierbar. Content-Tags sind in Entity, DTO und
Service vorbereitet, aber nicht als gleichwertiger eigener Editor-Workflow
ausgebaut.

### 4.7 validator

**Verständnis**

Wiederverwendbare Prüf- oder Restriktionsbeschreibung.

**Datenbank**

Validator-Tabelle plus item_validator für Many-to-many-Zuordnung.

**Backend**

Entity, Repository, Service, CRUD-Controller sowie Endpunkte zum Verknüpfen mit
Items.

**Frontend**

Validator-Editor, CRUD, Zuordnung und Entfernung an einer Aufgabe.

**Status**

Speicherung und Zuordnung umgesetzt; Ausführung bewusst extern.

### 4.8 modifier

**Verständnis**

Beschreibt eine Regel zur Transformation oder Erzeugung einer Variante.

**Datenbank**

Modifier-Tabelle und item_modifier als Many-to-many-Zuordnung.

**Backend**

Entity und Repository vorhanden. Item-DTO und ItemService können Modifier-IDs
lesen und zuordnen.

**Frontend**

Der Typ enthält modifierIds, aber es existiert kein vollständiger
Modifier-Verwaltungseditor.

**Status**

Strukturell angebunden, fachliche Ausführung und vollständige UI bewusst
zurückgestellt. Das entspricht der Abgrenzung mit dem Betreuer.

### 4.9 item

**Verständnis**

Zentrale Aufgabenentität. Das Item selbst trägt hauptsächlich Metadaten; die
eigentlichen Texte und Dateien liegen in ItemContent.

**Datenbank**

Fremdschlüssel auf Autor, Lizenz, Typ und optional Template.
root_item_id ist eine Self-Reference für Varianten.

**Backend**

Item-Entity mit Beziehungen zu Tags, Validatoren und Modifiern. ItemService
stellt CRUD, Root-/Variantenabfragen, Suche, Zuordnungen und Erkennung einer
Collection bereit.

**Frontend**

Baumknoten, Editor, Metadaten, Contents, Tags, Validatoren und Varianten.
Ein DTO wird in ein UI-Modell mit lesbaren Namen und Backend-IDs übersetzt.

**Status**

Kernfunktion vollständig umgesetzt.

### 4.10 item_content

**Verständnis**

Eigenständiger Inhaltsbaustein mit strukturiertem oder binärem Inhalt.

**Datenbank**

UUID, Lizenz, Content-Typ, Autor, JSONB, BYTEA und Zeitstempel. Tags können über
item_content_tags verknüpft werden.

**Backend**

Entity, Repository, Service und Controller. Unterstützt CRUD, Inhalte je Item,
Multipart-Upload und Blob-Download.

**Frontend**

Content-Liste und Content-Editor. JSON wird als Textinhalt bearbeitet; Bilder
werden angezeigt, andere Dateien als Download angeboten.

**Status**

Kernworkflow umgesetzt. Wiederverwendung eines Contents in mehreren Items ist
im Schema möglich, aber kein eigener UI-Workflow.

### 4.11 item_collection

**Verständnis**

Collection-Datensatz mit eigener ID, zugeordnetem Träger-Item und order-Flag.
Das Träger-Item liefert die sichtbare Identität; die Collection-ID adressiert
Collection-Operationen.

**Datenbank**

parent_item_id verweist auf Item. order unterscheidet Sequenz und Sammlung.

**Backend**

Entity, Repository, Service und Controller. Konvertierung eines Items ist
idempotent. Der Service verwaltet Reihenfolge und Mitglieder transaktional.

**Frontend**

Collection wird als Item mit zusätzlichem collectionId, items und order
dargestellt. So kann derselbe Baumrenderer Aufgaben und Collections behandeln.

**Status**

Umgesetzt.

## 5. Erklärung aller Join-Tabellen

### 5.1 item_contents

**Beziehung:** Item zu ItemContent  
**Zusatzfeld:** purpose  
**Backend:** eigene Entity mit EmbeddedId, weil die Beziehung ein Attribut hat  
**Frontend:** Content-Blöcke erscheinen innerhalb des ausgewählten Items  
**Warum keine einfache ManyToMany-Annotation:** purpose würde sonst verloren
gehen.

### 5.2 item_collection_sub_item

**Beziehung:** ItemCollection zu Item  
**Zusatzfeld:** position  
**Backend:** eigene Entity und Composite Key; Service berechnet Positionen  
**Frontend:** CollectionItem-Wrapper enthält Item, Collection-ID und Position  
**Warum:** Die Reihenfolge gehört zur Mitgliedschaft, nicht zum Item.

### 5.3 item_tags

**Beziehung:** Item zu Tag  
**Backend:** Many-to-many-Set verhindert doppelte Zuordnung  
**Frontend:** Chips, Baum-Auswahl und Filter  
**Status:** vollständig genutzt.

### 5.4 item_content_tags

**Beziehung:** ItemContent zu Tag  
**Backend:** in Entity, DTO und Service implementiert  
**Frontend:** IDs werden transportiert, aber kein gleichwertiger Content-Tag-
Editor wie für Items  
**Status:** technisch vorbereitet, UI-Ausbau offen.

### 5.5 item_validator

**Beziehung:** Item zu Validator  
**Backend:** Many-to-many plus Zuordnungsendpunkte  
**Frontend:** Validator-Panel  
**Status:** vollständig für Speicherung und Zuordnung.

### 5.6 item_modifier

**Beziehung:** Item zu Modifier  
**Backend:** Entity-Zuordnung und DTO-Unterstützung  
**Frontend:** modifierIds im Modell, aber kein vollständiges Management  
**Status:** vorbereitet und bewusst zurückgestellt.

### 5.7 item_content_types

**Beziehung:** ItemType zu ItemContentType  
**Bedeutung:** definiert erlaubte Content-Typen je Aufgabentyp  
**Backend:** JPA-Beziehung modelliert  
**Frontend:** Auswahl beider Typen vorhanden, automatische
Kompatibilitätsfilterung noch nicht vollständig umgesetzt  
**Status:** strukturell berücksichtigt, UI-Regel offen.

## 6. Verwendete Ansätze und Begründungen

### DTO statt direkte Entity-Ausgabe

JPA-Entities enthalten Lazy-Loading, bidirektionale Beziehungen und
Persistenzdetails. DTOs bilden einen stabilen API-Vertrag und verhindern
Rekursion bei der JSON-Serialisierung.

### Service-Schicht und Transaktionen

Positionsberechnung, Referenzvalidierung und mehrstufige Änderungen gehören
nicht in Controller. Die Service-Schicht bündelt diese Regeln und definiert
Transaktionsgrenzen.

### Composite Keys für attributierte Beziehungen

item_contents und item_collection_sub_item sind nicht nur einfache
Verknüpfungen. purpose und position machen sie zu fachlich relevanten
Beziehungsentitäten.

### ApiAdapter im Frontend

Der Store hängt nicht direkt von Axios ab. Dadurch können reale API,
Dummy-Daten und Tests denselben Vertrag verwenden.

### Optimistische Oberfläche mit Resynchronisation

Die Oberfläche reagiert sofort. Da Erstellung und Verknüpfung mehrere
API-Schritte benötigen können, wird bei einem Teilfehler der betroffene
Aggregatbereich neu geladen.

### Backend als Autorität für Reihenfolge

Mehrere Clients könnten gleichzeitig umsortieren. Deshalb berechnet das Backend
die endgültigen Geschwisterpositionen.

### Hierarchische Tags als flache API

Die Datenbank speichert nur parent_tag_id. Das Backend liefert eine flache Liste,
und das Frontend baut daraus den Baum. So bleibt die API einfach und die
Darstellung flexibel.

## 7. Bewusste Grenzen

Diese Punkte nicht verstecken, sondern als Scope-Entscheidung erklären:

- keine Ausführung von Validatoren und Modifiern,
- keine zentrale Authentifizierung,
- Modifier-UI noch nicht vollständig,
- Content-Tag-UI nur technisch vorbereitet,
- Kompatibilitätsregeln item_content_types noch kein vollständiger UI-Workflow,
- UI behandelt den Strukturbaum im ersten Schritt als eindeutige Position,
  obwohl das Schema Mehrfachzuordnung erlaubt,
- kein vollständiger Ende-zu-Ende-Test mit realer PostgreSQL in der Testsuite,
- Kubernetes-Ausrollen abhängig von Plattforminfrastruktur.

## 8. Typische Professorenfragen und Antworten

### Warum wurde ItemContent getrennt von Item modelliert?

Weil ein Item aus mehreren typisierten Bausteinen bestehen kann und das Schema
Wiederverwendung erlaubt. Außerdem können Content und Item unterschiedliche
Autoren oder Lizenzen besitzen.

### Warum liegt purpose in der Join-Tabelle?

Purpose beschreibt die Rolle eines Contents in einem bestimmten Item. Dieselbe
Inhaltsressource könnte in einem Item Hinweis und in einem anderen Beispiel
sein.

### Was ist der Unterschied zwischen Collection und root_item_id?

Collection organisiert unterschiedliche Items als Sammlung oder Sequenz.
root_item_id verbindet Varianten derselben Ausgangsaufgabe. Eine Reihenfolge
existiert nur in item_collection_sub_item.position.

### Warum besitzt eine Collection zwei IDs?

Das Träger-Item besitzt Item-Metadaten und ist im Baum sichtbar. Der
ItemCollection-Datensatz besitzt eine eigene ID für Mitgliedschaft, order und
Positionsoperationen.

### Warum werden Positionen im Backend berechnet?

Das Backend ist die gemeinsame Autorität und kann Geschwisterpositionen
transaktional konsistent halten.

### Was passiert bei einem partiellen Fehler?

Das Frontend zeigt zunächst optimistisch an. Schlägt ein Folgeschritt fehl, wird
die betroffene Collection oder der gesamte Baum neu geladen. So entstehen keine
dauerhaften Ghost-Items.

### Warum PostgreSQL statt H2?

Das Schema nutzt PostgreSQL-spezifische Eigenschaften wie JSONB und BYTEA. H2
würde diese Semantik nur unvollständig abbilden.

### Warum UUID?

UUIDs sind unabhängig von einer zentralen Sequenz, eignen sich für verteilte
Dienste und vermeiden leicht erratbare fortlaufende IDs.

### Warum wird der Validator nicht ausgeführt?

Die Anwendung ist ein Autorenwerkzeug. Ausführung und Korrektur gehören zu einem
anderen Plattformdienst. Die Schnittstelle dafür ist die gespeicherte Regel.

### Was würden Sie als Nächstes umsetzen?

Testcontainers-Ende-zu-Ende-Tests, zentrale Authentifizierung, Modifier-UI,
Content-Tag-Editor, Kompatibilitätsfilter für Content-Typen und weitere
Modularisierung des Stores.

### Wie wurde Qualität abgesichert?

Durch Typprüfung, 107 Frontend-Tests, 92 Backend-Tests, MockMvc-Controller-Tests,
Geschäftslogiktests, Baumvalidierung, Pull-Requests und einen erfolgreichen
Produktionsbuild.

## 9. Praktische Checkliste für den Präsentationstag

### Vorher

- Docker/Backend/Frontend starten und Funktionsfähigkeit prüfen.
- Browser auf die richtige Route setzen.
- Beispieldaten vorbereiten.
- Netzwerkabhängige Schritte vermeiden.
- PDF der Folien und Screenshots lokal speichern.
- Terminal mit grünem Testresultat vorbereiten.
- Demo einmal mit Stoppuhr durchführen.

### Während der Demo

- immer zuerst die fachliche Wirkung nennen,
- danach kurz den technischen Datenfluss erklären,
- nicht jede CRUD-Funktion einzeln zeigen,
- Unterschiede zwischen Item-ID und Collection-ID deutlich benennen,
- rootItemId niemals als Baum-Elternreferenz beschreiben,
- bekannte Grenzen offen und knapp benennen.

### Notfallplan

Falls das reale Backend nicht verfügbar ist:

- vorbereitete Screenshots verwenden,
- Architektur und Datenfluss erklären,
- Dummy-Modus nur verwenden, wenn dessen Datenmodell vorher auf die korrekte
  rootItemId-Semantik geprüft wurde.

