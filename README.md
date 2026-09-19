# Leetspeak Generator

Android-App zum Übersetzen von Text in Leetspeak — mit eigener Leet-Tastatur, frei
definierbaren Leet-Varianten, Rückübersetzung und Homescreen-Widget.

[![Android Tests](https://github.com/B31G3L/Leetspeak-Generator/actions/workflows/android-tests.yml/badge.svg)](https://github.com/B31G3L/Leetspeak-Generator/actions/workflows/android-tests.yml)

## Funktionen

**Übersetzen**
- **Simple** und **Extended** als mitgelieferte Leet-Varianten
- **Eigene Leets**: beliebige Buchstaben→Zeichen-Zuordnungen anlegen, bearbeiten, löschen und als Favorit markieren; Vorlagen erleichtern den Einstieg
- **Rückübersetzung**: Leetspeak zurück in lesbaren Text, inklusive automatischer Erkennung, ob eine Eingabe überhaupt Leetspeak ist
- **Live-Vorschau** der Übersetzung während der Eingabe
- **Übersetzungstabelle** zeigt die Ersetzungsregeln der aktiven Variante
- **Spracheingabe** per Mikrofon
- **Verlauf** der letzten Übersetzungen

**Leet-Tastatur (IME)**
- Systemweite Tastatur, die jeden Buchstaben direkt beim Tippen übersetzt — kein Umweg über Copy/Paste
- Leet-Modus direkt auf der Tastatur umschaltbar, inklusive eigener Leets
- Umschaltbare Tastatur-Layouts: QWERTZ, QWERTY, QWERTY (ES), AZERTY, Dvorak, Colemak
- Long-Press für Umlaute und Akzente (ä ö ü ß é à ñ ç …), Shift/Caps-Lock, Symbolebene, Tastenwiederholung bei Backspace

**Rundherum**
- **Homescreen-Widget** (Glance) zum direkten Start
- **Material 3** mit Hell-/Dunkel-Modus oder System-Theme
- **Mehrsprachig**: Deutsch, Englisch, Spanisch, Französisch, Italienisch — in der App umschaltbar
- Onboarding, haptisches Feedback, konfigurierbares Verhalten beim Kopieren
- TalkBack-tauglich (siehe [`docs/TALKBACK_TESTING.md`](docs/TALKBACK_TESTING.md))

## Installation

- **Google Play**: [Leetspeak Generator](https://play.google.com/store/apps/details?id=com.beigel.leetSpeak_Generator)
- **Manuell**: APK aus dem [Releases](https://github.com/B31G3L/Leetspeak-Generator/releases)-Bereich laden und Installation aus unbekannten Quellen erlauben

Nach der Installation muss die Leet-Tastatur einmalig aktiviert werden:
Einstellungen → Tastatur → Leet-Tastatur aktivieren (der Weg dorthin ist in den
App-Einstellungen verlinkt).

## Technik

| | |
|---|---|
| Sprache | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architektur | UseCases (`domain/usecase`) + Repository + ViewModel, Intents für UI-Events |
| DI | Hilt |
| Persistenz | DataStore (Einstellungen), SharedPreferences (Leets, Verlauf) |
| Widget | Glance |
| Tastatur | `InputMethodService` mit klassischen Views |
| Tests | JUnit, Robolectric, MockK/Mockito, Turbine, Truth |
| minSdk / targetSdk | 27 / 36 |
| JDK | 17 |

## Projektstruktur

```
app/src/main/java/com/beigel/leetSpeak_Generator/
├── data/           Datenmodelle und Preferences (Theme, Verlauf, Onboarding)
├── di/             Hilt-Module
├── domain/usecase/ Fachlogik: leet/, translation/, ui/
├── keyboard/       LeetKeyboardService (IME) + Tastatur-Layouts
├── manager/        LeetManager — geteilter Zugriff auf Custom Leets
├── presentation/   MainIntent (UI-Events)
├── repository/     LeetRepository
├── translation/    LeetTranslator, ReverseTranslator
├── ui/             Compose-Screens, Komponenten, Theme, Settings, Onboarding
├── utils/          ErrorHandler, SpeechInputManager
├── viewmodel/      MainViewModel
└── widget/         Glance-Widget
```

## Entwicklung

**Voraussetzungen:** Android Studio (aktuelle stabile Version), JDK 17, Android SDK 36.

```bash
git clone https://github.com/B31G3L/Leetspeak-Generator.git
cd Leetspeak-Generator
./gradlew assembleDebug
```

Tests laufen lokal und in der GitHub-Action:

```bash
./gradlew testDebugUnitTest
```

Abgedeckt sind vor allem `LeetTranslator`, `ReverseTranslator`, die Translation- und
UI-UseCases, `LeetRepository` und das `MainViewModel`.

## Mitwirken

Pull Requests sind willkommen. Bitte vorher `./gradlew testDebugUnitTest` laufen lassen
und neue Strings ausschließlich über `res/values/strings.xml` (plus die
`values-de/-es/-fr/-it`-Varianten) einpflegen.

## Lizenz

MIT — siehe [LICENSE](LICENSE).

## Kontakt

Beigel Apps — [github.com/B31G3L](https://github.com/B31G3L)

---

*Leetspeak Generator ist ein Hobbyprojekt und steht in keiner Verbindung zu kommerziellen Diensten.*
