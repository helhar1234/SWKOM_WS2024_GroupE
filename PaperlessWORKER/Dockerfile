FROM openjdk:17-jdk-slim

# Installiere Ghostscript und Tesseract
RUN apt-get update && apt-get install -y tesseract-ocr tesseract-ocr-eng ghostscript

WORKDIR /app

# Kopiere die JAR-Datei
COPY ./target/PaperlessWORKER-0.0.1-SNAPSHOT.jar paperless-worker.jar


# Expose Port 8082 (optional, nur zur Dokumentation)
EXPOSE 8082

# Führt die JAR aus
CMD ["java", "-jar", "paperless-worker.jar"]
