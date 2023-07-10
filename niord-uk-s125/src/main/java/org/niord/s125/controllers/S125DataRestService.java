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
package org.niord.s125.controllers;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.niord.s125.models.S125AtonTypes;
import org.niord.s125.models.vo.S125AtonTypeVo;
import org.slf4j.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST interface for accessing the S-125 Data Product information.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RequestScoped
@Transactional
@Path("/s125data")
public class S125DataRestService {

    @Inject
    Logger log;

    /**
     * Returns the list of the S-125 supported feature types.
     */
    @GET
    @Path("/feature-types")
    @Operation(
            description = "The list of S-125 supported feature types.",
            hidden = true
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = List.class)
            )
    )
    @Produces({"application/json;charset=UTF-8"})
    public List<S125AtonTypeVo> s125FeatureTypes(@Parameter(description = "Whether features that describe equipment should be selected", example = "false")
                                                 @QueryParam("equipment") boolean isEquipment) {
        log.debug("Request for the supported S-125 AtoN feature types");
        return Arrays.asList(S125AtonTypes.values())
                .stream()
                .filter(t -> isEquipment == t.isEquipment())
                .map(S125AtonTypeVo::new)
                .collect(Collectors.toList());
    }

}
