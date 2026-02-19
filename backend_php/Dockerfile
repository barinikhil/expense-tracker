FROM php:8.3-apache

RUN docker-php-ext-install pdo pdo_mysql

ENV APACHE_DOCUMENT_ROOT=/var/www/html/public
RUN sed -ri -e 's!/var/www/html!${APACHE_DOCUMENT_ROOT}!g' /etc/apache2/sites-available/*.conf \
    && sed -ri -e 's!/var/www/!${APACHE_DOCUMENT_ROOT}!g' /etc/apache2/apache2.conf /etc/apache2/conf-available/*.conf \
    && sed -ri 's/AllowOverride None/AllowOverride All/g' /etc/apache2/apache2.conf \
    && a2enmod rewrite

COPY . /var/www/html

EXPOSE 9081

RUN sed -i 's/80/9081/g' /etc/apache2/ports.conf /etc/apache2/sites-available/000-default.conf
