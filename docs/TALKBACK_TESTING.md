# TalkBack-Testleitfaden — Leetspeak Generator

Checkliste für einen manuellen Accessibility-Durchlauf auf einem echten Gerät.
Automatisiert lässt sich TalkBack nicht sinnvoll testen (Screenreader-Verhalten
hängt stark von echter Fokus-Reihenfolge, Sprachausgabe-Timing und Geräte-/
Android-Version ab) — dieser Leitfaden strukturiert den manuellen Durchlauf.

## Setup

1. Einstellungen → Bedienungshilfen → TalkBack aktivieren
2. Lautstärketasten-Kurzbefehl für TalkBack einrichten (schnelles An/Aus während des Testens)
3. Testgerät: mindestens 1× ein "kleines" Gerät (kompakter Bildschirm) und 1× ein
   Gerät mit vergrößerter Schriftgröße/Anzeigegröße (Einstellungen → Anzeige)
4. Einmal kompletten Durchlauf in Deutsch, einmal in einer anderen unterstützten
   Sprache (z.B. EN) — Layout kann bei längeren übersetzten Strings brechen

## Allgemeine Kriterien (bei jedem Screen prüfen)

- [ ] Jedes interaktive Element wird beim Wischen fokussiert (keine "toten" Buttons)
- [ ] Fokusreihenfolge folgt der visuellen/logischen Lesereihenfolge, nicht der Code-Reihenfolge
- [ ] Kein Element wird doppelt angesagt (z.B. Icon + Text separat vorgelesen statt als eine Einheit)
- [ ] Reine Dekor-Icons (neben Text, keine eigene Aktion) werden NICHT separat angesagt
- [ ] Buttons werden als "Schaltfläche" angekündigt, nicht als generisches Element
- [ ] Touch-Ziele lassen sich mit TalkBack-Doppeltipp zuverlässig treffen (kein Verrutschen)
- [ ] Zustandsänderungen (ausgewählt/favorisiert/aktiv) werden angesagt, wenn TalkBack den Fokus bewegt

## Hauptbildschirm (Input/Output)

- [ ] Eingabefeld: Ansage enthält Zweck ("Eingabe: Klartext" bzw. im Reverse-Modus den Modusnamen)
- [ ] Beim Tippen: Zeichenanzahl-Änderung führt nicht zu Ansage-Spam (keine Ansage pro Tastendruck)
- [ ] "Löschen"-Button im Eingabefeld: klare Ansage, erscheint/verschwindet TalkBack-fokussierbar korrekt mit der Sichtbarkeits-Animation
- [ ] Ausgabefeld: Modus wird korrekt angesagt ("Ausgabe: Simple Leet" o.ä.)
- [ ] Kopieren-Button: Ansage "Text kopieren", nach Antippen wechselt Icon zu "Kopiert" — wird der Zustandswechsel angesagt oder bleibt es beim alten Label hängen?
- [ ] Teilen-Button: eigene, klare Ansage (nicht zu verwechseln mit Kopieren)
- [ ] Reverse-Modus-Toggle: Ansage macht klar, in welche Richtung als Nächstes gewechselt wird
- [ ] Snackbar-Meldungen (Erfolg/Fehler, z.B. "In Zwischenablage kopiert"): werden automatisch angesagt, ohne dass man sie manuell fokussieren muss

## Modi-Auswahl (Simple / Extended / Custom Leets)

- [ ] Jede Modus-Karte: Name + Vorschau werden verständlich in einem Zug vorgelesen
- [ ] Ausgewählter Zustand ("Ausgewählt") wird bei der aktiven Karte angesagt
- [ ] Favoriten-Stern: Ansage unterscheidet klar "Favorit setzen" vs. "Favorit entfernen"
- [ ] Overflow-Menü (⋮) pro Karte: öffnet sich mit TalkBack normal, Menüpunkte einzeln fokussierbar
- [ ] "Übersetzungstabelle anzeigen": Dialog-Inhalt (Buchstabe → Zeichen Paare) ist zeilenweise sinnvoll vorlesbar, nicht als ein einziger unstrukturierter Textblock
- [ ] Custom Leet erstellen/bearbeiten: alle Eingabefelder (Basisname, jedes Buchstaben-Mapping) einzeln fokussierbar und beschriftet
- [ ] Lösch-Bestätigungsdialog: Fokus springt beim Öffnen automatisch in den Dialog, nicht dahinter auf die Liste

