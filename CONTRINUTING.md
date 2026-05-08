# Contributing Guide — Projekt 2: Aufgabendatenbank

Willkommen im Projekt! Diese Datei beschreibt, wie wir als Team zusammenarbeiten. **Bitte lies sie einmal komplett durch, bevor du etwas committest.**

##  1. Teamregeln

Wir verpflichten uns gemeinsam zu folgenden Regeln:

-  **Antwort innerhalb von 24 Stunden** auf Nachrichten im Projektkanal (werktags)
-  **Frag früh nach Hilfe** — verstecke Probleme nicht
-  **Nimm an wöchentlichen Standups teil** (oder informiere vorher, falls verhindert)
-  **Verzögerungen 48 Stunden vorher kommunizieren** — Schweigen ist das größte Problem


##  2. Erste Schritte mit Git 



### 2.1 Die 5 wichtigsten Befehle

| Befehl | Funktion |
|---|---|
| `git status` | Zeigt geänderte Dateien |
| `git pull` | Holt aktuelle Änderungen |
| `git add .` | Markiert Änderungen für Commit |
| `git commit -m "msg"` | Speichert lokal |
| `git push` | Lädt auf GitHub hoch |

### 2.2 Dein täglicher Workflow

```bash
# 1. Aktuellen Stand holen
git checkout main
git pull

# 2. Neuen Branch erstellen
git checkout -b feature/mein-task

# 3. Code schreiben...

# 4. Committen
git add .
git commit -m "feat: meine Änderung"

# 5. Auf GitHub hochladen
git push -u origin feature/mein-task

# 6. Pull Request auf GitHub öffnen
```

**Goldene Regel:** Im Zweifel **fragen, bevor du pushst**. Vorbeugen ist einfacher als reparieren.

---

##  3. Projektstruktur (Monorepo)

```
aufgabendatenbank/
├── README.md
├── CONTRIBUTING.md           # Diese Datei
├── docs/                     # Meeting-Notes, Architektur, Slides, Dokumentation, Lastenheft, Pflichtenheft
├── backend/                  # Spring Boot
├── frontend/                 # Vue.js + Vuetify
└── database/                 # SQL-Skripte, Schema
```

---

##  4. Branch-Strategie (GitHub Flow)

| Branch | Zweck | Regeln |
|---|---|---|
| `main` | Stabiler Code | **Geschützt** — nur via Pull Request |
| `feature/*`, `bugfix/*`, `docs/*` | Deine Arbeit | Freier Push |

**Naming:** lowercase mit Bindestrichen, auf Englisch, klar und spezifisch.

✅ Gut: `feature/add-tag-search`, `bugfix/collection-display-error`
❌ Schlecht: `feature/work`, `meine-änderung`, `test`

>  **Niemals direkt auf `main` arbeiten.** Immer einen Branch erstellen.

---

##  5. Commit-Konvention (Conventional Commits)

**Format:** `<type>: <kurze Beschreibung auf Deutsch>`

| Type | Wann benutzen |
|---|---|
| `feat` | Neue Funktion |
| `fix` | Bugfix |
| `docs` | Dokumentation |
| `refactor` | Code-Umstrukturierung ohne Verhaltensänderung |
| `test` | Tests hinzufügen/anpassen |
| `chore` | Build, Konfiguration, Dependencies |

---

## 6. Pull Request (PR) Prozess

**Jede Änderung in `main` MUSS über einen PR gehen. Keine Ausnahmen.**

### 6.1 Vor dem PR

- ✅ Aktuellsten `main` gepullt, Konflikte gelöst
- ✅ Code lokal getestet (läuft wirklich!)
- ✅ Keine Debug-Logs, kein auskommentierter Code


### 6.2 PR-Vorlage

```markdown
## Was macht dieser PR?
[Kurze Beschreibung]

## Verbundenes Issue
Closes #[issue-nummer]

## Wie testen?
1. [Schritt 1]
2. [Erwartetes Ergebnis]

## Checkliste
- [ ] Code lokal getestet
- [ ] Konvention für Commit-Messages beachtet
```

### 6.3 Merge-Regeln

-  **Mindestens 1 Approval erforderlich**
-  **Keine Merge-Konflikte** erlaubt
-  **"Squash and merge"** bevorzugt für saubere Historie

---

## 👀 7. Code Review

### Verwende klare Präfixe in Kommentaren

