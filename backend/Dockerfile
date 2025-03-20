# backend/Dockerfile
# étape 1 : Build avec Maven
FROM maven:3.9-eclipse-temurin-21 AS build

# Création du répertoire de travail
WORKDIR /appJo

# Correction des chemins de copie
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# étape 2 : Exécution avec OpenJDK
# image java slim pour réduire la taille de l'image.
FROM openjdk:21-jdk-slim

# copie du jar
COPY --from=build /appJo/target/*.jar appJo.jar

ENTRYPOINT ["java", "-jar", "appJo.jar", "--server.port=8080"]
EXPOSE 8080
