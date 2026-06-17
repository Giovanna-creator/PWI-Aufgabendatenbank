# Konzept: Aufgabenbeziehungen und Metadaten

Dieses Dokument beschreibt unser Verständnis der noch offenen Anforderungen und wie
wir sie im Frontend abbilden möchten. Es dient als Diskussionsgrundlage für die
Abstimmung mit Prof. Dr. Siepermann. Betrachtet werden die Punkte, die bisher noch
nicht umgesetzt sind: **Autor, Lizenz, Tags, Validatoren, Modifikatoren** sowie die
**vertikalen und horizontalen Aufgabenbeziehungen**.

## 1. Abgrenzung (Was unsere Anwendung leistet – und was nicht)

Die Aufgabendatenbank ist ein Werkzeug zum **Beschreiben, Strukturieren und Speichern**
von Aufgaben. Sie ist **kein** Lern- oder Abgabesystem.

- **In unserem Verantwortungsbereich:** Aufgaben erfassen, mit Metadaten (Autor,
  Lizenz, Tags) versehen, in Sequenzen und Varianten in Beziehung setzen sowie die
  zugehörigen Prüf- und Anpassungsregeln (Validator/Modifier) **definieren und
  speichern**.
- **Nicht in unserem Verantwortungsbereich:** die **Korrektur** bzw. das Lösen von
  Aufgaben durch Lernende. Die eigentliche Ausführung der Validatoren und Modifikatoren
  (Antwort prüfen, Varianten generieren) übernimmt ein **anderer Service** der
  Plattform.

## 2. Aufgabenbeziehungen (Kernpunkt)

Laut Pflichtenheft (Abschnitt 3.1) gibt es zwei Arten von Abhängigkeiten:

### Vertikal: Sequenzen (Lernpfade)

Aufeinander aufbauende Aufgaben, die in einer **festen Reihenfolge** durchlaufen werden.

> Beispiel-Pipeline: 1. Datenbank-Konzept entwerfen → 2. SERM-Diagramm zeichnen →
> 3. SQL-DDL schreiben → 4. Komplexe SQL-Abfrage formulieren.

- **Datenmodell:** `Item_Collection` / `Item_Collection_Sub_Item` mit `position`
  (geordnete Sammlung).
- **Darstellung:** als Baum bzw. „Lernpfad" in der Strukturansicht; die Kinder einer
  geordneten Sequenz zeigen ihre Position (1, 2, 3 …) neben dem Namen.

### Horizontal: Variationen & Restriktionen

**Dieselbe** Problemstellung in **mehreren Ausprägungen** mit unterschiedlichen
methodischen Vorgaben – nebeneinander, nicht aufeinander aufbauend.

> Beispiel (eine SQL-Abfrage): Variante A – zwingend mit `INNER JOIN`; Variante B –
> ohne verschachtelte Subqueries; Variante C – Ergebnis sortiert (`ORDER BY`).

- **Datenmodell:** `Modifier` und `Validator` (für die methodischen Vorgaben), `root_item_id`
  (zum Verbinden zusammengehöriger Varianten).