| Präfix | Bedeutung |
|---|---|
| `[BLOCKER]` | Muss vor dem Merge gefixt werden |
| `[Frage]` | Verständnisfrage |
| `[Vorschlag]` | Optionale Verbesserung |
| `[Lob]` | Schöne Arbeit! |

**Beispiele:**
- `[BLOCKER] NullPointerException möglich, wenn author null ist. Bitte Null-Check hinzufügen.`
- `[Vorschlag] Wir könnten das in eine Helper-Methode extrahieren.`
- `[Lob] Sehr saubere Implementierung der Tag-Hierarchie!`

> 💡 **Code Review ist Lernen** — alle sollten reviewen.

---

## 🎫 8. Issue-Management

**Keine Arbeit ohne entsprechendes Issue.**


### 8.1 Workflow

```
Open → Assigned → In Progress → In Review → Done
```

### 8.2 Selbstzuweisung

- ✅ `good first issue` und `priority: low` kannst du selbst nehmen
- ✅ Bei `priority: medium/high` zuerst die Projektleitung fragen
- ✅ **Maximal 2 aktive Issues** gleichzeitig
- ✅ Bei Verzögerung: **kommentiere das Issue**

---

## 💬 9. Kommunikation

### Kanäle

| Kanal | Zweck |
|---|---|
| GitHub Issues | Aufgaben-Diskussion, technische Entscheidungen |
| GitHub PRs | Code-spezifisches Feedback |
| Projektkanal (Discord/Teams) | Schnelle Fragen, tägliche Koordination |
| Wöchentliches Standup | Status, Blocker, Planung |


### Antwortzeiten

- **Projektkanal:** 24 Stunden (werktags)
- **PR-Review-Anfrage:** 48 Stunden
- **Kritischer Blocker:** SOFORT (Projektleitung erwähnen)
- **Nachrichten der Projektleitung:** Priorität

---

##  10. Qualitätsstandards

### Backend (Spring Boot)
- Java-Naming-Konventionen (camelCase, PascalCase für Klassen)
- `@Transactional` für Schreiboperationen
- Eingabevalidierung mit `javax.validation`
- Korrekte HTTP-Status-Codes
- JUnit-Unit-Tests für Service-Layer

### Frontend (Vue.js)
- **Composition API** verwenden
- Komponenten in **PascalCase** (`ItemEditor.vue`)
- Komponenten klein und fokussiert halten
- Pinia für State Management

### Datenbank
- **snake_case** für Tabellen/Spalten (passt zum ER-Schema)
- Foreign Keys mit Constraints
- Migrations-Dateien nicht nachträglich verändern

### Allgemein
- ❌ **Kein auskommentierter Code** in PRs (Git merkt sich alles)
- ❌ **Keine Debug-Logs** in Produktionscode
- ❌ **Keine hardcoded Credentials**
- ✅ **Deutsch im Code** (Kommentare)

---

## 11. Hilfe & Ressourcen

### Wenn du nicht weiterkommst

1. **GitHub Issues durchsuchen** — vielleicht gibt es schon eine Antwort
2. **Im Projektkanal fragen**
3. **Projektleitung (Joelle) kontaktieren**

### Externe Ressourcen

**Git**
- [Git Handbook](https://guides.github.com/introduction/git-handbook/)
- [Oh Shit, Git!?!](https://ohshitgit.com/) — bei häufigen Problemen
- [Git Cheat Sheet](https://education.github.com/git-cheat-sheet-education.pdf)

**Vue.js + Vuetify**
- [Vue.js Tutorial](https://vuejs.org/tutorial/)
- [Vuetify Components](https://vuetifyjs.com/en/components/all/)
- [Pinia](https://pinia.vuejs.org/introduction.html)

**Spring Boot**
- [Spring Boot Getting Started](https://spring.io/guides/gs/spring-boot/)
- [Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/)

**PostgreSQL**
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)

---

##  Schlussworte

### Wir erwarten von jedem Mitglied

-  Diese Datei vor dem ersten Beitrag lesen
-  Verantwortung für übernommene Aufgaben
-  Kollegen helfen und um Hilfe bitten
- Termine respektieren, proaktiv kommunizieren
- Qualität vor Quantität



---

**Zuletzt aktualisiert:** [Datum]
**Verantwortlich:** Joelle Giovanna Kamwa Mokam (Projektleitung)
**Fragen?** Im Projektkanal stellen oder als GitHub-Issue mit Label `area: docs`.

Viel Erfolg! 🚀
