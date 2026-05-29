# Treeview-Interaktionen zur Organisation von Übungen

## 1. Überblick

Dieses Dokument beschreibt, wie die Erstellung, Verwaltung und Strukturierung von Übungen (sowie Sammlungen), wie sie im Datenfluss von `AGENTS.md` definiert sind, mithilfe einer baumbasierten Benutzeroberfläche visualisiert und bedient werden können.
Die UI verhält sich ähnlich wie ein Datei-Explorer.

## 2. Komponenten-Stack

* **Vuetify Treeview (`v-treeview` oder ähnliche Vuetify-Komponenten):** Wird verwendet, um strukturierte hierarchische Daten, verschachtelte Sammlungen und Elemente darzustellen.
* **Vue DnD Kit:** Wird für Drag-and-Drop-Operationen genutzt, damit Benutzer Übungen intuitiv neu anordnen, zwischen Sammlungen verschieben und verschachtelte Strukturen erstellen können.

## 3. Visuelle Abbildung

Um die Backend-Datenmodelle in einer Treeview darzustellen:

* **Einheitliche Item-Architektur:** Es muss absolut klar sein, dass **jedes Item eine Sammlung sein kann und eine Sammlung immer ebenfalls ein Item ist**.
  Es gibt keine getrennten „Ordner“- und „Datei“-Entitäten im Kernschema – ein Item verhält sich lediglich wie ein „Ordner“ (expandierbarer Knoten), wenn es untergeordnete Items besitzt, oder wie eine „Datei“ (Leaf Node), wenn dies nicht der Fall ist.

* **Namenskonvention:** Alle Entitäten in der Treeview (sowohl Dateien als auch Ordner) sollten generisch als „Item“ bezeichnet werden (oder ihren `item_type` / ihre ID anzeigen), da Items im Root-Datenbankschema kein direktes „title“-Feld besitzen. Titel werden stattdessen in Content-Blöcken gespeichert.

* **Einzelne Übungen (Leaf Items):** Werden als „Dateien“ (Leaf Nodes) dargestellt, wenn sie aktuell keine untergeordneten Items besitzen.

* **Sammlungen (Parent Items):** Wenn ein Item andere Items enthält, wird es als „Ordner“ (expandierbarer Knoten) dargestellt.

  * **Ungeordnete Sammlungen:** Werden durch ein Standard-Ordnersymbol oder Aufzählungspunkte dargestellt. Untergeordnete Items besitzen keine sichtbaren Sequenznummern (`position = null`).
  * **Geordnete Sammlungen:** Werden durch ein spezielles „Sequenz“-Ordnersymbol (z. B. nummerierte Liste) dargestellt. Untergeordnete Items zeigen ihre Reihenfolge (`position`) deutlich neben ihrem Namen an.
  * **Standardzustand (ungeordnet):** Items, die innerhalb der Sammlung eines anderen Items platziert werden, sind standardmäßig ungeordnet. Sie werden durch ein normales Ordnersymbol oder Aufzählungspunkte dargestellt, ohne sichtbare Positionsnummern (`position = null`).
  * **Umwandlung in geordnet:** Eine ungeordnete Sammlung kann ausschließlich über eine Kontextmenü-Option am Parent-Node in eine geordnete Sequenz umgewandelt werden. Sobald sie geordnet ist (z. B. durch ein nummeriertes Listen-Icon gekennzeichnet), zeigen die Child-Items ihre jeweilige Reihenfolge (`position`) sichtbar neben ihrem Namen an.

## 4. Benutzerinteraktionen & Integration in den Datenfluss

### Primäre Tree-Navigation (Chevron vs. Node-Klick)

* **Sammlungen erweitern:** Da jedes Item potenziell selbst eine Sammlung sein kann und gleichzeitig eigene Inhalte/Daten besitzt, darf ein Klick auf den eigentlichen Node-Körper das Item **nicht** erweitern.

  * Ein Klick auf den Node-Körper dient ausschließlich dazu, die Details des Items auszuwählen, zu öffnen oder in einer Vorschau anzuzeigen (Inhalte und Metadaten).
  * Das Erweitern einer Sammlung, um Child-Items sichtbar zu machen, darf ausschließlich durch einen Klick auf das Chevron-/Pfeil-Icon neben dem Node ausgelöst werden.

