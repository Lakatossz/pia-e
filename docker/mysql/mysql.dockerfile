### cat mysql.dockerfile
##FROM mysql:latest
##
##MAINTAINER rosa@zcu.cz
##
##RUN chown -R mysql:root /var/lib/mysql/
##
##ARG MYSQL_DATABASE
##ARG MYSQL_USER
##ARG MYSQL_PASSWORD
##ARG MYSQL_ROOT_PASSWORD
##
##ENV MYSQL_DATABASE=$MYSQL_DATABASE
##ENV MYSQL_USER=$MYSQL_USER
##ENV MYSQL_PASSWORD=$MYSQL_PASSWORD
##ENV MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD
##
##ADD data.sql /etc/mysql/data.sql
##
##RUN sed -i 's/MYSQL_DATABASE/'$MYSQL_DATABASE'/g' /etc/mysql/data.sql
##RUN cp /etc/mysql/data.sql /docker-entrypoint-initdb.d
##
##EXPOSE 3306
#
## Use the official MySQL image as the base image
#FROM mysql:latest
#
## Set the environment variables for MySQL connection
#ENV MYSQL_DATABASE piae_v1_db
#ENV MYSQL_USER root
#ENV MYSQL_PASSWORD heslo
#ENV MYSQL_ROOT_PASSWORD heslo
#
## Optionally, you can provide an initialization script
## to create the database and tables when the container starts
#COPY ./init/init.sql /docker-entrypoint-initdb.d/
#
## You can also customize the MySQL configuration if needed
## COPY ./my-custom.cnf /etc/mysql/conf.d/
#
## The CMD instruction specifies the command to run on container startup
#CMD ["mysqld"]
#
## Expose the MySQL port
#EXPOSE 3306

FROM mysql:8.0.3

ENV MYSQL_DATABASE piae_v1_db
ENV MYSQL_USER root
ENV MYSQL_PASSWORD heslo
ENV MYSQL_ROOT_PASSWORD heslo

RUN #apt-get -y install locales

# Set the locale
#RUN sed -i '/cs_CZ.UTF-8/s/^# //g' /etc/locale.gen && \
#    locale-gen
ENV LANG cs_CZ.UTF-8
ENV LANGUAGE cs_CZ:cz
ENV LC_ALL cs_CZ.UTF-8

COPY /mysql/init/init.sql /docker-entrypoint-initdb.d

EXPOSE 3306
