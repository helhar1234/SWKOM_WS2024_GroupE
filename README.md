
# Projekt: SWKOM_WS2024_GroupE

## Übersicht

Dieses Projekt enthält einen REST-Server und ein WebUI, der mit Docker Compose in Containern ausgeführt wird. Der Server wird auf dem lokalen Port 8081 bereitgestellt, und Swagger ist ebenfalls verfügbar. Die WebUI wird auf dem lokalen Port 80 bereitgestellt.

## Voraussetzungen

- Docker installiert
- Docker Compose installiert
- Das Projekt enthält funktionierende `Dockerfiles` in den zugehörigen Projektordnern und das `docker-compose.yml`.

## Docker Compose Befehle

### 1. Docker-Image bauen
Um die Docker-Images zu bauen, verwende den folgenden Befehl:

```bash
docker-compose build
```

### 2. Container starten
Um die Container zu starten und die Anwendung auszuführen:

```bash
docker-compose up
```

Die Anwendung (WebUI) ist nun auf **http://localhost** verfügbar.

### 3. Container stoppen
Um die Container zu stoppen, nutze:

```bash
docker-compose down
```

### Only start single Services
<servicename> can be found in the docker-compose.yml file, e.g. paperlessrest
```bash
docker-compose start <servicename>
```

