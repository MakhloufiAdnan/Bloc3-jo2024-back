#!/bin/sh

# La commande 'exec' est importante, elle remplace le processus du script par le processus Java,
# ce qui permet à l'application de recevoir correctement les signaux de terminaison.
exec java ${JAVA_TOOL_OPTIONS} -jar /app/app.war