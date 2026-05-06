# Leetspeak Generator – Roadmap

## ✅ Erledigt

| # | Was |
|---|-----|
| 1 | Scroll-Fix in `AllOptionsSection` |
| 2 | Race Condition in `initializeFavoriteLeet` entfernt |
| 3 | Toter Icon-Parameter (`ImageVector`) bereinigt |
| 4 | `TranslationTableDialog` dedupliziert – nutzt jetzt `LeetTranslator.translateChar()` |
| 5 | Versionsnummer dynamisch aus `BuildConfig.VERSION_NAME` |
| 6 | `DomainModule` aufgeräumt – Hilt löst Use Cases automatisch auf |
| 7 | Undo-Snackbar beim Löschen eines Custom Leets |
| 8 | Löschen-Button mit Bestätigungs-Dialog im Bottom Sheet |

---

## 🐛 Bugs / Fixes

- [x] `WhatsNewDialog` – Versionsvergleich mit `contains("1337.00_8374_4")` ist hartcodiert, bricht bei jeder neuen Version
- [x] `LeetCreationDialogState` – `applyTemplate()` nutzt `indexOf`, was bei doppelten States falsch zählen kann
- [x] Aktiv genutzter Leet wird gelöscht – kein sauberer Fallback auf Simple Modus mit Feedback
- [x] Leerer Zustand im Bottom Sheet wenn noch keine Custom Leets existieren – aktuell einfach nichts sichtbar

---

## 🚀 Features

### Eingabe
- [x] **Spracheingabe** – Mikrofon-Button im Input, Sprache → Text → Leet
- [x] **Favoriten-Text** – häufig übersetzte Sätze speichern und per Tipp wiederverwenden
- [x] **Case-Modus** – Ausgabe wahlweise `GROSSBUCHSTABEN` / `kleinbuchstaben` / `aLtErNiErEnD`

### Ausgabe
- [x] **Zeichenanzahl-Anzeige** – bei Input und Output
- [ ] **Share-Button** – direkt beim Output, ohne Umweg über Copy
- [ ] **Verlauf** – letzte Übersetzungen speichern und wiederverwenden

### Modi / Leets
- [ ] **Vorschau im Modus-Selector** – zeigt direkt wie der aktuelle Input in jedem Modus aussehen würde
- [ ] **Zufalls-Modus** – wählt zufällig einen Custom Leet aus
- [x] **Drag & Drop** – Custom Leets im Bottom Sheet umsortieren

### Gamification
- [ ] **Leet-Quiz** – zeigt Leet-Text, User muss raten was es bedeutet
- [ ] **Tipp des Tages** – kleines Leetspeak-Wissen beim App-Start

### System
- [ ] **Leetspeak-Tastatur fertigstellen** – IME-Service mit Übersetzungs-Engine verbinden
- [ ] **Widget umbauen** – kein Übersetzungs-Widget mehr, nur schneller Launch-Button zur App
- [ ] **Deeplink-Support** – `leetspeak://translate?text=hello` öffnet App mit vorausgefülltem Text

---

## 🎨 UX / Design

- [ ] **Onboarding** – kurze 3-Schritte-Erklärung beim ersten Start für neue Nutzer
- [ ] **Animierter Moduswechsel** – Übergangseffekt wenn Leet-Modus gewechselt wird
- [ ] **Haptic Feedback konfigurierbar** – Ein/Aus-Schalter in den Einstellungen
- [ ] **Floating Action Button** – statt BottomBar-Modus-Button als Alternative
- [ ] **Leerer Zustand** – Illustration + Hinweis wenn noch keine Custom Leets vorhanden

---

## 🔧 Technisch / Qualität

- [ ] **Unit Tests** – für `LeetTranslator` und `ReverseTranslator`
- [ ] **Accessibility** – Content Descriptions vervollständigen, TalkBack testen
- [ ] **ProGuard-Regeln prüfen** – `CustomLeet` und Gson-Serialisierung absichern
- [ ] **In-App Changelog** – direkt aus Git-Tags generieren statt hartcodiert in `WhatsNewDialog`

---

## 📦 Store / Marketing

- [ ] **Play Store Screenshots** – automatisch generieren
- [ ] **In-App Changelog aus Git** – `WhatsNewDialog` mit echten Release-Notes befüllen

---

*Zuletzt aktualisiert: April 2026*