# Étape 1 : Build de l'application avec Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

# Variable d’environnement pour définir le fuseau horaire sur UTC.
ENV TZ=Etc/UTC \
    JAVA_OPTS="-Duser.timezone=UTC"

# Définition du répertoire de travail dans le conteneur de build
WORKDIR /app

# Copie du fichier pom.xml pour télécharger les dépendances en premier
COPY pom.xml .

# Copie du reste du code source de l'application
COPY src ./src

RUN mvn clean package -DskipTests -Pdocker

# Étape 2 : Création de l'image finale d'exécution
# Utilisation d'une image JRE Alpine légère pour réduire la taille de l'image finale.
FROM eclipse-temurin:21-jre-alpine

# Définition du répertoire de travail dans le conteneur final
WORKDIR /app

# Copie du fichier WAR build vers l'image finale
COPY --from=build /app/target/*.war app.war

# Exposition du port sur lequel l'application Spring Boot écoute à l'intérieur du conteneur.
EXPOSE 8080

# Options de la JVM pour l'exécution
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -Xss512k -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m -XX:NativeMemoryTracking=summary -Duser.timezone=UTC -XX:+ExitOnOutOfMemoryError -XX:+UnlockDiagnosticVMOptions -XX:+PrintNMTStatistics -Xlog:gc*:file=/tmp/gc.log:time,level,tags:filecount=5,filesize=10m"

# Commande pour démarrer l'application lorsque le conteneur est lancé.
ENTRYPOINT ["java", "-jar", "/app/app.war"]

