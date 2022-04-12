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

package org.niord.s125.services;

import _int.iho.s125.gml._0.DataSet;
import _int.iho.s125.gml._0.MemberType;
import _int.iho.s125.gml._0.S125AidsToNavigationType;

import _int.iho.s125.gml._0.S125BuoyCardinalType;
import _net.opengis.gml.profiles.AbstractFeatureType;
import org.niord.core.NiordApp;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonService;
import org.niord.core.geojson.JtsConverter;
import org.niord.model.geojson.GeometryVo;
import org.niord.s125.utils.S100Utils;
import org.niord.s125.utils.S125Utils;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

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

        AtonNode atonNode = this.atonService.findByAtonUid(atonUID);

        // Validate the AtoN
        if (atonNode == null) {
            throw new IllegalArgumentException("AtoN not found " + atonUID);
        }

        // Ensure we use a valid language
        language = app.getLanguage(language);

        // And get the geometry and the AtoN node VO object
        GeometryVo geometry = JtsConverter.fromJts(atonNode.getGeometry());

        S125AidsToNavigationType aidsToNavigationType = S125Utils.generateAidsToNavigation(atonNode);
        DataSet s125Dataset = new DataSet();
        MemberType s125DatasetMember = new MemberType();
        s125DatasetMember.setAbstractFeature(new JAXBElement(new QName("Member"), AbstractFeatureType.class, aidsToNavigationType));
        s125Dataset.getMembers().add(s125DatasetMember);
        return S100Utils.marshalS125(s125Dataset);
//        AtonNodeVo aton = atonNode.toVo();
//
//        // Pass down all the parameters to the freemarker script
//        Map<String, Object> data = new HashMap<>();
//        data.put("aton", aton);
//        data.put("atonUID", atonUID);
//        data.put("geometry", geometry);
//        data.put("language", language);
//
//        double[] bbox = GeoJsonUtils.computeBBox(new GeometryVo[]{geometry});
//        if (bbox != null) {
//            data.put("bbox", bbox);
//        }
//
//        // We need to set an incompatible version in our initial configuration
//        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
//        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates/gml"));
//
//        StringWriter result = new StringWriter();
//        Template fmTemplate = cfg.getTemplate("generate-s125.ftl");
//
//        fmTemplate.process(data, result);
//        return result.toString();
    }

}
