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

import org.niord.core.aton.AtonNode;
import org.niord.core.aton.vo.AtonNodeVo;
import org.slf4j.Logger;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.List;

/**
 * Dummy no-op item writer that does not persist  the item.
 *
 * Useful for development purposes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Dependent
@Named("noopAtonImportItemWriter")
public class NoopAtonImportItemWriter extends AbstractItemWriter {

    @Inject
    Logger log;

    /** {@inheritDoc} */
    @Override
    public void writeItems(List<Object> items) throws Exception {

        for (Object i : items) {
            AtonNode aton = (AtonNode) i;
            log.info("\n== Ignoring AtonItem ==\n" + printResult(aton.toVo()));
        }
    }

    /** Formats the aton as XML */
    private String printResult(AtonNodeVo aton) {

        StringWriter w = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AtonNodeVo.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(aton, w);
        } catch (JAXBException ignored) {
        }
        return w.toString();
    }

}
