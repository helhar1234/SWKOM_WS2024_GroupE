# Projekt: PaperlessREST

## Start Database
Die DB wird gestartet, indem Docker Compose ausgeführt wird:

```bash
docker-compose build
```

```bash
docker-compose up
```

### Alternative Start Database
Um NUR die DB zu starten kann man auch folgendes ausführen:
```bash
docker-compose start postgres
```

## Connect to DB via pgAdmin (for db Visualization)
### 1. Download pgAdmin
https://www.pgadmin.org/download/

### 2. Add a new Server
In pgAdmin, click on "Add new Server"

## 3. Properties
Fill in the needed Properties:
![Properties 1](pgAdmin_Connection-Properties.png.png)
![Properties 2](pgAdmin_Connection-Properties2.png.png)

POSTGRES NEEDS TO BE RUNNING FOR THIS TO WORK!