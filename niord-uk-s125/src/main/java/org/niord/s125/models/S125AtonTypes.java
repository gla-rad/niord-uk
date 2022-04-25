package org.niord.s125.models;

import java.util.Arrays;

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
    CARDINAL_BEACON("beacon_cardinal", "Cardinal Beacon", false),
    LATERAL_BEACON("beacon_lateral", "Lateral Beacon", false),
    ISOLATED_DANGER_BEACON("beacon_isolated_danger", "Isolated Danger Beacon", false),
    SAFE_WATER_BEACON("beacon_safe_water", "Safe Water Beacon", false),
    SPECIAL_PURPOSE_BEACON("beacon_special_purpose", "Special Purpose Beacon", false),
    CARDINAL_BUOY("buoy_cardinal", "Cardinal Buoy", false),
    LATERAL_BUOY("buoy_lateral", "Lateral Buoy", false),
    INSTALLATION_BUOY("buoy_installation", "Installation Buoy", false),
    ISOLATED_DANGER_BUOY("buoy_isolated_danger", "Isolated Danger Buoy", false),
    SAFE_WATER_BUOY("buoy_safe_water", "Safe Water Buoy", false),
    SPECIAL_PURPOSE_BUOY("buoy_special_purpose", "Special Purpose Beacon", false),
    LANDMARK("landmark", "Cardinal Beacon", false),
    LIGHTHOUSE_MAJOR("light_major", "Lighthouse - Major", false),
    LIGHTHOUSE_MINOR("light_minor", "Lighthouse - Minor", false),
    LIGHT_VESSEL("light_vessel", "Light Vessel", false),
    PHYSICAL_AIS_ATON("radio_station", "Physical AIS AtoN", false),
    VIRTUAL_ATON("virtual_aton", "Virtual AtoN", false);

    // Enum Variables
    String name;
    String description;
    boolean equipment;

    S125AtonTypes(String name, String description, boolean equipment) {
        this.name = name;
        this.description = description;
        this.equipment = equipment;
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
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
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
     * Sets equipment.
     *
     * @param equipment the equipment
     */
    public void setEquipment(boolean equipment) {
        this.equipment = equipment;
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
