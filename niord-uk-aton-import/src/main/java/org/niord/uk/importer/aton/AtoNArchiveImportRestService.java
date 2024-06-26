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

package org.niord.uk.importer.aton;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.niord.core.aton.vo.AtonOsmVo;
import org.niord.core.batch.AbstractBatchableRestService;
import org.niord.core.domain.DomainService;
import org.niord.core.user.Roles;
import org.niord.model.IJsonSerializable;
import org.niord.model.search.PagedSearchResultVo;
import org.slf4j.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Imports AtoN in JSON format from archive files.
 */
@Path("/import/atons")
@RequestScoped
@Transactional
@PermitAll
@SuppressWarnings("unused")
public class AtoNArchiveImportRestService extends AbstractBatchableRestService {

    @Inject
    Logger log;

    @Inject
    DomainService domainService;

    /**
     * Imports an uploaded AtoN zip archive
     *
     * @param input the multi-part form data input request
     * @return a status
     */
    @POST
    @Path("/upload-archive")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/plain")
    @RolesAllowed(Roles.ADMIN)
    public String importMessages(MultipartFormDataInput input) throws Exception {
        return executeBatchJobFromUploadedFile(input, "aton-archive-import");
    }

    /** {@inheritDoc} */
    @Override
    protected void checkBatchJob(String batchJobName, String fileName, InputStream inputStream, Map<String, Object> params) throws Exception {

        // Check that the zip file contains a messages.json file
        if (!checkForMessagesFileInImportArchive(inputStream)) {
            throw new Exception("Zip archive is missing a valid aton.json entry");
        }

        // Read and validate the parameters associated with the batch job
        ImportAtonArchiveParams batchData;
        try {
            batchData = new ObjectMapper().readValue((String)params.get("data"), ImportAtonArchiveParams.class);
        } catch (IOException e) {
            throw new Exception("Missing batch data with tag and message series", e);
        }


        // Update parameters
        params.remove("data");
        params.put("assignNewUids", batchData.getAssignNewUids() != null && batchData.getAssignNewUids());
    }

    /** Checks for a valid "aton.xml" zip file entry **/
    private boolean checkForMessagesFileInImportArchive(InputStream in) throws Exception {
        try (ZipInputStream zipFile = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zipFile.getNextEntry()) != null) {
                if ("aton.json".equals(entry.getName())) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        PagedSearchResultVo<AtonOsmVo> messages = mapper.readValue(
                                zipFile,
                                new TypeReference<>() {
                                });
                        return  messages != null;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /***************************
     * Helper classes
     ***************************/

    /** Defines the parameters used when starting an import an AtoN zip archive */
    public static class ImportAtonArchiveParams implements IJsonSerializable {

        Boolean assignNewUids;

        public Boolean getAssignNewUids() {
            return assignNewUids;
        }

        public void setAssignNewUids(Boolean assignNewUids) {
            this.assignNewUids = assignNewUids;
        }

    }

}
