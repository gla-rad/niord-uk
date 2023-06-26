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

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.niord.s125.services.S125Service;
import org.niord.s125.utils.XmlUtils;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

/**
 * A public REST API for accessing messages as S-125 GML.
 * <p>
 * You can test that the produced GML is valid according to the schema, using something along the lines of:
 * <pre>
 *     xmllint --noout --schema http://localhost:8080/rest/S-125/S125.xsd http://localhost:8080/rest/S-125/aton-001.gml
 * </pre>
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RequestScoped
@Path("/S-125")
public class S125RestService {

    @Inject
    Logger log;

    @Inject
    S125Service s125Service;

    /**
     * Returns the S-125 GML representation for multiple AtoN.
     */
    @POST
    @Path("/atons")
    @Operation(
            description = "Returns S-125 GML representation for a list of AtoN UIDs." +
                    "NB: Only use this service for test purposes, not for production."
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/gml+xml;charset=UTF-8",
                    schema = @Schema(implementation = String.class)
            )
    )
    @Produces({"application/gml+xml;charset=UTF-8"})
    public Response s125AtonDetails(
            @Parameter(description = "Indentation of the XML output", example = "4")
            @QueryParam("indent") @DefaultValue("4") Integer indent,
            @Parameter(description = "Two-letter ISO 639-1 language code", example = "en")
            @QueryParam("lang") @DefaultValue("en") String language,
            @Parameter(description = "The aton UIDs or aton ID", example = "[aton-001]")
            List<String> atonUID
    ) {

        long t0 = System.currentTimeMillis();

        try {
            String result = s125Service.generateGML(language, String.format("aton-dataset-export-%d", t0), atonUID.toArray(String[]::new));

            // Pretty print the result
            result = XmlUtils.xmlPrettyPrint(result, indent);

            log.info("Generated GML for AtoNs " + String.join(",", atonUID) + " in " + (System.currentTimeMillis() - t0) + " ms");
            return Response.ok(result)
                    .type("application/gml+xml;charset=UTF-8")
                    .build();

        } catch (IllegalArgumentException ex) {
            log.error(ex.getMessage());
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Error on input parameters: " + ex.getMessage())
                    .build();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML_TYPE)
                    .entity("Error generating GML: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Allows users to have access to the S-125 product XSD definition files.
     * These include the S-100 product definition, as well as the GRAD version
     * of S-125.
     *
     * @param file          The name of the file to be retrieved
     * @return The requested XSD file
     * @throws Exception
     */
    @GET
    @Path("/xsds/{file}")
    @Produces({"text/xml;charset=UTF-8"})
    public Response xsdFile(
            @PathParam("file") String file
    ) {

        String xsdFile = file + ".xsd";

        try (InputStream in = getClass().getResourceAsStream("/xsd/" + xsdFile)) {

            String xsd = IOUtils.toString(in);

            log.info("Returning XSD " + xsdFile);
            return Response.ok(xsd)
                    .type("application/gml+xml;charset=UTF-8")
                    .build();

        } catch (Exception ex) {
            log.error("XSD does not exist: " + xsdFile);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("XSD does not exist: " + xsdFile)
                    .build();
        }
    }

}
