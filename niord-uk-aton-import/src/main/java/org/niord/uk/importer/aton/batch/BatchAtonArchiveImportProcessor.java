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
package org.niord.uk.importer.aton.batch;

import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.core.aton.batch.BatchAtonImportProcessor;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

/**
 * Filters AtoNs that need to be added or updated.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Dependent
@Named("batchAtonArchiveImportProcessor")
public class BatchAtonArchiveImportProcessor extends BatchAtonImportProcessor {

    /** {@inheritDoc} **/
    @Override
    public Object processItem(Object item) throws Exception {
        // Convert the item to an AtoN node
        AtonNode aton = toAtonNode(item);

        if (aton == null) {
            return null;
        }

        // Check if we should assign a new UID
        Boolean assignNewUids = (Boolean)job.getProperties().get("assignNewUids");
        if (assignNewUids) {
            aton.updateTag(AtonTag.TAG_ATON_UID, "aton-import-" + aton.getId());
        }

        return super.processItem(item);
    }

}
