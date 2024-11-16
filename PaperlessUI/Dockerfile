# Dockerfile for serving static files with Nginx
FROM nginx:alpine

# Remove default Nginx content
RUN rm -rf /usr/share/nginx/html/*

# Copy static files into Nginx web directory
COPY ./index.html /usr/share/nginx/html/
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY ./assets /usr/share/nginx/html/assets/
COPY ./css /usr/share/nginx/html/css/
COPY ./js /usr/share/nginx/html/js/
COPY ./pages /usr/share/nginx/html/pages/

# Expose port 80 for HTTP
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
