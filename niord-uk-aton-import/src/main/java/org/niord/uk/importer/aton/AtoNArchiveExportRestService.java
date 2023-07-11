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

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.niord.core.aton.AtonExportService;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonSearchParams;
import org.niord.core.aton.AtonService;
import org.niord.core.aton.vo.AtonNodeVo;
import org.niord.core.batch.AbstractBatchableRestService;
import org.niord.core.domain.DomainService;
import org.niord.model.search.PagedSearchResultVo;
import org.slf4j.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.stream.Collectors;

/**
 * Exports AtoN in JSON format into archive files.
 */
@Path("/export/atons")
@RequestScoped
@Transactional
@PermitAll
@SuppressWarnings("unused")
public class AtoNArchiveExportRestService extends AbstractBatchableRestService {

    @Inject
    Logger log;

    @Inject
    DomainService domainService;

    @Inject
    AtonService atonService;

    @Inject
    AtonExportService atonExportService;

    /**
     * Generates a ZIP archive for the AtoN search result including S-125.
     */
    @GET
    @Path("/export.zip")
    @GZIP
    @NoCache
    public Response generateZipArchiveForSearch(@Context HttpServletRequest request) throws Exception {

        // Perform a search for at most 1000 AtoNs
        AtonSearchParams params = AtonSearchParams.instantiate(domainService.currentDomain(), request);
        params.maxSize(1000)
                .page(0);

        // Search and translate the result in a VO
        PagedSearchResultVo<AtonNodeVo> result = atonService.search(params).map(AtonNode::toVo);

        try {
            StreamingOutput stream = os -> this.atonExportService.export(result, os);

            return Response.ok(stream)
                    .type("application/zip")
                    .header("Content-Disposition", "attachment; filename=\"export.zip\"")
                    .build();

        } catch (Exception e) {
            log.error("Error generating ZIP archive for messages", e);
            throw e;
        }
    }

}
