# Representation Template — Analyse & Brainstorming

## 1. Aktuelle Lage

### Datenbank (unveränderbar — nur Datatype-Änderungen erlaubt)
```
item_representation_template
├── item_template_id  UUID  PK
└── template           TEXT  NOT NULL
```

Tabelle existiert, FK via `item.item_template_id` (ON DELETE SET NULL).
Keine neuen Spalten/Tabellen. Das `template`-TEXT-Feld ist der **einzige Datencontainer**.

### Backend
- **Entity** `ItemRepresentationTemplate` — `itemTemplateId` + `template` (String).
- **Item → @ManyToOne → ItemRepresentationTemplate** — optional, nullable.
- **DTOs**: `itemTemplateId` (UUID) in Response + Create.
- **Service**: mapped sauber, aber gibt nur die ID weiter — nie den Template-Inhalt.
- **Kein Controller** für Template-CRUD.

### Frontend
- **`Item.representationTemplate: string | null`** — speichert nur die UUID.
- **Store**: mapped `dto.itemTemplateId → representationTemplate`, setzt bei Neuanlage immer `null`.
- **Kein UI**: Keine Auswahl, keine Vorschau, keine Verwaltung.

### Fazit
Das Template ist **komplett verdrahtet aber funktionslos**.

---

## 2. Was soll ein Representation Template tun?

Das Template beschreibt, **wie die Contents eines Items angeordnet und strukturiert werden** — unabhängig von der konkreten Frontend-Implementierung.

### Aktuelle Content-Struktur (pro Item)
- Contents über `item_contents`-Join mit `purpose` (z. B. `"Aufgabenstellung"`, `"Hinweis"`, `"Lösung"`).
- Jeder Content hat `jsonContent` (JSONB) oder `blobContent` (BYTEA).
- Aktuelle Darstellung: flache Liste, Purpose als Header.

### Was das Template steuern soll
- **Reihenfolge** der Content-Blöcke (Purpose A vor Purpose B)
- **Verschachtelung/Struktur** (z. B. Lösung in einem `<details>`-Element)
- **Semantische Auszeichnung** (das Template sagt: "hier kommt ein Hinweis" — nicht "hier kommt Component X")
- Keine Frontend-Component-Namen, keine Implementierungsdetails

---

## 3. Ansatz: XML als reines Struktur-Format

### 3.1 Philosophie

Das Template ist ein XML-Dokument, das die **logische Struktur** eines Exercises beschreibt. Elementnamen sind semantisch (nicht implementierungs-spezifisch). Das `purpose`-Attribut verweist auf die Content-Blöcke des Items.

```xml
<exercise>
  <title />
  <task />
  <hint />
  <solution />
</exercise>
```

Bedeutung: "Zeige zuerst den Titel, dann die Aufgabe, dann einen Hinweis, dann die Lösung."

Der Elementname `title`, `task`, `hint`, `solution` ist die **semantische Rolle**. Das Frontend mapped diese auf passende Darstellungen (title → große Überschrift, hint → Callout-Box, solution → einklappbarer Bereich).

### 3.2 Purpose-Matching

Elemente referenzieren Content-Blöcke über deren `purpose`-Feld aus der `item_contents`-Join-Tabelle. Zwei Varianten:

**A) Elementname = Purpose** (einfach, wenn Namen standardisiert)
```xml
<exercise>
  <Aufgabenstellung />
  <Hinweis />
  <Lösung />
</exercise>
```

**B) `purpose`-Attribut** (flexibler, Elementname = Darstellungshinweis)
```xml
<layout>
  <section purpose="Aufgabenstellung" />
  <callout purpose="Hinweis" />
  <collapsible purpose="Lösung" />
</layout>
```

**Empfohlen: Variante B.** Elementname gibt dem Frontend einen Render-Hinweis (`section`, `callout`, `collapsible`), `purpose` matched den Content.

### 3.3 Beispiel-Templates

**Vertikales Layout (Default — wie aktuell):**
```xml
<layout>
  <section purpose="Aufgabenstellung" />
  <section purpose="Hinweis" />
  <section purpose="Lösung" />
</layout>
```

**Aufgabe mit ausgeklappter Lösung:**
```xml
<layout>
  <section purpose="Aufgabenstellung" />
  <callout purpose="Hinweis" />
  <section purpose="Lösung" />
</layout>
```

**Einklappbare Lösung:**
```xml
<layout>
  <section purpose="Aufgabenstellung" />
  <callout purpose="Hinweis" />
  <details>
    <summary>Lösung anzeigen</summary>
    <section purpose="Lösung" />
  </details>
</layout>
```

**Tabs:**
```xml
<tabs>
  <tab purpose="Aufgabenstellung" />
  <tab purpose="Hinweis" />
  <tab purpose="Lösung" />
</tabs>
```

### 3.4 Validierung

Optional: XSD für die Template-XMLs, um sicherzustellen, dass nur bekannte Elemente/Attribute verwendet werden.

---

## 4. Backend-Änderungen

### 4.1 CRUD-Controller (kein Schema-Change)

```java
@RestController
@RequestMapping("/api/representation-templates")
public class RepresentationTemplateController {
    // GET    → alle Templates (id + raw XML aus template-Spalte)
    // GET /{id} → eines
    // POST   → anlegen (XML-String wird validiert)
    // PUT /{id} → updaten
    // DELETE /{id} → löschen
}
```

Das Backend behandelt den `template`-String als opaque und validiert nur auf XML-Well-Formedness. Die Bedeutung interpretiert einzig das Frontend.

### 4.2 DTOs

