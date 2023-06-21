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
    public static CategoryOfLandmarkType parseCategoryOfLandmark(String categoryOfLandmark) {
        switch(categoryOfLandmark) {
            case "cairn": return CategoryOfLandmarkType.CAIRN;
            case "cemetery": return CategoryOfLandmarkType.CEMETERY;
            case "chimney": return CategoryOfLandmarkType.CHIMNEY;
            case "dish_aerial": return CategoryOfLandmarkType.DISH_AERIAL;
            case "flagstaff": return CategoryOfLandmarkType.FLAGSTAFF_FLAGPOLE;
            case "flare_stack": return CategoryOfLandmarkType.FLARE_STACK;
            case "mast": return CategoryOfLandmarkType.MAST;
            case "windsock": return CategoryOfLandmarkType.WIND_SOCK;
            case "monument": return CategoryOfLandmarkType.MONUMENT;
            case "column": return CategoryOfLandmarkType.COLUMN_PILLAR;
            case "memorial": return CategoryOfLandmarkType.MEMORIAL_PLAQUE;
            case "obelisk": return CategoryOfLandmarkType.OBELISK;
            case "statue": return CategoryOfLandmarkType.STATUE;
            case "cross": return CategoryOfLandmarkType.CROSS;
            case "dome": return CategoryOfLandmarkType.DOME;
            case "radar_scanner": return CategoryOfLandmarkType.RADAR_SCANNER;
            case "tower": return CategoryOfLandmarkType.TOWER;
            case "windmill": return CategoryOfLandmarkType.WINDMILL;
            case "windmotor": return CategoryOfLandmarkType.WINDMOTOR;
            case "spire": return CategoryOfLandmarkType.SPIRE_MINARET;
            case "boulder": return CategoryOfLandmarkType.LARGE_ROCK_OR_BOULDER_ON_LAND;
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
    public static VirtualAISAidToNavigationTypeType parseVirtualAisAidToNavigationType(String virtualAisAidsToNavigationType) {
        switch(virtualAisAidsToNavigationType) {
            case "north_cardinal": return VirtualAISAidToNavigationTypeType.NORTH_CARDINAL;
            case "south_cardinal": return VirtualAISAidToNavigationTypeType.SOUTH_CARDINAL;
            case "east_cardinal": return VirtualAISAidToNavigationTypeType.EAST_CARDINAL;
            case "west_cardinal": return VirtualAISAidToNavigationTypeType.WEST_CARDINAL;
            case "port_lateral": return VirtualAISAidToNavigationTypeType.PORT_LATERAL;
            case "starboard_lateral": return VirtualAISAidToNavigationTypeType.STARBOARD_LATERAL;
            case "preferred_port": return VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_PORT;
            case "preferred_starboard": return VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_STARBOARD;
            case "isolated_danger": return VirtualAISAidToNavigationTypeType.ISOLATED_DANGER;
            case "safe_water": return VirtualAISAidToNavigationTypeType.SAFE_WATER;
            case "special_purpose": return VirtualAISAidToNavigationTypeType.SPECIAL_PURPOSE;
            case "wreck": return VirtualAISAidToNavigationTypeType.NEW_DANGER_MARKING;
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
    public static NatureOfConstructionType parseNatureOfConstruction(String natureOfConstruction) {
        switch(natureOfConstruction) {
            case "masonry": return NatureOfConstructionType.MASONRY;
            case "concreted": return NatureOfConstructionType.CONCRETED;
            case "loose_boulders": return NatureOfConstructionType.LOOSE_BOULDERS;
            case "hard-surfaced": return NatureOfConstructionType.HARD_SURFACE;
            case "wooden": return NatureOfConstructionType.WOODEN;
            case "metal": return NatureOfConstructionType.METAL;
            case "grp": return NatureOfConstructionType.GLASS_REINFORCED_PLASTIC_GRP;
            case "painted": return NatureOfConstructionType.PAINTED;
            default: return null;
        }
    }

    /**
     * Translates the entry from the INT-1.preset.xml to the S-125 Status enum.
     *
     * @param status        The INT-1-preset.xml status
     * @return the S-125 Status enum entry
     */
    public static StatusType parseStatus(String status) {
        switch(status) {
            case "permanent": return StatusType.PERMANENT;
            case "occasional": return StatusType.OCCASIONAL;
            case "recommended": return StatusType.RECOMMENDED;
            case "not_in_use": return StatusType.NOT_IN_USE;
            case "intermittent": return StatusType.PERIODIC_INTERMITTENT;
            case "reserved": return StatusType.RESERVED;
            case "temporary": return StatusType.TEMPORARY;
            case "private": return StatusType.PRIVATE;
            case "mandatory": return StatusType.MANDATORY;
            case "extinguished": return StatusType.EXTINGUISHED;
            case "illuminated": return StatusType.ILLUMINATED;
            case "historic": return StatusType.HISTORIC;
            case "public": return StatusType.PUBLIC;
            case "synchronised": return StatusType.SYNCHRONIZED;
            case "watched": return StatusType.WATCHED;
            case "unwatched": return StatusType.UN_WATCHED;
            case "existence_doubtful": return StatusType.EXISTENCE_DOUBTFUL;
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
    public static RadarConspicuousType parseRadarConspicuous(String radarConspicuous) {
        switch(radarConspicuous) {
            case "conspicuous": return RadarConspicuousType.RADAR_CONSPICUOUS;
            case "not_conspicuous": return  RadarConspicuousType.NOT_RADAR_CONSPICUOUS;
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
    public static VisualProminenceType parseVisuallyConspicuous(String visuallyConspicuous) {
        switch(visuallyConspicuous) {
            case "conspicuous": return VisualProminenceType.VISUALLY_CONSPICUOUS;
            case "not_conspicuous": return  VisualProminenceType.NOT_VISUALLY_CONSPICUOUS;
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
    public static MarksNavigationalSystemOfType parseMarksNavigationalSystemOf(String marksNavigationalSystemOf) {
        switch(marksNavigationalSystemOf) {
            case "iala-a": return MarksNavigationalSystemOfType.IALA_A;
            case "iala-b": return  MarksNavigationalSystemOfType.IALA_B;
            case "cevni": return MarksNavigationalSystemOfType.OTHER_SYSTEM;
            case "none": return MarksNavigationalSystemOfType.NO_SYSTEM;
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
    public static CategoryOfInstallationBuoyType parseCategoryOfInstallationBuoy(String installationBuoy) {
        switch (installationBuoy) {
            case "calm": return CategoryOfInstallationBuoyType.CATENARY_ANCHOR_LEG_MOORING_CALM;
            case "sbm": return CategoryOfInstallationBuoyType.SINGLE_BUOY_MOORING_SBM_OR_SPM;
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
    public static CategoryOfSpecialPurposeMarkType parseCategoryOfSpecialPurposeMark(String specialPurposeMark) {
        switch (specialPurposeMark) {
            case("firing_danger_area"): return CategoryOfSpecialPurposeMarkType.FIRING_DANGER_MARK;
            case("target"): return CategoryOfSpecialPurposeMarkType.TARGET_MARK;
            case("marker_ship"): return CategoryOfSpecialPurposeMarkType.MARKER_SHIP_MARK;
            case("degaussing_range"): return CategoryOfSpecialPurposeMarkType.DEGAUSSING_RANGE_MARK;
            case("barge"): return CategoryOfSpecialPurposeMarkType.BARGE_MARK;
            case("cable"): return CategoryOfSpecialPurposeMarkType.CABLE_MARK;
            case("spoil_ground"): return CategoryOfSpecialPurposeMarkType.SPOIL_GROUND_MARK;
            case("outfall"): return CategoryOfSpecialPurposeMarkType.OUTFALL_MARK;
            case("odas"): return CategoryOfSpecialPurposeMarkType.ODAS_OCEAN_DATA_ACQUISITION_SYSTEM;
            case("recording"): return CategoryOfSpecialPurposeMarkType.RECORDING_MARK;
            case("seaplane_anchorage"): return CategoryOfSpecialPurposeMarkType.SEAPLANE_ANCHORAGE_MARK;
            case("recreation_zone"): return CategoryOfSpecialPurposeMarkType.RECREATION_ZONE_MARK;
            case("private"): return CategoryOfSpecialPurposeMarkType.PRIVATE_MARK;
            case("mooring"): return CategoryOfSpecialPurposeMarkType.MOORING_MARK;
            case("lanby"): return CategoryOfSpecialPurposeMarkType.LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY;
            case("leading"): return CategoryOfSpecialPurposeMarkType.LEADING_MARK;
            case("measured_distance"): return CategoryOfSpecialPurposeMarkType.MEASURED_DISTANCE_MARK;
            case("notice"): return CategoryOfSpecialPurposeMarkType.NOTICE_MARK;
            case("tss"): return CategoryOfSpecialPurposeMarkType.TSS_MARK_TRAFFIC_SEPARATION_SCHEME;
            case("no_anchoring"): return CategoryOfSpecialPurposeMarkType.ANCHORING_PROHIBITED_MARK;
            case("no_berthing"): return CategoryOfSpecialPurposeMarkType.BERTHING_PROHIBITED_MARK;
            case("no_overtaking"): return CategoryOfSpecialPurposeMarkType.OVERTAKING_PROHIBITED_MARK;
            case("no_two-way_traffic"): return CategoryOfSpecialPurposeMarkType.TWO_WAY_TRAFFIC_PROHIBITED_MARK;
            case("reduced_wake"): return CategoryOfSpecialPurposeMarkType.REDUCED_WAKE_MARK;
            case("speed_limit"): return CategoryOfSpecialPurposeMarkType.SPEED_LIMIT_MARK;
            case("stop"): return CategoryOfSpecialPurposeMarkType.STOP_MARK;
            case("warning"): return CategoryOfSpecialPurposeMarkType.GENERAL_WARNING_MARK;
            case("sound_ship_siren"): return CategoryOfSpecialPurposeMarkType.SOUND_SHIP_S_SIREN_MARK;
            case("restricted_vertical_clearance"): return CategoryOfSpecialPurposeMarkType.RESTRICTED_VERTICAL_CLEARANCE_MARK;
            case("maximum_vessel_draught"): return CategoryOfSpecialPurposeMarkType.MAXIMUM_VESSEL_S_DRAUGHT_MARK;
            case("restricted_horizontal_clearance"): return CategoryOfSpecialPurposeMarkType.RESTRICTED_HORIZONTAL_CLEARANCE_MARK;
            case("strong_current"): return CategoryOfSpecialPurposeMarkType.STRONG_CURRENT_WARNING_MARK;
            case("berthing"): return CategoryOfSpecialPurposeMarkType.BERTHING_PERMITTED_MARK;
            case("overhead_power_cable"): return CategoryOfSpecialPurposeMarkType.OVERHEAD_POWER_CABLE_MARK;
            case("channel_edge_gradient"): return CategoryOfSpecialPurposeMarkType.CHANNEL_EDGE_GRADIENT_MARK;
            case("telephone"): return CategoryOfSpecialPurposeMarkType.TELEPHONE_MARK;
            case("ferry_crossing"): return CategoryOfSpecialPurposeMarkType.FERRY_CROSSING_MARK;
            case("pipeline"): return CategoryOfSpecialPurposeMarkType.PIPELINE_MARK;
            case("anchorage"): return CategoryOfSpecialPurposeMarkType.ANCHORAGE_MARK;
            case("clearing"): return CategoryOfSpecialPurposeMarkType.CLEARING_MARK;
            case("control"): return CategoryOfSpecialPurposeMarkType.CONTROL_MARK;
            case("diving"): return CategoryOfSpecialPurposeMarkType.DIVING_MARK;
            case("refuge_beacon"): return CategoryOfSpecialPurposeMarkType.REFUGE_BEACON;
            case("foul_ground"): return CategoryOfSpecialPurposeMarkType.FOUL_GROUND_MARK;
            case("yachting"): return CategoryOfSpecialPurposeMarkType.YACHTING_MARK;
            case("heliport"): return CategoryOfSpecialPurposeMarkType.HELIPORT_MARK;
            case("gps"): return CategoryOfSpecialPurposeMarkType.GNSS_MARK;
            case("seaplane_landing"): return CategoryOfSpecialPurposeMarkType.SEAPLANE_LANDING_MARK;
            case("no_entry"): return CategoryOfSpecialPurposeMarkType.ENTRY_PROHIBITED_MARK;
            case("work_in_progress"): return CategoryOfSpecialPurposeMarkType.WORK_IN_PROGRESS_MARK;
            case("unknown_purpose"): return CategoryOfSpecialPurposeMarkType.MARK_WITH_UNKNOWN_PURPOSE;
            case("wellhead"): return CategoryOfSpecialPurposeMarkType.WELLHEAD_MARK;
            case("channel_separation"): return CategoryOfSpecialPurposeMarkType.CHANNEL_SEPARATION_MARK;
            case("marine_farm"): return CategoryOfSpecialPurposeMarkType.MARINE_FARM_MARK;
            case("artificial_reef"): return CategoryOfSpecialPurposeMarkType.ARTIFICIAL_REEF_MARK;
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
    public static CategoryOfLateralMarkType parseCategoryOfLateralMark(String lateralMark) {
        switch (lateralMark) {
            case "port": return CategoryOfLateralMarkType.PORT_HAND_LATERAL_MARK;
            case "starboard": return CategoryOfLateralMarkType.STARBOARD_HAND_LATERAL_MARK;
            case "preferred_channel_starboard": return CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK;
            case "preferred_channel_port": return CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK;
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
    public static CategoryOfCardinalMarkType parseCategoryOfCardinalMark(String cardinalMark) {
        switch (cardinalMark) {
            case "north": return CategoryOfCardinalMarkType.NORTH_CARDINAL_MARK;
            case "east": return CategoryOfCardinalMarkType.EAST_CARDINAL_MARK;
            case "south": return CategoryOfCardinalMarkType.SOUTH_CARDINAL_MARK;
            case "west": return CategoryOfCardinalMarkType.WEST_CARDINAL_MARK;
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
    public static BeaconShapeType parseBeaconShape(String beaconShape) {
        switch (beaconShape) {
            case "stake": case "pole": case "perch": case "post": return BeaconShapeType.STAKE_POLE_PERCH_POST;
            case "withy": return BeaconShapeType.WITHY;
            case "tower": return BeaconShapeType.BEACON_TOWER;
            case "lattice": return BeaconShapeType.LATTICE_BEACON;
            case "pile": return BeaconShapeType.PILE_BEACON;
            case "cairn": return BeaconShapeType.CAIRN;
            case "buoyant": return BeaconShapeType.BUOYANT_BEACON;
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
    public static BuoyShapeType parseBuoyShape(String buoyShape) {
        switch (buoyShape) {
            case "conical": return BuoyShapeType.CONICAL_NUN_OGIVAL;
            case "can": return BuoyShapeType.CAN_CYLINDRICAL;
            case "spherical": return BuoyShapeType.SPHERICAL;
            case "super-buoy": return BuoyShapeType.SUPER_BUOY;
            case "pillar": return BuoyShapeType.PILLAR;
            case "spar": return BuoyShapeType.SPAR_SPINDLE;
            case "barrel": return BuoyShapeType.BARREL_TUN;
            case "ice-buoy": return BuoyShapeType.ICE_BUOY;
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
    public static CategoryOfLightType parseLightCategory(String lightCategory) {
        switch (lightCategory) {
            case "front": return CategoryOfLightType.FRONT;
            case "rear": return CategoryOfLightType.REAR;
            case "lower": return CategoryOfLightType.LOWER;
            case "upper": return CategoryOfLightType.UPPER;
            case "horizontal": return CategoryOfLightType.HORIZONTALLY_DISPOSED;
            case "vertical":  return CategoryOfLightType.VERTICALLY_DISPOSED;
            case "directional":  return CategoryOfLightType.DIRECTIONAL_FUNCTION;
            case "leading": return CategoryOfLightType.LEADING_LIGHT;
            case "aero": return CategoryOfLightType.AERO_LIGHT;
            case "air_obstruction": return CategoryOfLightType.AIR_OBSTRUCTION_LIGHT;
            case "fog_detector": return CategoryOfLightType.FOG_DETECTOR_LIGHT;
            case "floodlight": return CategoryOfLightType.FLOOD_LIGHT;
            case "strip_light": return CategoryOfLightType.STRIP_LIGHT;
            case "subsidiary": return CategoryOfLightType.SUBSIDIARY_LIGHT;
            case "spotlight": return CategoryOfLightType.SPOTLIGHT;
            case "moire": return CategoryOfLightType.MOIRE_EFFECT;
            case "emergency": return CategoryOfLightType.EMERGENCY;
            case "bearing": return CategoryOfLightType.BEARING_LIGHT;
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
    public static LightCharacteristicType parseLightCharacter(String lightCharacter) {
        switch (lightCharacter) {
            case "F": return LightCharacteristicType.FIXED;
            case "Fl": return LightCharacteristicType.FLASHING;
            case "LFl": return LightCharacteristicType.LONG_FLASHING;
            case "Q": return LightCharacteristicType.QUICK_FLASHING;
            case "VQ": return LightCharacteristicType.VERY_QUICK_FLASHING;
            case "UQ": return LightCharacteristicType.ULTRA_QUICK_FLASHING;
            case "Iso": return LightCharacteristicType.ISOPHASED;
            case "Oc": return LightCharacteristicType.OCCULTING;
            case "IQ": return LightCharacteristicType.INTERRUPTED_QUICK_FLASHING;
            case "IVQ": return LightCharacteristicType.INTERRUPTED_VERY_QUICK_FLASHING;
            case "IUQ": return LightCharacteristicType.INTERRUPTED_ULTRA_QUICK_FLASHING;
            case "Mo": return LightCharacteristicType.MORSE;
            case "FFl": return LightCharacteristicType.FIXED_AND_FLASH;
            case "FlLFl": return LightCharacteristicType.FLASH_AND_LONG_FLASH;
            case "OcFl": return LightCharacteristicType.OCCULTING_AND_FLASH;
            case "FLFl": return LightCharacteristicType.FIXED_AND_LONG_FLASH;
            case "Al.Oc": return LightCharacteristicType.OCCULTING_ALTERNATING;
            case "Al.LFl": return LightCharacteristicType.LONG_FLASH_ALTERNATING;
            case "Al.Fl": return LightCharacteristicType.FLASH_ALTERNATING;
            case "Al.Gr": return LightCharacteristicType.FLASH_ALTERNATING;
            case "Q+LFl": return LightCharacteristicType.QUICK_FLASH_PLUS_LONG_FLASH;
            case "VQ+LFl": return LightCharacteristicType.VERY_QUICK_FLASH_PLUS_LONG_FLASH;
            case "UQ+LFl": return LightCharacteristicType.ULTRA_QUICK_FLASH_PLUS_LONG_FLASH;
            case "Al": return LightCharacteristicType.ALTERNATING;
            case "Al.FFl": return LightCharacteristicType.FIXED_AND_ALTERNATING_FLASHING;
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
    public static ColourPatternType parseColourPattern(String colourPattern) {
        switch(colourPattern) {
            case "horizontal": return ColourPatternType.HORIZONTAL_STRIPES;
            case "vertical": return ColourPatternType.VERTICAL_STRIPES;
            case "diagonal": return ColourPatternType.DIAGONAL_STRIPES;
            case "squared": return ColourPatternType.SQUARED;
            case "stripes": return ColourPatternType.STRIPES_DIRECTION_UNKNOWN;
            case "border": return ColourPatternType.BORDER_STRIPE;
            case "single": return ColourPatternType.SINGLE_COLOUR;
            default: return null;
        }
    }

    /**
     * Translates the colour from the INT-1.preset.xml to the S-125 Colour enum.
     *
     * @param colour     The INT-1-preset.xml colour
     * @return the S-125 Colour Pattern enum
     */
    public static ColourType parseColour(String colour) {
        switch(colour) {
            case "white": return ColourType.WHITE;
            case "black": return ColourType.BLACK;
            case "red": return ColourType.RED;
            case "green": return ColourType.GREEN;
            case "blue": return ColourType.BLUE;
            case "yellow": return ColourType.YELLOW;
            case "grey": return ColourType.GREY;
            case "brown": return ColourType.BROWN;
            case "amber": return ColourType.AMBER;
            case "violet": return ColourType.VIOLET;
            case "orange": return ColourType.ORANGE;
            case "magenta": return ColourType.MAGENTA;
            case "pink": return ColourType.PINK;
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
    public static FunctionType parseFunction(String function) {
        switch(function) {
            case "harbour_master": return FunctionType.HARBOUR_MASTER_S_OFFICE;
            case "customs": return FunctionType.CUSTOMS_OFFICE;
            case "health": return FunctionType.HEALTH_OFFICE;
            case "hospital": return FunctionType.HOSPITAL;
            case "post_office": return FunctionType.POST_OFFICE;
            case "hotel": return FunctionType.HOTEL;
            case "railway_station": return FunctionType.RAILWAY_STATION;
            case "police_station": return FunctionType.POLICE_STATION;
            case "water-police_station": return FunctionType.WATER_POLICE_STATION;
            case "pilot_office": return FunctionType.PILOT_OFFICE;
            case "pilot_lookout": return FunctionType.PILOT_LOOKOUT;
            case "bank": return FunctionType.BANK_OFFICE;
            case "district_control": return FunctionType.HEADQUARTERS_FOR_DISTRICT_CONTROL;
            case "transit_shed": return FunctionType.TRANSIT_SHED_WAREHOUSE;
            case "factory": return FunctionType.FACTORY;
            case "power_station": return FunctionType.POWER_STATION;
            case "administrative": return FunctionType.ADMINISTRATIVE;
            case "educational": return FunctionType.EDUCATIONAL_FACILITY;
            case "church": return FunctionType.CHURCH;
            case "chapel": return FunctionType.CHAPEL;
            case "temple": return FunctionType.TEMPLE;
            case "pagoda": return FunctionType.PAGODA;
            case "shinto_shrine": return FunctionType.SHINTO_SHRINE;
            case "buddhist_temple": return FunctionType.BUDDHIST_TEMPLE;
            case "mosque": return FunctionType.MOSQUE;
            case "marebout": return FunctionType.MARABOUT;
            case "lookout": return FunctionType.LOOKOUT;
            case "communication": return FunctionType.COMMUNICATION;
            case "television": return FunctionType.TELEVISION;
            case "radio": return FunctionType.RADIO;
            case "radar": return FunctionType.RADAR;
            case "light_support": return FunctionType.LIGHT_SUPPORT;
            case "microwave": return FunctionType.MICROWAVE;
            case "cooling": return FunctionType.COOLING;
            case "observation": return FunctionType.OBSERVATION;
            case "time_ball": return FunctionType.TIME_BALL;
            case "clock": return FunctionType.CLOCK;
            case "control": return FunctionType.CONTROL;
            case "airship_mooring": return FunctionType.AIRSHIP_MOORING;
            case "stadium": return FunctionType.STADIUM;
            case "bus_station": return FunctionType.BUS_STATION;
            default: return null;
        }
    }

    /**
     * Translates the topmark/daymark shape from the INT-1.preset.xml to the
     * S-125 topmark/daymark shape.
     *
     * @param topmarkDaymarkShape   The INT-1-preset.xml topmark/daymark shape
     * @return The S-125 topmark/daymark shape enum
     */
    public static TopmarkDaymarkShapeType parseTopmarkDaymarkShape(String topmarkDaymarkShape) {
        switch(topmarkDaymarkShape) {
            case "cone, point up": return TopmarkDaymarkShapeType.VALUE_1;
            case "sphere": return TopmarkDaymarkShapeType.VALUE_2;
            case "2 spheres": return TopmarkDaymarkShapeType.VALUE_3;
            case "cylinder": return TopmarkDaymarkShapeType.VALUE_4;
            case "board": return TopmarkDaymarkShapeType.VALUE_5;
            case "x-shape": return TopmarkDaymarkShapeType.VALUE_6;
            case "2 cones point together": return TopmarkDaymarkShapeType.VALUE_7;
            case "2 cones base together": return TopmarkDaymarkShapeType.VALUE_8;
            case "rhombus": return TopmarkDaymarkShapeType.VALUE_9;
            case "2 cones uo": return TopmarkDaymarkShapeType.VALUE_10;
            case "2 cones down": return TopmarkDaymarkShapeType.VALUE_11;
            case "square": return TopmarkDaymarkShapeType.VALUE_12;
            case "rectangle, horizontal": return TopmarkDaymarkShapeType.VALUE_13;
            case "rectangle, vertica": return TopmarkDaymarkShapeType.VALUE_14;
            case "trapezium, up": return TopmarkDaymarkShapeType.VALUE_15;
            case "trapezium, down": return TopmarkDaymarkShapeType.VALUE_16;
            case "triangle, point up": return TopmarkDaymarkShapeType.VALUE_17;
            case "triangle, point down": return TopmarkDaymarkShapeType.VALUE_18;
            case "other shape": return TopmarkDaymarkShapeType.VALUE_19;
            case "tubular": return TopmarkDaymarkShapeType.VALUE_20;
            default: return null;
        }
    }

    /**
     * Translates the fog signal type from the INT-1.preset.xml to the
     * S-125 fog signal type.
     *
     * @param fogSignalCategory     The INT-1-preset.xml fog signal type
     * @return The S-125 fog signal type enum
     */
    public static CategoryOfFogSignalType parseFogSignalCategory(String fogSignalCategory) {
        switch(fogSignalCategory) {
            case "bell": return CategoryOfFogSignalType.BELL;
            case "horm": return CategoryOfFogSignalType.HORN;
            case "siren": return CategoryOfFogSignalType.SIREN;
            case "whistle": return CategoryOfFogSignalType.WHISTLE;
            default: return null;
        }
    }

    /**
     * Translates the radio station category from the INT-1.preset.xml to the
     * S-125 radio station category.
     *
     * @param radioStationCategory  The INT-1-preset.xml radio station category
     * @return The S-125 radio station category enum
     */
    public static CategoryOfRadioStationType parseRadioStationCategory(String radioStationCategory) {
        switch(radioStationCategory) {
            case "ais": return CategoryOfRadioStationType.AIS_BASE_STATION;
            case "differential": return CategoryOfRadioStationType.DIFFERENTIAL_GNSS;
            default: return null;
        }
    }

    /**
     * Translates the building shape from the INT-1.preset.xml to the S-125
     * building shape.
     *
     * @param buildingShape         The INT-1-preset.xml building shape
     * @return The S-125 building shape enum
     */
    public static BuildingShapeType parseBuildingShape(String buildingShape) {
        switch(buildingShape) {
            case "cubic": return BuildingShapeType.CUBIC;
            case "spherical": return BuildingShapeType.SPHERICAL;
            case "high rise building": return BuildingShapeType.HIGH_RISE_BUILDING;
            case "cylindrical": return BuildingShapeType.CYLINDRICAL;
            case "pyramid": return BuildingShapeType.PYRAMID;
            default: return null;
        }
    }

    /**
     * Translates the building shape from the INT-1.preset.xml to the S-125
     * building shape.
     *
     * @param siloTankCategory      The INT-1-preset.xml building shape
     * @return The S-125 building shape enum
     */
    public static CategoryOfSiloTankType parseSiloTankCategory(String siloTankCategory) {
        switch(siloTankCategory) {
            case "silo": return CategoryOfSiloTankType.SILO_IN_GENERAL;
            case "tank": return CategoryOfSiloTankType.TANK_IN_GENERAL;
            default: return null;
        }
    }

}
