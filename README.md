# 🎄 Christmas Plugin for Minecraft

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.21+-green.svg)
![Paper](https://img.shields.io/badge/Paper-Required-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![License](https://img.shields.io/badge/License-Proprietary-red.svg)

Ein umfangreiches Weihnachts-Plugin für Minecraft Paper Server mit Adventskalender, Santa Claus Events, Christmas Crackers und vielem mehr!

**Entwickelt von [niliees](https://nsce.fr) | © 2025 NSCE**

</div>

---

## 📋 Inhaltsverzeichnis

- [Features](#-features)
- [Installation](#-installation)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [Konfiguration](#-konfiguration)
- [Verwendung](#-verwendung)
- [Dependencies](#-dependencies)
- [Support](#-support)

---

## ✨ Features

### 🎄 Adventskalender
- ✅ **Vollständig konfigurierbares GUI** mit 54 Slots
- ✅ **25 Tage** mit individuellen Geschenken
- ✅ **Mehrere Geschenke pro Tag** möglich
- ✅ **Gruppen-System**: Verschiedene Geschenke für VIP, Premium, Donor, etc.
- ✅ **Flexible Belohnungen**: Items, Commands, Permissions
- ✅ **Vorherige Tage claimen** (optional aktivierbar)
- ✅ **Test-Modus** für Administratoren zum Testen aller Tage
- ✅ **Automatische Zeitprüfung** (nur im Dezember verfügbar)
- ✅ **Persistente Daten** - Spieler verlieren nicht ihren Fortschritt

### 🎅 Santa Claus Event
- ✅ **Spawn Santa** für bestimmte Zeit oder permanent
- ✅ **Konfigurierbare Spawn-Locations** (fest oder zufällig)
- ✅ **Gift-Drops** mit einstellbarer Wahrscheinlichkeit
- ✅ **Schneespur** die nach Zeit verschwindet
- ✅ **WorldGuard-Support** - Funktioniert auch in geschützten Regionen
- ✅ **Teleport-Befehl** zu Santa's Position
- ✅ **Anpassbares Aussehen** (Name, Skin, Entity-Type)

### 🎁 Christmas Crackers
- ✅ **Eigene Cracker-Typen** definieren
- ✅ **Alle Items garantiert** - Spieler erhalten ALLE konfigurierten Items
- ✅ **Verschiedene Rewards**: Items und Commands
- ✅ **Glow-Effekt** und Custom Model Data Support
- ✅ **Einfaches Verschenken** an andere Spieler
- ✅ **Automatisches Droppen** wenn Inventar voll ist

### ❄️ Schneefall-Effekt
- ✅ **5 Intensitätsstufen** (von sanft bis Schneesturm)
- ✅ **Per-Player Toggle** - Jeder kann für sich entscheiden
- ✅ **Weltbasierte Blacklist** (z.B. kein Schnee im Nether)
- ✅ **Performance-optimiert** mit Partikel-System

### 🔧 Technische Features
- ✅ **Paper Plugin API** - Moderne Plugin-Architektur
- ✅ **Vault-Integration** für Permissions und Gruppen
- ✅ **WorldGuard-Integration** für geschützte Regionen
- ✅ **100% konfigurierbar** - Alle Nachrichten und Settings
- ✅ **Hot-Reload** - Config ohne Serverneustart neu laden
- ✅ **Tab-Completion** für alle Befehle
- ✅ **Effizient programmiert** - Kein Lag auf dem Server

---

## 📦 Installation

### Schritt 1: Download
Lade die neueste Version des Plugins herunter:
```
Christmas_Plugin-1.0-SNAPSHOT.jar
```

### Schritt 2: Installation
1. Platziere die JAR-Datei in den `plugins/` Ordner deines Servers
2. Starte den Server **neu** (nicht reload!)
3. Das Plugin erstellt automatisch alle benötigten Konfigurationsdateien

### Schritt 3: Konfiguration
Bearbeite die Dateien in `plugins/ChristmasPlugin/`:
- `config.yml` - Hauptkonfiguration
- `messages.yml` - Alle Nachrichten anpassen
- `gifts.yml` - Adventskalender Geschenke definieren

### Schritt 4: Reload (optional)
Nach Änderungen der Config:
```
/christmas reload
```

---

## 🎮 Commands

### 📅 Advent Calendar Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/adventcalendar` | Öffnet den Adventskalender GUI | `christmasplus.adventcalendar` |
| `/adventcalendar reset <player> <day\|all>` | Resettet einen Tag oder alle Tage für einen Spieler | `christmasplus.adventcalendar.reset` |
| `/adventcalendar test` | Aktiviert/Deaktiviert Test-Modus (alle Tage claimen) | `christmasplus.adventcalendar.test` |

**Aliases:** `/ac`, `/calendar`, `/advent`

**Beispiele:**
```
/adventcalendar                    # Öffnet den Kalender
/adventcalendar reset Notch 15     # Resettet Tag 15 für Notch
/adventcalendar reset Notch all    # Resettet alle Tage für Notch
/adventcalendar test               # Aktiviert Test-Modus
```

---

### 🎅 Christmas Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/christmas snow` | Schneeeffekt für sich selbst an/ausschalten | `christmasplus.christmas.snow` |
| `/christmas santa start [time] [world] [x] [y] [z]` | Startet Santa Event an bestimmter Position | `christmasplus.christmas.santa` |
| `/christmas santa start [time] random` | Startet Santa Event an zufälliger Location | `christmasplus.christmas.santa` |
| `/christmas santa stop` | Stoppt das laufende Santa Event | `christmasplus.christmas.santa` |
| `/christmas santa teleport` | Teleportiert zu Santa's Position | `christmasplus.christmas.santa` |
| `/christmas reload` | Lädt die Konfiguration neu | `christmasplus.christmas.reload` |

**Aliases:** `/xmas`

**Zeitformat:** `5s`, `10m`, `1h` (Sekunden, Minuten, Stunden)

**Beispiele:**
```
/christmas snow                           # Toggle Schneeeffekt
/christmas santa start 10m                # Santa für 10 Minuten an deiner Position
/christmas santa start 5m random          # Santa für 5 Minuten an zufälliger Location
/christmas santa start 30m world 0 64 0   # Santa für 30 Min an bestimmter Position
/christmas santa stop                     # Event stoppen
/christmas santa teleport                 # Zu Santa teleportieren
/christmas reload                         # Config neu laden
```

---

### 🎁 Cracker Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/cracker list` | Zeigt alle verfügbaren Cracker-Typen | `christmasplus.cracker.list` |
| `/cracker give <player> <cracker> [amount]` | Gibt einem Spieler Crackers | `christmasplus.cracker.give` |
| `/cracker get <cracker> [amount]` | Gibt dir selbst Crackers | `christmasplus.cracker.get` |

**Aliases:** `/crackers`

**Standard Cracker-Typen:** `basic`, `premium`

**Beispiele:**
```
/cracker list                    # Zeigt alle Cracker-Typen
/cracker give Notch basic 5      # Gibt Notch 5 Basic Crackers
/cracker get premium 1           # Gibt dir 1 Premium Cracker
```

---

## 🔐 Permissions

### Haupt-Permissions

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `christmasplus.*` | Alle Permissions | OP |
| `christmasplus.adventcalendar` | Zugriff auf Adventskalender | true |
| `christmasplus.christmas` | Zugriff auf /christmas Command | true |
| `christmasplus.cracker` | Zugriff auf /cracker Command | true |

### Adventskalender Permissions

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `christmasplus.adventcalendar` | Kalender öffnen | true |
| `christmasplus.adventcalendar.reset` | Tage für Spieler zurücksetzen | OP |
| `christmasplus.adventcalendar.test` | Test-Modus aktivieren | OP |

### Christmas Permissions

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `christmasplus.christmas.snow` | Schneeeffekt togglen | true |
| `christmasplus.christmas.santa` | Santa Event verwalten | OP |
| `christmasplus.christmas.reload` | Config neu laden | OP |

### Cracker Permissions

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `christmasplus.cracker.list` | Cracker-Liste anzeigen | true |
| `christmasplus.cracker.give` | Crackers an andere geben | OP |
| `christmasplus.cracker.get` | Crackers für sich selbst | OP |
| `christmasplus.cracker.get.multiple` | Mehrere Crackers auf einmal | OP |

### Geschenke-Gruppen Permissions

| Permission | Beschreibung | Priorität |
|------------|--------------|-----------|
| `christmasplus.group.donor` | Donor Geschenke (beste Rewards) | 3 |
| `christmasplus.group.premium` | Premium Geschenke | 2 |
| `christmasplus.group.vip` | VIP Geschenke | 1 |
| *(keine)* | Default Geschenke | 0 |

**Hinweis:** Spieler erhalten automatisch die Geschenke der höchsten Gruppe, zu der sie Zugriff haben!

---

## ⚙️ Konfiguration

### 📁 Konfigurationsdateien

Das Plugin erstellt folgende Dateien im Ordner `plugins/ChristmasPlugin/`:

| Datei | Beschreibung |
|-------|--------------|
| `config.yml` | Hauptkonfiguration - Alle Features und Einstellungen |
| `messages.yml` | Alle Nachrichten des Plugins (vollständig anpassbar) |
| `gifts.yml` | Adventskalender Geschenke für alle 25 Tage |
| `calendar-data.yml` | Spielerdaten (automatisch erstellt, nicht bearbeiten!) |
| `paper-plugin.yml` | Plugin-Metadaten (nicht bearbeiten!) |

---

### 🔧 config.yml - Wichtige Einstellungen

#### Adventskalender
```yaml
advent-calendar:
  enabled: true                    # Kalender aktiviert?
  allow-previous-days: true        # Vorherige Tage claimen erlauben?
  start-date: "12-01"             # Startdatum (Monat-Tag)
  end-date: "12-25"               # Enddatum
  gui:
    title: "&c&lAdvent Calendar"  # GUI-Titel
    size: 54                       # GUI-Größe (9, 18, 27, 36, 45, 54)
```

#### Santa Claus Event
```yaml
santa-claus:
  enabled: true
  default-duration: 600           # Standard-Dauer in Sekunden (10 Min)
  entity-type: "VILLAGER"         # Entity-Typ für Santa
  name: "&c&lSanta Claus &f&l🎅"  # Santa's Name
  gift-drop:
    enabled: true
    chance: 15.0                  # Drop-Chance in % (0-100)
    interval: 5                   # Sekunden zwischen Drops
  snow-trail:
    enabled: true                 # Schneespur aktiviert?
    fade-delay: 100              # Ticks bis Schnee verschwindet
```

#### Christmas Crackers
```yaml
crackers:
  enabled: true
  types:
    basic:                        # Cracker-ID
      name: "&e&lBasic Cracker"   # Anzeigename
      material: "PAPER"           # Item-Material
      glow: true                  # Glow-Effekt?
      rewards:                    # Alle Belohnungen (Spieler bekommt ALLES)
        - type: "ITEM"
          item: "DIAMOND"
          amount: 1
        - type: "ITEM"
          item: "EMERALD"
          amount: 3
```

**Hinweis:** Spieler erhalten **ALLE** konfigurierten Items beim Öffnen eines Crackers, keine Wahrscheinlichkeiten!

#### Schneeeffekt
```yaml
snow-effect:
  enabled: true
  default-intensity: 3            # Standard-Intensität (1-5)
  disabled-worlds:                # Welten ohne Schnee
    - "world_nether"
    - "world_the_end"
```

---

### 💬 messages.yml - Nachrichten anpassen

Alle Nachrichten können in `messages.yml` angepasst werden:

```yaml
prefix: "&c&l[Christmas] &r"

advent-calendar:
  claimed-success: "{prefix}&aYou have claimed your gift for day {day}!"
  already-claimed: "{prefix}&cYou have already claimed this day's gift!"
  not-available-yet: "{prefix}&cThis day is not available yet!"

santa:
  event-started: "{prefix}&aSanta Claus has arrived at &e{world} ({x}, {y}, {z})&a!"
  event-stopped: "{prefix}&cSanta Claus has left! See you next time!"
```

**Verfügbare Platzhalter:**
- `{prefix}` - Plugin-Prefix
- `{player}` - Spielername
- `{day}` - Tag-Nummer
- `{amount}` - Anzahl
- `{world}`, `{x}`, `{y}`, `{z}` - Koordinaten

---

### 🎁 gifts.yml - Geschenke konfigurieren

Definiere Geschenke für jeden Tag und jede Gruppe:

```yaml
day-1:
  groups:
    default:                      # Standard-Spieler
      items:
        - type: "ITEM"
          material: "DIAMOND"
          amount: 1
          name: "&b&lDay 1 Diamond"
          lore:
            - "&7A special gift!"
          enchantments:
            - "SHARPNESS:3"
            
    vip:                         # VIP-Spieler
      items:
        - type: "ITEM"
          material: "DIAMOND"
          amount: 3              # VIPs bekommen mehr!
        - type: "COMMAND"        # Zusätzlicher Befehl
          command: "give {player} emerald 5"
        - type: "PERMISSION"     # Temporäre Permission
          permission: "some.permission"
          duration: 3600         # Sekunden (-1 = permanent)
```

**Gift-Typen (Adventskalender):**
- `ITEM` - Items mit Enchantments, Lore, etc.
- `COMMAND` - Führt Command aus (z.B. für andere Plugins)
- `PERMISSION` - Gibt temporäre oder permanente Permission (benötigt Vault)

**Reward-Typen (Christmas Crackers):**
- `ITEM` - Items (Spieler erhält ALLE konfigurierten Items)
- `COMMAND` - Führt Command aus

**Hinweis:** Crackers unterstützen keine Permission-Belohnungen. Verwende stattdessen Commands oder den Adventskalender.

---

## 📖 Verwendung

### Für Spieler

#### 🎄 Adventskalender nutzen
1. Nutze `/adventcalendar` oder `/ac` um den Kalender zu öffnen
2. Klicke auf einen verfügbaren Tag (grünes Wool)
3. Erhalte deine Geschenke automatisch
4. Bereits geclaimte Tage sind gelb markiert
5. Zukünftige Tage sind rot und gesperrt

#### ❄️ Schneeeffekt
```
/christmas snow     # Schnee aktivieren/deaktivieren
```
Der Schneeeffekt ist rein visuell und hat keine Auswirkungen auf Gameplay!

#### 🎁 Crackers verwenden
1. Erhalte einen Cracker von einem Admin: `/cracker give <dein Name> basic 1`
2. **Rechtsklick** mit dem Cracker in der Hand
3. Erhalte eine zufällige Belohnung!

---

### Für Admins

#### 🎅 Santa Event starten
```bash
# An deiner Position für 10 Minuten
/christmas santa start 10m

# An zufälliger Location für 30 Minuten
/christmas santa start 30m random

# An bestimmter Position
/christmas santa start 15m world 0 64 0

# Event stoppen
/christmas santa stop

# Zu Santa teleportieren
/christmas santa teleport
```

#### 📅 Adventskalender verwalten
```bash
# Tag 15 für Spieler zurücksetzen
/adventcalendar reset Notch 15

# Alle Tage zurücksetzen
/adventcalendar reset Notch all

# Test-Modus aktivieren (alle Tage verfügbar)
/adventcalendar test
```

#### 🎁 Crackers verteilen
```bash
# Crackers an Spieler geben
/cracker give Notch basic 5
/cracker give Notch premium 1

# Alle verfügbaren Typen anzeigen
/cracker list
```

#### 🔄 Config neu laden
```bash
/christmas reload
```

---

## 📦 Dependencies

### ✅ Benötigt

| Dependency | Version | Download |
|------------|---------|----------|
| **Paper** | 1.21+ | [papermc.io](https://papermc.io/downloads) |
| **Java** | 21+ | [adoptium.net](https://adoptium.net/) |

### 🔌 Optional

| Plugin | Funktion | Download |
|--------|----------|----------|
| **Vault** | Permissions-System für Geschenke-Gruppen | [Spigot](https://www.spigotmc.org/resources/vault.34315/) |
| **WorldGuard** | Santa in geschützten Regionen spawnen | [EngineHub](https://enginehub.org/worldguard) |
| **LuckPerms** | Permissions-Plugin (für Vault) | [luckperms.net](https://luckperms.net/) |

**Hinweis:** Das Plugin funktioniert auch ohne Vault und WorldGuard, aber einige Features sind dann eingeschränkt!

---

## 🛠️ Entwicklung

### Projekt-Struktur
```
Christmas Plugin/
├── pom.xml                           # Maven Konfiguration
├── README.md                         # Diese Datei
└── src/main/
    ├── java/xyz/niliees/christmasPlugin/
    │   ├── ChristmasPlugin.java      # Hauptklasse
    │   ├── commands/                 # Command Handler
    │   │   ├── AdventCalendarCommand.java
    │   │   ├── ChristmasCommand.java
    │   │   └── CrackerCommand.java
    │   ├── config/                   # Config Manager
    │   │   └── ConfigManager.java
    │   ├── listeners/                # Event Listener
    │   │   ├── AdventCalendarListener.java
    │   │   ├── CrackerListener.java
    │   │   └── SantaListener.java
    │   ├── managers/                 # Feature Manager
    │   │   ├── AdventCalendarManager.java
    │   │   ├── CrackerManager.java
    │   │   ├── GiftManager.java
    │   │   ├── MessageManager.java
    │   │   ├── SantaManager.java
    │   │   └── SnowEffectManager.java
    │   └── models/                   # Datenmodelle
    │       ├── Gift.java
    │       └── GiftType.java
    └── resources/
        ├── config.yml                # Hauptkonfiguration
        ├── messages.yml              # Nachrichten
        ├── gifts.yml                 # Geschenke
        └── paper-plugin.yml          # Plugin-Metadaten
```

### Build
```bash
# Plugin kompilieren
mvn clean package

# Output: target/Christmas_Plugin-1.0-SNAPSHOT.jar
```

### Technische Details
- **Plugin API:** Paper Plugin API (nicht Bukkit!)
- **Command Registration:** Programmatisch (nicht YAML-basiert)
- **Java Version:** 21 (mit modernen Features)
- **Architektur:** Manager-Pattern für saubere Code-Organisation
- **Performance:** Async-Tasks wo möglich, effiziente Datenstrukturen

---

## 🐛 Troubleshooting

### Problem: Plugin lädt nicht
```
Error: Paper plugins do not support YAML-based command declarations
```
**Lösung:** Stelle sicher, dass du die neueste Version verwendest. Commands sind programmatisch registriert!

### Problem: Vault-Warnung
```
✗ Vault not found! Some features may not work correctly.
```
**Lösung:** Das ist nur eine Warnung! Plugin funktioniert ohne Vault, aber Geschenke-Gruppen brauchen Vault + Permissions-Plugin.

### Problem: Commands funktionieren nicht
**Lösung:** 
1. Prüfe ob du die richtige Permission hast
2. Nutze Tab-Completion um Commands zu vervollständigen
3. Prüfe die Console für Fehler

### Problem: Santa spawnt nicht
**Lösung:**
1. Prüfe ob Santa in `config.yml` aktiviert ist
2. Bei `random` spawn: Sind Locations konfiguriert?
3. Bei WorldGuard: Ist `ignore-worldguard: true` gesetzt?

---

## 📞 Support

### Bei Problemen oder Fragen:

- 🌐 **Website:** [nsce.fr](https://nsce.fr)
- 💻 **GitHub:** [github.com/niliees](https://github.com/niliees)
- 📧 **Email:** support@nsce.fr

### Bug Reports
Bitte erstelle ein Issue auf GitHub mit:
- Server-Version (Paper/Spigot + Version)
- Java-Version
- Plugin-Version
- Vollständige Fehlermeldung aus der Console
- Schritte zur Reproduktion

---

## 📝 Changelog

### Version 1.0-SNAPSHOT
- ✨ Initial Release
- ✅ Adventskalender mit 25 Tagen
- ✅ Santa Claus Event System
- ✅ Christmas Crackers
- ✅ Schneefall-Effekt
- ✅ Vollständige Vault-Integration
- ✅ WorldGuard-Support
- ✅ Paper Plugin API Support

---

## �� Lizenz

**Proprietary License**

Dieses Plugin wurde von **niliees** entwickelt.  
© 2024 NSCE - Alle Rechte vorbehalten.

**Nutzungsbedingungen:**
- ✅ Verwendung auf privaten und öffentlichen Servern erlaubt
- ❌ Weiterverkauf oder Redistribution verboten
- ❌ Dekompilierung oder Reverse Engineering verboten
- ✅ Modifikation der Konfigurationsdateien erlaubt

Bei Fragen zur Lizenz: support@nsce.fr

---

<div align="center">

**Entwickelt mit ❤️ und ☕ von [niliees](https://nsce.fr)**

🎄 Frohe Weihnachten! 🎅

</div>


