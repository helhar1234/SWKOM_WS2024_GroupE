FROM openjdk:17-jdk-slim

WORKDIR /app

CMD ["java", "-jar", "PaperlessWORKER-0.0.1-SNAPSHOT.jar"]

# Kopiere den Service-Code
COPY ./target/PaperlessWORKER-0.0.1-SNAPSHOT.jar paperless-worker.jar

RUN apt-get update && apt-get install -y \
    ghostscript \
    tesseract-ocr \
    tesseract-ocr-eng
