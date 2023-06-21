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

import org.apache.commons.lang3.StringUtils;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.core.user.User;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AtoN batch processor used for converting the Excel row into OSM seamark
 * format.
 * <p/>
 * Filters out AtoNs that have not changed
 * <p/>
 * The AtoN model adheres to the OSM seamark specification, please refer to:
 * http://wiki.openstreetmap.org/wiki/Key:seamark
 * and sub-pages.
 */
@Dependent
@Named("batchUkAtonImportProcessor")
public class BatchUkAtonImportProcessor extends AbstractUkAtonImportProcessor {


    /** {@inheritDoc} **/
    @Override
    protected AtonNode parseAtonExcelRow() throws Exception {

        User user = job.getUser();

        AtonNode aton = new AtonNode();

        // TODO: aton.setId();
        aton.setVisible(true);
        aton.setLat(latLonValue(BatchUkAtonImportReader.LAT));
        aton.setLon(latLonValue(BatchUkAtonImportReader.LON));
        aton.setTimestamp(new Date());
        aton.setUser(user != null ? user.getUsername() : "");
        aton.setUid(user != null ? user.getId() : -1);
        aton.setChangeset(getChangeSet());
        aton.setVersion(1);     // Unknown version

        // Generate the AtoN UID using lowercase and underscores
        aton.updateTag(AtonTag.TAG_ATON_UID, this.generateAtonUuid());

        if (StringUtils.isNotBlank(stringValue(BatchUkAtonImportReader.NAME))) {
            aton.updateTag("seamark:name", stringValue(BatchUkAtonImportReader.NAME));
        } else {
            return null;
        }

        // An AtoN consists of a master type (e.g. "lighthouse") and a set of
        // equipment types (e.g. AIS). The master type will be imported first
        // and the rest need to be picked up and imported at a second stage
        // as children entries.
        AtonType masterType = AtonType.findByCode(stringValue(BatchUkAtonImportReader.TYPE));
        Set<AtonType> equipmentTypes = new HashSet<>();
        if(StringUtils.isNotBlank(stringValue(BatchUkAtonImportReader.CHARACTER))) {
            equipmentTypes.add(AtonType.LIGHT);
        }
        if(StringUtils.isNotBlank(stringValue(BatchUkAtonImportReader.FOG_SIGNALS))) {
            equipmentTypes.add(AtonType.FOG_SIGNAL);
        }
        if(StringUtils.isNotBlank(stringValue(BatchUkAtonImportReader.RADIO_AIDS))) {
            equipmentTypes.addAll(parseAidsTypes(stringValue(BatchUkAtonImportReader.RADIO_AIDS)));
        }

        // Last check, for buoys and beacons do we have a category?
        if(masterType.isBuoy() || masterType.isBeacon()) {
            if(StringUtils.isBlank(stringValue(BatchUkAtonImportReader.DESIGN_CODE))) {
                return null;
            }
        }

        // And now populate the AtoN
        generateAton(aton, masterType);

        // Add the equipment types as additional children
        for(AtonType equipmentType: equipmentTypes) {
            // Create the new equipment entry
            AtonNode equipment = new AtonNode();
            equipment.setVisible(true);
            equipment.setLat(aton.getLat());
            equipment.setLon(aton.getLon());
            equipment.setTimestamp(aton.getTimestamp());
            equipment.setUser(aton.getUser());
            equipment.setUid(aton.getUid());
            equipment.setChangeset(aton.getChangeset());
            equipment.setVersion(aton.getVersion());

            equipment.updateTag(AtonTag.TAG_ATON_UID, String.format("%s-%s", aton.getAtonUid(), equipmentType.type));

            // Select a custom name for the equipment
            equipment.updateTag("seamark:name", String.format("%s %s", aton.getTagValue("seamark:name"), equipmentType.type));

            // And now populate the equipment
            generateAton(equipment, equipmentType);

            // And add the equipment
            aton.updateChild(equipment);
        }

        // And return
        return aton;
    }

    /********************************/
    /** Generating OSM AtoN        **/
    /********************************/

