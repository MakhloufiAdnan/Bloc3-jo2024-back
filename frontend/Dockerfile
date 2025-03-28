# Étape 1 : Construire l'application avec Node.js
FROM node:23.0-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm install

# Copier tous les fichiers source du frontend
COPY . .

# Créer le dossier dist/css avant de compiler le SCSS
RUN mkdir -p dist/css && npm run build-css

# Étape 2 : Configurer le serveur Nginx pour servir les fichiers compilés
FROM nginx:stable-alpine3.20

# Copie de la configuration Nginx personnalisée
COPY ./nginx/nginx.conf /etc/nginx/nginx.conf

# Copier les fichiers compilés dans Nginx (tout le dossier dist)
COPY --from=build /app/dist /usr/share/nginx/html

# Exposer le port 80 pour accéder à l'application
EXPOSE 80

# Démarrer le serveur Nginx en mode "foreground"
CMD ["nginx", "-g", "daemon off;"]


