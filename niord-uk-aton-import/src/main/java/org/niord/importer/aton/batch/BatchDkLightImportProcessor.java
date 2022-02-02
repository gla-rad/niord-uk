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
package org.niord.importer.aton.batch;

import org.apache.commons.lang.StringUtils;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.core.user.User;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * AtoN batch processor used for converting the legacy "AFM" Light Excel row into OSM seamark format.
 * Before running this batch job, run the dk-aton-import job.
 *
 * The AtoN model adheres to the OSM seamark specification, please refer to:
 * http://wiki.openstreetmap.org/wiki/Key:seamark
 * and sub-pages.
 */
@Dependent
@Named("batchDkLightImportProcessor")
public class BatchDkLightImportProcessor extends AbstractDkAtonImportProcessor {

    @Inject
    Logger log;

    /** {@inheritDoc} **/
    @Override
    protected AtonNode parseAtonExcelRow() throws Exception {

        // Only process active lights
        if (!"DRIFT".equalsIgnoreCase(stringValue("STATUS"))) {
            getLog().info("Skipping inactive light/fog-signal " + stringValue("NR_DK"));
            return null;
        }

        User user = job.getUser();

        AtonNode aton = new AtonNode();

        aton.setVisible(true);
        aton.setLat(numericValue("LATITUDE"));
        aton.setLon(numericValue("LONGITUDE"));
        aton.setTimestamp(dateValue("Ajourfoert_dato"));
        aton.setUser(user != null ? user.getUsername() : "");
        aton.setUid(user != null ? user.getId() : -1);
        aton.setChangeset(getChangeSet());
        aton.setVersion(1);     // Unknown version

        // If no AtoN UID exists, construct it
        String atonUid = stringValue("AFM_NR");
        if (StringUtils.isBlank(atonUid)) {
            atonUid = "light-" + stringValue("NR_DK");
        }
        aton.updateTag(AtonTag.TAG_ATON_UID, atonUid);
        aton.updateTag(AtonTag.TAG_LIGHT_NUMBER, stringValue("NR_DK"));
        aton.updateTag(AtonTag.TAG_INT_LIGHT_NUMBER, stringValue("NR_INT"));
        aton.updateTag(AtonTag.TAG_LOCALITY, stringValue("Lokalitet"));
        aton.updateTag("seamark:name", stringValue("AFM_navn"));
        aton.updateTag("seamark:light:information", stringValue("Fyrudseende"));
        aton.updateTag("seamark:light:elevation", parseElevation());


        /********* Light Character Parsing *******/

        String lightChar = stringValue("Fyrkarakter");

        LightSeamark light = DkLightParser.newInstance();

        // Parse the light character, e.g. "Iso.WRG.2s"
        DkLightParser.parseLightCharacteristics(light, lightChar);

        // Parse the light elevations
        DkLightParser.parseHeight(light, numericValueOrNull("Fyrbygnings_hoejde"));

        // Parse the light sector angles, e.g. "G114,52°-116,52° W116,52°-117,52° R117,52°-119,52°."
        DkLightParser.parseLightSectorAngles(light, stringValue("Lysvinkler"));

        // Parse the light ranges
        DkLightParser.parseRange(light,
                stringValue("Lysstyrke_1"),
                stringValue("Lysstyrke_2"),
                stringValue("Lysstyrke_3"));

        // Parse the exhibition
        DkLightParser.parseExhibition(light, stringValue("Braendetid"));


        /********* Fog Signal Parsing *******/

        String fogSignalSpec = stringValue("Taagesignal");

        FogSignalSeamark fogSignal = DkFogSignalParser.newInstance();

        // Parse the fog signal, e.g. "HORN(3)30s   (2+2+2+2+2+20)"
        DkFogSignalParser.parseFogSignal(fogSignal, fogSignalSpec);


        // Either the light or the fog signal (or both) must be valid
        if (!light.isValid() && !fogSignal.isValid()) {
            getLog().info("Skipping invalid light/fog-signal " + stringValue("NR_DK")
                    + ": light=" + lightChar
                    + ", fog-signal=" + fogSignalSpec);
            return null;
        }

        // Copy the fog signal OSM tags to the AtoN.
        if (fogSignal.isValid()) {
            fogSignal.toOsm().forEach(t -> aton.updateTag(t.getK(), t.getV()));
        }

        // Copy the light OSM tags to the AtoN.
        // NB: Any "seamark:type" will override a fog-signal type. Lights should take precedence.
        if (light.isValid()) {
            light.toOsm().forEach(t -> aton.updateTag(t.getK(), t.getV()));
        }

        return aton;
    }


    /** {@inheritDoc} */
    @Override
    protected void mergeAtonNodes(AtonNode original, AtonNode aton) {

        // Do not override the original AtoN type
        String origType = original.getTagValue("seamark:type");
        if (origType != null) {
            aton.removeTags("seamark:type");
        }

        // If the new AtoN contains light information, remove any light information from the original
        if (aton.matchingTags("seamark:\\w*light\\.*").size() > 0) {
            original.removeTags("seamark:\\w*light\\.*");
        }

        // If the new AtoN contains fog signal information, remove any fog signal information from the original
        if (aton.matchingTags("seamark:fog_signal\\.*").size() > 0) {
            original.removeTags("seamark:fog_signal\\.*");
        }

        // Override any remaining tags in the original
        original.updateNode(aton);
    }


    /** Parses the elevation fields */
    private String parseElevation() {
        String elevation = null;
        for (int x = 1; x <= 4; x++) {
            elevation = appendValue(elevation, numericValueOrNull("Flammehoejde_" + x));
        }
        return elevation;
    }

}
