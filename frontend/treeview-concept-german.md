# Treeview-Interaktionen zur Organisation von Übungen

## 1. Überblick

Dieses Dokument beschreibt, wie die Erstellung, Verwaltung und Strukturierung von Übungen (sowie Sammlungen), wie sie im Datenfluss von `AGENTS.md` definiert sind, mithilfe einer baumbasierten Benutzeroberfläche visualisiert und bedient werden können.
Die UI verhält sich ähnlich wie ein Datei-Explorer.

## 2. Komponenten-Stack

- **Vuetify Treeview (`v-treeview` oder ähnliche Vuetify-Komponenten):** Wird verwendet, um strukturierte hierarchische Daten, verschachtelte Sammlungen und Elemente darzustellen.
- **Vue DnD Kit:** Wird für Drag-and-Drop-Operationen genutzt, damit Benutzer Übungen intuitiv neu anordnen, zwischen Sammlungen verschieben und verschachtelte Strukturen erstellen können.

## 3. Visuelle Abbildung

Um die Backend-Datenmodelle in einer Treeview darzustellen:

- **Einheitliche Item-Architektur:** Es muss absolut klar sein, dass **jedes Item eine Sammlung sein kann und eine Sammlung immer ebenfalls ein Item ist**.
  Es gibt keine getrennten „Ordner“- und „Datei“-Entitäten im Kernschema – ein Item verhält sich lediglich wie ein „Ordner“ (expandierbarer Knoten), wenn es untergeordnete Items besitzt, oder wie eine „Datei“ (Leaf Node), wenn dies nicht der Fall ist.

- **Namenskonvention:** Alle Entitäten in der Treeview (sowohl Dateien als auch Ordner) sollten generisch als „Item“ bezeichnet werden (oder ihren `item_type` / ihre ID anzeigen), da Items im Root-Datenbankschema kein direktes „title“-Feld besitzen. Titel werden stattdessen in Content-Blöcken gespeichert.

- **Einzelne Übungen (Leaf Items):** Werden als „Dateien“ (Leaf Nodes) dargestellt, wenn sie aktuell keine untergeordneten Items besitzen.

- **Sammlungen (Parent Items):** Wenn ein Item andere Items enthält, wird es als „Ordner“ (expandierbarer Knoten) dargestellt.
  - **Ungeordnete Sammlungen:** Werden durch ein Standard-Ordnersymbol oder Aufzählungspunkte dargestellt. Untergeordnete Items besitzen keine sichtbaren Sequenznummern (`position = null`).
  - **Geordnete Sammlungen:** Werden durch ein spezielles „Sequenz“-Ordnersymbol (z. B. nummerierte Liste) dargestellt. Untergeordnete Items zeigen ihre Reihenfolge (`position`) deutlich neben ihrem Namen an.
  - **Standardzustand (ungeordnet):** Items, die innerhalb der Sammlung eines anderen Items platziert werden, sind standardmäßig ungeordnet. Sie werden durch ein normales Ordnersymbol oder Aufzählungspunkte dargestellt, ohne sichtbare Positionsnummern (`position = null`).
  - **Umwandlung in geordnet:** Eine ungeordnete Sammlung kann ausschließlich über eine Kontextmenü-Option am Parent-Node in eine geordnete Sequenz umgewandelt werden. Sobald sie geordnet ist (z. B. durch ein nummeriertes Listen-Icon gekennzeichnet), zeigen die Child-Items ihre jeweilige Reihenfolge (`position`) sichtbar neben ihrem Namen an.

## 4. Benutzerinteraktionen & Integration in den Datenfluss

### Primäre Tree-Navigation (Chevron vs. Node-Klick)

- **Sammlungen erweitern:** Da jedes Item potenziell selbst eine Sammlung sein kann und gleichzeitig eigene Inhalte/Daten besitzt, darf ein Klick auf den eigentlichen Node-Körper das Item **nicht** erweitern.
  - Ein Klick auf den Node-Körper dient ausschließlich dazu, die Details des Items auszuwählen, zu öffnen oder in einer Vorschau anzuzeigen (Inhalte und Metadaten).
  - Das Erweitern einer Sammlung, um Child-Items sichtbar zu machen, darf ausschließlich durch einen Klick auf das Chevron-/Pfeil-Icon neben dem Node ausgelöst werden.

### A. Drag-and-Drop-Neuanordnung (innerhalb einer geordneten Sammlung)

