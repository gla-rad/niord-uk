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

import _int.iho.s_125.gml.cs0._1.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The S-125 Enum Parser Utility Class.
 * <p/>
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
            case "hard-surfaced" -> NatureOfConstructionType.HARD_SURFACED;
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
            case "unwatched" -> StatusType.UNWATCHED;
            case "confirmed" -> StatusType.CONFIRMED;
            case "candidate" -> StatusType.CANDIDATE;
            case "under_modification" -> StatusType.UNDER_MODIFICATION;
            case "candidate_for_modification" -> StatusType.CANDIDATE_FOR_MODIFICATION;
            case "under_removal/deletion" -> StatusType.UNDER_REMOVAL_DELETION;
            case "removed/deleted" -> StatusType.REMOVED_DELETED;
            case "experimental" -> StatusType.EXPERIMENTAL;
            case "discontinued" -> StatusType.DISCONTINUED;
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
            case "calm" ->  CategoryOfInstallationBuoyType.CATENARY_ANCHOR_LEG_MOORING;
            case "sbm" -> CategoryOfInstallationBuoyType.SINGLE_BUOY_MOORING;
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
            case ("odas") -> CategoryOfSpecialPurposeMarkType.ODAS;
            case ("recording") -> CategoryOfSpecialPurposeMarkType.RECORDING_MARK;
            case ("seaplane_anchorage") -> CategoryOfSpecialPurposeMarkType.SEAPLANE_ANCHORAGE_MARK;
            case ("recreation_zone") -> CategoryOfSpecialPurposeMarkType.RECREATION_ZONE_MARK;
            case ("private") -> CategoryOfSpecialPurposeMarkType.PRIVATE_MARK;
            case ("mooring") -> CategoryOfSpecialPurposeMarkType.MOORING_MARK;
            case ("lanby") -> CategoryOfSpecialPurposeMarkType.LANBY;
            case ("leading") -> CategoryOfSpecialPurposeMarkType.LEADING_MARK;
            case ("measured_distance") -> CategoryOfSpecialPurposeMarkType.MEASURED_DISTANCE_MARK;
            case ("notice") -> CategoryOfSpecialPurposeMarkType.NOTICE_MARK;
            case ("tss") -> CategoryOfSpecialPurposeMarkType.TSS_MARK;
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
            case "preferred_channel_port" ->CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK;
            case "preferred_channel_starboard" -> CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK;
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
            case "conical" -> BuoyShapeType.CONICAL;
            case "can" -> BuoyShapeType.CAN;
            case "spherical" -> BuoyShapeType.SPHERICAL;
            case "pillar" -> BuoyShapeType.PILLAR;
            case "spar" -> BuoyShapeType.SPAR;
            case "barrel" -> BuoyShapeType.BARREL;
            case "super-buoy" -> BuoyShapeType.SUPERBUOY;
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
            case "directional function" -> CategoryOfLightType.DIRECTIONAL_FUNCTION;
            case "leading" -> CategoryOfLightType.LEADING_LIGHT;
            case "aero" -> CategoryOfLightType.AERO_LIGHT;
            case "air_obstruction" -> CategoryOfLightType.AIR_OBSTRUCTION_LIGHT;
            case "floodlight" -> CategoryOfLightType.FLOOD_LIGHT;
            case "strip_light" -> CategoryOfLightType.STRIP_LIGHT;
            case "subsidiary" -> CategoryOfLightType.SUBSIDIARY_LIGHT;
            case "spotlight" -> CategoryOfLightType.SPOTLIGHT;
            case "front" -> CategoryOfLightType.FRONT;
            case "rear" -> CategoryOfLightType.REAR;
            case "lower" -> CategoryOfLightType.LOWER;
            case "upper" -> CategoryOfLightType.UPPER;
            case "emergency" -> CategoryOfLightType.EMERGENCY;
            case "bearing" -> CategoryOfLightType.BEARING_LIGHT;
            case "horizontal" -> CategoryOfLightType.HORIZONTALLY_DISPOSED;
            case "vertical" -> CategoryOfLightType.VERTICALLY_DISPOSED;
            default -> null;
        };
    }

    /**
     * Translates the visibility of a light from the INT-1.preset.xml to the
     * S-201 Light Visibility enum.
     *
     * @param lightVisibility     The INT-1-preset.xml light visibility
     * @return the S-201 category of light enum entry
     */
    public static LightVisibilityType parseLightVisibility(String lightVisibility) {
        return switch (lightVisibility) {
            case "high" -> LightVisibilityType.HIGH_INTENSITY;
            case "low" -> LightVisibilityType.LOW_INTENSITY;
            case "faint" -> LightVisibilityType.FAINT;
            case "intensified" -> LightVisibilityType.INTENSIFIED;
            case "unintensified" -> LightVisibilityType.UNINTENSIFIED;
            case "restricted" -> LightVisibilityType.VISIBILITY_DELIBERATELY_RESTRICTED;
            case "obscured" -> LightVisibilityType.OBSCURED;
            case "part_obscured" -> LightVisibilityType.PARTIALLY_OBSCURED;
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
            case "UQ" -> LightCharacteristicType.CONTINUOUS_ULTRA_QUICK_FLASHING;
            case "Iso" -> LightCharacteristicType.ISOPHASED;
            case "Oc" -> LightCharacteristicType.OCCULTING;
            case "Mo" -> LightCharacteristicType.MORSE;
            case "FFl" -> LightCharacteristicType.FIXED_AND_FLASH;
            case "FlLFl" -> LightCharacteristicType.FLASH_AND_LONG_FLASH;
            case "OcFl" -> LightCharacteristicType.OCCULTING_AND_FLASH;
            case "FLFl" -> LightCharacteristicType.FIXED_AND_LONG_FLASH;
            case "Al.Oc" -> LightCharacteristicType.OCCULTING_ALTERNATING;
            case "Al.LFl" -> LightCharacteristicType.LONG_FLASH_ALTERNATING;
            case "Al.Fl" -> LightCharacteristicType.FLASH_ALTERNATING;
            case "Al.FFl" -> LightCharacteristicType.FIXED_AND_ALTERNATING_FLASHING;
            case "Al.Gr" -> LightCharacteristicType.GROUP_ALTERNATING;
            case "Q+LFl" -> LightCharacteristicType.QUICK_FLASH_PLUS_LONG_FLASH;
            case "VQ+LFl" -> LightCharacteristicType.VERY_QUICK_FLASH_PLUS_LONG_FLASH;
            case "UQ+LFl" -> LightCharacteristicType.ULTRA_QUICK_FLASH_PLUS_LONG_FLASH;
            case "Al" -> LightCharacteristicType.ALTERNATING;
            default -> {
                if (lightCharacter.matches("Oc\\w?([0-9]*)")) {
                    yield LightCharacteristicType.GROUP_OCCULTING_LIGHT;
                } else if (lightCharacter.matches("Oc\\w?([0-9]*\\+[0-1]*)")) {
                    yield LightCharacteristicType.COMPOSITE_GROUP_OCCULTING_LIGHT;
                } else if (lightCharacter.matches("Fl\\w?([0-9]*)")) {
                    yield LightCharacteristicType.GROUP_FLASHING_LIGHT;
                } else if (lightCharacter.matches("Fl\\w?([0-9]*\\+[0-1]*)")) {
                    yield LightCharacteristicType.COMPOSITE_GROUP_FLASHING_LIGHT;
                } else if (lightCharacter.matches("Q\\w?([0-9]*)")) {
                    yield LightCharacteristicType.GROUP_QUICK_LIGHT;
                } else if (lightCharacter.matches("VQ\\w?([0-9]*\\+[0-1]*)")) {
                    yield LightCharacteristicType.GROUP_VERY_QUICK_LIGHT;
                } else {
                    yield null;
                }
            }
        };
    }

    /**
     * Translates the Signal Generation from the INT-1.preset.xml to the
     * S-201 Signal Generation enum.
     *
     * @param signalGeneration     The INT-1-preset.xml Signal Generation
     * @return the S-201 Signal Generation enum
     */
    public static SignalGenerationType parseSignalGeneration(String signalGeneration) {
        return switch (signalGeneration) {
            case "Automatically" -> SignalGenerationType.AUTOMATICALLY;
            case "By Wave Action" -> SignalGenerationType.BY_WAVE_ACTION;
            case "By Hand" -> SignalGenerationType.BY_HAND;
            case "By Wind" -> SignalGenerationType.BY_WIND;
            case "Radio Activated" -> SignalGenerationType.RADIO_ACTIVATED;
            case "Call Activated" -> SignalGenerationType.CALL_ACTIVATED;
            default -> null;
        };
    }

    /**
     * Translates the Signal Status from the INT-1.preset.xml to the
     * S-201 Signal Status enum.
     *
     * @param signalStatus     The INT-1-preset.xml Signal Status
     * @return the S-201 Signal Status enum
     */
    public static SignalStatusType parseSignalStatus(String signalStatus) {
        return switch (signalStatus) {
            case "lit/sound" -> SignalStatusType.LIT_SOUND;
            case "eclipsed/silent" -> SignalStatusType.ECLIPSED_SILENT;
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
            case "amber" -> ColourType.AMBER;
            case "violet" -> ColourType.VIOLET;
            case "orange" -> ColourType.ORANGE;
            case "magenta" -> ColourType.MAGENTA;
            case "pink" -> ColourType.PINK;
            case "green A" -> ColourType.GREEN_A;
            case "green B" -> ColourType.GREEN_B;
            case "white temporary" -> ColourType.WHITE_TEMPORARY;
            case "red temporary" -> ColourType.RED_TEMPORARY;
            case "yellow temporary" -> ColourType.YELLOW_TEMPORARY;
            case "green preferred" -> ColourType.GREEN_PREFERRED;
            case "green temporary" -> ColourType.GREEN_TEMPORARY;
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
        return switch (fogSignalCategory) {
            case "bell" -> CategoryOfFogSignalType.BELL;
            case "horm" -> CategoryOfFogSignalType.HORN;
            case "siren" -> CategoryOfFogSignalType.SIREN;
            case "whistle" -> CategoryOfFogSignalType.WHISTLE;
            default -> null;
        };
    }

    /**
     * Translates the radio station category from the INT-1.preset.xml to the
     * S-125 radio station category.
     *
     * @param radioStationCategory  The INT-1-preset.xml radio station category
     * @return The S-125 radio station category enum
     */
    public static CategoryOfRadioStationType parseRadioStationCategory(String radioStationCategory) {
        return switch (radioStationCategory) {
            case "ais" -> CategoryOfRadioStationType.AIS_BASE_STATION;
            case "differential" -> CategoryOfRadioStationType.DIFFERENTIAL_GNSS;
            default -> null;
        };
    }

    /**
     * Translates the radio transponder category from the INT-1.preset.xml to
     * the S-125 radio transponder beacon (RACON) category.
     *
     * @param radioTransponderBeaconCategory  The INT-1-preset.xml radio transponder category
     * @return The S-125 radio station category enum
     */
    public static CategoryOfRadarTransponderBeaconType parseRadioTransponderBeaconCategory(String radioTransponderBeaconCategory) {
        return switch (radioTransponderBeaconCategory) {
            case "ramark" -> CategoryOfRadarTransponderBeaconType.RAMARK_RADAR_BEACON_TRANSMITTING_CONTINUOUSLY;
            case "racon" -> CategoryOfRadarTransponderBeaconType.RACON_RADAR_TRANSPONDER_BEACON;
            default -> null;
        };
    }

    /**
     * Translates the building shape from the INT-1.preset.xml to the S-125
     * building shape.
     *
     * @param buildingShape         The INT-1-preset.xml building shape
     * @return The S-125 building shape enum
     */
    public static BuildingShapeType parseBuildingShape(String buildingShape) {
        return switch (buildingShape) {
            case "cubic" -> BuildingShapeType.CUBIC;
            case "spherical" -> BuildingShapeType.SPHERICAL;
            case "high rise building" -> BuildingShapeType.HIGH_RISE_BUILDING;
            case "cylindrical" -> BuildingShapeType.CYLINDRICAL;
            case "pyramid" -> BuildingShapeType.PYRAMID;
            default -> null;
        };
    }

    /**
     * Translates the building shape from the INT-1.preset.xml to the S-125
     * building shape.
     *
     * @param siloTankCategory      The INT-1-preset.xml building shape
     * @return The S-125 building shape enum
     */
    public static CategoryOfSiloTankType parseSiloTankCategory(String siloTankCategory) {
        return switch (siloTankCategory) {
            case "silo" -> CategoryOfSiloTankType.SILO_IN_GENERAL;
            case "tank" -> CategoryOfSiloTankType.TANK_IN_GENERAL;
            default -> null;
        };
    }

    /**
     * Translates the vertical datum value from the INT-1.preset.xml to
     * the S-201 VerticalDatumType enum.
     *
     * @param verticalDatumType         The INT-1-preset.xml vertical datum
     * @return the S-201 Vertical Datum Type enum entry
     */
    public static VerticalDatumType parseVerticalDatum(String verticalDatumType) {
        return switch (verticalDatumType) {
            case "Mean Low Water Springs" -> VerticalDatumType.MEAN_LOW_WATER_SPRINGS;
            case "Mean Lower Low Water Springs" -> VerticalDatumType.MEAN_LOWER_LOW_WATER;
            case "Mean Sea Level" -> VerticalDatumType.MEAN_SEA_LEVEL;
            case "Lowest Low Water" -> VerticalDatumType.LOWEST_LOW_WATER;
            case "Mean Low Water" -> VerticalDatumType.MEAN_LOW_WATER;
            case "Lowest Low Water Springs" -> VerticalDatumType.LOWEST_LOW_WATER_SPRINGS;
            case "Approximate Mean Low Water Springs" -> VerticalDatumType.APPROXIMATE_MEAN_LOW_WATER;
            case "Indian Spring Low Water" -> VerticalDatumType.INDIAN_SPRING_LOW_WATER;
            case "Low Water Springs" -> VerticalDatumType.LOW_WATER_SPRINGS;
            case "Approximate Lowest Astronomical Tide" -> VerticalDatumType.APPROXIMATE_LOWEST_ASTRONOMICAL_TIDE;
            case "Nearly Lowest Low Water" -> VerticalDatumType.NEARLY_LOWEST_LOW_WATER;
            case "Mean Lower Low Water" -> VerticalDatumType.MEAN_LOWER_LOW_WATER;
            case "Low Water" -> VerticalDatumType.LOW_WATER;
            case "Approximate Mean Low Water" -> VerticalDatumType.APPROXIMATE_MEAN_LOW_WATER;
            case "Approximate Mean Lower Low Water" -> VerticalDatumType.APPROXIMATE_MEAN_LOWER_LOW_WATER;
            case "Mean High Water" -> VerticalDatumType.MEAN_HIGH_WATER;
            case "Mean High Water Springs" -> VerticalDatumType.MEAN_HIGH_WATER_SPRINGS;
            case "High Water" -> VerticalDatumType.HIGH_WATER;
            case "Approximate Mean Sea Level" -> VerticalDatumType.APPROXIMATE_MEAN_SEA_LEVEL;
            case "High Water Springs" -> VerticalDatumType.HIGH_WATER_SPRINGS;
            case "Mean Higher High Water" -> VerticalDatumType.MEAN_HIGHER_HIGH_WATER;
            case "Equinoctial Spring Low Water" -> VerticalDatumType.EQUINOCTIAL_SPRING_LOW_WATER;
            case "Lowest Astronomical Tide" -> VerticalDatumType.LOWEST_ASTRONOMICAL_TIDE;
            case "Local Datum" -> VerticalDatumType.LOCAL_DATUM;
            case "International Great Lakes Datum 1985" -> VerticalDatumType.INTERNATIONAL_GREAT_LAKES_DATUM_1985;
            case "Mean Water Level" -> VerticalDatumType.MEAN_WATER_LEVEL;
            case "Lower Low Water Large Tide" -> VerticalDatumType.LOWER_LOW_WATER_LARGE_TIDE;
            case "Higher High Water Large Tide" -> VerticalDatumType.HIGHER_HIGH_WATER_LARGE_TIDE;
            case "Nearly Highest High Water" -> VerticalDatumType.NEARLY_HIGHEST_HIGH_WATER;
            case "Highest Astronomical Tide" -> VerticalDatumType.HIGHEST_ASTRONOMICAL_TIDE;
            case "Local Low Water Reference Level" -> VerticalDatumType.LOCAL_LOW_WATER_REFERENCE_LEVEL;
            case "Local High Water Reference Level" -> VerticalDatumType.LOCAL_HIGH_WATER_REFERENCE_LEVEL;
            case "Local Mean Water Reference Level" -> VerticalDatumType.LOCAL_MEAN_WATER_REFERENCE_LEVEL;
            case "Equivalent Height of Water (German GlW)" -> VerticalDatumType.EQUIVALENT_HEIGHT_OF_WATER_GERMAN_GL_W;
            case "Highest Shipping Height of Water (German HSW)" -> VerticalDatumType.HIGHEST_SHIPPING_HEIGHT_OF_WATER_GERMAN_HSW;
            case "Reference Low Water Level According to Danube Commission" -> VerticalDatumType.REFERENCE_LOW_WATER_LEVEL_ACCORDING_TO_DANUBE_COMMISSION;
            case "Highest Shipping Height of Water According to Danube Commission" -> VerticalDatumType.HIGHEST_SHIPPING_HEIGHT_OF_WATER_ACCORDING_TO_DANUBE_COMMISSION;
            case "Dutch River Low Water Reference Level (OLR)" -> VerticalDatumType.DUTCH_RIVER_LOW_WATER_REFERENCE_LEVEL_OLR;
            case "Russian Project Water Level" -> VerticalDatumType.RUSSIAN_PROJECT_WATER_LEVEL;
            case "Russian Normal Backwater Level" -> VerticalDatumType.RUSSIAN_NORMAL_BACKWATER_LEVEL;
            case "Ohio River Datum" -> VerticalDatumType.OHIO_RIVER_DATUM;
            case "Dutch High Water Reference Level" -> VerticalDatumType.DUTCH_HIGH_WATER_REFERENCE_LEVEL;
            case "Baltic Sea Chart Datum 2000" -> VerticalDatumType.BALTIC_SEA_CHART_DATUM_2000;
            case "Dutch Estuary Low Water Reference Level (OLW)" -> VerticalDatumType.DUTCH_ESTUARY_LOW_WATER_REFERENCE_LEVEL_OLW;
            default -> null;
        };
    }

}
