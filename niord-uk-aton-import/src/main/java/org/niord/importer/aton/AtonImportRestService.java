/*
 * Copyright 2016 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.niord.importer.aton;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.vo.AtonNodeVo;
import org.niord.core.aton.vo.AtonOsmVo;
import org.niord.core.batch.BatchService;
import org.niord.core.repo.RepositoryService;
import org.niord.core.sequence.DefaultSequence;
import org.niord.core.sequence.Sequence;
import org.niord.core.sequence.SequenceService;
import org.niord.core.user.Roles;
import org.niord.core.user.UserService;
import org.niord.core.util.WebUtils;
import org.niord.importer.aton.batch.AbstractUkAtonImportProcessor;
import org.slf4j.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports AtoN from Excel sheets.
 */
@Path("/import/atons")
@RequestScoped
@PermitAll
@SuppressWarnings("unused")
public class AtonImportRestService {

    private final static Sequence AFM_SEQUENCE = new DefaultSequence("AFM_ATON_VERSION", 1);

    @Inject
    Logger log;

    @Inject
    BatchService batchService;

    @Inject
    UserService userService;

    @Inject
    SequenceService sequenceService;

    @Inject
    RepositoryService repositoryService;

    /**
     * Imports an uploaded AtoN Excel file
     *
     * @param input the multi-part form input
     * @return a status
     */
    @POST
    @Path("/upload-xls")
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA,
                    schema = @Schema(implementation = org.niord.core.model.MultipartBody.class)
            )
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN,
                    schema = @Schema(implementation = String.class)
            )
    )
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(description = "Performs the import operations from the AtoN xls/xlsx data files.")
    @RolesAllowed(Roles.ADMIN)
    public String importXls(@Parameter(hidden = true) @MultipartForm MultipartFormDataInput input) throws Exception {

        // Initialise the form parsing parameters
        final Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        final List<String> fileNames = new ArrayList<>();
        final StringBuilder txt = new StringBuilder();

        // Process the uploaded files
        final List<InputPart> inputParts = uploadForm.get("file");
        for (InputPart inputPart : inputParts) {
            try {
                final MultivaluedMap<String, String> header = inputPart.getHeaders();
                final String name = WebUtils.getFileName(header).toLowerCase();
                final InputStream inputStream = inputPart.getBody(InputStream.class, null);

                // AtoN Import
                if (name.startsWith("aton") && (name.endsWith(".xls") || name.endsWith(".xlsx"))) {
                    importAtoN(inputStream, name, txt);

                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

        return txt.toString();
    }

    /**
     * Extracts the AtoNs from the Excel sheet
     * @param inputStream the Excel sheet input stream
     * @param fileName the name of the PDF file
     * @param txt a log of the import
     */
    private void importAtoN(InputStream inputStream, String fileName, StringBuilder txt) throws Exception {
        log.info("Extracting AtoNs from Excel sheet " + fileName);

        // Start batch job to import AtoNs
        batchService.startBatchJobWithDataFile(
                "uk-aton-import",
                inputStream,
                fileName,
                initBatchProperties());

        log.info("Started 'uk-aton-import' batch job with file " + fileName);
        txt.append("Started 'uk-aton-import' batch job with file ").append(fileName);
    }

    /** Initializes the properties to use with the batch data */
    private Map<String, Object> initBatchProperties() {
        int changeset = (int)sequenceService.nextValue(AFM_SEQUENCE);
        Map<String, Object> properties = new HashMap<>();
        properties.put(AbstractUkAtonImportProcessor.CHANGE_SET_PROPERTY, changeset);
        return properties;
    }

    /** Converts the list of AtoNs to an OSM Json representation **/
    private AtonOsmVo toOsm(List<AtonNode> atons) {
        AtonOsmVo osm = new AtonOsmVo();
        osm.setVersion(1.0f);
        osm.setNodes(atons.stream()
                .map(AtonNode::toVo)
                .toArray(AtonNodeVo[]::new));
        return osm;
    }

}