    /**
     * Generates type-specific AtoN OSM tags.
     *
     * Important: The light details are handled by other Excel imports.
     *
     * @see <a href="http://wiki.openstreetmap.org/wiki/Seamarks/Lights">OpenStreetMap Light definitions</a>
     * @see <a href="http://wiki.openstreetmap.org/wiki/Seamarks/Beacons">OpenStreetMap Beacon definitions</a>
     * @see <a href="http://wiki.openstreetmap.org/wiki/Seamarks/Buoys>OpenStreetMap Buoy definitions</a>
     *
     * @param aton the current AtoN node
     */
    public void generateAton(AtonNode aton, AtonType type) {
        // Sanity Checks
        if (Objects.isNull(aton) || Objects.isNull(type)) {
            return;
        }

        String osmType;
        // Now handle based on the type
        switch (type) {
            case LIGHTHOUSE:
                updateAtonTags(aton,
                        "seamark:type", "light_major",
                        "seamark:landmark:category", "tower",
                        "seamark:landmark:colour", "white",
                        "seamark:landmark:colour_pattern", "vertical",
                        "seamark:landmark:function", "light_support",
                        "seamark:landmark:height", "",
                        "seamark:landmark:construction", "hard-surfaced",
                        "seamark:landmark:conspicuity", "conspicuous",
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:lighthouse:radar_conspicuous", "conspicuous"
                );
                break;
            case LIGHT_VESSEL:
            case LIGHT_FLOAT:
                updateAtonTags(aton,
                        "seamark:type", "light_vessel",
                        "seamark:${type}:category", "tower",
                        "seamark:${type}:colour", "red",
                        "seamark:${type}:colour_pattern", "vertical",
                        "seamark:${type}:height", "",
                        "seamark:${type}:construction", "hard-surfaced",
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:${type}:radar_conspicuous", "conspicuous",
                        "s125:aidsToNavigation:${type}:visually_conspicuous", "conspicuous",
                        "s125:aidsToNavigation:${type}:nature_of_construction", "metal"
                );
                break;
            case BUOY:
                DesignCode buoyDesignCode = UkDesignCodeParser.newInstance();

                // Parse the design code, e.g. "2S5PL/B"
                String buoyDesignCodeSpec = stringValue(BatchUkAtonImportReader.DESIGN_CODE);
                UkDesignCodeParser.parseDesignCode(buoyDesignCode, buoyDesignCodeSpec);

                updateAtonTags(aton,
                        "seamark:type", buoyDesignCode.getType().getBuoyType(),
                        "seamark:${type}:category", buoyDesignCode.getType().getCategory(),
                        "seamark:${type}:shape", buoyDesignCode.getShape().name().toLowerCase(),
                        "seamark:${type}:system", "iala-a",
                        "seamark:${type}:colour", buoyDesignCode.getType().getColour(),
                        "seamark:${type}:colour_pattern", buoyDesignCode.getType().getColourPattern(),
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:generic_beacon:height", "1",
                        "s125:aidsToNavigation:generic_buoy:radar_conspicuous", "conspicuous",
                        "s125:aidsToNavigation:generic_buoy:nature_of_construction", "metal"
                );
                break;
            case BEACON:
                DesignCode beaconDesignCode = UkDesignCodeParser.newInstance();

                // Parse the design code, e.g. "2S5PL/B"
                String beaconDesignCodeSpec = stringValue(BatchUkAtonImportReader.DESIGN_CODE);
                UkDesignCodeParser.parseDesignCode(beaconDesignCode, beaconDesignCodeSpec);

                updateAtonTags(aton,
                        "seamark:type", beaconDesignCode.getType().getBeaconType(),
                        "seamark:${type}:category", beaconDesignCode.getType().getCategory(),
                        "seamark:${type}:shape", beaconDesignCode.getShape().name().toLowerCase(),
                        "seamark:${type}:system", "iala-a",
                        "seamark:${type}:colour", beaconDesignCode.getType().getColour(),
                        "seamark:${type}:colour_pattern", beaconDesignCode.getType().getColourPattern(),
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:generic_beacon:height", "1",
                        "s125:aidsToNavigation:generic_beacon:radar_conspicuous", "conspicuous",
                        "s125:aidsToNavigation:generic_beacon:nature_of_construction", "metal"
                );
                break;
            case LIGHT:
            case SECTOR_LIGHT:
                LightSeamark light = UkLightParser.newInstance();

                // Parse the light character, e.g. "Iso.WRG.2s"
                String lightChar = stringValue(BatchUkAtonImportReader.CHARACTER);
                UkLightParser.parseLightCharacteristics(light, lightChar);

                // Copy the light OSM tags to the AtoN.
                // NB: Any "seamark:type" will override a fog-signal type. Lights should take precedence.
                if (light.isValid()) {
                    light.toOsm().forEach(t -> aton.updateTag(t.getK(), t.getV()));
                }

                updateAtonTags(aton,
                        "seamark:type", "light",
                        "seamark:${type}:visibility", "high intensity",
                        "seamark:${type}:orientation", "",
                        "seamark:${type}:range", Optional.of(BatchUkAtonImportReader.RANGE)
                                .map(this::numericValue)
                                .map(String::valueOf)
                                .orElse(""),
                        "seamark:status", "permanent"
                );
                osmType = "light";
                break;
            case AIS:
                updateAtonTags(aton,
                        "seamark:type", "radio_station",
                        "seamark:${type}:category", "ais",
                        "seamark:${type}:mmsi", Optional.of(BatchUkAtonImportReader.MMSI)
                                .map(this::numericValue)
                                .map(String::valueOf)
                                .orElse(""),
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:generic_beacon:estimated_range_of_transmission", Optional.of(BatchUkAtonImportReader.RANGE)
                                .map(this::numericValue)
                                .map(String::valueOf)
                                .orElse("")
                );
                break;
            case DGPS:
                updateAtonTags(aton,
                        "seamark:type", "radio_station",
                        "seamark:${type}:category", "differential",
                        "seamark:status", "permanent"
                );
                break;
            case LORAN:
                updateAtonTags(aton,
                        "seamark:type", "radio_station",
                        "seamark:${type}:category", "loran",
                        "seamark:status", "permanent"
                );
                break;
            case VIRTUAL_ATON:
                updateAtonTags(aton,
                        "seamark:type", "radio_station",
                        "seamark:${type}:category", "ais",
                        "seamark:${type}:mmsi", Optional.of(BatchUkAtonImportReader.MMSI)
                                .map(this::numericValue)
                                .map(String::valueOf)
                                .orElse(""),
                        "seamark:virtual_aton:category", "special_purpose",
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:virtual_ais_aid_to_navigation:estimated_range_of_transmission", Optional.of(BatchUkAtonImportReader.RANGE)
                                .map(this::numericValue)
                                .map(String::valueOf)
                                .orElse("")
                );
                break;
            case RACON:
                updateAtonTags(aton,
                        "seamark:type", "radar_reflector",
                        "seamark:${type}:height", "",
                        "seamark:status", "permanent"
                );
                break;
            case FOG_SIGNAL:
                FogSignalSeamark fogSignalSeamark = UkFogSignalParser.newInstance();

                // Parse the design code, e.g. "2S5PL/B"
                String fogSignalSeamarkSpec = stringValue(BatchUkAtonImportReader.FOG_SIGNALS);
                UkFogSignalParser.parseFogSignal(fogSignalSeamark, fogSignalSeamarkSpec);

                updateAtonTags(aton,
                        "seamark:type", "fog_signal",
                        "seamark:${type}:category", fogSignalSeamark.getCategory().name(),
                        "seamark:status", "permanent",
                        "s125:aidsToNavigation:fog_signal:signal_sequence", fogSignalSeamark.getSequence()
                );
                break;
        }

        // Finally add the standard supplementary fields
        /********** Standard S-125 fields ********/
        updateAtonTags(aton,
                "s125:aidsToNavigation:information", stringValue(BatchUkAtonImportReader.COMMENT),
                "s125:aidsToNavigation:information_in_national_language", stringValue(BatchUkAtonImportReader.COMMENT),
                "s125:aidsToNavigation:date_start", "",
                "s125:aidsToNavigation:date_end", "",
                "s125:aidsToNavigation:period_start", "",
                "s125:aidsToNavigation:period_end", "",
                "s125:aidsToNavigation:scale_minimum", "0"
        );
        /*****************************************/
    }

