# Representation Template — Implementation Plan (Phase 1)

## Ziel
Das Template definiert die **Anzeigereihenfolge der Contents** eines Items. Purposes werden laut `<purpose>`-Elementen im Template sortiert.

---

## 1. Template-XML (im DB-Feld `template`)

```sql
INSERT INTO item_representation_template (template) VALUES
  ('<layout><purpose>Aufgabenstellung</purpose><purpose>Hinweis</purpose><purpose>Lösung</purpose></layout>');
```

Jeder `<purpose>`-Eintrag matcht auf den `purpose`-String aus `item_contents`. Contents deren Purpose nicht im Template steht → **ans Ende**.

---

## 2. Backend

### 2.1 Service: `RepresentationTemplateService`

Analog zu `ValidatorService`. CRUD auf `ItemRepresentationTemplateRepository`.

### 2.2 Controller: `RepresentationTemplateController`

| Methode | Pfad | Aufgabe |
|---------|------|---------|
| GET | `/api/representation-templates` | Alle |
| GET | `/{id}` | Eines |
| POST | `/` | Anlegen |
| PUT | `/{id}` | Updaten |
| DELETE | `/{id}` | Löschen |

### 2.3 DTOs

```java
public class ReprTemplateResponseDto {
    private UUID id;
    private String template;  // entspricht DB-Spaltenname
}
public class ReprTemplateCreateDto {
    @NotEmpty private String template;
}
```

### 2.4 Item-Endpunkt — nicht ändern

`ItemResponseDto` behält nur `itemTemplateId` (UUID). Das Frontend resolved die UUID gegen den lokalen Template-Cache (siehe 3.3). Keine neue Kopplung, kein Overfetching.

---

## 3. Frontend

### 3.1 Template-XML parsen

```typescript
// representation/parseOrderXml.ts
export function parseOrderXml(xml: string): string[] {
  const matches = [...xml.matchAll(/<purpose>(.*?)<\/purpose>/g)]
  return matches.map(m => m[1])
}
```

### 3.2 Contents sortieren

```typescript
// representation/applyTemplateOrder.ts
import type { Content } from '@/lib/types'

export function applyTemplateOrder(
  contents: Content[],
  template: string | null
): Content[] {
  if (!template) return contents
  const order = parseOrderXml(template)
  const ordered = order
    .map(p => contents.find(c => c.purpose === p))
    .filter((c): c is Content => c != null)
  const remaining = contents.filter(c => !order.includes(c.purpose))
  return [...ordered, ...remaining]
}
```

### 3.3 Template-Cache im Store

Templates werden **beim App-Start wie Referenzdaten** geladen (analog `loadReferenceData`):

```typescript
// exerciseStore.ts
state: {
  templates: ReprTemplateDTO[],   // Template-Cache
}

getters: {
  templateById: (state) => {
    const map: Record<string, string | null> = {}
    for (const t of state.templates) map[t.id] = t.template
    return (id: string | null) => (id ? map[id] ?? null : null)
  }
}

actions: {
  async loadRepresentationTemplates() {
    this.templates = await _adapter!.getRepresentationTemplates()
  }
}
```

### 3.4 AdbContentList.vue ändern

```vue
<script setup lang="ts">
import { applyTemplateOrder } from '../representation/applyTemplateOrder'

const orderedContents = computed(() => {
  const item = store.selectedInnerItem
  if (!item) return []
  const template = store.templateById(item.representationTemplate)
  return applyTemplateOrder(item.contents ?? [], template)
})
</script>

<!-- Template-Referenzen wie zuvor -->
<AdbContentEditor
  v-for="(content, index) in orderedContents"
  :key="content.id ?? index"
  :content="content"
  :index="itemIndex(index)"
  @update:text="(val) => store.updateContentText(realIndex(index), val)"
  @update:purpose="(val) => store.updateContentPurpose(realIndex(index), val)"
  @update:meta="(m) => store.updateContentMeta(realIndex(index), m)"
  @delete="store.removeContentFromSelectedItem(realIndex(index))"
/>
```

**Wichtig:** Die Events feuern auf den `realIndex` (Position im original `item.contents`), nicht im sortierten Array. Sonst wird der falsche Content editiert.

```typescript
function itemIndex(sortedIndex: number): number {
  const item = store.selectedInnerItem
  if (!item) return sortedIndex
  return item.contents.indexOf(orderedContents.value[sortedIndex])
}
// realIndex = Alias für itemIndex
```

### 3.5 API-Adapter ergänzen

```typescript
interface ApiAdapter {
  getRepresentationTemplates(): Promise<ReprTemplateDTO[]>
}
```

### 3.6 Dev-Mock

Dev-Adapter mapped `item.representationTemplate` (UUID) → findet Template im Cache → liefert `template`-String.

### 3.7 Dropdown (später, Phase 1 optional)

```vue
<AdbRefSelect
  :model-value="inner.representationTemplate"
  :items="store.templateDropdownItems"
  label="Darstellung"
  @update:model-value="onTemplateChange"
/>
```

Die Namen fürs Dropdown müssen aus dem XML extrahiert werden. Ohne Namensspalte in der DB:
- Erstes `<name>`-Element im XML
- Oder hartcodiertes Mapping im Frontend (einfach für Phase 1)

---

## 4. Seed-Daten (init.sql)

```sql
INSERT INTO item_representation_template (template) VALUES
  ('<layout><purpose>Aufgabenstellung</purpose><purpose>Hinweis</purpose><purpose>Lösung</purpose></layout>'),
  ('<layout><purpose>Aufgabenstellung</purpose><purpose>Lösung</purpose></layout>'),
  ('<layout><purpose>Lösung</purpose><purpose>Aufgabenstellung</purpose></layout>');
```

---

## 5. Fallback

`representationTemplate = null` → `applyTemplateOrder(contents, null)` → unveränderte Reihenfolge.

---

## 6. SOLID-Check

| Prinzip | Status |
|---------|--------|
| **SRP** | ✅ `parseOrderXml`, `applyTemplateOrder`, `templateById`-Getter — alle eine Sache |
| **OCP** | ✅ Neues XML-Element (`<row>`) → neuer Match im Regex bzw. erweiterter Parser, kein Backend-Touch |
| **LSP** | ✅ Keine Vererbung im Plan |
| **ISP** | ✅ `ReprTemplateDTO` schlank, `ApiAdapter` kriegt nur eine Methode mehr |
| **DIP** | ✅ `RepresentationTemplateService` analog zu `ValidatorService`, DI via Konstruktor |

## 7. Datenfluss zusammengefasst

```
App-Start
  → loadRepresentationTemplates()  → Cache: templates: ReprTemplateDTO[]
  → loadReferenceData()

User klickt Item
  → selectItem() → loadItemContent(id) → item.contents gefüllt
  → AdbContentList.computed:
      template = store.templateById(item.representationTemplate)
      orderedContents = applyTemplateOrder(item.contents, template)

User wählt Template (Dropdown)
  → updateItemMeta({ itemTemplateId: selectedId })
  → computed reagiert automatisch (neues template → neue Sortierung)
```
