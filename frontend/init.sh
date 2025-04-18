#!/bin/sh
# Remplace la variable ${API_BASE_URL} dans le template,
# puis démarre Nginx au premier plan.
envsubst '${API_BASE_URL}' \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/nginx.conf

exec nginx -g 'daemon off;'