# Konzept: Aufgabenbeziehungen und Metadaten

Dieses Dokument fasst – nach Rücksprache mit Prof. Dr. Siepermann – unser Konzept für
die noch nicht (vollständig) umgesetzten Punkte zusammen: **Autor, Lizenz, Tags,
Validatoren, Modifikatoren**, die **Aufgabenbeziehungen** (vertikal/horizontal) sowie
**Collections** und den **Erstellungs-Workflow**.

## 1. Abgrenzung (Was unsere Anwendung leistet – und was nicht)

Die Aufgabendatenbank ist ein Werkzeug zum **Beschreiben, Strukturieren und Speichern**
von Aufgaben. Sie ist **kein** Lern- oder Abgabesystem.

- **In unserem Verantwortungsbereich:** Aufgaben erfassen, mit Metadaten (Autor, Lizenz,
  Tags, Typ) versehen, in Sequenzen und Varianten in Beziehung setzen sowie die
  zugehörigen Regeln (Validatoren) **definieren und speichern**.
- **Nicht in unserem Verantwortungsbereich:** die **Korrektur** bzw. das Lösen von
  Aufgaben durch Lernende. Die eigentliche Ausführung (Antwort prüfen, Varianten
  erzeugen) übernimmt ein **anderer Service**.
- **Zugriff:** In dieser Iteration gibt es **keine Rechteeinschränkung** – jeder Nutzer
  kann Aufgaben anlegen und bearbeiten (Vorgabe: „jeder kann alles machen“). Eine echte
  Anmeldung (THM-CAS) kommt später zentral; bis dahin wird der Nutzer gemockt.

## 2. Aufgabenbeziehungen (Kernpunkt)

Maßgeblich ist die Struktur des vorgegebenen Schemas: Eine **Reihenfolge** (`position`)
gibt es nur bei `item_collection_sub_item`, **nicht** bei `root_item_id`. Daraus ergibt
sich folgende Zuordnung:

### Vertikal: Sequenzen (Lernpfade) über geordnete Collections

Aufeinander aufbauende Aufgaben in **fester Reihenfolge** (Lernpfad).

> Beispiel-Pipeline: 1. Datenbank-Konzept → 2. SERM-Diagramm → 3. SQL-DDL →
> 4. Komplexe SQL-Abfrage.

- **Datenmodell:** `Item_Collection` mit `order = true` und
  `Item_Collection_Sub_Item.position` (1, 2, 3 …) – nur hier existiert eine echte
  Reihenfolge.
- **Darstellung:** als „Lernpfad" in der Strukturansicht; die Kinder zeigen ihre Position.

### Horizontal: Variationen & Restriktionen über `root_item_id` + Validator/Modifier

**Dieselbe** Problemstellung in **mehreren Ausprägungen** mit unterschiedlichen
methodischen Vorgaben – nebeneinander, ohne Reihenfolge.

> Beispiel (eine SQL-Abfrage): Variante A – zwingend mit `INNER JOIN`; Variante B – ohne
> verschachtelte Subqueries; Variante C – Ergebnis sortiert (`ORDER BY`).

- **Datenmodell:** `root_item_id` verbindet die zusammengehörigen Varianten (gemeinsames
  Root-Item); `Validator`/`Modifier` halten die jeweilige methodische Vorgabe.
