# Use an official JDK runtime as a parent image
FROM openjdk:17-jdk-alpine
CMD ["java", "-jar", "target/PaperlessREST-0.0.1-SNAPSHOT.jar"]
# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/PaperlessREST-0.0.1-SNAPSHOT.jar paperless-rest.jar

# Expose port 8081
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "paperless-rest.jar"]


