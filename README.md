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
* MySQL 8.0.30+ (NB: proper spatial support is a requirement)
* Quarkus 2.7+
* JBoss Keycloak 16+
* Apache ActiveMQ (preferably Artemis - Optional)

## Development Set-Up

The [niord-appsrv](https://github.com/NiordOrg/niord-appsrv) project contains 
scripts for setting up Quarkus, Keycloak, etc.

However, the easiest way to get started developing on this project is to use
Docker.

To create a MySQL database container for Niord you can run:

    docker run -p 3306:3306 --name mysql -d -e MYSQL_DATABASE=niord -e MYSQL_USER=niord -e MYSQL_PASSWORD=niord -e MYSQL_ROOT_PASSWORD=root_password mysql:8.0.30

While to setup Keycloak using docker (with MySQL), you can run:

    docker network create keycloak-network
    docker run --name mysql_kc -d --net keycloak-network -e MYSQL_DATABASE=keycloak -e MYSQL_USER=keycloak -e MYSQL_PASSWORD=password -e MYSQL_ROOT_PASSWORD=root_password mysql:8.0.30 
    docker run -p 8080:8080 --name keycloak -d --net keycloak-network -e KEYCLOAK_USER=user -e KEYCLOAK_PASSWORD=admin -e DB_VENDOR=mysql -e DB_ADDR=mysql_kc -e DB_DATABASE=keycloak -e DB_USER=keycloak -e DB_PASSWORD=password jboss/keycloak:latest

For the ApacheMQ message broker you can find more information in the project
[web page](https://activemq.apache.org/download.html). In a nutshell, download
the artemis zip file and extract it. Then create a new broker and run it:

    apache-artemis/bin/artemis create ../broker/
    apache-artemis/broker/artemis-service start

Note that while generating the broker, you will be asked to provide a
name for the broker, a username and a password.

### Application Configuration

Before continuing you should create the keycloak niord realm required. Log
into the keycloak server under *http://localhost:8080* using the username and
password that you selected. Then create a new realm and provide the
*niord-boostrap-realm.json* as the JSON input. You can then need to get 
access to the realm and client secrets and configuration.

Once all the back-end services are in place, the Niord system should be also 
be appropriately configured to interact with them. This configuration is 
included in two files

* niord-uk/web/src/main/resources/application.properties
* niord-uk/web/src/main/resources/META-INF/persistence.xml

The second file is only required because quarkus doesn't support all the
desirable configuration for hibernate-search using Lucene undexing, i.e.
to put all the generated indexes in one directory so we keep everything tidy.

In the *application.properties* file you will need to enter the details for 

the OIDC client and Keycloak admin configurations:

    quarkus.oidc.auth-server-url=http://<keycloak-server>:8080/auth/realms/niord
    keycloak.admin.clientId=<client-name>
    keycloak.admin.clientSecret=<client-secret>

the MySQL database connection (also update the *persistence.xml*):

    quarkus.datasource.jdbc.url=jdbc:mysql://<mysql-db-server>:3306/niord
    quarkus.datasource.db-kind=mysql
    quarkus.datasource.username=<mysql-db-username>
    quarkus.datasource.password=<mysql-db-password>

and finally the Apache Artemis MQ communication:

    # Configures the Artemis MQ properties.
    quarkus.qpid-jms.url=amqp://<amp-server>:5672
    quarkus.qpid-jms.username=<amp-username>
    quarkus.qpid-jms.password=<amp-password>

### Finishing touches

To then run Niord under quarkus you should run the quarkus development maven
goal under the *niord-uk-web* module directory. 

    mvn quarkus:dev

The final step is to import the Danish test base data into Niord. To do so
copy the provided *niord-dev-basedata.zip* in the following directory (under
your own user's) main folder. You will also need to create the full folder
structure if it doesn't already exist.

    ~/.niord/batch-jobs/batch-sets/
    
Within a minute or so, this will import domains, areas, categories, etc. needed
to run the Niord-DK project. First clean up a bit:
* In Niord, under Sysadmin -> Domains, click the "Create in Keycloak" button 
  for the "NW" and "NM" domains. This will create the two domains in Keycloak. 
* In Keycloak, edit the "Sysadmin" user group. Under "Role Mappings", in the 
  drop-down "Client Roles" select first "niord-nw" then "niord-nm" and assign 
  the "sysadmin" client roles to the group.
* While in Keycloak, you may also want to define new user groups for editors
  and admins, and assign the appropriate client roles for "niord-nw" and
  "niord-nm" to the groups. Additionally, for admin-related groups (who should
  be able to manage users in Niord), assign the "manage-clients" and
  "manage-users" client roles of the "realm-management" client to the groups.
* Delete the "Master" domain in Niord and the corresponding
  "niord-client-master" client in Keycloak (Optional).
* Go through the configuration and settings of the Niord Sysadmin pages and 
  adjust as appropriate.

Finally, success! You can open your browser and point it to the following
location *http://localhost:8888*. You should be able to see the AngularJS
front-end of Niord! Login using the sysadmin (both username and password)
credentials.

