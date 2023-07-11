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

package org.niord.uk.s125.services;

import org.grad.eNav.s125.utils.S125Utils;
import org.niord.core.NiordApp;
import org.niord.core.aton.AtonLink;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonService;
import org.niord.uk.s125.models.S125DatasetInfo;
import org.niord.uk.s125.utils.S125DatasetBuilder;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * The S-125 Service
 *
 * This is the basic implementation of the S-125 (GRAD Version of course)
 * service. It basically just calls the associated ftl freemarker scripts
 * from the module's resources and returns the populated output.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@RequestScoped
public class S125Service {

    @Inject
    AtonService atonService;

    @Inject
    NiordApp app;

    /**
     * Generates S-125 compliant GML for the message
     * @param gmlDatasetId the GML dataset identifier string
     * @param language the language
     * @param atonUIDs the aton UID
     * @return the generated GML
     */
    @Transactional
    public String generateGML(String language, String gmlDatasetId, String... atonUIDs) throws Exception {
        // Try to access the AtoN
        final List<AtonNode> atonNodes = this.atonService.findByAtonUids(atonUIDs);

        // Iterate through the AtoN links and pick up all references
        this.iterativeLinkRetrieval(atonNodes);

        // Validate the AtoN
        if (atonNodes.isEmpty()) {
            throw new IllegalArgumentException("No AtoN not found for UIDs: " + Arrays.toString(atonUIDs));
        }

        // Use the utilities to translate the AtoN node to an S-125 dataset
        return Optional.of(atonNodes)
                .map(l -> new S125DatasetBuilder().packageToDataset(new S125DatasetInfo(gmlDatasetId, app.getOrganisation(), l), l))
                .map(d -> {try {return S125Utils.marshalS125(d);} catch (JAXBException e) {return null;}} )
                .orElse(null);
    }

    /**
     * This is a small helper function that iterates over the provided list
     * of AtoN nodes and will pick up the included links in order to append
     * them to the provided list. This way we can construct a dataset with
     * all the AtoN nodes that are applicable.
     *
     * @param atonNodes the AtoN nodes list to be iterated through
     */
    protected void iterativeLinkRetrieval(List<AtonNode> atonNodes) {
        // Get the linked AtoN nodes to be appended
        final Set<AtonNode> appended = atonNodes.stream()
                .map(AtonNode::getLinks)
                .flatMap(Set::stream)
                .map(AtonLink::getPeers)
                .flatMap(Set::stream)
                .filter(not(atonNodes::contains))
                .collect(Collectors.toSet());

        // If there are appended node, iterate through those as well
        if(appended.size() > 0) {
            // Append the results to the original list
            atonNodes.addAll(appended);

            // And iterate again
            iterativeLinkRetrieval(atonNodes);
        }
    }

}
