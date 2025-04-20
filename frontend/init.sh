#!/bin/sh

# Explicitly set default values (Heroku should override PORT)
export PORT="${PORT:-80}"
export API_BASE_URL="${API_BASE_URL:-localhost:8080}"

# Set Nginx specific variables as environment variables
export NGINX_HOST="$host"
export NGINX_REAL_IP="$remote_addr"
export NGINX_FORWARDED_FOR="$proxy_add_x_forwarded_for"

echo "PORT is: $PORT"
echo "API_BASE_URL is: $API_BASE_URL"
echo "NGINX_HOST is: $NGINX_HOST"
echo "NGINX_REAL_IP is: $NGINX_REAL_IP"
echo "NGINX_FORWARDED_FOR is: $NGINX_FORWARDED_FOR"

# Substitution des variables dans la conf nginx
envsubst '$PORT $API_BASE_URL $NGINX_HOST $NGINX_REAL_IP $NGINX_FORWARDED_FOR' \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/nginx.conf

# Debug : afficher le contenu généré
echo "======= nginx.conf généré ======="
cat /etc/nginx/nginx.conf
echo "================================="

# Lancement de nginx en mode foreground
exec nginx -g 'daemon off;'