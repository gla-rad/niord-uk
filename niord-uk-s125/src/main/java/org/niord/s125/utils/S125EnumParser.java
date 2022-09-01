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
package org.niord.s125.utils;

import _int.iala_aism.s125.gml._0_0.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The S-125 Enum Parser Utility Class.
 *
 * This is a helper class that provides all the parsing utilities to translate
 * the JOSM seachart entries to S-125 enums.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S125EnumParser {

    /**
     * Splits the list string separated by a special character into an actual
     * Java list or S125 enum entries. The colours should be separated by a
     * semi-colon character (;). The individual entry parsing is performed by
     * the function provided as an argument.
     *
     * @param stringList     The string list separated by a character (;)
     * @return the Java list object
     */
    public static <R> Collection<R> splitAndParse(String stringList, Function<String, R> function) {
        return Optional.ofNullable(stringList)
                .map(c -> c.split(";"))
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .map(function)
                .collect(Collectors.toList());
    }

    /**
     * Translates the category of landmark from the INT-1.preset.xml to the
     * S-125 Category of Landmark enum.
     *
     * @param categoryOfLandmark        The INT-1-preset.xml category of landmark
     * @return the S-125 Category of Landmark enum entry
     */
    public static S125CategoryOfLandmark parseCategoryOfLandmark(String categoryOfLandmark) {
        switch(categoryOfLandmark) {
            case "cairn": return S125CategoryOfLandmark.CAIRN;
            case "cemetery": return S125CategoryOfLandmark.CEMETERY;
            case "chimney": return S125CategoryOfLandmark.CHIMNEY;
            case "dish_aerial": return S125CategoryOfLandmark.DISH_AERIAL;
            case "flagstaff": return S125CategoryOfLandmark.FLAGSTAFF_FLAGPOLE;
            case "flare_stack": return S125CategoryOfLandmark.FLARE_STACK;
            case "mast": return S125CategoryOfLandmark.MAST;
            case "windsock": return S125CategoryOfLandmark.WINDSOCK;
            case "monument": return S125CategoryOfLandmark.MONUMENT;
            case "column": return S125CategoryOfLandmark.COLUMN_PILLAR;
            case "memorial": return S125CategoryOfLandmark.MEMORIAL_PLAQUE;
            case "obelisk": return S125CategoryOfLandmark.OBELISK;
            case "statue": return S125CategoryOfLandmark.STATUE;
            case "cross": return S125CategoryOfLandmark.CROSS;
            case "dome": return S125CategoryOfLandmark.DOME;
            case "radar_scanner": return S125CategoryOfLandmark.RADAR_SCANNER;
            case "tower": return S125CategoryOfLandmark.TOWER;
            case "windmill": return S125CategoryOfLandmark.WINDMILL;
            case "windmotor": return S125CategoryOfLandmark.WINDMOTOR;
            case "spire": return S125CategoryOfLandmark.SPIRE_MINARET;
            case "boulder": return S125CategoryOfLandmark.LARGE_ROCK_OR_BOULDER_ON_LAND;
            default: return null;
        }
    }

    /**
     * Translates the virtual AtoN category from the INT-1.preset.xml to the 
     * S-125 Virtual AIS Aid Navigation Purpose Type enum.
     *
     * @param virtualAisAidsToNavigationType        The INT-1-preset.xml virtual AtoN category
     * @return the S-125 Virtual AIS Aid Navigation Purpose Type
     */
    public static S125CategoryOfVirtualAISAidToNavigation parseVirtualAisAidToNavigationType(String virtualAisAidsToNavigationType) {
        switch(virtualAisAidsToNavigationType) {
            case "north_cardinal": return S125CategoryOfVirtualAISAidToNavigation.NORTH_CARDINAL;
            case "south_cardinal": return S125CategoryOfVirtualAISAidToNavigation.SOUTH_CARDINAL;
            case "east_cardinal": return S125CategoryOfVirtualAISAidToNavigation.EAST_CARDINAL;
            case "west_cardinal": return S125CategoryOfVirtualAISAidToNavigation.WEST_CARDINAL;
            case "port_lateral": return S125CategoryOfVirtualAISAidToNavigation.PORT_LATERAL;
            case "starboard_lateral": return S125CategoryOfVirtualAISAidToNavigation.STARBOARD_LATERAL;
            case "preferred_port": return S125CategoryOfVirtualAISAidToNavigation.PREFERRED_CHANNEL_TO_PORT;
            case "preferred_starboard": return S125CategoryOfVirtualAISAidToNavigation.PREFERRED_CHANNEL_TO_STARBOARD;
            case "isolated_danger": return S125CategoryOfVirtualAISAidToNavigation.ISOLATED_DANGER;
            case "safe_water": return S125CategoryOfVirtualAISAidToNavigation.SAFE_WATER;
            case "special_purpose": return S125CategoryOfVirtualAISAidToNavigation.SPECIAL_PURPOSE;
            case "wreck": return S125CategoryOfVirtualAISAidToNavigation.EMERGENCY_WRECK_MARKING;
            default: return null;
        }
    }
    
    /**
     * Translates the construction from the INT-1.preset.xml to the S-125
     * Nature Of Construction enum.
     *
     * @param natureOfConstruction        The INT-1-preset.xml construction
     * @return the S-125 Nature Of Construction enum entry
     */
    public static S125NatureOfConstruction parseNatureOfConstruction(String natureOfConstruction) {
        switch(natureOfConstruction) {
            case "masonry": return S125NatureOfConstruction.MASONRY;
            case "concreted": return S125NatureOfConstruction.CONCRETED;
            case "loose_boulders": return S125NatureOfConstruction.LOOSE_BOULDERS;
            case "hard-surfaced": return S125NatureOfConstruction.HARD_SURFACED;
            case "wooden": return S125NatureOfConstruction.WOODEN;
            case "metal": return S125NatureOfConstruction.METAL;
            case "grp": return S125NatureOfConstruction.GLASS_REINFORCED_PLASTIC_GRP;
            case "painted": return S125NatureOfConstruction.PAINTED;
            default: return null;
        }
    }

    /**
     * Translates the entry from the INT-1.preset.xml to the S-125 Status enum.
     *
     * @param status        The INT-1-preset.xml status
     * @return the S-125 Status enum entry
     */
    public static S125Status parseStatus(String status) {
        switch(status) {
            case "permanent": return S125Status.PERMANENT;
            case "occasional": return S125Status.OCCASIONAL;
            case "recommended": return S125Status.RECOMMENDED;
            case "not_in_use": return S125Status.NOT_IN_USE;
            case "intermittent": return S125Status.PERIODIC_INTERMITTENT;
            case "reserved": return S125Status.RESERVED;
            case "temporary": return S125Status.TEMPORARY;
            case "private": return S125Status.PRIVATE;
            case "mandatory": return S125Status.MANDATORY;
            case "extinguished": return S125Status.EXTINGUISHED;
            case "illuminated": return S125Status.ILLUMINATED;
            case "historic": return S125Status.HISTORIC;
            case "public": return S125Status.PUBLIC;
            case "synchronised": return S125Status.SYNCHRONIZED;
            case "watched": return S125Status.WATCHED;
            case "unwatched": return S125Status.UN_WATCHED;
            case "existence_doubtful": return S125Status.EXISTENCE_DOUBTFUL;
            default: return null;
        }
    }
    
    /**
     * Translates the radar conspicuous from the INT-1.preset.xml to
     * the S-125 Radar Conspicuous System enum.
     *
     * @param radarConspicuous     The INT-1-preset.xml radar conspicuous
     * @return the S-125 Radar Conspicuous System enum entry
     */
    public static S125RadarConspicuous parseRadarConspicuous(String radarConspicuous) {
        switch(radarConspicuous) {
            case "conspicuous": return S125RadarConspicuous.RADAR_CONSPICUOUS;
            case "not_conspicuous": return  S125RadarConspicuous.NOT_RADAR_CONSPICUOUS;
            default: return null;
        }
    }

    /**
     * Translates the visually conspicuous from the INT-1.preset.xml to
     * the S-125 Visually Conspicuous System enum.
     *
     * @param visuallyConspicuous     The INT-1-preset.xml visually conspicuous
     * @return the S-125 Visually Conspicuous System enum entry
     */
    public static S125VisuallyConspicuous parseVisuallyConspicuous(String visuallyConspicuous) {
        switch(visuallyConspicuous) {
            case "conspicuous": return S125VisuallyConspicuous.VISUALLY_CONSPICUOUS;
            case "not_conspicuous": return  S125VisuallyConspicuous.NOT_VISUALLY_CONSPICUOUS;
            default: return null;
        }
    }

    /**
     * Translates the marks navigational system of from the INT-1.preset.xml to
     * the S-125 Marks Navigational System Of enum.
     *
     * @param marksNavigationalSystemOf     The INT-1-preset.xml marks navigational system of
     * @return the S-125 Marks Navigational System Of enum entry
     */
    public static S125MarksNavigationalSystemOf parseMarksNavigationalSystemOf(String marksNavigationalSystemOf) {
        switch(marksNavigationalSystemOf) {
            case "iala-a": return S125MarksNavigationalSystemOf.IALA_A;
            case "iala-b": return  S125MarksNavigationalSystemOf.IALA_B;
            case "cevni": return S125MarksNavigationalSystemOf.OTHER_SYSTEM;
            case "none": return S125MarksNavigationalSystemOf.NO_SYSTEM;
            default: return null;
        }
    }

    /**
     * Translates the category of installation buoy from the INT-1.preset.xml to the
     * S-125 Category of Installation Buoy enum.
     *
     * @param installationBuoy  The INT-1-preset.xml category of installation buoy
     * @return the S-125 Category of Installation Buoy enum entry
     */
    public static S125CategoryOfInstallationBuoy parseCategoryOfInstallationBuoy(String installationBuoy) {
        switch (installationBuoy) {
            case "calm": return S125CategoryOfInstallationBuoy.CATENARY_ANCHOR_LEG_MOORING_CALM;
            case "sbm": return S125CategoryOfInstallationBuoy.SINGLE_BUOY_MOORING_SBM_OR_SPM;
            default: return null;
        }
    }

    /**
     * Translates the category of special purpose mark from the INT-1.preset.xml to the
     * S-125 Category of Special Purpose Mark enum.
     *
     * @param specialPurposeMark    The INT-1-preset.xml special purpose mark
     * @return the S-125 Category of Special Purpose Mark enum entry
     */
    public static S125CategoryOfSpecialPurposeMark parseCategoryOfSpecialPurposeMark(String specialPurposeMark) {
        switch (specialPurposeMark) {
            case("firing_danger_area"): return S125CategoryOfSpecialPurposeMark.FIRING_DANGER_AREA_MARK;
            case("target"): return S125CategoryOfSpecialPurposeMark.TARGET_MARK;
            case("marker_ship"): return S125CategoryOfSpecialPurposeMark.MARKER_SHIP_MARK;
            case("degaussing_range"): return S125CategoryOfSpecialPurposeMark.DEGAUSSING_RANGE_MARK;
            case("barge"): return S125CategoryOfSpecialPurposeMark.BARGE_MARK;
            case("cable"): return S125CategoryOfSpecialPurposeMark.CABLE_MARK;
            case("spoil_ground"): return S125CategoryOfSpecialPurposeMark.SPOIL_GROUND_MARK;
            case("outfall"): return S125CategoryOfSpecialPurposeMark.OUTFALL_MARK;
            case("odas"): return S125CategoryOfSpecialPurposeMark.ODAS_OCEAN_DATA_ACQUISITION_SYSTEM;
            case("recording"): return S125CategoryOfSpecialPurposeMark.RECORDING_MARK;
            case("seaplane_anchorage"): return S125CategoryOfSpecialPurposeMark.SEAPLANE_ANCHORAGE_MARK;
            case("recreation_zone"): return S125CategoryOfSpecialPurposeMark.RECREATION_ZONE_MARK;
            case("private"): return S125CategoryOfSpecialPurposeMark.PRIVATE_MARK;
            case("mooring"): return S125CategoryOfSpecialPurposeMark.MOORING_MARK;
            case("lanby"): return S125CategoryOfSpecialPurposeMark.LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY;
            case("leading"): return S125CategoryOfSpecialPurposeMark.LEADING_MARK;
            case("measured_distance"): return S125CategoryOfSpecialPurposeMark.MEASURED_DISTANCE_MARK;
            case("notice"): return S125CategoryOfSpecialPurposeMark.NOTICE_MARK;
            case("tss"): return S125CategoryOfSpecialPurposeMark.TSS_MARK_TRAFFIC_SEPARATION_SCHEME;
            case("no_anchoring"): return S125CategoryOfSpecialPurposeMark.ANCHORING_PROHIBITED_MARK;
            case("no_berthing"): return S125CategoryOfSpecialPurposeMark.BERTHING_PROHIBITED_MARK;
            case("no_overtaking"): return S125CategoryOfSpecialPurposeMark.OVERTAKING_PROHIBITED_MARK;
            case("no_two-way_traffic"): return S125CategoryOfSpecialPurposeMark.TWO_WAY_TRAFFIC_PROHIBITED_MARK;
            case("reduced_wake"): return S125CategoryOfSpecialPurposeMark.REDUCED_WAKE_MARK;
            case("speed_limit"): return S125CategoryOfSpecialPurposeMark.SPEED_LIMIT_MARK;
            case("stop"): return S125CategoryOfSpecialPurposeMark.STOP_MARK;
            case("warning"): return S125CategoryOfSpecialPurposeMark.GENERAL_WARNING_MARK;
            case("sound_ship_siren"): return S125CategoryOfSpecialPurposeMark.SOUND_SHIP_S_SIREN_MARK;
            case("restricted_vertical_clearance"): return S125CategoryOfSpecialPurposeMark.RESTRICTED_VERTICAL_CLEARANCE_MARK;
            case("maximum_vessel_draught"): return S125CategoryOfSpecialPurposeMark.MAXIMUM_VESSEL_S_DRAUGHT_MARK;
            case("restricted_horizontal_clearance"): return S125CategoryOfSpecialPurposeMark.RESTRICTED_HORIZONTAL_CLEARANCE_MARK;
            case("strong_current"): return S125CategoryOfSpecialPurposeMark.STRONG_CURRENT_WARNING_MARK;
            case("berthing"): return S125CategoryOfSpecialPurposeMark.BERTHING_PERMITTED_MARK;
            case("overhead_power_cable"): return S125CategoryOfSpecialPurposeMark.OVERHEAD_POWER_CABLE_MARK;
            case("channel_edge_gradient"): return S125CategoryOfSpecialPurposeMark.CHANNEL_EDGE_GRADIENT_MARK;
            case("telephone"): return S125CategoryOfSpecialPurposeMark.TELEPHONE_MARK;
            case("ferry_crossing"): return S125CategoryOfSpecialPurposeMark.FERRY_CROSSING_MARK;
            case("pipeline"): return S125CategoryOfSpecialPurposeMark.PIPELINE_MARK;
            case("anchorage"): return S125CategoryOfSpecialPurposeMark.ANCHORAGE_MARK;
            case("clearing"): return S125CategoryOfSpecialPurposeMark.CLEARING_MARK;
            case("control"): return S125CategoryOfSpecialPurposeMark.CONTROL_MARK;
            case("diving"): return S125CategoryOfSpecialPurposeMark.DIVING_MARK;
            case("refuge_beacon"): return S125CategoryOfSpecialPurposeMark.REFUGE_BEACON;
            case("foul_ground"): return S125CategoryOfSpecialPurposeMark.FOUL_GROUND_MARK;
            case("yachting"): return S125CategoryOfSpecialPurposeMark.YACHTING_MARK;
            case("heliport"): return S125CategoryOfSpecialPurposeMark.HELIPORT_MARK;
            case("gps"): return S125CategoryOfSpecialPurposeMark.GPS_MARK;
            case("seaplane_landing"): return S125CategoryOfSpecialPurposeMark.SEAPLANE_LANDING_MARK;
            case("no_entry"): return S125CategoryOfSpecialPurposeMark.ENTRY_PROHIBITED_MARK;
            case("work_in_progress"): return S125CategoryOfSpecialPurposeMark.WORK_IN_PROGRESS_MARK;
            case("unknown_purpose"): return S125CategoryOfSpecialPurposeMark.MARK_WITH_UNKNOWN_PURPOSE;
            case("wellhead"): return S125CategoryOfSpecialPurposeMark.WELLHEAD_MARK;
            case("channel_separation"): return S125CategoryOfSpecialPurposeMark.CHANNEL_SEPARATION_MARK;
            case("marine_farm"): return S125CategoryOfSpecialPurposeMark.MARINE_FARM_MARK;
            case("artificial_reef"): return S125CategoryOfSpecialPurposeMark.ARTIFICIAL_REEF_MARK;
            default: return null;
        }
    }

    /**
     * Translates the category of lateral mark from the INT-1.preset.xml to the
     * S-125 Category of Lateral Mark enum.
     *
     * @param lateralMark   The INT-1-preset.xml lateral mark
     * @return the S-125 Category of Lateral Mark enum entry
     */
    public static S125CategoryOfLateralMark parseCategoryOfLateralMark(String lateralMark) {
        switch (lateralMark) {
            case "port": return S125CategoryOfLateralMark.PORT_HAND_LATERAL_MARK;
            case "starboard": return S125CategoryOfLateralMark.STARBOARD_HAND_LATERAL_MARK;
            case "preferred_channel_starboard": return S125CategoryOfLateralMark.PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK;
            case "preferred_channel_port": return S125CategoryOfLateralMark.PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK;
            default: return null;
        }
    }

    /**
     * Translates the category of cardinal mark from the INT-1.preset.xml to the
     * S-125 Category of Cardinal Mark enum.
     *
     * @param cardinalMark  The INT-1-preset.xml cardinal mark
     * @return the S-125 Category of Cardinal Mark enum entry
     */
    public static S125CategoryOfCardinalMark parseCategoryOfCardinalMark(String cardinalMark) {
        switch (cardinalMark) {
            case "north": return S125CategoryOfCardinalMark.NORTH_CARDINAL_MARK;
            case "east": return S125CategoryOfCardinalMark.EAST_CARDINAL_MARK;
            case "south": return S125CategoryOfCardinalMark.SOUTH_CARDINAL_MARK;
            case "west": return S125CategoryOfCardinalMark.WEST_CARDINAL_MARK;
            default: return null;
        }
    }

    /**
     * Translates the beacon shape from the INT-1.preset.xml to the
     * S-125 Beacon Shape enum.
     *
     * @param beaconShape    The INT-1-preset.xml beacon shape
     * @return the S-125 Beacon Shape enum entry
     */
    public static S125BeaconShape parseBeaconShape(String beaconShape) {
        switch (beaconShape) {
            case "stake": case "pole": case "perch": case "post": return S125BeaconShape.STAKE_POLE_PERCH_POST;
            case "withy": return S125BeaconShape.WITHY;
            case "tower": return S125BeaconShape.BEACON_TOWER;
            case "lattice": return S125BeaconShape.LATTICE_BEACON;
            case "pile": return S125BeaconShape.PILE_BEACON;
            case "cairn": return S125BeaconShape.CAIRN;
            case "buoyant": return S125BeaconShape.BUOYANT_BEACON;
            default: return null;
        }
    }

    /**
     * Translates the buoy shape from the INT-1.preset.xml to the
     * S-125 Buoy Shape enum.
     *
     * @param buoyShape     The INT-1-preset.xml buoy shape
     * @return the S-125 Buoy Shape enum entry
     */
    public static S125BuoyShape parseBuoyShape(String buoyShape) {
        switch (buoyShape) {
            case "conical": return S125BuoyShape.CONICAL_NUN_OGIVAL;
            case "can": return S125BuoyShape.CAN_CYLINDRICAL;
            case "spherical": return S125BuoyShape.SPHERICAL;
            case "super-buoy": return S125BuoyShape.SUPER_BUOY;
            case "pillar": return S125BuoyShape.PILLAR;
            case "spar": return S125BuoyShape.SPAR_SPINDLE;
            case "barrel": return S125BuoyShape.BARREL_TUN;
            case "ice-buoy": return S125BuoyShape.ICE_BUOY;
            default: return null;
        }
    }

    /**
     * Translates the category of a light from the INT-1.preset.xml to the
     * S-125 Category of Light enum.
     *
     * @param lightCategory     The INT-1-preset.xml light category
     * @return the S-125 category of light enum entry
     */
    public static S125CategoryOfLight parseLightCategory(String lightCategory) {
        switch (lightCategory) {
            case "front": return S125CategoryOfLight.FRONT;
            case "rear": return S125CategoryOfLight.REAR;
            case "lower": return S125CategoryOfLight.LOWER;
            case "upper": return S125CategoryOfLight.UPPER;
            case "horizontal": return S125CategoryOfLight.HORIZONTALLY_DISPOSED;
            case "vertical":  return S125CategoryOfLight.VERTICALLY_DISPOSED;
            case "directional":  return S125CategoryOfLight.DIRECTIONAL_FUNCTION;
            case "leading": return S125CategoryOfLight.LEADING_LIGHT;
            case "aero": return S125CategoryOfLight.AERO_LIGHT;
            case "air_obstruction": return S125CategoryOfLight.AIR_OBSTRUCTION_LIGHT;
            case "fog_detector": return S125CategoryOfLight.FOG_DETECTOR_LIGHT;
            case "floodlight": return S125CategoryOfLight.FLOOD_LIGHT;
            case "strip_light": return S125CategoryOfLight.STRIP_LIGHT;
            case "subsidiary": return S125CategoryOfLight.SUBSIDIARY_LIGHT;
            case "spotlight": return S125CategoryOfLight.SPOTLIGHT;
            case "moire": return S125CategoryOfLight.MOIRÉ_EFFECT;
            case "emergency": return S125CategoryOfLight.EMERGENCY;
            case "bearing": return S125CategoryOfLight.BEARING_LIGHT;
            default: return null;
        }
    }

    /**
     * Translates the character of a light from the INT-1.preset.xml to the
     * S-125 Light Characteristic enum.
     *
     * @param lightCharacter    The INT-1-preset.xml light character
     * @return the S-125 category of light enum entry
     */
    public static S125LightCharacteristic parseLightCharacter(String lightCharacter) {
        switch (lightCharacter) {
            case "F": return S125LightCharacteristic.FIXED;
            case "Fl": return S125LightCharacteristic.FLASHING;
            case "LFl": return S125LightCharacteristic.LONG_FLASHING;
            case "Q": return S125LightCharacteristic.QUICK_FLASHING;
            case "VQ": return S125LightCharacteristic.VERY_QUICK_FLASHING;
            case "UQ": return S125LightCharacteristic.ULTRA_QUICK_FLASHING;
            case "Iso": return S125LightCharacteristic.ISOPHASED;
            case "Oc": return S125LightCharacteristic.OCCULTING;
            case "IQ": return S125LightCharacteristic.INTERRUPTED_QUICK_FLASHING;
            case "IVQ": return S125LightCharacteristic.INTERRUPTED_VERY_QUICK_FLASHING;
            case "IUQ": return S125LightCharacteristic.INTERRUPTED_ULTRA_QUICK_FLASHING;
            case "Mo": return S125LightCharacteristic.MORSE;
            case "FFl": return S125LightCharacteristic.FIXED_FLASH;
            case "FlLFl": return S125LightCharacteristic.FLASH_LONG_FLASH;
            case "OcFl": return S125LightCharacteristic.OCCULTING_FLASH;
            case "FLFl": return S125LightCharacteristic.FIXED_LONG_FLASH;
            case "Al.Oc": return S125LightCharacteristic.OCCULTING_ALTERNATING;
            case "Al.LFl": return S125LightCharacteristic.LONG_FLASH_ALTERNATING;
            case "Al.Fl": return S125LightCharacteristic.FLASH_ALTERNATING;
            case "Al.Gr": return S125LightCharacteristic.GROUP_ALTERNATING;
            case "Q+LFl": return S125LightCharacteristic.QUICK_FLASH_PLUS_LONG_FLASH;
            case "VQ+LFl": return S125LightCharacteristic.VERY_QUICK_FLASH_PLUS_LONG_FLASH;
            case "UQ+LFl": return S125LightCharacteristic.ULTRA_QUICK_FLASH_PLUS_LONG_FLASH;
            case "Al": return S125LightCharacteristic.ALTERNATING;
            case "Al.FFl": return S125LightCharacteristic.FIXED_AND_ALTERNATING_FLASHING;
            default: return null;
        }
    }

    /**
     * Translates the colour pattern from the INT-1.preset.xml to the
     * S-125 Colour Pattern enum.
     *
     * @param colourPattern     The INT-1-preset.xml colour pattern
     * @return the S-125 Colour Pattern enum
     */
    public static S125ColourPattern parseColourPattern(String colourPattern) {
        switch(colourPattern) {
            case "horizontal": return S125ColourPattern.HORIZONTAL_STRIPES;
            case "vertical": return S125ColourPattern.VERTICAL_STRIPES;
            case "diagonal": return S125ColourPattern.DIAGONAL_STRIPES;
            case "squared": return S125ColourPattern.SQUARED;
            case "stripes": return S125ColourPattern.STRIPES_DIRECTION_UNKNOWN;
            case "border": return S125ColourPattern.BORDER_STRIPE;
            default: return null;
        }
    }

    /**
     * Translates the colour from the INT-1.preset.xml to the S-125 Colour enum.
     *
     * @param colour     The INT-1-preset.xml colour
     * @return the S-125 Colour Pattern enum
     */
    public static S125Colour parseColour(String colour) {
        switch(colour) {
            case "white": return S125Colour.WHITE;
            case "black": return S125Colour.BLACK;
            case "red": return S125Colour.RED;
            case "green": return S125Colour.GREEN;
            case "blue": return S125Colour.BLUE;
            case "yellow": return S125Colour.YELLOW;
            case "grey": return S125Colour.GREY;
            case "brown": return S125Colour.BROWN;
            case "amber": return S125Colour.AMBER;
            case "violet": return S125Colour.VIOLET;
            case "orange": return S125Colour.ORANGE;
            case "magenta": return S125Colour.MAGENTA;
            case "pink": return S125Colour.PINK;
            default: return null;
        }
    }

    /**
     * Translates the function from the INT-1.preset.xml to the S-125 Function
     * enum.
     *
     * @param function     The INT-1-preset.xml function
     * @return the S-125 Function enum
     */
    public static S125Function parseFunction(String function) {
        switch(function) {
            case "harbour_master": return S125Function.HARBOUR_MASTER_S_OFFICE;
            case "customs": return S125Function.CUSTOM_OFFICE;
            case "health": return S125Function.HEALTH_OFFICE;
            case "hospital": return S125Function.HOSPITAL;
            case "post_office": return S125Function.POST_OFFICE;
            case "hotel": return S125Function.HOTEL;
            case "railway_station": return S125Function.RAILWAY_STATION;
            case "police_station": return S125Function.POLICE_STATION;
            case "water-police_station": return S125Function.WATER_POLICE_STATION;
            case "pilot_office": return S125Function.PILOT_OFFICE;
            case "pilot_lookout": return S125Function.PILOT_LOOKOUT;
            case "bank": return S125Function.BANK_OFFICE;
            case "district_control": return S125Function.HEADQUARTERS_FOR_DISTRICT_CONTROL;
            case "transit_shed": return S125Function.TRANSIT_SHED_WAREHOUSE;
            case "factory": return S125Function.FACTORY;
            case "power_station": return S125Function.POWER_STATION;
            case "administrative": return S125Function.ADMINISTRATIVE;
            case "educational": return S125Function.EDUCATIONAL_FACILITY;
            case "church": return S125Function.CHURCH;
            case "chapel": return S125Function.CHAPEL;
            case "temple": return S125Function.TEMPLE;
            case "pagoda": return S125Function.PAGODA;
            case "shinto_shrine": return S125Function.SHINTO_SHRINE;
            case "buddhist_temple": return S125Function.BUDDHIST_TEMPLE;
            case "mosque": return S125Function.MOSQUE;
            case "marebout": return S125Function.MARABOUT;
            case "lookout": return S125Function.LOOKOUT;
            case "communication": return S125Function.COMMUNICATION;
            case "television": return S125Function.TELEVISION;
            case "radio": return S125Function.RADIO;
            case "radar": return S125Function.RADAR;
            case "light_support": return S125Function.LIGHT_SUPPORT;
            case "microwave": return S125Function.MICROWAVE;
            case "cooling": return S125Function.COOLING;
            case "observation": return S125Function.OBSERVATION;
            case "time_ball": return S125Function.TIMEBALL;
            case "clock": return S125Function.CLOCK;
            case "control": return S125Function.CONTROL;
            case "airship_mooring": return S125Function.AIRSHIP_MOORING;
            case "stadium": return S125Function.STADIUM;
            case "bus_station": return S125Function.BUS_STATION;
            default: return null;
        }
    }

}