- **Aktion:** Der Benutzer zieht eine Übung innerhalb derselben geordneten Sammlung nach oben oder unten.
- **Vue DnD Kit:** Erkennt die Sortierinteraktion und berechnet den neuen Index.
- **Backend-Flow:** Löst `PUT /collections/{collectionId}/items/{itemId}` mit der neu berechneten `position` aus. Das Backend berechnet die Positionen der Geschwisterelemente automatisch neu.

### B. Verschieben von Items zwischen Sammlungen

- **Aktion:** Der Benutzer zieht ein Item aus Sammlung A und legt es in Sammlung B ab.
- **Vue DnD Kit:** Erkennt das Drop-Event über einem anderen Droppable-Container (Ordner).
- **Backend-Flow:** Löst `PUT /collections/items/{itemId}` mit der Ziel-`collectionId` aus. Falls Sammlung B geordnet ist, weist das Backend automatisch eine Position zu oder verwendet den Drop-Index.

### C. Übungen erweitern (horizontaler Vektor per Drag & Drop)

- **Aktion:** Der Benutzer zieht eine Übung direkt auf eine andere bestehende Basisübung (nicht auf einen Ordner).
- **Frontend-Logik:**
  1. Fordert den Benutzer zur Gruppierung auf oder erstellt automatisch eine neue Sammlung.
  2. `POST /items/{id}/collections` (erstellt eine Sammlung, die mit dem Basis-Item verbunden ist).
  3. `POST /collections/{collectionId}/items` (verschiebt das gezogene Item in die neue Sammlung).

### D. Reihenfolge einer Sammlung umschalten

- **Aktion:** Ein Switch, Kontrollkästchen oder eine Kontextmenü-Option an einem „Collection“-Node schaltet den sequenziellen Modus ein oder aus.
- **Backend-Flow:** Ruft `PUT /collections/{collectionId}` mit `{ order: true/false }` auf.
- **UI-Update:** Die UI blendet Positionsnummern der Child-Items dynamisch ein oder aus – abhängig vom neuen Zustand.

### E. Neue Einträge erstellen

- **Neue Übung:** Öffnet einen Dialog zur Definition von Content-Blöcken (`purpose`, `jsonContent`). Führt `POST /items` aus und fügt das Item entweder an der Root-Ebene oder innerhalb der aktuell aktiven Sammlung via `POST /collections/{collectionId}/items` ein.
- **Neue Sammlung:** Führt `POST /collections` aus und erstellt einen neuen Ordnerknoten in der Treeview.

## 5. Implementierungsaspekte

- **Draggable vs. Droppable:** Es muss zwischen Sortier-Kontexten (innerhalb derselben Sammlung) und Cross-Container-Drops (Verschieben in eine andere Sammlung) unterschieden werden. Die Sensoren von Vue DnD Kit sollten so konfiguriert werden, dass verschachtelte Droppable-Zonen (Ordner) unterstützt werden.

- **Optimistische UI-Updates:** Da Backend-Operationen – insbesondere Positions-Neuberechnungen – Zeit benötigen können, sollte der Vue-State (über Pinia oder lokalen Komponenten-State) die Struktur der Treeview unmittelbar nach einem Drop-Ereignis optimistisch aktualisieren. Nur bei einem API-Fehler sollte ein Rollback erfolgen.

- **Root-Items (Implementierung B für Sequenzen):** Falls Sequenzen über `rootItem` statt über explizite Sammlungen verwaltet werden, sollte das Verknüpfen eines Items mit einem anderen ein `PUT /items/{id}` auslösen, das die neue `rootItemId` setzt. Für die Darstellung könnten Items mit derselben `rootItem` unter einem virtuellen Ordnerknoten gruppiert werden.

---

# Datenfluss für Übungen und Sammlungen

## 1. Neue Übung erstellen (keine Sammlung, kein Root-Item)

### Beschreibung

Der Benutzer erstellt ein einzelnes Item ohne Collection und ohne Root-Referenz.

### Ablauf

1. Benutzer erstellt ein neues `Item`
2. Benutzer fügt Content-Blöcke hinzu:
   - Titel der Übung (`purpose`)
   - Beschreibung der Übung (`jsonContent`)

### Beispiel

```json
{
  "purpose": "Exercise Title",
  "jsonContent": {
    "text": "This is a description of the exercise"
  }
}
```

### API

```http
POST /items
```

---

# 2. Erstellung einer ungeordneten Gruppe von Übungen

## Beschreibung

Eine Collection ist selbst ebenfalls ein Item.
Die Collection enthält Child-Items ohne feste Reihenfolge.

