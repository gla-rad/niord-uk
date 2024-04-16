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
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * A testing call for the S-125 Enum Parser Utility.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S125EnumParserTest {

    @Test
    public void testSplitAndParse() {
        assertTrue(S125EnumParser.splitAndParse(null, Function.identity()).isEmpty());
        assertFalse(S125EnumParser.splitAndParse("", Function.identity()).isEmpty());
        assertFalse(S125EnumParser.splitAndParse("test", Function.identity()).isEmpty());

        // And actual test
        final Collection<Integer> result = S125EnumParser.splitAndParse("1;2;3", Integer::parseInt);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Check the contents
        final List<Integer> resultList = result.stream().toList();
        assertEquals(3, resultList.size());
        assertEquals(Integer.valueOf(1), resultList.get(0));
        assertEquals(Integer.valueOf(2), resultList.get(1));
        assertEquals(Integer.valueOf(3), resultList.get(2));
    }

    @Test
    public void testParseCategoryOfLandmark() {
        assertEquals(CategoryOfLandmarkType.CHIMNEY, S125EnumParser.parseCategoryOfLandmark("chimney"));
        assertEquals(CategoryOfLandmarkType.MAST, S125EnumParser.parseCategoryOfLandmark("mast"));
        assertEquals(CategoryOfLandmarkType.MONUMENT, S125EnumParser.parseCategoryOfLandmark("monument"));
        assertEquals(CategoryOfLandmarkType.DOME, S125EnumParser.parseCategoryOfLandmark("dome"));
        assertEquals(CategoryOfLandmarkType.RADAR_SCANNER, S125EnumParser.parseCategoryOfLandmark("radar_scanner"));
        assertEquals(CategoryOfLandmarkType.TOWER, S125EnumParser.parseCategoryOfLandmark("tower"));
        assertEquals(CategoryOfLandmarkType.WINDMOTOR, S125EnumParser.parseCategoryOfLandmark("windmotor"));
        assertNull(S125EnumParser.parseCategoryOfLandmark("error"));
    }

    @Test
    public void testParseVirtualAisAidToNavigationType() {
        assertEquals(VirtualAISAidToNavigationTypeType.NORTH_CARDINAL, S125EnumParser.parseVirtualAisAidToNavigationType("north_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.SOUTH_CARDINAL, S125EnumParser.parseVirtualAisAidToNavigationType("south_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.EAST_CARDINAL, S125EnumParser.parseVirtualAisAidToNavigationType("east_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.WEST_CARDINAL, S125EnumParser.parseVirtualAisAidToNavigationType("west_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.PORT_LATERAL, S125EnumParser.parseVirtualAisAidToNavigationType("port_lateral"));
        assertEquals(VirtualAISAidToNavigationTypeType.STARBOARD_LATERAL, S125EnumParser.parseVirtualAisAidToNavigationType("starboard_lateral"));
        assertEquals(VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_PORT, S125EnumParser.parseVirtualAisAidToNavigationType("preferred_port"));
        assertEquals(VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_STARBOARD, S125EnumParser.parseVirtualAisAidToNavigationType("preferred_starboard"));
        assertEquals(VirtualAISAidToNavigationTypeType.ISOLATED_DANGER, S125EnumParser.parseVirtualAisAidToNavigationType("isolated_danger"));
        assertEquals(VirtualAISAidToNavigationTypeType.SAFE_WATER, S125EnumParser.parseVirtualAisAidToNavigationType("safe_water"));
        assertEquals(VirtualAISAidToNavigationTypeType.SPECIAL_PURPOSE, S125EnumParser.parseVirtualAisAidToNavigationType("special_purpose"));
        assertEquals(VirtualAISAidToNavigationTypeType.NEW_DANGER_MARKING, S125EnumParser.parseVirtualAisAidToNavigationType("wreck"));
        assertNull(S125EnumParser.parseVirtualAisAidToNavigationType("error"));
    }

    @Test
    public void testParseNatureOfConstruction() {
        assertEquals(NatureOfConstructionType.MASONRY, S125EnumParser.parseNatureOfConstruction("masonry"));
        assertEquals(NatureOfConstructionType.HARD_SURFACE, S125EnumParser.parseNatureOfConstruction("hard-surfaced"));
        assertEquals(NatureOfConstructionType.CONCRETED, S125EnumParser.parseNatureOfConstruction("concreted"));
        assertEquals(NatureOfConstructionType.LOOSE_BOULDERS, S125EnumParser.parseNatureOfConstruction("loose_boulders"));
        assertEquals(NatureOfConstructionType.WOODEN, S125EnumParser.parseNatureOfConstruction("wooden"));
        assertEquals(NatureOfConstructionType.METAL, S125EnumParser.parseNatureOfConstruction("metal"));
        assertEquals(NatureOfConstructionType.PAINTED, S125EnumParser.parseNatureOfConstruction("painted"));
        assertEquals(NatureOfConstructionType.FIBERGLASS, S125EnumParser.parseNatureOfConstruction("grp"));
        assertEquals(NatureOfConstructionType.PLASTIC, S125EnumParser.parseNatureOfConstruction("plastic"));
        assertNull(S125EnumParser.parseNatureOfConstruction("error"));
    }

    @Test
    public void testParseStatus() {
        assertEquals(StatusType.PERMANENT, S125EnumParser.parseStatus("permanent"));
        assertEquals(StatusType.NOT_IN_USE, S125EnumParser.parseStatus("not_in_use"));
        assertEquals(StatusType.PERIODIC_INTERMITTENT, S125EnumParser.parseStatus("periodic/intermittent"));
        assertEquals(StatusType.TEMPORARY, S125EnumParser.parseStatus("temporary"));
        assertEquals(StatusType.PRIVATE, S125EnumParser.parseStatus("private"));
        assertEquals(StatusType.PUBLIC, S125EnumParser.parseStatus("public"));
        assertEquals(StatusType.WATCHED, S125EnumParser.parseStatus("watched"));
        assertEquals(StatusType.UN_WATCHED, S125EnumParser.parseStatus("unwatched"));
        assertEquals(StatusType.CONFIRMED, S125EnumParser.parseStatus("confirmed"));
        assertEquals(StatusType.CANDIDATE, S125EnumParser.parseStatus("candidate"));
        assertEquals(StatusType.UNDER_MODIFICATION, S125EnumParser.parseStatus("under_modification"));
        assertEquals(StatusType.CANDIDATE_FOR_MODIFICATION, S125EnumParser.parseStatus("candidate_for_modification"));
        assertEquals(StatusType.UNDER_REMOVAL_DELETION, S125EnumParser.parseStatus("under_removal/deletion"));
        assertEquals(StatusType.REMOVED_DELETED, S125EnumParser.parseStatus("removed/deleted"));
        assertEquals(StatusType.EXPERIMENTAL, S125EnumParser.parseStatus("experimental"));
        assertEquals(StatusType.TEMPORARILY_DISCONTINUED, S125EnumParser.parseStatus("temporarily discontinued"));
        assertEquals(StatusType.TEMPORARILY_RELOCATED, S125EnumParser.parseStatus("temporarily relocated"));
        assertNull(S125EnumParser.parseStatus("error"));
    }

    @Test
    public void testParseRadarConspicuous() {
        assertEquals(RadarConspicuousType.RADAR_CONSPICUOUS, S125EnumParser.parseRadarConspicuous("conspicuous"));
        assertEquals(RadarConspicuousType.NOT_RADAR_CONSPICUOUS, S125EnumParser.parseRadarConspicuous("not_conspicuous"));
        assertNull(S125EnumParser.parseRadarConspicuous("error"));
    }

    @Test
    public void testParseVisualProminence() {
        assertEquals(VisualProminenceType.VISUALLY_CONSPICUOUS, S125EnumParser.parseVisualProminence("conspicuous"));
        assertEquals(VisualProminenceType.NOT_VISUALLY_CONSPICUOUS, S125EnumParser.parseVisualProminence("not_conspicuous"));
        assertNull(S125EnumParser.parseVisualProminence("error"));
    }

    @Test
    public void testParseMarksNavigationalSystemOf() {
        assertEquals(MarksNavigationalSystemOfType.IALA_A, S125EnumParser.parseMarksNavigationalSystemOf("iala-a"));
        assertEquals(MarksNavigationalSystemOfType.IALA_B, S125EnumParser.parseMarksNavigationalSystemOf("iala-b"));
        assertEquals(MarksNavigationalSystemOfType.OTHER_SYSTEM, S125EnumParser.parseMarksNavigationalSystemOf("cevni"));
        assertEquals(MarksNavigationalSystemOfType.NO_SYSTEM, S125EnumParser.parseMarksNavigationalSystemOf("none"));
        assertNull(S125EnumParser.parseMarksNavigationalSystemOf("error"));
    }

    @Test
    public void testParseCategoryOfInstallationBuoy() {
        assertEquals(CategoryOfInstallationBuoyType.CATENARY_ANCHOR_LEG_MOORING_CALM, S125EnumParser.parseCategoryOfInstallationBuoy("calm"));
        assertEquals(CategoryOfInstallationBuoyType.SINGLE_BUOY_MOORING_SBM_OR_SPM, S125EnumParser.parseCategoryOfInstallationBuoy("sbm"));
        assertNull(S125EnumParser.parseCategoryOfInstallationBuoy("error"));
    }

    @Test
    public void testParseCategoryOfSpecialPurposeMark() {
        assertEquals(CategoryOfSpecialPurposeMarkType.FIRING_DANGER_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("firing_danger_area"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TARGET_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("target"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MARKER_SHIP_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("marker_ship"));
        assertEquals(CategoryOfSpecialPurposeMarkType.DEGAUSSING_RANGE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("degaussing_range"));
        assertEquals(CategoryOfSpecialPurposeMarkType.BARGE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("barge"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CABLE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("cable"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SPOIL_GROUND_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("spoil_ground"));
        assertEquals(CategoryOfSpecialPurposeMarkType.OUTFALL_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("outfall"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ODAS_OCEAN_DATA_ACQUISITION_SYSTEM, S125EnumParser.parseCategoryOfSpecialPurposeMark("odas"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RECORDING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("recording"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SEAPLANE_ANCHORAGE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("seaplane_anchorage"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RECREATION_ZONE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("recreation_zone"));
        assertEquals(CategoryOfSpecialPurposeMarkType.PRIVATE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("private"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MOORING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("mooring"));
        assertEquals(CategoryOfSpecialPurposeMarkType.LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY, S125EnumParser.parseCategoryOfSpecialPurposeMark("lanby"));
        assertEquals(CategoryOfSpecialPurposeMarkType.LEADING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("leading"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MEASURED_DISTANCE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("measured_distance"));
        assertEquals(CategoryOfSpecialPurposeMarkType.NOTICE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("notice"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TSS_MARK_TRAFFIC_SEPARATION_SCHEME, S125EnumParser.parseCategoryOfSpecialPurposeMark("tss"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ANCHORING_PROHIBITED_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("no_anchoring"));
        assertEquals(CategoryOfSpecialPurposeMarkType.BERTHING_PROHIBITED_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("no_berthing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.OVERTAKING_PROHIBITED_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("no_overtaking"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TWO_WAY_TRAFFIC_PROHIBITED_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("no_two-way_traffic"));
        assertEquals(CategoryOfSpecialPurposeMarkType.REDUCED_WAKE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("reduced_wake"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SPEED_LIMIT_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("speed_limit"));
        assertEquals(CategoryOfSpecialPurposeMarkType.STOP_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("stop"));
        assertEquals(CategoryOfSpecialPurposeMarkType.GENERAL_WARNING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("warning"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SOUND_SHIP_S_SIREN_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("sound_ship_siren"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RESTRICTED_VERTICAL_CLEARANCE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("restricted_vertical_clearance"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MAXIMUM_VESSEL_S_DRAUGHT_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("maximum_vessel_draught"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RESTRICTED_HORIZONTAL_CLEARANCE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("restricted_horizontal_clearance"));
        assertEquals(CategoryOfSpecialPurposeMarkType.STRONG_CURRENT_WARNING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("strong_current"));
        assertEquals(CategoryOfSpecialPurposeMarkType.OVERHEAD_POWER_CABLE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("overhead_power_cable"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CHANNEL_EDGE_GRADIENT_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("channel_edge_gradient"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TELEPHONE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("telephone"));
        assertEquals(CategoryOfSpecialPurposeMarkType.FERRY_CROSSING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("ferry_crossing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.PIPELINE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("pipeline"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ANCHORAGE_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("anchorage"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CLEARING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("clearing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CONTROL_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("control"));
        assertEquals(CategoryOfSpecialPurposeMarkType.DIVING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("diving"));
        assertEquals(CategoryOfSpecialPurposeMarkType.REFUGE_BEACON, S125EnumParser.parseCategoryOfSpecialPurposeMark("refuge_beacon"));
        assertEquals(CategoryOfSpecialPurposeMarkType.FOUL_GROUND_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("foul_ground"));
        assertEquals(CategoryOfSpecialPurposeMarkType.YACHTING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("yachting"));
        assertEquals(CategoryOfSpecialPurposeMarkType.HELIPORT_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("heliport"));
        assertEquals(CategoryOfSpecialPurposeMarkType.GNSS_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("gps"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SEAPLANE_LANDING_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("seaplane_landing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ENTRY_PROHIBITED_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("no_entry"));
        assertEquals(CategoryOfSpecialPurposeMarkType.WORK_IN_PROGRESS_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("work_in_progress"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MARK_WITH_UNKNOWN_PURPOSE, S125EnumParser.parseCategoryOfSpecialPurposeMark("unknown_purpose"));
        assertEquals(CategoryOfSpecialPurposeMarkType.WELLHEAD_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("wellhead"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CHANNEL_SEPARATION_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("channel_separation"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MARINE_FARM_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("marine_farm"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ARTIFICIAL_REEF_MARK, S125EnumParser.parseCategoryOfSpecialPurposeMark("artificial_reef"));
        assertNull(S125EnumParser.parseCategoryOfSpecialPurposeMark("error"));
    }

    @Test
    public void testParseCategoryOfLateralMark() {
        assertEquals(CategoryOfLateralMarkType.PORT_HAND_LATERAL_MARK, S125EnumParser.parseCategoryOfLateralMark("port"));
        assertEquals(CategoryOfLateralMarkType.STARBOARD_HAND_LATERAL_MARK, S125EnumParser.parseCategoryOfLateralMark("starboard"));
        assertEquals(CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK, S125EnumParser.parseCategoryOfLateralMark("preferred_channel_starboard"));
        assertEquals(CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK, S125EnumParser.parseCategoryOfLateralMark("preferred_channel_port"));
        assertNull(S125EnumParser.parseCategoryOfLateralMark("error"));
    }

    @Test
    public void testParseCategoryOfCardinalMark() {
        assertEquals(CategoryOfCardinalMarkType.NORTH_CARDINAL_MARK, S125EnumParser.parseCategoryOfCardinalMark("north"));
        assertEquals(CategoryOfCardinalMarkType.EAST_CARDINAL_MARK, S125EnumParser.parseCategoryOfCardinalMark("east"));
        assertEquals(CategoryOfCardinalMarkType.SOUTH_CARDINAL_MARK, S125EnumParser.parseCategoryOfCardinalMark("south"));
        assertEquals(CategoryOfCardinalMarkType.WEST_CARDINAL_MARK, S125EnumParser.parseCategoryOfCardinalMark("west"));
        assertNull(S125EnumParser.parseCategoryOfCardinalMark("error"));
    }

    @Test
    public void testParseBeaconShape() {
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S125EnumParser.parseBeaconShape("stake"));
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S125EnumParser.parseBeaconShape("pole"));
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S125EnumParser.parseBeaconShape("perch"));
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S125EnumParser.parseBeaconShape("post"));
        assertEquals(BeaconShapeType.BEACON_TOWER, S125EnumParser.parseBeaconShape("tower"));
        assertEquals(BeaconShapeType.LATTICE_BEACON, S125EnumParser.parseBeaconShape("lattice"));
        assertEquals(BeaconShapeType.PILE_BEACON, S125EnumParser.parseBeaconShape("pile"));
        assertNull(S125EnumParser.parseBeaconShape("error"));
    }

    @Test
    public void testParseBuoyShape() {
        assertEquals(BuoyShapeType.CONICAL_NUN_OGIVAL, S125EnumParser.parseBuoyShape("conical"));
        assertEquals(BuoyShapeType.CAN_CYLINDRICAL, S125EnumParser.parseBuoyShape("can"));
        assertEquals(BuoyShapeType.SPHERICAL, S125EnumParser.parseBuoyShape("spherical"));
        assertEquals(BuoyShapeType.SUPER_BUOY, S125EnumParser.parseBuoyShape("super-buoy"));
        assertEquals(BuoyShapeType.PILLAR, S125EnumParser.parseBuoyShape("pillar"));
        assertEquals(BuoyShapeType.SPAR_SPINDLE, S125EnumParser.parseBuoyShape("spar"));
        assertEquals(BuoyShapeType.BARREL_TUN, S125EnumParser.parseBuoyShape("barrel"));
        assertEquals(BuoyShapeType.ICE_BUOY, S125EnumParser.parseBuoyShape("ice-buoy"));
        assertNull(S125EnumParser.parseBuoyShape("error"));
    }

    @Test
    public void testParseLightCategory() {
        assertEquals(CategoryOfLightType.LEADING_LIGHT, S125EnumParser.parseLightCategory("leading"));
        assertEquals(CategoryOfLightType.AERO_LIGHT, S125EnumParser.parseLightCategory("aero"));
        assertEquals(CategoryOfLightType.AIR_OBSTRUCTION_LIGHT, S125EnumParser.parseLightCategory("air_obstruction"));
        assertEquals(CategoryOfLightType.FOG_DETECTOR_LIGHT, S125EnumParser.parseLightCategory("fog_detector"));
        assertEquals(CategoryOfLightType.FLOOD_LIGHT, S125EnumParser.parseLightCategory("floodlight"));
        assertEquals(CategoryOfLightType.STRIP_LIGHT, S125EnumParser.parseLightCategory("strip_light"));
        assertEquals(CategoryOfLightType.SUBSIDIARY_LIGHT, S125EnumParser.parseLightCategory("subsidiary"));
        assertEquals(CategoryOfLightType.SPOTLIGHT, S125EnumParser.parseLightCategory("spotlight"));
        assertEquals(CategoryOfLightType.FRONT, S125EnumParser.parseLightCategory("front"));
        assertEquals(CategoryOfLightType.REAR, S125EnumParser.parseLightCategory("rear"));
        assertEquals(CategoryOfLightType.LOWER, S125EnumParser.parseLightCategory("lower"));
        assertEquals(CategoryOfLightType.UPPER, S125EnumParser.parseLightCategory("upper"));
        assertEquals(CategoryOfLightType.EMERGENCY, S125EnumParser.parseLightCategory("emergency"));
        assertEquals(CategoryOfLightType.HORIZONTALLY_DISPOSED, S125EnumParser.parseLightCategory("horizontal"));
        assertEquals(CategoryOfLightType.VERTICALLY_DISPOSED, S125EnumParser.parseLightCategory("vertical"));
        assertEquals(CategoryOfLightType.BRIDGE_LIGHT, S125EnumParser.parseLightCategory("bridge_light"));
        assertNull(S125EnumParser.parseLightCategory("error"));
    }

    @Test
    public void testParseLightCharacter() {
        assertEquals(LightCharacteristicType.FIXED, S125EnumParser.parseLightCharacter("F"));
        assertEquals(LightCharacteristicType.FLASHING, S125EnumParser.parseLightCharacter("Fl"));
        assertEquals(LightCharacteristicType.LONG_FLASHING, S125EnumParser.parseLightCharacter("LFl"));
        assertEquals(LightCharacteristicType.QUICK_FLASHING, S125EnumParser.parseLightCharacter("Q"));
        assertEquals(LightCharacteristicType.VERY_QUICK_FLASHING, S125EnumParser.parseLightCharacter("VQ"));
        assertEquals(LightCharacteristicType.ULTRA_QUICK_FLASHING, S125EnumParser.parseLightCharacter("UQ"));
        assertEquals(LightCharacteristicType.ISOPHASED, S125EnumParser.parseLightCharacter("Iso"));
        assertEquals(LightCharacteristicType.OCCULTING, S125EnumParser.parseLightCharacter("Oc"));
        assertEquals(LightCharacteristicType.INTERRUPTED_QUICK_FLASHING, S125EnumParser.parseLightCharacter("IQ"));
        assertEquals(LightCharacteristicType.INTERRUPTED_VERY_QUICK_FLASHING, S125EnumParser.parseLightCharacter("IVQ"));
        assertEquals(LightCharacteristicType.INTERRUPTED_ULTRA_QUICK_FLASHING, S125EnumParser.parseLightCharacter("IUQ"));
        assertEquals(LightCharacteristicType.MORSE, S125EnumParser.parseLightCharacter("Mo"));
        assertEquals(LightCharacteristicType.FIXED_AND_FLASH, S125EnumParser.parseLightCharacter("FFl"));
        assertEquals(LightCharacteristicType.FLASH_AND_LONG_FLASH, S125EnumParser.parseLightCharacter("FlLFl"));
        assertEquals(LightCharacteristicType.OCCULTING_AND_FLASH, S125EnumParser.parseLightCharacter("OcFl"));
        assertEquals(LightCharacteristicType.FIXED_AND_LONG_FLASH, S125EnumParser.parseLightCharacter("FLFl"));
        assertEquals(LightCharacteristicType.OCCULTING_ALTERNATING, S125EnumParser.parseLightCharacter("Al.Oc"));
        assertEquals(LightCharacteristicType.LONG_FLASH_ALTERNATING, S125EnumParser.parseLightCharacter("Al.LFl"));
        assertEquals(LightCharacteristicType.FLASH_ALTERNATING, S125EnumParser.parseLightCharacter("Al.Fl"));
        assertEquals(LightCharacteristicType.FLASH_ALTERNATING, S125EnumParser.parseLightCharacter("Al.FFl"));
        assertEquals(LightCharacteristicType.FLASH_ALTERNATING, S125EnumParser.parseLightCharacter("Al.Gr"));
        assertEquals(LightCharacteristicType.QUICK_FLASH_PLUS_LONG_FLASH, S125EnumParser.parseLightCharacter("Q+LFl"));
        assertEquals(LightCharacteristicType.VERY_QUICK_FLASH_PLUS_LONG_FLASH, S125EnumParser.parseLightCharacter("VQ+LFl"));
        assertEquals(LightCharacteristicType.ULTRA_QUICK_FLASH_PLUS_LONG_FLASH, S125EnumParser.parseLightCharacter("UQ+LFl"));
        assertEquals(LightCharacteristicType.ALTERNATING, S125EnumParser.parseLightCharacter("Al"));
        assertNull(S125EnumParser.parseLightCharacter("error"));
    }

    @Test
    public void testParseColourPattern() {
        assertEquals(ColourPatternType.HORIZONTAL_STRIPES, S125EnumParser.parseColourPattern("horizontal"));
        assertEquals(ColourPatternType.VERTICAL_STRIPES, S125EnumParser.parseColourPattern("vertical"));
        assertEquals(ColourPatternType.DIAGONAL_STRIPES, S125EnumParser.parseColourPattern("diagonal"));
        assertEquals(ColourPatternType.SQUARED, S125EnumParser.parseColourPattern("squared"));
        assertEquals(ColourPatternType.STRIPES_DIRECTION_UNKNOWN, S125EnumParser.parseColourPattern("stripes"));
        assertEquals(ColourPatternType.BORDER_STRIPE, S125EnumParser.parseColourPattern("border"));
        assertEquals(ColourPatternType.SINGLE_COLOUR, S125EnumParser.parseColourPattern("single"));
        assertNull(S125EnumParser.parseColourPattern("error"));
    }

    @Test
    public void testParseColour() {
        assertEquals(ColourType.WHITE, S125EnumParser.parseColour("white"));
        assertEquals(ColourType.BLACK, S125EnumParser.parseColour("black"));
        assertEquals(ColourType.RED, S125EnumParser.parseColour("red"));
        assertEquals(ColourType.GREEN, S125EnumParser.parseColour("green"));
        assertEquals(ColourType.BLUE, S125EnumParser.parseColour("blue"));
        assertEquals(ColourType.YELLOW, S125EnumParser.parseColour("yellow"));
        assertEquals(ColourType.GREY, S125EnumParser.parseColour("grey"));
        assertEquals(ColourType.BROWN, S125EnumParser.parseColour("brown"));
        assertEquals(ColourType.FLUORESCENT_WHITE, S125EnumParser.parseColour("fluorescent_white"));
        assertEquals(ColourType.FLUORESCENT_RED, S125EnumParser.parseColour("fluorescent_red"));
        assertEquals(ColourType.FLUORESCENT_GREEN, S125EnumParser.parseColour("fluorescent_green"));
        assertEquals(ColourType.FLUORESCENT_ORANGE, S125EnumParser.parseColour("fluorescent_orange"));
        assertNull(S125EnumParser.parseColour("error"));
    }

    @Test
    public void testParseFunction() {
        assertEquals(FunctionType.CUSTOMS_OFFICE, S125EnumParser.parseFunction("customs"));
        assertEquals(FunctionType.HOSPITAL, S125EnumParser.parseFunction("hospital"));
        assertEquals(FunctionType.POST_OFFICE, S125EnumParser.parseFunction("post_office"));
        assertEquals(FunctionType.HOTEL, S125EnumParser.parseFunction("hotel"));
        assertEquals(FunctionType.RAILWAY_STATION, S125EnumParser.parseFunction("railway_station"));
        assertEquals(FunctionType.POLICE_STATION, S125EnumParser.parseFunction("police_station"));
        assertEquals(FunctionType.WATER_POLICE_STATION, S125EnumParser.parseFunction("water-police_station"));
        assertEquals(FunctionType.BANK_OFFICE, S125EnumParser.parseFunction("bank"));
        assertEquals(FunctionType.POWER_STATION, S125EnumParser.parseFunction("power_station"));
        assertEquals(FunctionType.EDUCATIONAL_FACILITY, S125EnumParser.parseFunction("educational"));
        assertEquals(FunctionType.CHURCH, S125EnumParser.parseFunction("church"));
        assertEquals(FunctionType.TEMPLE, S125EnumParser.parseFunction("temple"));
        assertEquals(FunctionType.TELEVISION, S125EnumParser.parseFunction("television"));
        assertEquals(FunctionType.RADIO, S125EnumParser.parseFunction("radio"));
        assertEquals(FunctionType.RADAR, S125EnumParser.parseFunction("radar"));
        assertEquals(FunctionType.LIGHT_SUPPORT, S125EnumParser.parseFunction("light_support"));
        assertEquals(FunctionType.BUS_STATION, S125EnumParser.parseFunction("bus_station"));
        assertNull(S125EnumParser.parseFunction("error"));
    }

    @Test
    public void testParseFogSignalCategory() {
        assertEquals(CategoryOfFogSignalType.BELL, S125EnumParser.parseFogSignalCategory("bell"));
        assertEquals(CategoryOfFogSignalType.HORN, S125EnumParser.parseFogSignalCategory("horm"));
        assertEquals(CategoryOfFogSignalType.SIREN, S125EnumParser.parseFogSignalCategory("siren"));
        assertEquals(CategoryOfFogSignalType.WHISTLE, S125EnumParser.parseFogSignalCategory("whistle"));
        assertNull(S125EnumParser.parseFogSignalCategory("error"));
    }

    @Test
    public void testParseRadioStationCategory() {
        assertEquals(CategoryOfRadioStationType.AIS_BASE_STATION, S125EnumParser.parseRadioStationCategory("ais"));
        assertEquals(CategoryOfRadioStationType.DIFFERENTIAL_GNSS, S125EnumParser.parseRadioStationCategory("differential"));
        assertNull(S125EnumParser.parseRadioStationCategory("error"));
    }

    @Test
    public void testParseRadioTransponderBeaconCategory() {
        assertEquals(CategoryOfRadarTransponderBeaconType.RAMARK_RADAR_BEACON_TRANSMITTING_CONTINUOUSLY, S125EnumParser.parseRadioTransponderBeaconCategory("ramark"));
        assertEquals(CategoryOfRadarTransponderBeaconType.RACON_RADAR_TRANSPONDER_BEACON, S125EnumParser.parseRadioTransponderBeaconCategory("racon"));
        assertNull(S125EnumParser.parseRadioTransponderBeaconCategory("error"));
    }

    @Test
    public void testParseBuildingShape() {
        assertEquals(BuildingShapeType.CUBIC, S125EnumParser.parseBuildingShape("cubic"));
        assertEquals(BuildingShapeType.SPHERICAL, S125EnumParser.parseBuildingShape("spherical"));
        assertEquals(BuildingShapeType.HIGH_RISE_BUILDING, S125EnumParser.parseBuildingShape("high rise building"));
        assertEquals(BuildingShapeType.CYLINDRICAL, S125EnumParser.parseBuildingShape("cylindrical"));
        assertEquals(BuildingShapeType.PYRAMID, S125EnumParser.parseBuildingShape("pyramid"));
        assertNull(S125EnumParser.parseBuildingShape("error"));
    }

    @Test
    public void testParseSiloTankCategory() {
        assertEquals(CategoryOfSiloTankType.SILO_IN_GENERAL, S125EnumParser.parseSiloTankCategory("silo"));
        assertEquals(CategoryOfSiloTankType.TANK_IN_GENERAL, S125EnumParser.parseSiloTankCategory("tank"));
        assertNull(S125EnumParser.parseSiloTankCategory("error"));
    }

}