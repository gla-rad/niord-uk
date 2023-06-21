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
package org.niord.s125.models;

import java.util.Arrays;
import java.util.List;

/**
 * The S-125 AtoN Types Enum.
 * <p>
 * This enumeration can be used to determine the types of AtoN that are
 * provided through Niord as AtonNodes. They will determine which S-125
 * feature type will be used for each structure or equipment.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public enum S125AtonTypes {
    CARDINAL_BEACON("beacon_cardinal", "Cardinal Beacon", false, new String[] {
            "Supplementary Information",
            "Cardinal Beacon"
    }),
    LATERAL_BEACON("beacon_lateral", "Lateral Beacon", false, new String[] {
            "Supplementary Information",
            "Lateral Beacon"
    }),
    ISOLATED_DANGER_BEACON("beacon_isolated_danger", "Isolated Danger Beacon", false, new String[] {
            "Supplementary Information",
            "Isolated Danger Beacon"
    }),
    SAFE_WATER_BEACON("beacon_safe_water", "Safe Water Beacon", false, new String[] {
            "Supplementary Information",
            "Safe Water Beacon"
    }),
    SPECIAL_PURPOSE_BEACON("beacon_special_purpose", "Special Purpose Beacon", false, new String[] {
            "Supplementary Information",
            "Special Purpose Beacon"
    }),
    CARDINAL_BUOY("buoy_cardinal", "Cardinal Buoy", false, new String[] {
            "Supplementary Information",
            "Cardinal Buoy"
    }),
    LATERAL_BUOY("buoy_lateral", "Lateral Buoy", false, new String[] {
            "Supplementary Information",
            "Lateral Buoy"
    }),
    INSTALLATION_BUOY("buoy_installation", "Installation Buoy", false, new String[] {
            "Supplementary Information",
            "Installation Buoy"
    }),
    ISOLATED_DANGER_BUOY("buoy_isolated_danger", "Isolated Danger Buoy", false, new String[] {
            "Supplementary Information",
            "Isolated Danger Buoy"
    }),
    SAFE_WATER_BUOY("buoy_safe_water", "Safe Water Buoy", false, new String[] {
            "Supplementary Information",
            "Safe Water Buoy"
    }),
    SPECIAL_PURPOSE_BUOY("buoy_special_purpose", "Special Purpose Buoy", false, new String[] {
            "Supplementary Information",
            "Special Purpose Buoy"
    }),
    LANDMARK("landmark", "Landmark", false, new String[] {
            "Supplementary Information",
            "Landmarks in General"
    }),
    LIGHTHOUSE("light_major", "Lighthouse - Major", false, new String[] {
            "Supplementary Information",
            "Landmarks in General",
            "Major and Minor Lights (P1)"
    }),
    LIGHT_VESSEL("light_vessel", "Light Vessel", false, new String[] {
            "Supplementary Information",
            "Major Floating Light (P6)"
    }),
    VIRTUAL_ATON("virtual_aton", "Virtual AtoN", false, new String[] {
            "Supplementary Information",
            "Virtual AIS transmitter (S18.1-S18.7)"
    }),
    DAYMARK("daymark", "Daymark", true, new String[] {
            "Supplementary Information",
            "Daymarks in General"
    }),
    ENVIRONMENT_OBSERVATION("platform", "Environment Observation", true, new String[] {
            "Supplementary Information",
            "Observation Platform(L13)"
    }),
    FOG_SIGNAL("fog_signal", "Fog Signal", true, new String[] {
            "Supplementary Information",
            "Fog Signal (R1-R22)"
    }),
    LIGHT("light", "Light", true, new String[] {
            "Supplementary Information",
            "Lights in General"
    }),
    RADAR_REFLECTOR("radar_reflector", "Radar Reflector", true, new String[] {
            "Supplementary Information",
            "Radar Reflector (S4)"
    }),
    RETRO_REFLECTOR("retro_reflector", "Retro Reflector", true, new String[] {
            "Supplementary Information",
            "Retro Reflector (S6)"
    }),
    SILOS_AND_TANKS("tank", "Silos and Tanks", true, new String[] {
            "Supplementary Information",
            "Silos (E33)",
            "Tanks (E32)"
    }),
    TOPMARK("topmark", "Topmark", true, new String[] {
            "Supplementary Information",
            "Topmarks in General"
    }),
    RADIO_STATION("radio_station", "Radio Station", true, new String[] {
            "Supplementary Information",
            "Radio Transponder (S10-S16)"
    }),
    PHYSICAL_AIS_ATON("ais_station", "Physical AIS AtoN", true, new String[] {
            "Supplementary Information",
            "AIS transmitter (S17.1-S17.2)"
    });

    // Enum Variables
    final String name;
    final String description;
    final boolean equipment;
    final String[] josmNodeTypes;

    S125AtonTypes(String name, String description, boolean equipment, String[] jomsNodeTypes) {
        this.name = name;
        this.description = description;
        this.equipment = equipment;
        this.josmNodeTypes = jomsNodeTypes;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Is equipment boolean.
     *
     * @return the boolean
     */
    public boolean isEquipment() {
        return equipment;
    }

    /**
     * Gets JOSM node types.
     *
     * @return the JOSM node types
     */
    public List<String> getJosmNodeTypes() {
        return Arrays.asList(this.josmNodeTypes);
    }

    /**
     * Find the enum entry that corresponds to the provided JOSM Seachart
     * type.
     *
     * @param type      The JOSM Seachart type string
     * @return The respective S125 AtoN Type enum entry
     */
    public static S125AtonTypes fromSeamarkType(String type) {
        return Arrays.stream(S125AtonTypes.values())
                .filter(t -> t.getName().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }
}