- **Darstellung:** als Variantenbereich („Erweiterte Restriktionen") an der Aufgabe.

### Ungeordnete Collections (Sammlungen)

Eine **ungeordnete** Collection (`order = false`) ist eine einfache **Gruppierung** von
Aufgaben ohne Reihenfolge – z. B. ein thematisches „Übungsblatt".

- **Datenmodell:** `Item_Collection` mit `order = false` (Positionen `null`).
- **Darstellung:** als Ordner/Liste ohne Nummern.

## 3. Metadaten-Konzepte im Überblick

| Konzept | Bedeutung | Geplante Darstellung | Stand |
|---|---|---|---|
| **Autor** | Autor einer Aufgabe (frei wählbar **und** änderbar) | Auswahl im Editor, Default = aktueller (gemockter) Nutzer; Filter „Aufgaben eines Autors“ | Auswahl prototypisch umgesetzt |
| **Lizenz** | Nutzungsrechte – **jeder Inhalt** (`ItemContent`) hat eine Lizenz (Pflicht) | Auswahlfeld; Hinweis: `license` hat **kein** Beschreibungsfeld im Schema | Auswahl prototypisch umgesetzt |
| **Tag** | **Hierarchische** Schlagwörter, Pfad-Schreibweise `#DBS/Relationale DB/NFormen/NF3` | Baum-/Pfad-Auswahl, Chips, Filter; **Hierarchie ist sehr wichtig** | konzipiert |
| **Typ** | **Jedes Item** und **jeder ItemContent** bekommt einen Typ | Auswahl im Editor (Item-Typ und Inhalts-Typ) | Auswahl prototypisch umgesetzt |
| **Validator** | Prüf-/Restriktionsregel (Ausführung extern) | Bereich „Validatoren“: Beschreibung + Regeltext (Freitext), mehrere möglich | konzipiert |
| **Modifier** | Regel zur Erzeugung von Varianten | **vorerst zurückgestellt** (später, Umsetzung uns überlassen) | zurückgestellt |

**Zusätzlich (Vorgabe Prof.):** Nutzer müssen **neue** Autoren, Lizenzen, Tags und Typen
**anlegen** können – nicht nur aus festen Listen wählen.

## 4. Erstellungs-Workflow und Bearbeitung

### Aufgabe erstellen (Formular mit Bestätigung)

Derzeit wird eine Aufgabe **sofort** beim Klick auf „Aufgabe erstellen“ angelegt.
Geplant ist stattdessen ein bewusster Ablauf:

1. „Aufgabe erstellen“ öffnet ein **Formular** mit allen Angaben – Typ, Autor, Lizenz,
   Tags, Inhalte sowie Validatoren.
2. Erst mit **„Erstellen / Bestätigen“** wird die Aufgabe gespeichert; danach erscheint
   sie in der Strukturansicht links neben dem Editor.
3. Über die Strukturansicht lässt sie sich auswählen und bearbeiten.

**Wichtig für die erste Iteration:** Das Formular ist **nicht strikt**. Felder dürfen
leer bleiben, und die Aufgabe kann trotzdem bestätigt werden. Grund: Teile der
Funktionalität (z. B. das Hochladen von Dateien in allen Formaten) sind noch nicht
umgesetzt. Eine verpflichtende Vollständigkeitsprüfung würde das Erstellen aktuell
unnötig blockieren. Pflichtfelder können später ergänzt werden.

### Bearbeitung

Es gibt **keine** Rechteeinschränkung: **jeder kann alles bearbeiten** (Vorgabe Prof.).
Alle Felder – inklusive Autor – bleiben nach der Erstellung änderbar. Eine spätere
THM-CAS-Anmeldung kann darauf bei Bedarf Rechte aufsetzen.

## 5. Use Cases

1. **Aufgabe erstellen und beschreiben:** Nutzer legt eine Aufgabe an, wählt Typ, Autor,
   Lizenz und vergibt Tags (oder legt neue Tags/Lizenzen/Autoren an).
2. **Inhalte hinzufügen:** Inhaltsbausteine (Text, Bild, PDF) mit einem Zweck
   (Aufgabenstellung, Hinweis …) und je einer Lizenz ergänzen.
3. **Vertikale Sequenz (Lernpfad) bilden:** Aufgaben in einer **geordneten** Collection
   in Reihenfolge anordnen (1, 2, 3 …).
4. **Horizontale Variante anlegen:** Zu einer Aufgabe eine Variante mit Restriktion
   definieren (gemeinsames `root_item_id`, Validator „muss `INNER JOIN` enthalten“).
5. **Sammlung anlegen:** Aufgaben in einer **ungeordneten** Collection gruppieren.
6. **Wiederfinden:** Aufgaben über Tags, Typ oder Autor filtern und suchen.

## 6. Frontend-Skizze

**Editor einer Einzelaufgabe** (Metadaten + Inhalte + horizontale Restriktionen):

```
┌─ Aufgabe-Editor ───────────────────────────────────────────┐
│ Typ:[SQL-Abfrage ▾] Autor:[Siepermann ▾] Lizenz:[CC-BY ▾]   │  Metadaten
│ Tags: [#DBS/Joins ✕] [+]                                    │
├────────────────────────────────────────────────────────────┤
│ Inhalte: • Aufgabenstellung (Text / PDF / Bild)  Lizenz:▾   │  Inhaltsbausteine
│          • Hinweis …                                        │
├────────────────────────────────────────────────────────────┤
│ Validatoren (Restriktion, wird extern ausgeführt):          │  horizontal
│   ▸ Beschreibung:[ muss INNER JOIN enthalten ]  Regel:[ … ] │
│   (Modifier: später)                                        │
└────────────────────────────────────────────────────────────┘
```

**Strukturansicht** (geordnete Collection = Lernpfad; ungeordnete = Sammlung):

```
📁 Lernpfad „SQL-Grundlagen" (Collection, geordnet)
    1. Datenbank-Konzept   2. SERM-Diagramm   3. SQL-DDL   4. SQL-Abfrage

📁 Sammlung „Übungsblatt 1" (Collection, ungeordnet)
    • Aufgabe A   • Aufgabe B   • Aufgabe C
```

## 7. Stand der Klärung

**Bereits geklärt (mit Prof.):**
- Korrektur/Lösen ist **nicht** unsere Aufgabe; Validatoren werden nur **definiert und
  gespeichert**, extern ausgeführt.
- **Jeder kann alles** anlegen und bearbeiten (keine Rechteeinschränkung).
- **Tags hierarchisch**, Nutzer dürfen eigene anlegen.
- **Modifier** vorerst zurückgestellt.
- Vertikal (geordnete Sequenz) = **geordnete Collection** (`position`); horizontal
  (Varianten) = `root_item_id` + Validator/Modifier; **ungeordnete Collection** = Sammlung.

**Noch offen:**
- **Template** (`item_representation_template`): legt fest, wo/wie die Inhalte einer
  Aufgabe dargestellt werden – Konzept noch nicht abschließend verstanden (eigenes
  Ticket).
- **Lizenz-Ebene:** primär am Inhalt (`ItemContent`) – soll die Aufgabe (`Item`)
  zusätzlich eine Lizenz tragen?
- **Validator-Format:** Freitext für den Anfang (Umsetzung ist uns überlassen).
