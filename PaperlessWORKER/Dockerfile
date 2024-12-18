FROM openjdk:17-jdk-slim

WORKDIR /app

# Kopiere die JAR-Datei
COPY ./target/PaperlessWORKER-0.0.1-SNAPSHOT.jar paperless-worker.jar

# Führt die eben kopierte JAR aus
CMD ["java", "-jar", "paperless-worker.jar"]

RUN apt-get update && apt-get install -y \
    ghostscript \
    tesseract-ocr \
    tesseract-ocr-eng
