/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.niord.core.eureka.controllers;

import org.niord.core.eureka.services.EurekaClientService;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST interface to the Eureka Client
 */
@Path("/")
@Stateless
@PermitAll
@SuppressWarnings("unused")
public class EurekaClientRestService {

    @Inject
    EurekaClientService eurekaClientService;

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
        return Response.ok(this.eurekaClientService.getStatus(), MediaType.TEXT_PLAIN)
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
        return Response.ok(this.eurekaClientService.getHealth(), MediaType.APPLICATION_JSON)
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
    public Response about() {
        return Response.ok(this.eurekaClientService.getInfo(), MediaType.APPLICATION_JSON)
                .build();
    }

}