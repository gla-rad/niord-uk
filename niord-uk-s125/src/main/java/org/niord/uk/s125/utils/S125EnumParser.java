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
package org.niord.uk.s125.utils;

import _int.iho.s125.gml.cs0._1.*;

import java.util.*;
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
                .filter(Objects::nonNull)
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
        return switch (categoryOfLandmark) {
            case "chimney" -> CategoryOfLandmarkType.CHIMNEY;
            case "mast" -> CategoryOfLandmarkType.MAST;
            case "monument" -> CategoryOfLandmarkType.MONUMENT;
            case "dome" -> CategoryOfLandmarkType.DOME;
            case "radar_scanner" -> CategoryOfLandmarkType.RADAR_SCANNER;
            case "tower" -> CategoryOfLandmarkType.TOWER;
            case "windmotor" -> CategoryOfLandmarkType.WINDMOTOR;
            default -> null;
        };
    }

    /**
     * Translates the virtual AtoN category from the INT-1.preset.xml to the 
     * S-125 Virtual AIS Aid Navigation Purpose Type enum.
     *
     * @param virtualAisAidsToNavigationType        The INT-1-preset.xml virtual AtoN category
     * @return the S-125 Virtual AIS Aid Navigation Purpose Type
     */
    public static VirtualAISAidToNavigationTypeType parseVirtualAisAidToNavigationType(String virtualAisAidsToNavigationType) {
        return switch (virtualAisAidsToNavigationType) {
            case "north_cardinal" -> VirtualAISAidToNavigationTypeType.NORTH_CARDINAL;
            case "south_cardinal" -> VirtualAISAidToNavigationTypeType.SOUTH_CARDINAL;
            case "east_cardinal" -> VirtualAISAidToNavigationTypeType.EAST_CARDINAL;
            case "west_cardinal" -> VirtualAISAidToNavigationTypeType.WEST_CARDINAL;
            case "port_lateral" -> VirtualAISAidToNavigationTypeType.PORT_LATERAL;
            case "starboard_lateral" -> VirtualAISAidToNavigationTypeType.STARBOARD_LATERAL;
            case "preferred_port" -> VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_PORT;
            case "preferred_starboard" -> VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_STARBOARD;
            case "isolated_danger" -> VirtualAISAidToNavigationTypeType.ISOLATED_DANGER;
            case "safe_water" -> VirtualAISAidToNavigationTypeType.SAFE_WATER;
            case "special_purpose" -> VirtualAISAidToNavigationTypeType.SPECIAL_PURPOSE;
            case "wreck" -> VirtualAISAidToNavigationTypeType.NEW_DANGER_MARKING;
            default -> null;
        };
    }
    
    /**
     * Translates the construction from the INT-1.preset.xml to the S-125
     * Nature Of Construction enum.
     *
     * @param natureOfConstruction        The INT-1-preset.xml construction
     * @return the S-125 Nature Of Construction enum entry
     */
    public static NatureOfConstructionType parseNatureOfConstruction(String natureOfConstruction) {
        return switch (natureOfConstruction) {
            case "masonry" -> NatureOfConstructionType.MASONRY;
            case "hard-surfaced" -> NatureOfConstructionType.HARD_SURFACE;
            case "concreted" -> NatureOfConstructionType.CONCRETED;
            case "loose_boulders" -> NatureOfConstructionType.LOOSE_BOULDERS;
            case "wooden" -> NatureOfConstructionType.WOODEN;
            case "metal" -> NatureOfConstructionType.METAL;
            case "painted" -> NatureOfConstructionType.PAINTED;
            case "grp" -> NatureOfConstructionType.FIBERGLASS;
            case "plastic" -> NatureOfConstructionType.PLASTIC;
            default -> null;
        };
    }

    /**
     * Translates the entry from the INT-1.preset.xml to the S-125 Status enum.
     *
     * @param status        The INT-1-preset.xml status
     * @return the S-125 Status enum entry
     */
    public static StatusType parseStatus(String status) {
        return switch (status) {
            case "permanent" -> StatusType.PERMANENT;
            case "not_in_use" -> StatusType.NOT_IN_USE;
            case "periodic/intermittent" -> StatusType.PERIODIC_INTERMITTENT;
            case "temporary" -> StatusType.TEMPORARY;
            case "private" -> StatusType.PRIVATE;
            case "public" -> StatusType.PUBLIC;
            case "watched" -> StatusType.WATCHED;
            case "unwatched" -> StatusType.UN_WATCHED;
            case "confirmed" -> StatusType.CONFIRMED;
            case "candidate" -> StatusType.CANDIDATE;
            case "under_modification" -> StatusType.UNDER_MODIFICATION;
            case "candidate_for_modification" -> StatusType.CANDIDATE_FOR_MODIFICATION;
            case "under_removal/deletion" -> StatusType.UNDER_REMOVAL_DELETION;
            case "removed/deleted" -> StatusType.REMOVED_DELETED;
            case "experimental" -> StatusType.EXPERIMENTAL;
            case "temporarily discontinued" -> StatusType.TEMPORARILY_DISCONTINUED;
            case "temporarily relocated" -> StatusType.TEMPORARILY_RELOCATED;
            default -> null;
        };
    }
    
    /**
     * Translates the radar conspicuous from the INT-1.preset.xml to
     * the S-125 Radar Conspicuous System enum.
     *
     * @param radarConspicuous     The INT-1-preset.xml radar conspicuous
     * @return the S-125 Radar Conspicuous System enum entry
     */
    public static RadarConspicuousType parseRadarConspicuous(String radarConspicuous) {
        return switch (radarConspicuous) {
            case "conspicuous" -> RadarConspicuousType.RADAR_CONSPICUOUS;
            case "not_conspicuous" -> RadarConspicuousType.NOT_RADAR_CONSPICUOUS;
            default -> null;
        };
    }

    /**
     * Translates the visually conspicuous from the INT-1.preset.xml to
     * the S-125 Visually Conspicuous System enum.
     *
     * @param visuallyConspicuous     The INT-1-preset.xml visually conspicuous
     * @return the S-125 Visually Conspicuous System enum entry
     */
    public static VisualProminenceType parseVisualProminence(String visuallyConspicuous) {
        return switch (visuallyConspicuous) {
            case "conspicuous" -> VisualProminenceType.VISUALLY_CONSPICUOUS;
            case "not_conspicuous" -> VisualProminenceType.NOT_VISUALLY_CONSPICUOUS;
            default -> null;
        };
    }

    /**
     * Translates the marks navigational system of from the INT-1.preset.xml to
     * the S-125 Marks Navigational System Of enum.
     *
     * @param marksNavigationalSystemOf     The INT-1-preset.xml marks navigational system of
     * @return the S-125 Marks Navigational System Of enum entry
     */
    public static MarksNavigationalSystemOfType parseMarksNavigationalSystemOf(String marksNavigationalSystemOf) {
        return switch (marksNavigationalSystemOf) {
            case "iala-a" -> MarksNavigationalSystemOfType.IALA_A;
            case "iala-b" -> MarksNavigationalSystemOfType.IALA_B;
            case "cevni" -> MarksNavigationalSystemOfType.OTHER_SYSTEM;
            case "none" -> MarksNavigationalSystemOfType.NO_SYSTEM;
            default -> null;
        };
    }

    /**
     * Translates the category of installation buoy from the INT-1.preset.xml to the
     * S-125 Category of Installation Buoy enum.
     *
     * @param installationBuoy  The INT-1-preset.xml category of installation buoy
     * @return the S-125 Category of Installation Buoy enum entry
     */
    public static CategoryOfInstallationBuoyType parseCategoryOfInstallationBuoy(String installationBuoy) {
        return switch (installationBuoy) {
            case "calm" ->  CategoryOfInstallationBuoyType.CATENARY_ANCHOR_LEG_MOORING_CALM;
            case "sbm" -> CategoryOfInstallationBuoyType.SINGLE_BUOY_MOORING_SBM_OR_SPM;
            default -> null;
        };
    }

    /**
     * Translates the category of special purpose mark from the INT-1.preset.xml to the
     * S-125 Category of Special Purpose Mark enum.
     *
     * @param specialPurposeMark    The INT-1-preset.xml special purpose mark
     * @return the S-125 Category of Special Purpose Mark enum entry
     */
    public static CategoryOfSpecialPurposeMarkType parseCategoryOfSpecialPurposeMark(String specialPurposeMark) {
        return switch (specialPurposeMark) {
            case ("firing_danger_area") -> CategoryOfSpecialPurposeMarkType.FIRING_DANGER_MARK;
            case ("target") -> CategoryOfSpecialPurposeMarkType.TARGET_MARK;
            case ("marker_ship") -> CategoryOfSpecialPurposeMarkType.MARKER_SHIP_MARK;
            case ("degaussing_range") -> CategoryOfSpecialPurposeMarkType.DEGAUSSING_RANGE_MARK;
            case ("barge") -> CategoryOfSpecialPurposeMarkType.BARGE_MARK;
            case ("cable") -> CategoryOfSpecialPurposeMarkType.CABLE_MARK;
            case ("spoil_ground") -> CategoryOfSpecialPurposeMarkType.SPOIL_GROUND_MARK;
            case ("outfall") -> CategoryOfSpecialPurposeMarkType.OUTFALL_MARK;
            case ("odas") -> CategoryOfSpecialPurposeMarkType.ODAS_OCEAN_DATA_ACQUISITION_SYSTEM;
            case ("recording") -> CategoryOfSpecialPurposeMarkType.RECORDING_MARK;
            case ("seaplane_anchorage") -> CategoryOfSpecialPurposeMarkType.SEAPLANE_ANCHORAGE_MARK;
            case ("recreation_zone") -> CategoryOfSpecialPurposeMarkType.RECREATION_ZONE_MARK;
            case ("private") -> CategoryOfSpecialPurposeMarkType.PRIVATE_MARK;
            case ("mooring") -> CategoryOfSpecialPurposeMarkType.MOORING_MARK;
            case ("lanby") -> CategoryOfSpecialPurposeMarkType.LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY;
            case ("leading") -> CategoryOfSpecialPurposeMarkType.LEADING_MARK;
            case ("measured_distance") -> CategoryOfSpecialPurposeMarkType.MEASURED_DISTANCE_MARK;
            case ("notice") -> CategoryOfSpecialPurposeMarkType.NOTICE_MARK;
            case ("tss") -> CategoryOfSpecialPurposeMarkType.TSS_MARK_TRAFFIC_SEPARATION_SCHEME;
            case ("no_anchoring") -> CategoryOfSpecialPurposeMarkType.ANCHORING_PROHIBITED_MARK;
            case ("no_berthing") -> CategoryOfSpecialPurposeMarkType.BERTHING_PROHIBITED_MARK;
            case ("no_overtaking") -> CategoryOfSpecialPurposeMarkType.OVERTAKING_PROHIBITED_MARK;
            case ("no_two-way_traffic") -> CategoryOfSpecialPurposeMarkType.TWO_WAY_TRAFFIC_PROHIBITED_MARK;
            case ("reduced_wake") -> CategoryOfSpecialPurposeMarkType.REDUCED_WAKE_MARK;
            case ("speed_limit") -> CategoryOfSpecialPurposeMarkType.SPEED_LIMIT_MARK;
            case ("stop") -> CategoryOfSpecialPurposeMarkType.STOP_MARK;
            case ("warning") -> CategoryOfSpecialPurposeMarkType.GENERAL_WARNING_MARK;
            case ("sound_ship_siren") -> CategoryOfSpecialPurposeMarkType.SOUND_SHIP_S_SIREN_MARK;
            case ("restricted_vertical_clearance") -> CategoryOfSpecialPurposeMarkType.RESTRICTED_VERTICAL_CLEARANCE_MARK;
            case ("maximum_vessel_draught") -> CategoryOfSpecialPurposeMarkType.MAXIMUM_VESSEL_S_DRAUGHT_MARK;
            case ("restricted_horizontal_clearance") -> CategoryOfSpecialPurposeMarkType.RESTRICTED_HORIZONTAL_CLEARANCE_MARK;
            case ("strong_current") -> CategoryOfSpecialPurposeMarkType.STRONG_CURRENT_WARNING_MARK;
            case ("berthing") -> CategoryOfSpecialPurposeMarkType.BERTHING_PERMITTED_MARK;
            case ("overhead_power_cable") -> CategoryOfSpecialPurposeMarkType.OVERHEAD_POWER_CABLE_MARK;
            case ("channel_edge_gradient") -> CategoryOfSpecialPurposeMarkType.CHANNEL_EDGE_GRADIENT_MARK;
            case ("telephone") -> CategoryOfSpecialPurposeMarkType.TELEPHONE_MARK;
            case ("ferry_crossing") -> CategoryOfSpecialPurposeMarkType.FERRY_CROSSING_MARK;
            case ("pipeline") -> CategoryOfSpecialPurposeMarkType.PIPELINE_MARK;
            case ("anchorage") -> CategoryOfSpecialPurposeMarkType.ANCHORAGE_MARK;
            case ("clearing") -> CategoryOfSpecialPurposeMarkType.CLEARING_MARK;
            case ("control") -> CategoryOfSpecialPurposeMarkType.CONTROL_MARK;
            case ("diving") -> CategoryOfSpecialPurposeMarkType.DIVING_MARK;
            case ("refuge_beacon") -> CategoryOfSpecialPurposeMarkType.REFUGE_BEACON;
            case ("foul_ground") -> CategoryOfSpecialPurposeMarkType.FOUL_GROUND_MARK;
            case ("yachting") -> CategoryOfSpecialPurposeMarkType.YACHTING_MARK;
            case ("heliport") -> CategoryOfSpecialPurposeMarkType.HELIPORT_MARK;
            case ("gps") -> CategoryOfSpecialPurposeMarkType.GNSS_MARK;
            case ("seaplane_landing") -> CategoryOfSpecialPurposeMarkType.SEAPLANE_LANDING_MARK;
            case ("no_entry") -> CategoryOfSpecialPurposeMarkType.ENTRY_PROHIBITED_MARK;
            case ("work_in_progress") -> CategoryOfSpecialPurposeMarkType.WORK_IN_PROGRESS_MARK;
            case ("unknown_purpose") -> CategoryOfSpecialPurposeMarkType.MARK_WITH_UNKNOWN_PURPOSE;
            case ("wellhead") -> CategoryOfSpecialPurposeMarkType.WELLHEAD_MARK;
            case ("channel_separation") ->  CategoryOfSpecialPurposeMarkType.CHANNEL_SEPARATION_MARK;
            case ("marine_farm") -> CategoryOfSpecialPurposeMarkType.MARINE_FARM_MARK;
            case ("artificial_reef") -> CategoryOfSpecialPurposeMarkType.ARTIFICIAL_REEF_MARK;
            default -> null;
        };
    }

    /**
     * Translates the category of lateral mark from the INT-1.preset.xml to the
     * S-125 Category of Lateral Mark enum.
     *
     * @param lateralMark   The INT-1-preset.xml lateral mark
     * @return the S-125 Category of Lateral Mark enum entry
     */
    public static CategoryOfLateralMarkType parseCategoryOfLateralMark(String lateralMark) {
        return switch (lateralMark) {
            case "port" -> CategoryOfLateralMarkType.PORT_HAND_LATERAL_MARK;
            case "starboard" -> CategoryOfLateralMarkType.STARBOARD_HAND_LATERAL_MARK;
            case "preferred_channel_starboard" ->CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK;
            case "preferred_channel_port" -> CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK;
            default -> null;
        };
    }

    /**
     * Translates the category of cardinal mark from the INT-1.preset.xml to the
     * S-125 Category of Cardinal Mark enum.
     *
     * @param cardinalMark  The INT-1-preset.xml cardinal mark
     * @return the S-125 Category of Cardinal Mark enum entry
     */
    public static CategoryOfCardinalMarkType parseCategoryOfCardinalMark(String cardinalMark) {
        return switch (cardinalMark) {
            case "north" -> CategoryOfCardinalMarkType.NORTH_CARDINAL_MARK;
            case "east" -> CategoryOfCardinalMarkType.EAST_CARDINAL_MARK;
            case "south" -> CategoryOfCardinalMarkType.SOUTH_CARDINAL_MARK;
            case "west" -> CategoryOfCardinalMarkType.WEST_CARDINAL_MARK;
            default -> null;
        };
    }

    /**
     * Translates the beacon shape from the INT-1.preset.xml to the
     * S-125 Beacon Shape enum.
     *
     * @param beaconShape    The INT-1-preset.xml beacon shape
     * @return the S-125 Beacon Shape enum entry
     */
    public static BeaconShapeType parseBeaconShape(String beaconShape) {
        return switch (beaconShape) {
            case "stake", "pole", "perch", "post" -> BeaconShapeType.STAKE_POLE_PERCH_POST;
            case "tower" -> BeaconShapeType.BEACON_TOWER;
            case "lattice" -> BeaconShapeType.LATTICE_BEACON;
            case "pile" -> BeaconShapeType.PILE_BEACON;
            default -> null;
        };
    }

    /**
     * Translates the buoy shape from the INT-1.preset.xml to the
     * S-125 Buoy Shape enum.
     *
     * @param buoyShape     The INT-1-preset.xml buoy shape
     * @return the S-125 Buoy Shape enum entry
     */
    public static BuoyShapeType parseBuoyShape(String buoyShape) {
        return switch (buoyShape) {
            case "conical" -> BuoyShapeType.CONICAL_NUN_OGIVAL;
            case "can" -> BuoyShapeType.CAN_CYLINDRICAL;
            case "spherical" -> BuoyShapeType.SPHERICAL;
            case "super-buoy" -> BuoyShapeType.SUPER_BUOY;
            case "pillar" -> BuoyShapeType.PILLAR;
            case "spar" -> BuoyShapeType.SPAR_SPINDLE;
            case "barrel" -> BuoyShapeType.BARREL_TUN;
            case "ice-buoy" -> BuoyShapeType.ICE_BUOY;
            default -> null;
        };
    }

    /**
     * Translates the category of a light from the INT-1.preset.xml to the
     * S-125 Category of Light enum.
     *
     * @param lightCategory     The INT-1-preset.xml light category
     * @return the S-125 category of light enum entry
     */
    public static CategoryOfLightType parseLightCategory(String lightCategory) {
        return switch (lightCategory) {
            case "leading" -> CategoryOfLightType.LEADING_LIGHT;
            case "aero" -> CategoryOfLightType.AERO_LIGHT;
            case "air_obstruction" -> CategoryOfLightType.AIR_OBSTRUCTION_LIGHT;
            case "fog_detector" -> CategoryOfLightType.FOG_DETECTOR_LIGHT;
            case "floodlight" -> CategoryOfLightType.FLOOD_LIGHT;
            case "strip_light" -> CategoryOfLightType.STRIP_LIGHT;
            case "subsidiary" -> CategoryOfLightType.SUBSIDIARY_LIGHT;
            case "spotlight" -> CategoryOfLightType.SPOTLIGHT;
            case "front" -> CategoryOfLightType.FRONT;
            case "rear" -> CategoryOfLightType.REAR;
            case "lower" -> CategoryOfLightType.LOWER;
            case "upper" -> CategoryOfLightType.UPPER;
            case "emergency" -> CategoryOfLightType.EMERGENCY;
            case "horizontal" -> CategoryOfLightType.HORIZONTALLY_DISPOSED;
            case "vertical" -> CategoryOfLightType.VERTICALLY_DISPOSED;
            case "bridge_light" -> CategoryOfLightType.BRIDGE_LIGHT;
            default -> null;
        };
    }

    /**
     * Translates the character of a light from the INT-1.preset.xml to the
     * S-125 Light Characteristic enum.
     *
     * @param lightCharacter    The INT-1-preset.xml light character
     * @return the S-125 category of light enum entry
     */
    public static LightCharacteristicType parseLightCharacter(String lightCharacter) {
        return switch (lightCharacter) {
            case "F" -> LightCharacteristicType.FIXED;
            case "Fl" -> LightCharacteristicType.FLASHING;
            case "LFl" -> LightCharacteristicType.LONG_FLASHING;
            case "Q" -> LightCharacteristicType.QUICK_FLASHING;
            case "VQ" -> LightCharacteristicType.VERY_QUICK_FLASHING;
            case "UQ" -> LightCharacteristicType.ULTRA_QUICK_FLASHING;
            case "Iso" -> LightCharacteristicType.ISOPHASED;
            case "Oc" -> LightCharacteristicType.OCCULTING;
            case "IQ" -> LightCharacteristicType.INTERRUPTED_QUICK_FLASHING;
            case "IVQ" -> LightCharacteristicType.INTERRUPTED_VERY_QUICK_FLASHING;
            case "IUQ" -> LightCharacteristicType.INTERRUPTED_ULTRA_QUICK_FLASHING;
            case "Mo" -> LightCharacteristicType.MORSE;
            case "FFl" -> LightCharacteristicType.FIXED_AND_FLASH;
            case "FlLFl" -> LightCharacteristicType.FLASH_AND_LONG_FLASH;
            case "OcFl" -> LightCharacteristicType.OCCULTING_AND_FLASH;
            case "FLFl" -> LightCharacteristicType.FIXED_AND_LONG_FLASH;
            case "Al.Oc" -> LightCharacteristicType.OCCULTING_ALTERNATING;
            case "Al.LFl" -> LightCharacteristicType.LONG_FLASH_ALTERNATING;
            case "Al.Fl" -> LightCharacteristicType.FLASH_ALTERNATING;
            case "Al.FFl" -> LightCharacteristicType.FLASH_ALTERNATING;
            case "Al.Gr" -> LightCharacteristicType.FLASH_ALTERNATING;
            case "Q+LFl" -> LightCharacteristicType.QUICK_FLASH_PLUS_LONG_FLASH;
            case "VQ+LFl" -> LightCharacteristicType.VERY_QUICK_FLASH_PLUS_LONG_FLASH;
            case "UQ+LFl" -> LightCharacteristicType.ULTRA_QUICK_FLASH_PLUS_LONG_FLASH;
            case "Al" -> LightCharacteristicType.ALTERNATING;
            default -> null;
        };
    }

    /**
     * Translates the colour pattern from the INT-1.preset.xml to the
     * S-125 Colour Pattern enum.
     *
     * @param colourPattern     The INT-1-preset.xml colour pattern
     * @return the S-125 Colour Pattern enum
     */
    public static ColourPatternType parseColourPattern(String colourPattern) {
        return switch (colourPattern) {
            case "horizontal" -> ColourPatternType.HORIZONTAL_STRIPES;
            case "vertical" -> ColourPatternType.VERTICAL_STRIPES;
            case "diagonal" -> ColourPatternType.DIAGONAL_STRIPES;
            case "squared" -> ColourPatternType.SQUARED;
            case "stripes" -> ColourPatternType.STRIPES_DIRECTION_UNKNOWN;
            case "border" -> ColourPatternType.BORDER_STRIPE;
            case "single" -> ColourPatternType.SINGLE_COLOUR;
            default -> null;
        };
    }

    /**
     * Translates the colour from the INT-1.preset.xml to the S-125 Colour enum.
     *
     * @param colour     The INT-1-preset.xml colour
     * @return the S-125 Colour Pattern enum
     */
    public static ColourType parseColour(String colour) {
        return switch (colour) {
            case "white" -> ColourType.WHITE;
            case "black" -> ColourType.BLACK;
            case "red" -> ColourType.RED;
            case "green" -> ColourType.GREEN;
            case "blue" -> ColourType.BLUE;
            case "yellow" -> ColourType.YELLOW;
            case "grey" -> ColourType.GREY;
            case "brown" -> ColourType.BROWN;
            case "fluorescent_white" -> ColourType.FLUORESCENT_WHITE;
            case "fluorescent_red" -> ColourType.FLUORESCENT_RED;
            case "fluorescent_green" -> ColourType.FLUORESCENT_GREEN;
            case "fluorescent_orange" -> ColourType.FLUORESCENT_ORANGE;
            default -> null;
        };
    }

    /**
     * Translates the function from the INT-1.preset.xml to the S-125 Function
     * enum.
     *
     * @param function     The INT-1-preset.xml function
     * @return the S-125 Function enum
     */
    public static FunctionType parseFunction(String function) {
        return switch (function) {
            case "customs" -> FunctionType.CUSTOMS_OFFICE;
            case "hospital" -> FunctionType.HOSPITAL;
            case "post_office" -> FunctionType.POST_OFFICE;
            case "hotel" -> FunctionType.HOTEL;
            case "railway_station" -> FunctionType.RAILWAY_STATION;
            case "police_station" -> FunctionType.POLICE_STATION;
            case "water-police_station" -> FunctionType.WATER_POLICE_STATION;
            case "bank" -> FunctionType.BANK_OFFICE;
            case "power_station" -> FunctionType.POWER_STATION;
            case "educational" -> FunctionType.EDUCATIONAL_FACILITY;
            case "church" -> FunctionType.CHURCH;
            case "temple" -> FunctionType.TEMPLE;
            case "television" -> FunctionType.TELEVISION;
            case "radio" -> FunctionType.RADIO;
            case "radar" -> FunctionType.RADAR;
            case "light_support" -> FunctionType.LIGHT_SUPPORT;
            case "bus_station" -> FunctionType.BUS_STATION;
            default -> null;
        };
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
     * Translates the radio transponder category from the INT-1.preset.xml to
     * the S-125 radio transponder beacon (RACON) category.
     *
     * @param radioTransponderBeaconCategory  The INT-1-preset.xml radio transponder category
     * @return The S-125 radio station category enum
     */
    public static CategoryOfRadarTransponderBeaconType parseRadioTransponderBeaconCategory(String radioTransponderBeaconCategory) {
        switch(radioTransponderBeaconCategory) {
            case "ramark": return CategoryOfRadarTransponderBeaconType.RAMARK_RADAR_BEACON_TRANSMITTING_CONTINUOUSLY;
            case "racon": return CategoryOfRadarTransponderBeaconType.RACON_RADAR_TRANSPONDER_BEACON;
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
