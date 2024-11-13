# Start from an OpenJDK base image
FROM openjdk:17-jdk-alpine

# Install Maven in the container
RUN apk add --no-cache maven

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven build file (pom.xml) first to leverage Docker layer caching
COPY pom.xml .

# Download all the dependencies (this step helps to leverage Docker's cache)
RUN mvn dependency:go-offline

#todo: better copy jar file after building outside... ?

# Copy the entire source code
COPY src ./src

# Build the Maven project (this runs clean and package)
RUN mvn clean package

# Expose the port that your application runs on (e.g., 8081 for a Spring Boot app)
EXPOSE 8081

# Set the entrypoint to run the jar file produced by Maven
CMD ["java", "-jar", "target/SWKOM_WS2024_GroupE-0.0.1-SNAPSHOT.jar"]
