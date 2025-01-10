
# Projekt: SWKOM_WS2024_GroupE

## Übersicht

Dieses Projekt enthält eine Softwarearchitektur mit UI, REST-Schnittstelle, OCR-Worker, ElasticSearch, RabbitMQ, MinIO und PostgreSQL. Diese Services werden mit Docker Compose in Containern ausgeführt. 

## Voraussetzungen

- Docker installiert
- Docker Compose installiert
- Jedes Projekt enthält funktionierende `Dockerfiles` in den zugehörigen Projektordnern und das `docker-compose.yml`.

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
**servicename** can be found in the docker-compose.yml file, e.g. paperlessrest
```bash
docker-compose start <servicename>
```

## System Components

1. **PaperlessREST** (`http://localhost:8081/swagger-ui/index.html`):
   - Backend service handling document uploads, searches, and API endpoints.
   - Uses PostgreSQL for database storage, MinIO for file storage, and Elasticsearch for document indexing.

2. **PaperlessUI** (`localhost`):
   - The user interface for interacting with the system.
   - Allows users to upload, search, and manage documents.

3. **PostgreSQL** (`localhost:5432`):
   - The database for storing document metadata.
   - User: `paperless`, Password: `paperless`, Database: `paperless_DB`.

4. **Adminer** (`...`):
   - missing

5. **RabbitMQ** (`localhost:15672` for UI, `localhost:5672` for messaging):
   - Message broker for communication between services.
   - Default user: `paperless`, Password: `paperless`.

6. **MinIO** (`localhost:9000` for API, `localhost:9090/browser/documents` for console):
   - File storage system for uploaded documents.
   - Root user: `paperless`, Password: `paperless`.

7. **PaperlessWORKER** (`localhost:8082`):
   - Worker service that processes tasks such as OCR or data extraction.

8. **Elasticsearch** (`localhost:9200`):
   - Search engine for indexing and querying document contents.

9. **Elasticsearch-UI (Kibana)** (`localhost:9092/app/discover`):
   - Visualizes data stored in Elasticsearch.
   - Add data visualization to see elastic data (name: `paperless`, index: `documents`)

## Problems with fetching Updates from Repository
```bash
git fetch
```
(not git fetch --all)

```bash
git reset --hard origin/<branch> 
```
(NOT git reset --hard HEAD)

```bash
git clean -fd 
```
(this is required to get rid of spurious files which reset doesn't get rid of)
