# Étape 1 : Construire l'application avec Node.js
FROM node:lts-alpine3.19 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le package.json et package-lock.json
COPY package.json package-lock.json ./

#Installer les dépendances du projet
RUN npm install

# Installer sass globalement
RUN npm install -g sass

# Copier tous les fichiers source du frontend
COPY . .

# Créer le dossier dist/css avant de compiler le SCSS
RUN mkdir -p dist/css && npm run build-css

# Étape 2 : Configurer le serveur Nginx pour servir les fichiers compilés
FROM nginx:stable-alpine3.20

# script d'entrée + config template
COPY nginx/nginx.conf.template /etc/nginx/nginx.conf.template
COPY init.sh /init.sh
RUN chmod +x /init.sh

# Copier les fichiers compilés dans Nginx (tout le dossier dist)
COPY --from=build /app/dist /usr/share/nginx/html

# Exposer le port 80 pour accéder à l'application
EXPOSE 80

# Lancement dynamique
ENTRYPOINT ["/init.sh"]