- **Darstellung:** als Konfigurationsbereich („Erweiterte Restriktionen") direkt an einer
  Einzelaufgabe.

> **Hinweis:** Die horizontalen Abhängigkeiten **sind** genau die Validatoren und
> Modifikatoren. Ein Validator steht hier typischerweise für eine **Restriktion**
> („muss `INNER JOIN` enthalten"), ein Modifier für die **Erzeugung einer Variante**.

## 3. Die fünf Konzepte im Überblick

| Konzept | Bedeutung | Geplante Darstellung | Stand |
|---|---|---|---|
| **Autor** | Wer die Aufgabe erstellt hat | „Erstellt von …"; Auswahl im Editor; Filter „Aufgaben eines Autors" | Auswahl prototypisch umgesetzt |
| **Lizenz** | Nutzungsrechte (z. B. CC-BY, MIT, Internal-THM) | Auswahlfeld im Editor + kleines Badge an der Aufgabe | Auswahl prototypisch umgesetzt |
| **Tag** | Hierarchische thematische Schlagwörter (SQL → Joins → INNER JOIN) | Mehrfachauswahl als Baum, Anzeige als Chips, Filter nach Thema | konzipiert |
| **Validator** | Prüf-/Restriktionsregel zur Aufgabe (Ausführung extern) | Bereich „Validatoren": Beschreibung + Regeltext, mehrere möglich | konzipiert |
| **Modifier** | Regel zur Erzeugung von Varianten (Ausführung extern) | Bereich „Modifikatoren": Beschreibung + Regeltext, mehrere möglich | konzipiert |

## 4. Erstellungs-Workflow und Bearbeitung

### Aufgabe erstellen (Formular mit Bestätigung)

Derzeit wird eine Aufgabe **sofort** beim Klick auf „Aufgabe erstellen" angelegt.
Geplant ist stattdessen ein bewusster Ablauf:

1. „Aufgabe erstellen" öffnet ein **Formular** mit allen Angaben – Typ, Autor, Lizenz,
   Tags, Inhalte sowie Validatoren und Modifikatoren.
2. Erst mit **„Erstellen / Bestätigen"** wird die Aufgabe gespeichert; danach erscheint
   sie in der Strukturansicht links neben dem Editor.
3. Über die Strukturansicht lässt sie sich auswählen und – eingeschränkt – bearbeiten.

**Wichtig für die erste Iteration:** Das Formular ist **nicht strikt**. Felder dürfen
leer bleiben, und die Aufgabe kann trotzdem bestätigt werden. Grund: Teile der
Funktionalität (z. B. das Hochladen von Dateien in allen Formaten) sind noch nicht
umgesetzt. Eine verpflichtende Vollständigkeitsprüfung würde das Erstellen aktuell
unnötig blockieren. Pflichtfelder können später ergänzt werden.

### Bearbeitung nach der Erstellung

Welche Felder nach der Erstellung änderbar sind, richtet sich nach **zwei voneinander
unabhängigen** Regeln:

- **Fachliche Unveränderlichkeit (unabhängig von der Anmeldung):** Der **Autor
  (Ersteller)** und das Erstellungsdatum sind historische Angaben und nach der
  Erstellung **schreibgeschützt**.
- **Bearbeitungsrechte (später über die Anmeldung):** Wer eine bestehende Aufgabe ändern
  darf, wird später über die zentrale THM-CAS-Anmeldung geregelt – voraussichtlich nur
  der Ersteller bzw. eine berechtigte Rolle, alle anderen nur lesend. Solange die
  Anmeldung gemockt ist, sehen wir bereits einen Lese-/Bearbeitungsmodus vor, den die
  Anmeldung später nur noch zuweisen muss.

| Feld | Nach Erstellung änderbar? | Wer (später, mit Anmeldung) |
|---|---|---|
| **Autor** (Ersteller) | nein – schreibgeschützt | – |
| Lizenz / Tags / Typ / Inhalte | ja | Ersteller bzw. berechtigte Rolle |

## 5. Use Cases

1. **Aufgabe erstellen und beschreiben:** Eine Lehrkraft legt eine Aufgabe an, wählt
   Typ, Autor und Lizenz und vergibt Tags.
2. **Inhalte hinzufügen:** Zu einer Aufgabe werden Inhaltsbausteine (Text, Bild, PDF)
   mit einem Zweck (Aufgabenstellung, Hinweis …) ergänzt.
3. **Sequenz bilden (vertikal):** Mehrere Aufgaben werden zu einem geordneten Lernpfad
   zusammengefügt und in der Reihenfolge angeordnet.
4. **Variante/Restriktion anlegen (horizontal):** Zu einer Aufgabe wird eine Variante
   mit einer methodischen Vorgabe definiert (z. B. „muss `INNER JOIN` enthalten").
5. **Wiederfinden:** Aufgaben werden über Tags, Typ oder Autor gefiltert und gesucht.

## 6. Frontend-Skizze

**Editor einer Einzelaufgabe** (Metadaten + Inhalte + horizontale Abhängigkeiten):

```
┌─ Aufgabe-Editor ───────────────────────────────────────────┐
│ Typ:[SQL-Abfrage ▾] Autor:[Siepermann ▾] Lizenz:[CC-BY ▾]   │  Metadaten
│ Tags: [SQL ✕] [Joins ✕] [+]                                 │
├────────────────────────────────────────────────────────────┤
│ Inhalte: • Aufgabenstellung (Text / PDF / Bild)             │  Inhaltsbausteine
│          • Hinweis …                                        │
├────────────────────────────────────────────────────────────┤
│ Validatoren (Restriktion, wird extern ausgeführt):          │  horizontal
│   ▸ Beschreibung:[ muss INNER JOIN enthalten ]              │
│     Regel:[ … ]                                             │
│ Modifikatoren (Varianten-Regel):                            │
│   ▸ Beschreibung:[ Zahlenwert variieren ]                   │
│     Regel:[ … ]                                             │
└────────────────────────────────────────────────────────────┘
```

**Strukturansicht** (vertikale Sequenzen als Lernpfad):

```
📁 SQL-Grundkurs (geordnete Sequenz)
   1. Datenbank-Konzept entwerfen
   2. SERM-Diagramm zeichnen
   3. SQL-DDL schreiben
   4. Komplexe SQL-Abfrage
```

## 7. Offene Fragen zur Abstimmung

1. **Validatoren/Modifikatoren – Umfang:** Wir gehen davon aus, dass wir die Regeln
   nur **definieren und speichern**, während die Ausführung (Korrektur, Varianten-
   erzeugung) in einem anderen Service liegt. Ist das so richtig?
2. **Format der Regeln:** In welcher Form soll eine Validator-/Modifier-Regel erfasst
   werden (Freitext, JSON, eine festgelegte Syntax)? Da ein anderer Service sie
   ausführt, brauchen wir hier ein gemeinsames Format. Bis dahin würden wir ein
   **Freitextfeld** vorsehen.
3. **Typabhängigkeit:** Sollen Validatoren/Modifikatoren **je Aufgabentyp**
   unterschiedlich dargestellt werden, oder genügt eine generische Regel-Eingabe?
4. **Tags:** Sind die Tags **vorgegeben** (festes hierarchisches Vokabular), oder dürfen
   Autorinnen und Autoren eigene Tags anlegen?
5. **Autor/Anmeldung:** Bis zur zentralen THM-CAS-Anmeldung mocken wir den
   angemeldeten Benutzer. Ist das für die erste Iteration in Ordnung?
6. **Autor & Bearbeitungsrechte:** Soll der Autor nach der Erstellung unveränderlich
   sein (= Ersteller)? Und wer darf eine bestehende Aufgabe bearbeiten – nur der
   Ersteller bzw. eine bestimmte Rolle?
