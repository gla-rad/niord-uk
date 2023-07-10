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

package org.niord.importer.aton.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.niord.core.aton.vo.AtonNodeVo;
import org.niord.core.batch.AbstractItemHandler;
import org.niord.core.repo.RepositoryService;
import org.niord.model.search.PagedSearchResultVo;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads AtoNs rom a zip file containing an aton.json file
 * <p>
 * Please note, the actual aton-import.xml job file is not placed in the META-INF/batch-jobs of this project,
 * but rather, in the META-INF/batch-jobs folder of the niord-web project.<br>
 * This is because of a class-loading bug in the Wildfly implementation. See e.g.
 * https://issues.jboss.org/browse/WFLY-4988
 * <p>
 * The AtoN json file must adhere to the OSM seamark specification; please refer to:
 * http://wiki.openstreetmap.org/wiki/Key:seamark
 * and sub-pages.
 * <p>
 * JSON Example:
 * <pre>
 * {
 *  "id" : 672436827,
 *  "lat" : 50.8070813,
 *  "lon" : -1.2841124,
 *  "user" : "malcolmh",
 *  "uid" : 128186,
 *  "visible" : true,
 *  "version" : 11,
 *  "changeset" : 9107813,
 *  "timestamp" : 1314134556000,
 *  "tags" : {
 *    "seamark:buoy_cardinal:category" : "north",
 *    "seamark:buoy_cardinal:colour" : "black;yellow",
 *    "seamark:buoy_cardinal:colour_pattern" : "horizontal",
 *    "seamark:buoy_cardinal:shape" : "pillar",
 *    "seamark:light:character" : "VQ",
 *    "seamark:light:colour" : "white",
 *    "seamark:name" : "Calshot",
 *    "seamark:topmark:colour" : "black",
 *    "seamark:topmark:shape" : "2 cones up",
 *    "seamark:type" : "buoy_cardinal"
 *    }
 *  }
 * </pre>
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Dependent
@Named("batchAtonArchiveImportReader")
public class BatchAtonArchiveImportReader extends AbstractItemHandler {

    private AtonNodeVo[] atons;
    private int atonNo = 0;

    @Inject
    RepositoryService repositoryService;

    /** {@inheritDoc} **/
    @Override
    public void open(Serializable prevCheckpointInfo) throws Exception {

        try {
            // Get hold of the data file
            atons = extractAtonArchive().toArray(AtonNodeVo[]::new);

            if (prevCheckpointInfo != null) {
                atonNo = (Integer) prevCheckpointInfo;
            }

            getLog().info("Start processing " + atons.length + " AtoNs from index " + atonNo);

        } catch (Exception e) {
            getLog().log(Level.SEVERE, "Error opening aton-import data file", e);
            throw e;
        }
    }

    /** Extracts the AtoN archive and reads in the batch import AtoN */
    protected List<AtonNodeVo> extractAtonArchive() throws Exception {

        // Default implementation reads the messages from a message.json batch file
        Path path = batchService.getBatchJobDataFile(jobContext.getInstanceId());

        // Extract the archive into a temporary repository path
        String tempArchiveRepoPath = repositoryService.getNewTempDir().getPath();
        Path dest = repositoryService.getRepoRoot().resolve(tempArchiveRepoPath);
        extractAtonArchive(path, dest);
        getLog().info("Extracted AtoN archive to " + dest);

        // Fetch the messages.json from the root of the extracted archive
        Path messageFilePath = dest.resolve("aton.json");
        if (!Files.exists(messageFilePath)) {
            getLog().log(Level.SEVERE, "No valid aton.json file found in the archive");
            throw new Exception("No valid aton.json file found in the archive");
        }

        // Read the messages.json file
        PagedSearchResultVo<AtonNodeVo> aton;
        try {
            ObjectMapper mapper = new ObjectMapper();
            aton = mapper.readValue(
                    messageFilePath.toFile(),
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            getLog().log(Level.SEVERE, "Invalid aton.json file");
            throw new Exception("Invalid aton.json file");
        }

        // Return the extracted data
        return aton.getData();
    }

    /** {@inheritDoc} **/
    @Override
    public Object readItem() throws Exception {
        if (atonNo < atons.length) {

            // Every now and then, update the progress
            if (atonNo % 10 == 0) {
                updateProgress((int)(100.0 * atonNo / atons.length));
            }

            getLog().info("Reading AtoN no " + atonNo);
            return atons[atonNo++];
        }
        return null;
    }

    /** {@inheritDoc} **/
    @Override
    public Serializable checkpointInfo() throws Exception {
        return atonNo;
    }

    /** Utility method that extracts the given zip file to a given destination **/
    @SuppressWarnings("all")
    private void extractAtonArchive(Path path, Path destination) throws IOException {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destination.toFile(),  entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(in, out);
                    }
                }
            }
        }
    }
}
