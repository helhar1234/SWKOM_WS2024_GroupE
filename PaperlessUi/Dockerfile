# Dockerfile for serving static files with Nginx
FROM nginx:alpine

# Kopiere die statischen Dateien in das Nginx-Webverzeichnis
COPY ./index.html /usr/share/nginx/html/
COPY ./css /usr/share/nginx/html/css/
COPY ./js /usr/share/nginx/html/js/

# Nginx läuft standardmäßig auf Port 80
EXPOSE 80