    /**
     * Updates the AtoN with the given tags, which must be in k,v,k,v,...,k,v order.
     *
     * Any occurrence of "${type}" in the keys will be substituted with the "seamark:type" value.
     *
     * @param aton the AtoN to update
     * @param tags the tags
     */
    private void updateAtonTags(AtonNode aton, String... tags) {

        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of key-value tag parameters " + tags.length);
        }

        // Build a map of the parameters (NB: preserve order)
        Map<String, String> tagLookup = new LinkedHashMap<>();
        for (int x = 0; x < tags.length; x += 2) {
            if (StringUtils.isNotBlank(tags[x]) && StringUtils.isNotBlank(tags[x + 1])) {
                tagLookup.put(tags[x], tags[x + 1]);
            }
        }

        // Ensure that the seamark:type is either specified in the tags parameters or already defined
        String type = StringUtils.isNotBlank(tagLookup.get("seamark:type"))
                ? tagLookup.get("seamark:type")
                : aton.getTagValue("seamark:type");

        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("No seamark:type defined for AtoN " + aton);
        }

        // Update the AtoN
        tagLookup.entrySet().stream()
                .forEach(tag -> {

                    // Substitute "${type}" in the key with the "seamark:type" value
                    String key = tag.getKey();
                    if (key.contains("${type}")) {
                        key = key.replace("${type}", type);
                    }

                    aton.updateTag(key, tag.getValue());
                });

    }

    /**
     * Defines the rules about generating the UUID of an AtoN based on the
     * row information. Currently, the provided name should be:
     * <ul>
     *     <li>Lowercase</li>
     *     <li>All spaces should be replaces by dashes</li>
     * </ul>
     * If no name is provided then an autogenerated UUID will be returned.
     *
     * @return the formatted AtoN UUID based on the name of the row
     */
    private String generateAtonUuid() {
        return Optional.of(BatchUkAtonImportReader.NAME)
                .map(this::stringValue)
                .map(String::toLowerCase)
                .map(str -> str.replaceAll(" ","-"))
                .orElseGet(() -> String.format("autogenerated-uuid-%d", new Random().nextInt(999999)));
    }

    /*************************/
    /** AtoN Type Parsing   **/
    /*************************/

    /** Parses the "KARAKNR" field into individual types **/
    Set<AtonType> parseAidsTypes(String types) {
        // Each digit denotes a separate type
        return Arrays.stream(String.valueOf(types).split("/"))
                .map(AtonType::findByCode)
                .collect(Collectors.toSet());
    }
}
