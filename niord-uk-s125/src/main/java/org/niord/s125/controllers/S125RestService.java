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

package org.niord.s125.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.niord.s125.services.S125Service;
import org.niord.s125.utils.XmlUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

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
@Api(value = "/S-125",
        description = "Public API for accessing messages as S-125 GML. " +
                "NB: Only use this service for test purposes, not for production.",
        tags = {"S-125" })
@Path("/S-125")
public class S125RestService {

    @Inject
    Logger log;

    @Inject
    S125Service s125Service;

    /**
     * Returns the S-125 GML representation for the given AtoN.
     */
    @ApiOperation(
            value = "Returns S-125 GML representation for the aton." +
                    "NB: Only use this service for test purposes, not for production.",
            response = String.class,
            tags = {"S-125"}
    )
    @GET
    @Path("/atons/{atonUID}")
    @Produces({"application/gml+xml;charset=UTF-8"})
    public Response s125AtonDetails(
            @ApiParam(value = "The aton UID or aton ID", example = "aton-001")
            @PathParam("atonUID") String atonUID,

            @ApiParam(value = "Two-letter ISO 639-1 language code", example = "en")
            @QueryParam("lang") @DefaultValue("en") String language
    ) {

        long t0 = System.currentTimeMillis();

        try {
            String result = s125Service.generateGML(atonUID, language);

            // Pretty print the result
            result = XmlUtils.xmlPrettyPrint(result);

            log.info("Generated GML for AtoN " + atonUID + " in " + (System.currentTimeMillis() - t0) + " ms");
            return Response.ok(result)
                    .type("application/gml+xml;charset=UTF-8")
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("AtoN does not exist: " + atonUID);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("No aton found with UID: " + atonUID)
                    .build();
        } catch (Exception e) {
            log.error("Error generating S-125 GML for aton " + atonUID + ": " + e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML_TYPE)
                    .entity("Error generating GML: " + e.getMessage())
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

        } catch (Exception e) {
            e.printStackTrace();
            log.error("XSD does not exist: " + xsdFile);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("XSD does not exist: " + xsdFile)
                    .build();
        }
    }

}