### A. Drag-and-Drop-Neuanordnung (innerhalb einer geordneten Sammlung)

* **Aktion:** Der Benutzer zieht eine Übung innerhalb derselben geordneten Sammlung nach oben oder unten.
* **Vue DnD Kit:** Erkennt die Sortierinteraktion und berechnet den neuen Index.
* **Backend-Flow:** Löst `PUT /collections/{collectionId}/items/{itemId}` mit der neu berechneten `position` aus. Das Backend berechnet die Positionen der Geschwisterelemente automatisch neu.

### B. Verschieben von Items zwischen Sammlungen

* **Aktion:** Der Benutzer zieht ein Item aus Sammlung A und legt es in Sammlung B ab.
* **Vue DnD Kit:** Erkennt das Drop-Event über einem anderen Droppable-Container (Ordner).
* **Backend-Flow:** Löst `PUT /collections/items/{itemId}` mit der Ziel-`collectionId` aus. Falls Sammlung B geordnet ist, weist das Backend automatisch eine Position zu oder verwendet den Drop-Index.

### C. Übungen erweitern (horizontaler Vektor per Drag & Drop)

* **Aktion:** Der Benutzer zieht eine Übung direkt auf eine andere bestehende Basisübung (nicht auf einen Ordner).
* **Frontend-Logik:**

  1. Fordert den Benutzer zur Gruppierung auf oder erstellt automatisch eine neue Sammlung.
  2. `POST /items/{id}/collections` (erstellt eine Sammlung, die mit dem Basis-Item verbunden ist).
  3. `POST /collections/{collectionId}/items` (verschiebt das gezogene Item in die neue Sammlung).

### D. Reihenfolge einer Sammlung umschalten

* **Aktion:** Ein Switch, Kontrollkästchen oder eine Kontextmenü-Option an einem „Collection“-Node schaltet den sequenziellen Modus ein oder aus.
* **Backend-Flow:** Ruft `PUT /collections/{collectionId}` mit `{ order: true/false }` auf.
* **UI-Update:** Die UI blendet Positionsnummern der Child-Items dynamisch ein oder aus – abhängig vom neuen Zustand.

### E. Neue Einträge erstellen

* **Neue Übung:** Öffnet einen Dialog zur Definition von Content-Blöcken (`purpose`, `jsonContent`). Führt `POST /items` aus und fügt das Item entweder an der Root-Ebene oder innerhalb der aktuell aktiven Sammlung via `POST /collections/{collectionId}/items` ein.
* **Neue Sammlung:** Führt `POST /collections` aus und erstellt einen neuen Ordnerknoten in der Treeview.

## 5. Implementierungsaspekte

* **Draggable vs. Droppable:** Es muss zwischen Sortier-Kontexten (innerhalb derselben Sammlung) und Cross-Container-Drops (Verschieben in eine andere Sammlung) unterschieden werden. Die Sensoren von Vue DnD Kit sollten so konfiguriert werden, dass verschachtelte Droppable-Zonen (Ordner) unterstützt werden.

* **Optimistische UI-Updates:** Da Backend-Operationen – insbesondere Positions-Neuberechnungen – Zeit benötigen können, sollte der Vue-State (über Pinia oder lokalen Komponenten-State) die Struktur der Treeview unmittelbar nach einem Drop-Ereignis optimistisch aktualisieren. Nur bei einem API-Fehler sollte ein Rollback erfolgen.

* **Root-Items (Implementierung B für Sequenzen):** Falls Sequenzen über `rootItem` statt über explizite Sammlungen verwaltet werden, sollte das Verknüpfen eines Items mit einem anderen ein `PUT /items/{id}` auslösen, das die neue `rootItemId` setzt. Für die Darstellung könnten Items mit derselben `rootItem` unter einem virtuellen Ordnerknoten gruppiert werden.
