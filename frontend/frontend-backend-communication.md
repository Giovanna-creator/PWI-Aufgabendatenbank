

Daten für Frontend laden

# 1.GET Items Strategie

## Müssen alle root_items geladen werden

GET /api/items/root?=true (nicht vorhanden)


ob Item ein Collection ist

## Wenn Item Collection ist

rekursiv children laden

GET /api/collections/{id}/items

order mitgeladet wird (beeinflusst ui)

position wird auch mitgeladet 

muss wie GET /api/items/root?=true funktionieren, isCollection muss mitgeschickt werden

## Item_Content werden pro Aufgabe sofort geschickt 

# Aufgabe addieren Button (root)



POST /api/items 
# Collection addieren Button (root)

POST /api/collections (Logik nicht vorhanden, die Rute ist eher für Umwandlung)

Erstellt Item das auch Collection ist

erst Item erstellen, darunter Collection


## Kollektion Umwandlung:

POST /api/items/{id}/collection (vorhanden aber andere Rute)


# Items zu Kollektion addieren

POST /api/collection/{id}/items (nicht vorhanden)

es geht um Sub_Items von db_schema

wenn Kollektion geordnet war muss bei dem Backend ordering korrigiert werden
+reordering requests wenn Kollektion geordnet war

# Items bei der Kollektion löschen

es geht um Sub_Items von db_schema

DELETE /api/items/{id}

Müssen auch alles was davon abhängig gelöscht werden

Nämlich Item_Contents und Collections

(Beziehung mit Collection bei Items Erstellen) OneToOne

wenn Kollektion geordnet war muss bei dem Backend ordering korrigiert werden
+reordering requests wenn Kollektion geordnet war

# ORDER/NO ORDER


## 1. ORDER bei einer Kollektion ändern

PUT /api/collections/{id}/order
order = true 


Bei dem Backend muss zufällige/nach Erstellungszeit die Positionen gegeben (so behebt man das Problem, wenn Zwischenstand fehlerhaft sein kann (es alles wenn position nicht vorhanden ist)) 
(order wird mitgeschickt)

REORDERING

PUT /api/collection/{id}/items/{id}         : x mal
mit position
muss geprüft werden ob Collection order=true hat
mit position Änderung 


## ORDER Löschen

PUT /api/collections/{id}

order = false

(BACKEND kümmert selbst um das Löschen der Positionen

BACKEND setzt null) -- VERALTET, UNTEN ERKLÄRUNG

muss man nicht. können die Items Positions behalten, es wird beim order=false einfach im Frontend nicht angezeigt


------------------------------------------



# Strukturierung der Items


# Unabhängiges Item -> Kollektion

heißt Item hat kein parent oder root_item_id

POST /api/collection/{id}/items

root_item bei dem Item wird gesetzt

es wird sub_item erstellt mit id von dem Collection und dem Item

# Item von anderen Kollektion-> Kollektion

### 1. Beziehung löschen
Es geht nur um Beziehung, für das Löschen der Items ist andere Rute reserviert

DELETE /api/collections/{id}/items/{id}

das Item in dem Fall muss root_item werden

bei dem Item wird root_item_id gelöscht

sub_item wird gelöscht (es geht nur um Beziehung)


### 2. Neue Beziehung erstellen

(es war schon bei: 
# Unabhängiges Item -> Kollektion

heißt Item hat kein parent oder root_item_id

POST /api/collection/{id}/items

root_item bei dem Item wird gesetzt

es wird sub_item erstellt mit id von dem Collection und dem Item



)


# Unabhängige Kollektion (root) unter andere Kollektion schieben


Erst Item(ist Kollektion) zu der Kollektion addieren

POST /api/collection/{id}/items

wie bei andere Items. 

da muss aber das Backend (iterativ/rekursiv) sich darum kümmern, dass die Items, die die Kollektion hat, richtiges root_item_id von der neuen Kollektion bekommen. Falls die Kollektion geordenet war, muss dem Item(Kollektion) weitere Position geordnet werden

Danach Falls die Kollektion geordenete war, muss noch von Frontend nochmals die Reordering Strategie erfolgen

Begründung:

darum muss Backend sich kümmern, sonst können nicht erlaubte Verbindungen entstehen, weil während der Veränderung können Zwischenständen der Bäumen existieren, wobei die Items nicht richtige root_item_id haben. 


und 

Sub item wird erstellt das Subitem für das Item(is Collection)

# bei Abhängigen (nicht root) Kollektion

so wie bei unabhängige müssen aber erst überall root_item_id auf null gesetzt werden und sub_item bezehung muss gelöscht werden. Als wird das Item(Kollektion) erst unbhängig (root)

DELETE /api/collections/{alterParenTId}/items/{itemId} 

reorderings strategien müssen da auch berücksichtigt werden


# *PRÄSI*


1. Anforderungen
2. Tech Stack
3. Datenbankschema
4. Stand, Auflistung der Features, Welche Features noch nicht
5. Strategien Visualisation Item Verbindung