## Verlauf (History)

- [ ] Jeder Verlaufseintrag: Modus, Zeit, Eingabe- und Ausgabetext werden als zusammenhängende, verständliche Einheit vorgelesen
- [ ] "Eintrag löschen"-Icon pro Zeile: eigene, eindeutige Ansage — nicht zu verwechseln mit dem Antippen der ganzen Zeile (Wiederverwenden)
- [ ] "Alles löschen": Bestätigungsdialog erscheint, Fokus wandert korrekt hinein
- [ ] Leerer Zustand ("Noch keine Übersetzungen…"): wird sinnvoll angesagt, keine leere/verwirrende Ansage

## Einstellungen

- [ ] Jede Sektion (Erscheinungsbild, Verhalten, Tastatur, Haptik, …) ist als Überschrift erkennbar, nicht nur optisch fett
- [ ] Ein-/Ausklapp-Pfeil: Ansage macht klar, ob Sektion gerade offen oder geschlossen ist
- [ ] Schalter (Switches): Zustand "an/aus" wird korrekt und aktuell angesagt
- [ ] "Tastatur in Systemeinstellungen aktivieren" / "Jetzt Tastatur wechseln": klare, unterscheidbare Ansagen
- [ ] Sprachauswahl: aktuell gewählte Sprache wird als solche angesagt

## Onboarding

- [ ] Jede Onboarding-Seite: Inhalt wird beim Erscheinen automatisch/sinnvoll fokussiert
- [ ] "Weiter"/"Fertig"-Button klar von reinen Seiten-Indikator-Punkten (Dots) unterscheidbar — Dots sollten nicht einzeln fokussierbar/ansagbar sein, wenn sie nicht interaktiv sind

## Leetspeak-Tastatur (IME) — besondere Hinweise

Eigene Tastaturen haben mit TalkBack Besonderheiten, die über normale App-Screens hinausgehen:

- [ ] Mit aktiviertem TalkBack UND aktivierter Leetspeak-Tastatur: lässt sich überhaupt noch zuverlässig tippen? (TalkBack fängt normalerweise Touch-Events ab und braucht Doppeltipp-Bestätigung — das kann sich mit einer komplett custom gebauten Tastatur anders verhalten als beim System-Keyboard)
- [ ] Jede Taste (Buchstaben, Leertaste, Backspace, Enter, Shift, 123/ABC, Modus-Pfeile ‹ ›) wird einzeln und verständlich angesagt
- [ ] Modus-Anzeige (aktueller Leet-Modus) in der Tastatur wird bei Wechsel angesagt
- [ ] Falls sich herausstellt, dass die Tastatur mit TalkBack unbenutzbar ist: das ist ein reales, bekanntes Risiko bei komplett selbst gebauten IMEs (siehe Hinweis unten) — dann zusätzlich zur reinen Bugfix-Iteration in Erwägung ziehen, ob ein alternativer Bedienweg (z.B. Text in der App eintippen + Teilen-Button) für TalkBack-Nutzer:innen weiterhin gut erreichbar ist

## Sprachausgabe-Reihenfolge bei dynamischen Inhalten

- [ ] Live-Übersetzung im Ausgabefeld: wird bei jeder Änderung neu (und komplett) vorgelesen, wenn man während des Tippens im Ausgabefeld fokussiert ist? Das kann bei schnellem Tippen zu einer Ansage-Flut führen — ggf. mit `liveRegion`-Politik (`Polite` statt `Assertive`) gegensteuern, falls es nervig ist
- [ ] Fehler-/Erfolgs-Meldungen unterbrechen keine gerade laufende, wichtigere Ansage abrupt

## Bekannte Grenzen dieses Leitfadens

Dieser Leitfaden ersetzt keinen echten Durchlauf. Insbesondere:
- Ansage-*Timing* und ob sich etwas "flüssig" oder "abgehackt" anfühlt, lässt sich nur hörend beurteilen
- Verhalten unterscheidet sich zwischen TalkBack-Versionen und Geräteherstellern (Samsung/Pixel/etc. patchen teils eigene Screenreader-Varianten)
- Gesten-Navigation vs. 3-Tasten-Navigation kann den Fokusfluss unterschiedlich beeinflussen
