# The GLA e-Navigation Service Architecture - Niord UK

## Quick Reference
* Maintained by:<br/>
[GRAD](https://www.gla-rad.org/)
* Where to get help:<br/>
[Unix & Linux](https://unix.stackexchange.com/help/on-topic),
[Stack Overflow](https://stackoverflow.com/help/on-topic),
[GRAD Wiki](https://rnavlab.gla-rad.org/wiki/E-Navigation_Service_Architecture)
(for GRAD members only)

## What is e-Navigation
The maritime domain is facing a number for challenges, mainly due to the
increasing demand, that may increase the risk of an accident or loss of life.
These challenges require technological solutions and e-Navigation is one such
solution. The International Maritime Organization ([IMO](https://www.imo.org/))
adopted a ‘Strategy for the development and implementation of e‐Navigation’
(MSC85/26, Annexes 20 and 21), providing the following definition of
e‐Navigation:

<div style="padding: 4px;
    background:lightgreen;
    border:2px;
    border-style:solid;
    border-radius:20px;
    color:black">
E-Navigation, as defined by the IMO, is the harmonised collection, integration,
exchange, presentation and analysis of maritime information on-board and ashore
by electronic means to enhance berth-to-berth navigation and related services,
for safety and security at sea and protection of the marine environment.
</div>

In response, the International Association of Lighthouse Authorities 
([IALA](https://www.iala-aism.org/)) published a number of guidelines such as 
[G1113](https://www.iala-aism.org/product/g1113/) and
[G1114](https://www.iala-aism.org/product/g1114/), which establish the relevant
principles for the design and implementation of harmonised shore-based technical
system architectures and propose a set of best practices to be followed. In
these, the terms Common Shore‐Based System (CSS) and Common Shore‐based System
Architecture (CSSA) were introduced to describe the shore‐based technical system
of the IMO’s overarching architecture.

To ensure the secure communication between ship and CSSA, the International
Electrotechnical Commission (IEC), in coordination with IALA, compiled a set of
system architecture and operational requirements for e-Navigation into a
standard better known as [SECOM](https://webstore.iec.ch/publication/64543).
This provides mechanisms for secure data exchange, as well as a TS interface
design that is in accordance with the service guidelines and templates defined
by IALA. Although SECOM is just a conceptual standard, the Maritime Connectivity
Platform ([MCP](https://maritimeconnectivity.net/)) provides an actual
implementation of a decentralised framework that supports SECOM.

## What is the GRAD e-Navigation Service Architecture

The GLA follow the developments on e-Navigation closely, contributing through
their role as an IALA member whenever possible. As part of their efforts, a
prototype GLA e-Navigation Service Architecture is being developed by the GLA
Research and Development Directorate (GRAD), to be used as the basis for the
provision of the future GLA e-Navigation services.

As a concept, the CSSA is based on the Service Oriented Architecture (SOA). A
pure-SOA approach however was found to be a bit cumbersome for the GLA
operations, as it usually requires the entire IT landscape being compatible,
resulting in high investment costs. In the context of e-Navigation, this could
become a serious problem, since different components of the system are designed
by independent teams/manufacturers. Instead, a more flexible microservice
architecture was opted for. This is based on a break-down of the larger
functional blocks into small independent services, each responsible for
performing its own orchestration, maintaining its own data and communicating
through lightweight mechanisms such as HTTP/HTTPS. It should be pointed out that
SOA and the microservice architecture are not necessarily that different.
Sometimes, microservices are even considered as an extension or a more
fine-grained version of SOA.

## The Niord-UK Service

As per the G-1114, there is a need for a UIA service. In the current
implementation, this is provided by the Nautical Information Directory (Niord),
a system able to produce and publish Navigational Warnings (NW) and Notices to
Mariners T&P (NM). It was originally developed as part of the
[EfficienSea2](https://efficiensea2.org/) EU  project and subsequently
implemented as a production system for the Danish Maritime Authority (DMA).
For the needs of the current project however, the original code was ported onto
the latest Red-Hat [Quarkus](https://quarkus.io/) and
[Angular](https://angular.io/) frameworks. In addition, the capability was added
to generate AtoN information, which can then be encoded into the IHO S-125 data
format. Any update on the AtoN information is communicated to the
“Message Broker” microservice, so that all other microservices that have
subscribed to receive updates will be notified. It has to be noted here that
although Niord is a very useful component of the demonstrator test-bed, it is a
not a core component of the architecture and any other NW/NM/AtoN management
system could be used instead, at least as long as it is able to generate S-100 
compliant data.

## How to use this image

This image can be used in just like a normal Docker container, but providing
the necessary environment variables while running it. The available environment
variables for configuring the application are presented in the following table.

| Variable        | Description                                             | Default Value         |
|-----------------|---------------------------------------------------------|-----------------------|
| KEYCLOAK_URL    | The URL of the OpenID Connect Keycloak server           | http://localhost:8080 |
| KEYCLOAK_REALM  | The realm used for the OpenID Connect authentication    | niord                 |
| KEYCLOAK_SECRET | The secret used for the OpenID Connect authentication   | secret                |
| AMQP_SERVER     | The server hostname of the AMQP (Apache Artemis) server | localhost             |
| AMQP_PORT       | The port used for the AMQP connection                   | 5672                  |
| AMQP_USERNAME   | The username for the AMQP connection                    | username              |
| AMQP_PASSWORD   | The password for the AMQP connection                    | password              |
| DB_SERVER       | The server hostname of the database (MySQL) server      | localhost             |
| DB_PORT         | The port used for the database (MySQL) connection       | 3306                  |
| DB_NAME         | The name of the database to connect to                  | niordq                |
| DB_USERNAME     | The username for the database connection                | username              |
| DB_PASSWORD     | The password for the database connection                | password              |
| EUREKA_SERVER   | The server hostname of the eureka server                | N/A                   |

### Environment Variables Configuration

In order to run the image you just need to provide the values of the environment
variables presented previously. If the default values are sufficient then they
can be omitted.

This can be done in the following way:

    docker run -t -i --rm \
        -p 8888:8888 \
        -v /path/to/config-directory/on/machine:/conf \
        -e KEYCLOAK_URL='https://keycloak.server.com' \
        -e KEYCLOAK_REALM='niord' \
        -e KEYCLOAK_SECRET='secret' \
        -e AMQP_SERVER='amqp.server.com' \
        -e AMQP_PORT='5672' \
        -e AMQP_USERNAME='niordq' \
        -e AMQP_PASSWORD='password' \
        -e DB_SERVER='database.server.com' \
        -e DB_NAME='niordq' \
        -e DB_PORT='3306' \
        -e DB_USERNAME='niordq' \
        -e DB_PASSWORD='password' \
        -e EUREKA_SERVER='eureka.server.com' \
        <image-id>

## Operation

The original Niord system has extensive documentation already available online
and you are advised to read this carefully before proceeding. It can found in
[this](https://docs.niord.org/) online location.

In the current *Quarkus-based* implementation, Niord is utilised as an AtoN and
Navigational-Warning (NW)/Notice-to-Mariners (NM) administration tool. The 
ability of the software to encode the data into the appropriate S-100 data
products is paramount, since this is the legitimate input format accepted by the
system. Over the course of this project, Niord was upgraded to use the developed
[S-125](https://github.com/gla-rad/S-125) library. This was achieved by
introducing a new module into Niord, making use of its modular and extendable
design.

The use of the S-125 library differs from the way Niord originally generated
S-100 data, for example for the defined NW/NM entries. This operation was
designed to be controlled by a set of FreeMarker1 scripts. This method had the
advantage of being updatable on runtime. It is however a technology not
supported by the S-100 data product specifications themselves, so potential
inconsistencies are quite likely, at least in the initial stages of the system
deployment. In contrast, a method based solely on the official S-100 XSD
resources is much more reliable and can be directly version-controlled and
maintained.

## Contributing
For contributing in this project, please have a look at the Github repository
[Niord-UK](https://github.com/gla-rad/niord-uk). Pull requests are welcome. For
major changes, please open an issue first to discuss what you would like to
change.

Please make sure to update tests as appropriate.

## License
Distributed under the Apache License, Version 2.0.

## Contact
Nikolaos Vastardis - 
[Nikolaos.Vastardis@gla-rad.org](mailto:Nikolaos.Vastardis@gla-rad.org)
