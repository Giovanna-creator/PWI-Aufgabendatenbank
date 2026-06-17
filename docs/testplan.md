# Testplan — Frontend ↔ Backend Verbindung

## Setup

**Docker-Modus (empfohlen):**
```bash
docker compose up --build
```
→ Frontend: http://localhost:8085
→ DB wird automatisch mit Seed-Daten befüllt

**Oder getrennt:**
```bash
docker compose up db -d
# Terminal 2:
cd backend
$env:DB_URL="jdbc:postgresql://localhost:5432/pwi_aufgabendatenbank"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
.\mvnw.cmd spring-boot:run
# Terminal 3:
cd frontend
npm run dev
```

---

## Szenario 1: App lädt & zeigt Seed-Daten

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 1.1 | `http://localhost:8085` öffnen | Seite lädt, linker Baum zeigt Items |
| 1.2 | Baum inspizieren | Es sollten Einträge aus den Seed-Daten sichtbar sein (z.B. aus `init.sql`: Items mit library-Bezug) |
| 1.3 | Browser-Konsole prüfen (F12) | Keine 404/500-Fehler im Network-Tab bei `GET /api/items?root=true` |

**Wenn Fehler:** Backend-Logs checken (`docker compose logs backend`). Prüfen ob DB-Seed gelaufen ist.

---

## Szenario 2: Aufgabe erstellen

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 2.1 | Klick "Aufgabe erstellen" | Neue Aufgabe erscheint sofort im Baum (optimistisch) |
| 2.2 | Network-Tab prüfen | `POST /api/items` → **201 Created** |
| 2.3 | Danach | `POST /api/contents/by-item/{id}` → **201 Created** |
| 2.4 | Neue Aufgabe anklicken | Rechter Editor zeigt den Content-Block mit "Neuer Inhalt" |
| 2.5 | Inhalt eingeben, woanders klicken | Keine Fehlermeldung; Text bleibt sichtbar |
| 2.6 | Seite neu laden (F5) | Die erstellte Aufgabe sollte noch im Baum sein (persistiert) |

**Erwartete HTTP-Responses:**
```
POST /api/items → 201
POST /api/contents/by-item/{uuid} → 201
```

---

## Szenario 3: Kollektion erstellen

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 3.1 | Klick "Kollektion erstellen" | Ordner erscheint im Baum |
| 3.2 | Network-Tab | `POST /api/collections` → **201 Created** |

---

## Szenario 4: Aufgabe in Kollektion hinzufügen

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 4.1 | Kollektion im Baum auswählen | Rechter Editor zeigt "Kollektion" |
| 4.2 | Im Kontextmenü der Kollektion (drei Punkte) "Aufgabe hinzufügen" klicken | Neue Aufgabe erscheint unter der Kollektion |
| 4.3 | Network-Tab prüfen | `POST /api/items` → 201, dann `POST /api/collections/{id}/items` → **201 Created** |
| 4.4 | Seite neu laden | Aufgabe sollte noch in der Kollektion sein |

---

## Szenario 5: Reihenfolge ein-/ausschalten

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 5.1 | Kollektion auswählen | Editor zeigt nummerierte-Liste-Icon |
| 5.2 | Icon klicken | `PUT /api/collections/{id}/order` → `{"order":true}` → **200 OK** |
| 5.3 | Baum prüfen | Items zeigen Nummern (1., 2., 3.…) |
| 5.4 | Erneut klicken | `PUT /api/collections/{id}/order` → `{"order":false}` → **200 OK** |
| 5.5 | Baum prüfen | Nummern verschwinden |

---

## Szenario 6: Item in Kollektion umwandeln

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 6.1 | Aufgabe (File) auswählen | Drei-Punkte-Menü → "In Kollektion umwandeln" |
| 6.2 | Klick | `POST /api/items/{id}/collection` → **200 OK** |
| 6.3 | Baum prüfen | Item zeigt jetzt Ordner-Icon (erweiterbar) |

---

## Szenario 7: Inhalt bearbeiten

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 7.1 | Aufgabe anklicken | Content wird via `GET /api/contents/by-item/{id}` geladen |
| 7.2 | Auf Purpose klicken, neuen Text eingeben, Enter | `PUT /api/contents/{id}` → **200 OK** |
| 7.3 | Auf Content-Text klicken, neuen Text eingeben, woanders klicken | `PUT /api/contents/{id}` → **200 OK** |
| 7.4 | Seite neu laden, Aufgabe erneut anklicken | Bearbeiteter Text wird angezeigt |

---

## Szenario 8: Inhalt löschen

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 8.1 | Aufgabe auswählen | Content-Block sichtbar |
| 8.2 | X-Icon auf Content klicken | Content verschwindet, `DELETE /api/contents/{id}` → **204 No Content** |

---

## Szenario 9: Aufgabe löschen

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 9.1 | Aufgabe auswählen | Drei-Punkte-Menü → "Löschen" |
| 9.2 | Bestätigen | `DELETE /api/items/{id}` → **204 No Content** |
| 9.3 | Seite neu laden | Aufgabe ist weg |

---

## Szenario 10: Kollektion löschen

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 10.1 | Kollektion auswählen | Löschen-Button im Editor oder Kontextmenü |
| 10.2 | Löschen bestätigen | Kollektion + alle enthaltenen Items werden gelöscht |
| 10.3 | Seite neu laden | Alles weg |

---

## Szenario 11: Drag-and-Drop (neu anordnen)

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 11.1 | Geordnete Kollektion öffnen | Items mit Nummern |
| 11.2 | Item per Drag-and-Drop an andere Position ziehen | Keine Fehlermeldung; Nummern aktualisieren sich |
| 11.3 | Network-Tab | `PUT /api/collections/{id}/items/{itemId}/position` → **204** für umsortierte Items |

---

## Szenario 12: Fehlerfall — Backend nicht erreichbar

| Schritt | Aktion | Erwartet |
|---------|--------|----------|
| 12.1 | Backend stoppen (`docker compose stop backend`) | |
| 12.2 | Aufgabe erstellen | Fehler-Notification (rot) erscheint; Item bleibt trotzdem im Baum (optimistisch) |

---

## Feedback-Tabelle (zum Ausfüllen)

| Szenario | Status (✅ / ❌) | Fehlermeldung | Bemerkung |
|----------|-----------------|---------------|-----------|
| 1 Seed laden | | | |
| 2 Aufgabe erstellen | | | |
| 3 Kollektion erstellen | | | |
| 4 Item zu Kollektion | | | |
| 5 Order togglen | | | |
| 6 In Kollektion wandeln | | | |
| 7 Inhalt bearbeiten | | | |
| 8 Inhalt löschen | | | |
| 9 Aufgabe löschen | | | |
| 10 Kollektion löschen | | | |
| 11 Drag-and-Drop | | | |
| 12 Backend offline | | | |
