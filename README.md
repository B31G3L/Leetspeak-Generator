# Leetspeak Generator

Eine Android-App zum Konvertieren normaler Texte in Leetspeak.

## Funktionen

- **Einfache Leetspeak**: Konvertiert Text in die einfache Leetspeak-Variante (z.B. A→4, E→3)
- **Erweiterte Leetspeak**: Konvertiert Text in die erweiterte Leetspeak-Variante (z.B. A→4, E→3, T→7, L→1)
- **Benutzerdefinierte Leetspeak**: Erstelle und speichere deine eigenen Leetspeak-Varianten
- **Dunkler Modus**: Vollständige Unterstützung des System-Themes (hell/dunkel)
- **Übersichtliche Tabelle**: Zeigt die Übersetzungstabelle für jede Variante an
- **Clipboard-Funktion**: Kopiert den konvertierten Text mit einem Klick in die Zwischenablage

## Screenshots

*Screenshots können hier hinzugefügt werden*

## Installation

Die App kann auf folgenden Wegen installiert werden:

1. **Direkt aus Google Play Store** (wenn veröffentlicht)
2. **Manuelle Installation der APK**: 
   - Lade die neueste APK-Datei aus dem Release-Bereich herunter
   - Erlaube in den Einstellungen deines Android-Geräts die Installation aus unbekannten Quellen
   - Öffne die heruntergeladene APK und folge den Anweisungen zur Installation

## Entwicklung

### Voraussetzungen

- Android Studio (empfohlen: neueste stabile Version)
- Java Development Kit (JDK) 8 oder höher
- Android SDK mit API-Level 34 oder höher

### Setup

1. Klone das Repository:
   ```
   git clone https://github.com/yourusername/leetspeak-generator.git
   ```

2. Öffne das Projekt in Android Studio:
   - Starte Android Studio
   - Wähle "Open an Existing Project"
   - Navigiere zum geklonten Repository-Ordner und öffne ihn

3. Warte, bis Gradle alle Abhängigkeiten synchronisiert hat

4. Erstelle und starte die App:
   - Wähle ein Gerät oder einen Emulator
   - Klicke auf "Run" (▶️)

### Projektstruktur

- **`app/src/main/java/com/beigel/leetSpeak_Generator/`**: Enthält den Java-Quellcode der App
  - `MainActivity.java`: Hauptaktivität der App
  - `CustomProfile.java`: Datenmodell für benutzerdefinierte Leetspeak-Profile
  - `ProfileManager.java`: Verwaltet das Speichern und Laden von Leetspeak-Profilen

- **`app/src/main/res/`**: Enthält Ressourcen wie Layouts, Zeichenfolgen und Bilder
  - `layout/`: XML-Layout-Dateien für die Benutzeroberfläche
  - `values/`: Definiert Farben, Stile, Zeichenfolgen und Dimensionen
  - `drawable/`: Enthält Grafiken und Icons

## Mitwirken

Beiträge sind willkommen! Wenn du zum Projekt beitragen möchtest:

1. Erstelle einen Fork des Projekts
2. Erstelle einen Branch für dein Feature (`git checkout -b feature/amazing-feature`)
3. Committe deine Änderungen (`git commit -m 'Füge ein tolles Feature hinzu'`)
4. Pushe zu deinem Branch (`git push origin feature/amazing-feature`)
5. Eröffne einen Pull Request

## Lizenz

Dieses Projekt ist unter der MIT-Lizenz lizenziert - siehe die [LICENSE](LICENSE) Datei für Details.

## Kontakt

Projektmaintainer: [Dein Name] - [deine.email@beispiel.com]

Projektlink: [https://github.com/yourusername/leetspeak-generator](https://github.com/yourusername/leetspeak-generator)

---

*Leetspeak Generator ist ein Hobbyprojekt und steht nicht in Verbindung mit kommerziellen Diensten.*