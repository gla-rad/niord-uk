/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.niord.uk.eureka.controllers;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.niord.uk.eureka.models.EurekaActuator;
import org.niord.uk.eureka.models.EurekaLink;
import org.niord.uk.eureka.services.EurekaClientService;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * REST interface to the Eureka Client
 */
@Path("/actuator")
@ApplicationScoped
@PermitAll
@SuppressWarnings("unused")
public class EurekaClientRestService {

    @ConfigProperty(name = "quarkus.http.port")
    Integer assignedPort;

    @Inject
    EurekaClientService eurekaClientService;

    /**
     * Returns the actuator endpoints of the service.
     *
     * @return the actuator endpoints of the service
     */
    @GET
    @Path("")
    @Produces("application/json;charset=UTF-8")
    @PermitAll
    public Response actuator() {
        // Create the eureka actuator information on the fly
        EurekaActuator eurekaActuator = new EurekaActuator();
        eurekaActuator.set_links(
                new Reflections(this.getClass().getPackageName(), new MethodAnnotationsScanner())
                        .getMethodsAnnotatedWith(Path.class)
                        .stream()
                        .filter(method -> method.getName().compareTo("actuator") != 0)
                        .collect(Collectors.toMap(
                                method -> method.getName(),
                                method -> new EurekaLink(
                                        Collections.singletonList(this.formatEndpoint(method.getAnnotation(Path.class).value())),
                                        false
                                )
                        ))
        );
        // And return
        return Response.ok(eurekaActuator)
                .build();
    }

    /**
     * Returns the health of the service.
     *
     * @return the health of the service
     */
    @GET
    @Path("/status")
    @Produces("text/plain;charset=UTF-8")
    @PermitAll
    public Response status() {
        return Response.ok(this.eurekaClientService.getStatus())
                .build();
    }

    /**
     * Returns the health of the service.
     *
     * @return the health of the service
     */
    @GET
    @Path("/health")
    @PermitAll
    @Produces("application/json;charset=UTF-8")
    public Response health() {
        return Response.ok(this.eurekaClientService.getHealth())
                .build();
    }

    /**
     * Returns information About Niord.
     *
     * @return the information about Niord
     */
    @GET
    @Path("/info")
    @PermitAll
    @Produces("application/json;charset=UTF-8")
    public Response info() {
        return Response.ok(this.eurekaClientService.getInfo())
                .build();
    }

    /**
     * A simple utility function that attempts to construct the full URL for
     * the discovered actuator endpoints.
     *
     * @param endpoint the actuator endpoint local path
     * @return the full URL
     */
    private String formatEndpoint(String endpoint) {
        return "http://"
                + this.eurekaClientService.getEurekaClientHostname()
                + ":" + this.assignedPort
                + "/rest/actuator"
                + endpoint;
    }

}