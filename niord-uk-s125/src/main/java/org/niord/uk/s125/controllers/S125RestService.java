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

package org.niord.uk.s125.controllers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.grad.eNav.s100.utils.SpecificJarClassLoader;
import org.grad.eNav.s125.utils.S125Utils;
import org.niord.uk.s125.services.S125Service;
import org.niord.uk.s125.utils.XmlUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URLClassLoader;
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

    /**
     * The System Logger.
     */
    @Inject
    Logger log;

    /**
     * The S-125 Service.
     */
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
            @Parameter(name="indent", description = "Indentation of the XML output", example = "4")
            @QueryParam("indent") @DefaultValue("4") Integer indent,
            @Parameter(name="language", description = "Two-letter ISO 639-1 language code", example = "en")
            @QueryParam("lang") @DefaultValue("en") String language,
            @Parameter(name="atonUIDs", description = "The aton UIDs or aton ID", example = "[aton-001]")
            List<String> atonUIDs
    ) {

        long t0 = System.currentTimeMillis();

        try {
            String result = s125Service.generateGML(language, String.format("aton-dataset-export-%d", t0), atonUIDs.toArray(String[]::new));

            // Pretty print the result
            result = XmlUtils.xmlPrettyPrint(result, indent);

            log.info("Generated GML for AtoNs " + String.join(",", atonUIDs) + " in " + (System.currentTimeMillis() - t0) + " ms");
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
     * @param file The name of the file to be retrieved
     * @return The requested XSD file
     */
    @GET
    @Path("/xsds/{file}")
    @Produces({"text/xml;charset=UTF-8"})
    public Response xsdFile(
            @PathParam("file") String file
    ) {
        final String xsdFile = FilenameUtils.removeExtension(file) + ".xsd";
        try (URLClassLoader classLoader = new SpecificJarClassLoader(S125Utils.class)) {
            try(InputStream is = classLoader.getResourceAsStream("xsd/" + xsdFile)) {
                // Sanity check if the resource was found
                if(is == null) { throw new NotFoundException(); }
                // Otherwise return the resource and a GML/XML
                log.info("Returning XSD " + xsdFile);
                final String xsd = new String(is.readAllBytes());
                return Response.ok(xsd)
                        .type("application/gml+xml;charset=UTF-8")
                        .build();
            }
        } catch (Exception ex) {
            log.error("XSD does not exist: " + xsdFile);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("XSD does not exist: " + xsdFile)
                    .build();
        }
    }

}
