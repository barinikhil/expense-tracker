# syntax=docker/dockerfile:1

FROM nginx:1.27-alpine

COPY dist/expense-tracker-frontend/browser /usr/share/nginx/html/

CMD nginx -g "daemon off;"
