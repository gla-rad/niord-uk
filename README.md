# Niord-UK [![Build Status](https://travis-ci.com/NiordOrg/niord-dk.svg?branch=master)](https://travis-ci.com/NiordOrg/niord-dk)

The niord-uk project contains the GRAD UK-specific extensions for the   
[niord](https://github.com/NiordOrg) system, i.e. a system for producing and
promulgating NW + NM T&P messages.

From version 3.0.0 onwards, Niord has been ported and run using the Red Hat
[Quarkus](https://quarkus.io/) framework.

Niord is documented extensively at http://docs.niord.org

# niord-uk-web

The *niord-web* web-application, found under the
[niord](https://github.com/gla-rad/niord) project, constitutes the main system 
for producing and promulgating navigational warnings and notices to mariners.

The *niord-uk-web* project is an overlay web-application that customizes 
*niord-web* for use in the UK for the General Lighthouse Authorities of UK
and Ireland. 

## Prerequisites

* Java 11 (Java 17 is also supported but jBerret causes issues)
* Maven 3.8.1
* MySQL 5.7.10+ (NB: proper spatial support is a requirement)
* Quarkus 2.7+
* JBoss Keycloak 12+
* Apache ActiveMQ (5 or Artemis)

## Development Set-Up

The [niord-appsrv](https://github.com/NiordOrg/niord-appsrv) project contains 
scripts for setting up Quarkus, Keycloak, etc.

However, the easiest way to get started developing on this project is to use
Docker.

    docker run -p 3306:3306 --name mysql -e MYSQL_DATABASE=keycloak -e MYSQL_USER=keycloak -e MYSQL_PASSWORD=password -e MYSQL_ROOT_PASSWORD=mysql -d mysql:latest
    docker run -p 8080:8080 -e KEYCLOAK_USER=user -e KEYCLOAK_PASSWORD=admin jboss/keycloak:latest

For the ApacheMQ message broken you can find more information in the project
[web page](https://activemq.apache.org/download.html).

### Starting MySQL and Keycloak

You may want to start by creating a *.env* file in your working directory, which
overrides the environment variables (such as database passwords) used in the 
docker compose file.

The following commands will start two MySQL databases, one for the application
server and one for Keycloak, and also run Keycloak itself.

    mkdir $HOME/.niord-dk
    docker-compose -f dev/docker-dev-compose.yml pull
    docker-compose -f dev/docker-dev-compose.yml up -d

The initial *mkdir* command is just to avoid permission problems since docker
would otherwise create it as owned by root.

Once this is up and running, create a Keycloak admin user (default 
niordadmin/keycloak) which can be used to create user groups and assign domain
roles to the groups:

    ./dev/keycloak-admin-user.sh

Enter [http://localhost:8090/auth/](http://localhost:8090/auth/) and check that
you can log in using the Keycloak admin user.

The next important step is to initialise the keycloak "niord" realm for
authentication. This can be done easily by importing the
*niord-bootstrap-realm.json* configuration file, found in the project's
top directory.

The final important step is to initialise a MySQL database with the name
*niord*. Here is a copy of the script also available in the
[niord-appsrv](https://github.com/NiordOrg/niord-appsrv) repo:

    CREATE DATABASE niord CHARACTER SET utf8 COLLATE utf8_general_ci;
    CREATE USER 'niord'@'localhost' IDENTIFIED BY 'niord';
    GRANT ALL PRIVILEGES ON *.* TO 'niord'@'localhost' WITH GRANT OPTION;
    CREATE USER 'niord'@'%' IDENTIFIED BY 'niord';
    GRANT ALL PRIVILEGES ON *.* TO 'niord'@'%' WITH GRANT OPTION;
    FLUSH PRIVILEGES;

### Finishing touches

Import the Danish test base data into Niord:

    ./dev/install-base-data.sh
    
Within a minute or so, this will import domains, areas, categories, etc. needed to run the Niord-DK project. 
First clean up a bit:
* In Niord, under Sysadmin -> Domains, click the "Create in Keycloak" button for 
  the "NW" and "NM" domains. This will create the two domains in Keycloak. 
* In Keycloak, edit the "Sysadmin" user group. Under "Role Mappings", in the 
  drop-down "Client Roles" select first "niord-nw" then "niord-nm" and assign 
  the "sysadmin" client roles to the group.
* While in Keycloak, you may also want to define new user groups for editors and
  admins, and assign the appropriate client roles for "niord-nw" and "niord-nm"
  to the groups. Additionally, for admin-related groups (who should be able to
  manage users in Niord), assign the "manage-clients" and "manage-users" client
  roles of the "realm-management" client to the groups.
* Delete the "Master" domain in Niord and the corresponding "niord-client-master"
  client in Keycloak.
* Go through the configuration and settings of the Niord Sysadmin pages and 
  adjust as appropriate.


