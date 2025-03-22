# backend/Dockerfile
# étape 1 : téléchargement de Maven (version alpine pour réduire la taille de l'image)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Création du répertoire de travail app && cd
WORKDIR /app

# Copie du projet
COPY . .

# Téléchargement des dépendances et compilation
RUN mvn clean package -DskipTests

# étape 2 : Utilisation d'un JRE pour executer le code (version alpine pour réduire la taille de l'image)
FROM eclipse-temurin:21-jre-alpine

# Création du dossier app && cd
WORKDIR /app

# copie de l'appli compilé dans l'image du JRE
COPY --from=build /app/target/*.jar app.jar

#Expose du porte 8080
EXPOSE 8080

#La commande qui sera executée lors de lancement du container
ENTRYPOINT ["java", "-jar", "app.jar"]

