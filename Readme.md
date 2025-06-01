Jeux Olympiques 2024 - Backend API
Ce dépôt contient le code source du backend de l'application des Jeux Olympiques 2024. Il s'agit d'une API RESTful développée avec Spring Boot en Java, gérant la logique métier, la persistance des données et l'authentification. Le déploiement est conteneurisé via Docker sur Heroku.

Technologies Utilisées
Java 21
Spring Boot 3.4.5
Spring Security (avec JWT pour l'authentification stateless)
Spring Data JPA (pour l'accès aux données)
PostgreSQL (base de données relationnelle)
Maven (outil de gestion de projet et de build)
Docker : Conteneurisation de l'application pour un déploiement facilité.
Heroku : Plateforme de déploiement (utilisant le buildpack container).
Structure du Projet
C:.
├───.dockerignore
├───.env
├───Dockerfile                 # Fichier Docker pour construire l'image du backend
├───heroku.yml                 # Configuration Heroku pour le déploiement Docker
├───pom.xml                    # Fichier Maven pour les dépendances et le build
├───src
│   ├───main
│   │   ├───java
│   │   │   └───fr
│   │   │       └───studi
│   │   │           └───bloc3jo2024
│   │   │               ├───config         # Configurations Spring (Security, JWT, CORS)
│   │   │               ├───controller     # Endpoints API REST (Auth, Admin, Offres, etc.)
│   │   │               ├───dto            # Objets de transfert de données (DTOs)
│   │   │               ├───entity         # Entités JPA (modèle de données)
│   │   │               ├───exception      # Gestion des exceptions personnalisées
│   │   │               ├───filter         # Filtres Spring Security (JWT)
│   │   │               ├───repository     # Interfaces Spring Data JPA pour l'accès DB
│   │   │               └───service        # Logique métier et services d'authentification
│   │   └───resources      # Fichiers de configuration de l'application (application.yml)
│   └───test               # Code source des tests unitaires et d'intégration
├───target                     # Répertoire de sortie après le build Maven
Installation et Lancement en Développement
Cloner le dépôt :

git clone https://github.com/MakhloufiAdnan/Bloc3-jo2024-back.git
cd bloc3_Jo2024
Configuration de la Base de Données :

Assurez-vous d'avoir une instance PostgreSQL en cours d'exécution.
Créez une base de données pour l'application (ex: jo2024_db).
Renseignez les informations de connexion à la base de données dans src/main/resources/application.yml ou via des variables d'environnement.
Populer l'administrateur : Pour le login admin, assurez-vous qu'un utilisateur avec le rôle ADMIN et un mot de passe hashé avec BCrypt existe dans votre base de données.

Pour valider les mails lors de l'inscription, vous pouvez télécharger : greenmail-standalone-2.1.0.jar depuisRéférentiel Maven GreenMail. 
Ensuite, Ouvrez votre terminal ou votre invite de commande et naviguez jusqu'au répertoire où vous avez téléchargé le fichier et 
exécutez la commande suivante : java -Dgreenmail.smtp.hostname=127.0.0.1 -Dgreenmail.smtp.port=3025 -Dgreenmail.api.hostname=127.0.0.1 -Dgreenmail.api.port=8025 -Dgreenmail.auth.disabled -jar greenmail-standalone-2.1.0.jar
Votre appplication.yml ou application.properties doit contenir les configurations suivantes pour le serveur SMTP :
spring.mail.host=localhost
spring.mail.port=25
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
spring.mail.properties.mail.smtp.starttls.required=false
spring.mail.properties.mail.smtp.ssl.enable=false

Lancer l'application Spring Boot :
Ouvrez un terminal dans le répertoire racine de votre backend (C:) et exécutez :

./mvn spring-boot:run

L'application démarrera et écoutera sur le port 8080.

 
Endpoints API Principaux
Authentification Utilisateur :
POST /api/auth/register : Inscription d'un nouvel utilisateur.
POST /api/auth/login : Connexion d'un utilisateur (retourne un JWT).
GET /api/auth/confirm?token={token}:Confirmation d'email.
POST /api/auth/password-reset-request?email={email} : Demande de réinitialisation de mot de passe.
POST /api/auth/password-reset?token={token}&newPassword={newPassword} : Réinitialisation du mot de passe.
Authentification Administrateur :
POST /api/admin/auth/login : Connexion de l'administrateur.
Note de Sécurité (Développement) : Actuellement, cet endpoint et d'autres endpoints /api/admin/** peuvent être temporairement configurés en permitAll() dans WebSecurityConfig.java pour faciliter le développement/démonstration.
Déploiement sur Heroku
Le déploiement est configuré via heroku.yml, qui indique à Heroku d'utiliser le Dockerfile pour construire et déployer l'application.

Variables d'environnement Heroku requises :

Ces variables doivent être configurées directement sur Heroku (via l'interface web ou la CLI heroku config:set) pour votre application backend :

ALWAYS_POSTGRES_HOST
ALWAYS_POSTGRES_PORT
ALWAYS_POSTGRES_DB
ALWAYS_POSTGRES_USER
ALWAYS_POSTGRES_PASSWORD
HIKARI_MAX_POOL_SIZE
HIKARI_MIN_IDLE
SMTP_HOST
SMTP_PORT
EMAIL_USERNAME
EMAIL_PASSWORD
JWT_SECRET (clé secrète forte pour la signature des JWTs)
JWT_EXPIRATION (durée de validité des JWTs en millisecondes)
ADMIN_EMAIL (email de l'administrateur défini dans les propriétés, si vous ne l'avez pas supprimé après le refactoring DB)
ADMIN_PASSWORD (hash BCrypt du mot de passe admin, si vous ne l'avez pas supprimé après le refactoring DB)
FRONTEND_URL_CONF (URL de votre frontend pour les liens de confirmation d'email)
FRONTEND_URL_RESET (URL de votre frontend pour les liens de réinitialisation de mot de passe)
PORT (Heroku injectera cela automatiquement, mais si vous utilisez une autre approche ou pour des tests, il doit être défini).