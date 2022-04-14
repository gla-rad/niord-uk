/*
 * Copyright (c) 2022 GLA UK Research and Development Directive
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

package org.niord.s125.services;

import org.grad.eNav.s125.utils.S100Utils;
import org.niord.core.NiordApp;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonService;
import org.niord.s125.models.S125DatasetInfo;
import org.niord.s125.utils.S125DatasetBuilder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.util.Collections;
import java.util.Optional;

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
     * @param atonUID the aton UID
     * @param language the language
     * @return the generated GML
     */
    public String generateGML(String atonUID, String language) throws Exception {
        // Try to access the AtoN
        AtonNode atonNode = this.atonService.findByAtonUid(atonUID);

        // Validate the AtoN
        if (atonNode == null) {
            throw new IllegalArgumentException("AtoN not found " + atonUID);
        }

        // Use the utilities to translate the AtoN node to an S-125 dataset
        return Optional.ofNullable(atonNode)
                .map(Collections::singletonList)
                .map(l -> new S125DatasetBuilder().packageToDataset(new S125DatasetInfo(atonNode.getAtonUid(), app.getOrganisation(), l), l))
                .map(d -> {try {return S100Utils.marshalS125(d);} catch (JAXBException e) {return null;}} )
                .orElse(null);
    }

}
