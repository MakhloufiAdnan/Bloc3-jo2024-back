#!/bin/sh
# Remplace ${PORT} et ${API_BASE_URL} par leurs vraies valeurs d’environnement
envsubst "${PORT} ${API_BASE_URL}" \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/nginx.conf

# Démarre nginx au premier plan
exec nginx -g 'daemon off;'