---

## Ablauf

### Schritt 1: Collection erstellen

- Ein neues Item ohne Content wird erstellt
- Anschließend wird eine Collection erzeugt
- `order = false`

### Schritt 2: Übungen zur Collection hinzufügen

- Für jede Übung wird ein neues Item erstellt
- Danach wird ein `CollectionItem` erzeugt
- `position = null`

---

## API-Flows

### Collection erstellen

```http
POST /collections
```

### Interner Ablauf

1. Item erstellen
2. Collection erstellen

---

### Item zur Collection hinzufügen

```http
POST /collections/{collectionId}/items
```

### Interner Ablauf

1. Item erstellen
2. CollectionItem erstellen
3. `position = null`

---

# 3. Erstellung einer geordneten Liste von Übungen (Implementierung 1)

## Beschreibung

Eine normale Collection wird erstellt, jedoch mit aktivierter Reihenfolge.

Die Child-Items besitzen feste Positionen:
`1, 2, 3 ...`

---

## Ablauf

### Schritt 1: Geordnete Collection erstellen

- Collection wird erzeugt
- `order = true`

### Schritt 2: Übungen hinzufügen

- Jedes neue Item erhält eine Position

---

## API-Flows

### Collection erstellen

```http
POST /collections
```

### Item hinzufügen

```http
POST /collections/{collectionId}/items
```

### Ergebnis

```text
position = 1
position = 2
position = 3
...
```

---

# 4. Erstellung einer geordneten Liste von Übungen (Implementierung 2)

## Beschreibung

Anstelle einer expliziten Collection wird eine Root-Item-Beziehung verwendet.

Alle Items gehören logisch zu einem gemeinsamen Root-Item.

---

## Ablauf

### Schritt 1

Root-Item erstellen

```http
POST /items
```

---

### Schritt 2

Neues Item mit `rootItemId` erstellen

```http
POST /items
```

### Beispiel

```json
{
  "rootItemId": "id-des-root-items"
}
```

---

### Schritt 3

Beliebig oft wiederholen

---

# 5. Bestehende geordnete Liste horizontal erweitern

## Beschreibung

Eine bestehende Übung erhält zusätzliche ungeordnete Child-Items.

Dadurch entsteht eine horizontale Erweiterung der Übung.

---

## Ablauf

### Schritt 1

Collection an bestehendes Item anhängen

```http
POST /items/{id}/collections
```

---

### Schritt 2

Items zur neuen Collection hinzufügen

```http
POST /collections/{collectionId}/items
```

---

### Schritt 3

Wiederholen

---

# 6. Ungeordnete Liste in geordnete Liste umwandeln

## Beschreibung

Eine bestehende Collection wird von ungeordnet auf geordnet umgestellt.

Das Backend vergibt automatisch Positionen.

---

## API

```http
PUT /collections/{collectionId}
```

### Payload

```json
{
  "order": true
}
```

---

## Ergebnis

Alle Items erhalten automatisch:

```text
position = 1, 2, 3 ...
```

---

# 7. Geordnete Liste in ungeordnete Liste umwandeln

## Beschreibung

Eine bestehende geordnete Collection wird zurück in eine ungeordnete Collection umgewandelt.

Alle Positionen werden entfernt.

---

## API

```http
PUT /collections/{collectionId}
```

### Payload

```json
{
  "order": false
}
```

---

## Ergebnis

```text
position = null
```

für alle CollectionItems.

---

# 8. Reordering-Strategie innerhalb einer Collection

## Beschreibung

Ein Item wird innerhalb einer geordneten Collection verschoben.

Beispiel:

- Item 7 wird auf Position 2 verschoben

Das Backend berechnet alle anderen Positionen automatisch neu.

---

## API

```http
PUT /collections/{collectionId}/items/{itemId}
```

### Payload

```json
{
  "position": 2
}
```

---

## Backend-Verhalten

Beispiel vorher:

```text
1 2 3 4 5 6 7
```

Nach Verschiebung:

```text
1 7 2 3 4 5 6
```

Positionen werden automatisch neu berechnet.

---

# 9. Bestehende Collection mit Items aus anderer Collection erweitern

## Beschreibung

Ein bestehendes Item wird in eine andere Collection verschoben oder eingefügt.

Falls die Ziel-Collection geordnet ist,
werden Positionen automatisch vergeben.

---

## API

```http
PUT /collections/items/{itemId}
```

x Mahl

### Payload

```json
{
  "collectionId": "target-collection-id"
}
```

---
