# Beschreibung der verwendeten Architektur

Die Architektur des verteilten Systems zur Berechnung und Darstellung der Mandelbrotmenge basiert auf einer klassischen Client-Server-Struktur mit verteilten Worker-Knoten. Die Aufteilung folgt einem **Model-View-Presenter (MVP)**-Muster auf der Client-Seite und einer **Master-Worker**-Struktur für die verteilte Berechnung.

## Architekturdiagramm

![Architekturdiagramm](/architektur%20.jpg)

## Komponentenbeschreibung

### 1. Client (MVP-Muster)

Der Client besteht aus folgenden Modulen:

- **Model**:  
  Verwaltet den Zustand der Mandelbrot-Berechnung. Es setzt Update-Flags zur Steuerung der Anzeigeaktualisierung.

- **View (GUI)**:  
  Die grafische Oberfläche zur Anzeige der Mandelbrotmenge, realisiert mit Java Swing und `BufferedImage`.

- **Presenter**:  
  Vermittelt zwischen Benutzeraktionen und dem Model. Reagiert auf Events und initiiert neue Berechnungen.

- **Ticker & UpdateRequest**:  
  Ticker stößt regelmäßig Überprüfungen an. `UpdateRequest` prüft, ob sich im Model etwas geändert hat, und triggert ggf. ein View-Update.

### 2. Server Master

Der Server Master ist die zentrale Koordinationsinstanz für die verteilte Berechnung. Er übernimmt folgende Aufgaben:

- Entgegennahme von Anfragen vom Client (inkl. Übergabeparameter wie Zoompunkt, Auflösung, ...).
- Zerlegung des darzustellenden Bereichs in einzelne **Work Tasks**.
- Registrierung der verfügbaren Worker mittels RMI.
- Verteilung der Aufgabenpakete auf die Worker.
- Sammlung der berechneten **Iterationsdaten**.
- Rückgabe der gesammelten Ergebnisse an den Client zur Visualisierung.

### 3. Worker

Die Worker können auf **unterschiedlichen Hosts** im Netzwerk laufen. Jeder Worker:

- Registriert sich beim Server Master.
- Erhält von dort die zu berechnenden Koordinatenbereiche.
- Führt die Berechnungen für die zugewiesenen Bereiche durch.
- Gibt die Ergebnisse an den Server Master zurück.

### Kommunikation zwischen den Komponenten

Die Kommunikation erfolgt über **Java RMI**.

- **Client → Server Master**: Anfragen zur Berechnung eines neuen Bildausschnitts
- **Server Master → Worker**: Verteilt die einzelnen Bereiche des `BufferedImage` für die Berechnung auf die jeweiligen Worker
- **Worker → Server Master**: Rückgabe der Iterationsdaten nach der Berechnung
- **Server Master → Client**: Weitergabe der Iterationsdaten für die Darstellung des Mandelbrotes