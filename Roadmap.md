# Leetspeak Generator – Roadmap

> **Format:** `[x]` = erledigt · `[ ]` = offen · `[!]` = Bug/Fix · `[~]` = in Arbeit
> 
> Neue Bugs bitte unter **🐛 Bugs / Fixes** eintragen, neue Feature-Ideen unter der passenden Kategorie.

---

## ✅ Veröffentlicht in v1337.014

### Architektur & Basis
- [x] Scroll-Fix in `AllOptionsSection`
- [x] Race Condition in `initializeFavoriteLeet` entfernt
- [x] Toter Icon-Parameter (`ImageVector`) bereinigt
- [x] `TranslationTableDialog` dedupliziert – nutzt `LeetTranslator.translateChar()`
- [x] Versionsnummer dynamisch aus `BuildConfig.VERSION_NAME`
- [x] `DomainModule` aufgeräumt – Hilt löst Use Cases automatisch auf
- [x] `WhatsNewDialog` vollständig entfernt
- [x] GitHub Actions CI (Unit Tests, Lint, Coverage)

### Bugs behoben
- [x] `WhatsNewDialog` – Versionsvergleich war hartcodiert, brach bei jeder neuen Version
- [x] `LeetCreationDialogState` – `applyTemplate()` nutzte `indexOf`, zählte bei doppelten States falsch
- [x] Aktiv genutzter Leet gelöscht – kein sauberer Fallback auf Simple Modus mit Feedback
- [x] Leerer Zustand im Bottom Sheet wenn noch keine Custom Leets existieren
- [x] `AnimatedContent` in `OutputCard` animierte bei **jedem Tastendruck** statt nur beim Moduswechsel → `animationKey: LeetTranslator.TranslationMode` als separater Parameter

### Features
- [x] **Undo-Snackbar** beim Löschen eines Custom Leets
- [x] **Bestätigungs-Dialog** beim Löschen im Bottom Sheet
- [x] **Spracheingabe** – Mikrofon-Button im Input, Sprache → Text → Leet
- [x] **Zeichenanzahl-Anzeige** – bei Input und Output
- [x] **Drag & Drop** – Leets im Bottom Sheet umsortieren (Long-Press + ziehen)
- [x] **Animierter Moduswechsel** – Slide/Fade beim Wechsel des Leet-Modus (nur Moduswechsel, nicht Tastendruck)
- [x] **Haptic Feedback konfigurierbar** – Ein/Aus-Schalter in den Einstellungen
- [x] **Onboarding** – 3-Screen HorizontalPager beim ersten Start
- [x] **Accessibility** – Content Descriptions für alle interaktiven Elemente
- [x] **Feedback-Funktion** – E-Mail-Intent im Menü (Lightbulb-Icon)
- [x] **About-Dialog** – Geplante Features statt Feature-Übersicht; App-Logo statt Schneeflocke
- [x] **5 Farbthemen** – PlanIt, NexTime, Leetspeak, DailyList, Unknown
- [x] **5 Sprachen** – EN, DE, ES, FR, IT
- [x] **Unit Tests** – `LeetTranslatorTest` (18 Tests) + `ReverseTranslatorTest` (alle grün)
- [x] **In-App Review** – Google Play Review API nach 3 App-Starts
- [x] **Ko-Fi Support-Button** in TopBar und About-Dialog

---

## 🐛 Bugs / Fixes

> Neue Bugs hier eintragen: Datum, Beschreibung, Priorität

| Prio | Status | Beschreibung |
|------|--------|-------------|
| – | – | *Hier neue Bugs eintragen* |

---

## 🚀 Geplante Features

### Ausgabe
- [ ] **Share-Button** – direkt beim Output, ohne Umweg über Copy
- [ ] **Verlauf** – letzte X Übersetzungen speichern und wiederverwenden


### Modi / Leets
- [ ] **Vorschau im Modus-Selector** – zeigt direkt wie der aktuelle Input in jedem Modus aussehen würde


### System / Platform
- [ ] **Leetspeak-Tastatur** – IME-Service mit Übersetzungs-Engine verbinden
- [ ] **Widget umbauen** – kein Übersetzungs-Widget mehr, nur schneller Launch-Button zur App
- [ ] **Deeplink-Support** – `leetspeak://translate?text=hello` öffnet App mit vorausgefülltem Text

---



## 🔧 Technisch / Qualität

- [ ] **ProGuard-Regeln prüfen** – `CustomLeet` und Gson-Serialisierung absichern
- [ ] **Weitere Unit Tests** – ViewModel, Repository, Use Cases
- [ ] **TalkBack vollständig testen** – Accessibility-Durchlauf auf echtem Gerät

---

## 📦 Store / Marketing

- [ ] **Play Store Screenshots** – aktuelle UI abfotografieren / automatisch generieren
- [ ] **Play Store Beschreibung** – alle 5 Sprachen aktualisieren
- [ ] **Feature Graphic** – Banner für Play Store erstellen

---

## 📋 Release-Log

| Version | Datum | Highlights |
|---------|-------|-----------|
| v1337.014 | Mai 2026 | Animation-Fix OutputCard, Feedback-Button, About-Dialog überarbeitet |
| v1337.009 | Apr 2026 | Onboarding, Drag & Drop, Spracheingabe, Haptic Feedback, 5 Themes |
| v1337.001 | 2025 | Initial Release |

---

*Zuletzt aktualisiert: Mai 2026*