```java
public class ReprTemplateResponseDto {
    private UUID id;
    private String templateXml;       // roher XML-String aus DB
}

public class ReprTemplateCreateDto {
    @NotEmpty
    private String templateXml;
}
```

### 4.3 Item-Endpunkt optional ergänzen

`ItemResponseDto` könnte zusätzlich das `templateXml` des verknüpften Templates liefern, damit das Frontend keinen Extra-Call braucht:

```java
// Neu in ItemResponseDto:
private String templateXml;
```

---

## 5. Frontend-Änderungen

### 5.1 Template-Registry (kein Component-Name mehr!)

Das Frontend mapped XML-Elemente auf Darstellungslogik, **nicht auf Component-Namen aus dem Backend**:

```typescript
// Muster: XML-Element → Render-Funktion/Component
// Hart im Frontend codiert — keine Backend-Kopplung
type Renderer = (content: Content, options?: object) => VNode

const renderers: Record<string, Renderer> = {
  section:     (c) => h('div', { class: 'section' }, renderContent(c)),
  callout:     (c) => h('div', { class: 'callout' }, renderContent(c)),
  details:     (c) => h('details', {}, [h('summary', 'Lösung'), renderContent(c)]),
  tabs:        /* Tab-Container */
  tab:         /* einzelner Tab */
  collapsible: /* einklappbarer Bereich */
}
```

### 5.2 Template-Parser

```typescript
interface TemplateNode {
  tag: string
  purpose?: string
  children: TemplateNode[]
  attributes: Record<string, string>
}

function parseTemplateXml(xml: string): TemplateNode {
  // DOMParser → Rekursive Knoten
}
```

### 5.3 Datenfluss

```
1. Seed: INSERT INTO item_representation_template (template) VALUES ('<layout>...</layout>')
2. GET /api/representation-templates → [{ id, templateXml: "<layout>..." }]
3. Frontend parses XML → extrahiert Anzeigename (erster <name>-Tag oder hartcodiert)
4. Dropdown zeigt verfügbare Templates (User wählt)
5. Item.representationTemplate = template ID
6. Rendering: resolveTemplate(item.representationTemplate)
   → parseTemplateXml(templateXml) → recursive render mit renderer-registry
```

### 5.4 Neue/Komponenten

**`RepresentationRenderer.vue`** — parses Template-XML, traversiert Knoten, rendert mit Registry.

**`exerciseStore.ts`** — + `loadRepresentationTemplates()`, + parsed `templateMetas`.

**`AdbEditor.vue`** — Dropdown "Darstellung" im Meta-Bereich.

### 5.5 Anzeigename fürs Dropdown

Da die DB nur `template` (XML) hat, braucht das Frontend einen Namen für die Dropdown-Liste:

**Option 1:** Erstes `<name>`-Element im XML
```xml
<layout>
  <name>Vertikales Layout</name>
  <description>Standard: alle Inhalte untereinander</description>
  <section purpose="Aufgabenstellung" />
  ...
</layout>
```

**Option 2:** Frontend-hardcodierte Namen per Template-ID (einfach, aber starr)

**Option 3:** `template`-Attribut `display-name`
```xml
<layout display-name="Tabs">
  ...
</layout>
```

**Empfohlen: Option 1** — selbstbeschreibend, kein Schema-Change, flexibel.

---

## 6. Seed-Daten (init.sql — kein Schema-Change!)

```sql
INSERT INTO item_representation_template (template) VALUES
  ('<layout><name>Vertikales Layout</name><section purpose="Aufgabenstellung"/><section purpose="Hinweis"/><section purpose="Lösung"/></layout>'),
  ('<layout><name>Tabs</name><tabs><tab purpose="Aufgabenstellung"/><tab purpose="Hinweis"/><tab purpose="Lösung"/></tabs></layout>'),
  ('<layout><name>Accordion</name><section purpose="Aufgabenstellung"/><section purpose="Hinweis"/><details><summary>Lösung anzeigen</summary><section purpose="Lösung"/></details></layout>'),
  ('<layout><name>Side-by-Side</name><columns><column><section purpose="Aufgabenstellung"/></column><column><section purpose="Hinweis"/><section purpose="Lösung"/></column></columns></layout>');
```

---

## 7. Konkrete Änderungen (kein Schema-Change)

| Schicht | Änderung |
|---------|----------|
| DB | Seed-INSERTs in `init.sql` |
| Backend | Neuer Controller + DTOs für Templates |
| Backend | `ItemResponseDto`: optional `templateXml` |
| Frontend | `api-adapter.types.ts`: + `ReprTemplateDTO` |
| Frontend | API-Adapter: `getRepresentationTemplates()` |
| Frontend | Store: `loadRepresentationTemplates()`, `templates` State |
| Frontend | Neu: `RepresentationRenderer.vue` (XML-Parser + Registry) |
| Frontend | Neu: Layout-Interpretation (keine Component-Namen!) |
| Frontend | `AdbEditor.vue`: Dropdown "Darstellung" |

---

## 8. Vorteile dieses Ansatzes

1. **Entkoppelt:** Das XML beschreibt **was** (Struktur), nicht **wie** (Implementation). Das Frontend kann die XML vollständig anders rendern, ohne dass das Backend davon weiß.

2. **Zukunftssicher:** Gleiche XML könnte für HTML-Rendering, PDF-Generierung oder eine API-Response verwendet werden.

3. **Schema-konform:** Nur das bestehende `template`-TEXT-Feld wird genutzt.

4. **Erweiterbar:** Neue XML-Elemente können im Frontend registriert werden, ohne Backend-Änderung.

5. **Purpose-basiert:** Die `purpose`-Attribute matchen direkt auf die `item_contents`-Join-Tabelle — kein neues Datenmodell nötig.
