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

package org.niord.uk.s201.utils;

import _int.iho.s201.gml.cs0._1.*;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * A testing call for the S-201 Enum Parser Utility.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S201EnumParserTest {

    /**
     * Test that we can split and parse multiple terms from a single line,
     * separated by semicolons.
     */
    @Test
    public void testSplitAndParse() {
        assertTrue(S201EnumParser.splitAndParse(null, Function.identity()).isEmpty());
        assertFalse(S201EnumParser.splitAndParse("", Function.identity()).isEmpty());
        assertFalse(S201EnumParser.splitAndParse("test", Function.identity()).isEmpty());

        // And actual test
        final Collection<Integer> result = S201EnumParser.splitAndParse("1;2;3", Integer::parseInt);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        // Check the contents
        final List<Integer> resultList = result.stream().toList();
        assertEquals(3, resultList.size());
        assertEquals(Integer.valueOf(1), resultList.get(0));
        assertEquals(Integer.valueOf(2), resultList.get(1));
        assertEquals(Integer.valueOf(3), resultList.get(2));
    }

    /**
     * Test that we can successfully parse the shackle type enum.
     */
    @Test
    public void testParseShackleType() {
        assertEquals(ShackleTypeType.FORELOCK_SHACKLES, S201EnumParser.parseShackleType("forelock_shackles"));
        assertEquals(ShackleTypeType.CLENCHING_SHACKLES, S201EnumParser.parseShackleType("clenching_shackles"));
        assertEquals(ShackleTypeType.BOLT_SHACKLES, S201EnumParser.parseShackleType("bolt_shackles"));
        assertEquals(ShackleTypeType.SCREW_PIN_SHACKLES, S201EnumParser.parseShackleType("screw_pin_shackles"));
        assertEquals(ShackleTypeType.KENTER_SHACKLE, S201EnumParser.parseShackleType("kenter_shackle"));
        assertEquals(ShackleTypeType.QUICK_RELEASE_LINK, S201EnumParser.parseShackleType("quick_release_link"));
        assertNull(S201EnumParser.parseShackleType("error"));
    }

    /**
     * Test that we can successfully parse the power of source enum.
     */
    @Test
    public void testParseCategoryOfPowerSource() {
        assertEquals(CategoryOfPowerSourceType.BATTERY, S201EnumParser.parseCategoryOfPowerSource("battery"));
        assertEquals(CategoryOfPowerSourceType.GENERATOR, S201EnumParser.parseCategoryOfPowerSource("generator"));
        assertEquals(CategoryOfPowerSourceType.SOLAR_PANEL, S201EnumParser.parseCategoryOfPowerSource("solar-panel"));
        assertEquals(CategoryOfPowerSourceType.ELECTRICAL_SERVICE, S201EnumParser.parseCategoryOfPowerSource("electrical-service"));
        assertNull(S201EnumParser.parseCategoryOfPowerSource("error"));
    }

    /**
     * Test that we can successfully parse the aid of availability category enum.
     */
    @Test
    public void testParseAidAvailabilityCategory() {
        assertEquals(AidAvailabilityCategoryType.CATEGORY_1, S201EnumParser.parseAidAvailabilityCategory("category_1"));
        assertEquals(AidAvailabilityCategoryType.CATEGORY_2, S201EnumParser.parseAidAvailabilityCategory("category_2"));
        assertEquals(AidAvailabilityCategoryType.CATEGORY_3, S201EnumParser.parseAidAvailabilityCategory("category_3"));
        assertNull(S201EnumParser.parseAidAvailabilityCategory("error"));
    }

    /**
     * Test that we can successfully parse the category of landmark enum.
     */
    @Test
    public void testParseCategoryOfLandmark() {
        assertEquals(CategoryOfLandmarkType.CHIMNEY, S201EnumParser.parseCategoryOfLandmark("chimney"));
        assertEquals(CategoryOfLandmarkType.MAST, S201EnumParser.parseCategoryOfLandmark("mast"));
        assertEquals(CategoryOfLandmarkType.MONUMENT, S201EnumParser.parseCategoryOfLandmark("monument"));
        assertEquals(CategoryOfLandmarkType.DOME, S201EnumParser.parseCategoryOfLandmark("dome"));
        assertEquals(CategoryOfLandmarkType.RADAR_SCANNER, S201EnumParser.parseCategoryOfLandmark("radar_scanner"));
        assertEquals(CategoryOfLandmarkType.TOWER, S201EnumParser.parseCategoryOfLandmark("tower"));
        assertEquals(CategoryOfLandmarkType.WINDMOTOR, S201EnumParser.parseCategoryOfLandmark("windmotor"));
        assertNull(S201EnumParser.parseCategoryOfLandmark("error"));
    }

    /**
     * Test that we can successfully parse the virtual aid to navigation type enum.
     */
    @Test
    public void testParseVirtualAisAidToNavigationType() {
        assertEquals(VirtualAISAidToNavigationTypeType.NORTH_CARDINAL, S201EnumParser.parseVirtualAisAidToNavigationType("north_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.SOUTH_CARDINAL, S201EnumParser.parseVirtualAisAidToNavigationType("south_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.EAST_CARDINAL, S201EnumParser.parseVirtualAisAidToNavigationType("east_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.WEST_CARDINAL, S201EnumParser.parseVirtualAisAidToNavigationType("west_cardinal"));
        assertEquals(VirtualAISAidToNavigationTypeType.PORT_LATERAL, S201EnumParser.parseVirtualAisAidToNavigationType("port_lateral"));
        assertEquals(VirtualAISAidToNavigationTypeType.STARBOARD_LATERAL, S201EnumParser.parseVirtualAisAidToNavigationType("starboard_lateral"));
        assertEquals(VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_PORT, S201EnumParser.parseVirtualAisAidToNavigationType("preferred_port"));
        assertEquals(VirtualAISAidToNavigationTypeType.PREFERRED_CHANNEL_TO_STARBOARD, S201EnumParser.parseVirtualAisAidToNavigationType("preferred_starboard"));
        assertEquals(VirtualAISAidToNavigationTypeType.ISOLATED_DANGER, S201EnumParser.parseVirtualAisAidToNavigationType("isolated_danger"));
        assertEquals(VirtualAISAidToNavigationTypeType.SAFE_WATER, S201EnumParser.parseVirtualAisAidToNavigationType("safe_water"));
        assertEquals(VirtualAISAidToNavigationTypeType.SPECIAL_PURPOSE, S201EnumParser.parseVirtualAisAidToNavigationType("special_purpose"));
        assertEquals(VirtualAISAidToNavigationTypeType.NEW_DANGER_MARKING, S201EnumParser.parseVirtualAisAidToNavigationType("wreck"));
        assertNull(S201EnumParser.parseVirtualAisAidToNavigationType("error"));
    }

    /**
     * Test that we can successfully parse the nature of construction enum.
     */
    @Test
    public void testParseNatureOfConstruction() {
        assertEquals(NatureOfConstructionType.MASONRY, S201EnumParser.parseNatureOfConstruction("masonry"));
        assertEquals(NatureOfConstructionType.HARD_SURFACE, S201EnumParser.parseNatureOfConstruction("hard-surfaced"));
        assertEquals(NatureOfConstructionType.CONCRETED, S201EnumParser.parseNatureOfConstruction("concreted"));
        assertEquals(NatureOfConstructionType.LOOSE_BOULDERS, S201EnumParser.parseNatureOfConstruction("loose_boulders"));
        assertEquals(NatureOfConstructionType.WOODEN, S201EnumParser.parseNatureOfConstruction("wooden"));
        assertEquals(NatureOfConstructionType.METAL, S201EnumParser.parseNatureOfConstruction("metal"));
        assertEquals(NatureOfConstructionType.PAINTED, S201EnumParser.parseNatureOfConstruction("painted"));
        assertEquals(NatureOfConstructionType.FIBERGLASS, S201EnumParser.parseNatureOfConstruction("grp"));
        assertEquals(NatureOfConstructionType.PLASTIC, S201EnumParser.parseNatureOfConstruction("plastic"));
        assertNull(S201EnumParser.parseNatureOfConstruction("error"));
    }

    /**
     * Test that we can successfully parse the status enum.
     */
    @Test
    public void testParseStatus() {
        assertEquals(StatusType.PERMANENT, S201EnumParser.parseStatus("permanent"));
        assertEquals(StatusType.NOT_IN_USE, S201EnumParser.parseStatus("not_in_use"));
        assertEquals(StatusType.PERIODIC_INTERMITTENT, S201EnumParser.parseStatus("periodic/intermittent"));
        assertEquals(StatusType.TEMPORARY, S201EnumParser.parseStatus("temporary"));
        assertEquals(StatusType.PRIVATE, S201EnumParser.parseStatus("private"));
        assertEquals(StatusType.PUBLIC, S201EnumParser.parseStatus("public"));
        assertEquals(StatusType.WATCHED, S201EnumParser.parseStatus("watched"));
        assertEquals(StatusType.UN_WATCHED, S201EnumParser.parseStatus("unwatched"));
        assertEquals(StatusType.CONFIRMED, S201EnumParser.parseStatus("confirmed"));
        assertEquals(StatusType.CANDIDATE, S201EnumParser.parseStatus("candidate"));
        assertEquals(StatusType.UNDER_MODIFICATION, S201EnumParser.parseStatus("under_modification"));
        assertEquals(StatusType.CANDIDATE_FOR_MODIFICATION, S201EnumParser.parseStatus("candidate_for_modification"));
        assertEquals(StatusType.UNDER_REMOVAL_DELETION, S201EnumParser.parseStatus("under_removal/deletion"));
        assertEquals(StatusType.REMOVED_DELETED, S201EnumParser.parseStatus("removed/deleted"));
        assertEquals(StatusType.EXPERIMENTAL, S201EnumParser.parseStatus("experimental"));
        assertEquals(StatusType.TEMPORARILY_DISCONTINUED, S201EnumParser.parseStatus("temporarily discontinued"));
        assertEquals(StatusType.TEMPORARILY_RELOCATED, S201EnumParser.parseStatus("temporarily relocated"));
        assertNull(S201EnumParser.parseStatus("error"));
    }

    /**
     * Test that we can successfully parse the radar conspicuous enum.
     */
    @Test
    public void testParseRadarConspicuous() {
        assertEquals(RadarConspicuousType.RADAR_CONSPICUOUS, S201EnumParser.parseRadarConspicuous("conspicuous"));
        assertEquals(RadarConspicuousType.NOT_RADAR_CONSPICUOUS, S201EnumParser.parseRadarConspicuous("not_conspicuous"));
        assertNull(S201EnumParser.parseRadarConspicuous("error"));
    }

    /**
     * Test that we can successfully parse the visual prominence enum.
     */
    @Test
    public void testParseVisualProminence() {
        assertEquals(VisualProminenceType.VISUALLY_CONSPICUOUS, S201EnumParser.parseVisualProminence("conspicuous"));
        assertEquals(VisualProminenceType.NOT_VISUALLY_CONSPICUOUS, S201EnumParser.parseVisualProminence("not_conspicuous"));
        assertNull(S201EnumParser.parseVisualProminence("error"));
    }

    /**
     * Test that we can successfully parse the marks navigational system of enum.
     */
    @Test
    public void testParseMarksNavigationalSystemOf() {
        assertEquals(MarksNavigationalSystemOfType.IALA_A, S201EnumParser.parseMarksNavigationalSystemOf("iala-a"));
        assertEquals(MarksNavigationalSystemOfType.IALA_B, S201EnumParser.parseMarksNavigationalSystemOf("iala-b"));
        assertEquals(MarksNavigationalSystemOfType.OTHER_SYSTEM, S201EnumParser.parseMarksNavigationalSystemOf("cevni"));
        assertEquals(MarksNavigationalSystemOfType.NO_SYSTEM, S201EnumParser.parseMarksNavigationalSystemOf("none"));
        assertNull(S201EnumParser.parseMarksNavigationalSystemOf("error"));
    }

    /**
     * Test that we can successfully parse the category of installation buoy enum.
     */
    @Test
    public void testParseCategoryOfInstallationBuoy() {
        assertEquals(CategoryOfInstallationBuoyType.CATENARY_ANCHOR_LEG_MOORING_CALM, S201EnumParser.parseCategoryOfInstallationBuoy("calm"));
        assertEquals(CategoryOfInstallationBuoyType.SINGLE_BUOY_MOORING_SBM_OR_SPM, S201EnumParser.parseCategoryOfInstallationBuoy("sbm"));
        assertNull(S201EnumParser.parseCategoryOfInstallationBuoy("error"));
    }

    /**
     * Test that we can successfully parse the category of special purpose enum.
     */
    @Test
    public void testParseCategoryOfSpecialPurposeMark() {
        assertEquals(CategoryOfSpecialPurposeMarkType.FIRING_DANGER_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("firing_danger_area"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TARGET_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("target"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MARKER_SHIP_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("marker_ship"));
        assertEquals(CategoryOfSpecialPurposeMarkType.DEGAUSSING_RANGE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("degaussing_range"));
        assertEquals(CategoryOfSpecialPurposeMarkType.BARGE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("barge"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CABLE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("cable"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SPOIL_GROUND_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("spoil_ground"));
        assertEquals(CategoryOfSpecialPurposeMarkType.OUTFALL_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("outfall"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ODAS_OCEAN_DATA_ACQUISITION_SYSTEM, S201EnumParser.parseCategoryOfSpecialPurposeMark("odas"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RECORDING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("recording"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SEAPLANE_ANCHORAGE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("seaplane_anchorage"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RECREATION_ZONE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("recreation_zone"));
        assertEquals(CategoryOfSpecialPurposeMarkType.PRIVATE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("private"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MOORING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("mooring"));
        assertEquals(CategoryOfSpecialPurposeMarkType.LANBY_LARGE_AUTOMATIC_NAVIGATIONAL_BUOY, S201EnumParser.parseCategoryOfSpecialPurposeMark("lanby"));
        assertEquals(CategoryOfSpecialPurposeMarkType.LEADING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("leading"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MEASURED_DISTANCE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("measured_distance"));
        assertEquals(CategoryOfSpecialPurposeMarkType.NOTICE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("notice"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TSS_MARK_TRAFFIC_SEPARATION_SCHEME, S201EnumParser.parseCategoryOfSpecialPurposeMark("tss"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ANCHORING_PROHIBITED_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("no_anchoring"));
        assertEquals(CategoryOfSpecialPurposeMarkType.BERTHING_PROHIBITED_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("no_berthing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.OVERTAKING_PROHIBITED_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("no_overtaking"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TWO_WAY_TRAFFIC_PROHIBITED_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("no_two-way_traffic"));
        assertEquals(CategoryOfSpecialPurposeMarkType.REDUCED_WAKE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("reduced_wake"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SPEED_LIMIT_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("speed_limit"));
        assertEquals(CategoryOfSpecialPurposeMarkType.STOP_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("stop"));
        assertEquals(CategoryOfSpecialPurposeMarkType.GENERAL_WARNING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("warning"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SOUND_SHIP_S_SIREN_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("sound_ship_siren"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RESTRICTED_VERTICAL_CLEARANCE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("restricted_vertical_clearance"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MAXIMUM_VESSEL_S_DRAUGHT_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("maximum_vessel_draught"));
        assertEquals(CategoryOfSpecialPurposeMarkType.RESTRICTED_HORIZONTAL_CLEARANCE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("restricted_horizontal_clearance"));
        assertEquals(CategoryOfSpecialPurposeMarkType.STRONG_CURRENT_WARNING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("strong_current"));
        assertEquals(CategoryOfSpecialPurposeMarkType.OVERHEAD_POWER_CABLE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("overhead_power_cable"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CHANNEL_EDGE_GRADIENT_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("channel_edge_gradient"));
        assertEquals(CategoryOfSpecialPurposeMarkType.TELEPHONE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("telephone"));
        assertEquals(CategoryOfSpecialPurposeMarkType.FERRY_CROSSING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("ferry_crossing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.PIPELINE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("pipeline"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ANCHORAGE_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("anchorage"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CLEARING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("clearing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CONTROL_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("control"));
        assertEquals(CategoryOfSpecialPurposeMarkType.DIVING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("diving"));
        assertEquals(CategoryOfSpecialPurposeMarkType.REFUGE_BEACON, S201EnumParser.parseCategoryOfSpecialPurposeMark("refuge_beacon"));
        assertEquals(CategoryOfSpecialPurposeMarkType.FOUL_GROUND_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("foul_ground"));
        assertEquals(CategoryOfSpecialPurposeMarkType.YACHTING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("yachting"));
        assertEquals(CategoryOfSpecialPurposeMarkType.HELIPORT_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("heliport"));
        assertEquals(CategoryOfSpecialPurposeMarkType.GNSS_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("gps"));
        assertEquals(CategoryOfSpecialPurposeMarkType.SEAPLANE_LANDING_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("seaplane_landing"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ENTRY_PROHIBITED_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("no_entry"));
        assertEquals(CategoryOfSpecialPurposeMarkType.WORK_IN_PROGRESS_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("work_in_progress"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MARK_WITH_UNKNOWN_PURPOSE, S201EnumParser.parseCategoryOfSpecialPurposeMark("unknown_purpose"));
        assertEquals(CategoryOfSpecialPurposeMarkType.WELLHEAD_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("wellhead"));
        assertEquals(CategoryOfSpecialPurposeMarkType.CHANNEL_SEPARATION_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("channel_separation"));
        assertEquals(CategoryOfSpecialPurposeMarkType.MARINE_FARM_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("marine_farm"));
        assertEquals(CategoryOfSpecialPurposeMarkType.ARTIFICIAL_REEF_MARK, S201EnumParser.parseCategoryOfSpecialPurposeMark("artificial_reef"));
        assertNull(S201EnumParser.parseCategoryOfSpecialPurposeMark("error"));
    }

    /**
     * Test that we can successfully parse the category of lateral mark enum.
     */
    @Test
    public void testParseCategoryOfLateralMark() {
        assertEquals(CategoryOfLateralMarkType.PORT_HAND_LATERAL_MARK, S201EnumParser.parseCategoryOfLateralMark("port"));
        assertEquals(CategoryOfLateralMarkType.STARBOARD_HAND_LATERAL_MARK, S201EnumParser.parseCategoryOfLateralMark("starboard"));
        assertEquals(CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_STARBOARD_LATERAL_MARK, S201EnumParser.parseCategoryOfLateralMark("preferred_channel_starboard"));
        assertEquals(CategoryOfLateralMarkType.PREFERRED_CHANNEL_TO_PORT_LATERAL_MARK, S201EnumParser.parseCategoryOfLateralMark("preferred_channel_port"));
        assertNull(S201EnumParser.parseCategoryOfLateralMark("error"));
    }

    /**
     * Test that we can successfully parse the category of cardinal enum.
     */
    @Test
    public void testParseCategoryOfCardinalMark() {
        assertEquals(CategoryOfCardinalMarkType.NORTH_CARDINAL_MARK, S201EnumParser.parseCategoryOfCardinalMark("north"));
        assertEquals(CategoryOfCardinalMarkType.EAST_CARDINAL_MARK, S201EnumParser.parseCategoryOfCardinalMark("east"));
        assertEquals(CategoryOfCardinalMarkType.SOUTH_CARDINAL_MARK, S201EnumParser.parseCategoryOfCardinalMark("south"));
        assertEquals(CategoryOfCardinalMarkType.WEST_CARDINAL_MARK, S201EnumParser.parseCategoryOfCardinalMark("west"));
        assertNull(S201EnumParser.parseCategoryOfCardinalMark("error"));
    }

    /**
     * Test that we can successfully parse the beacon shape enum.
     */
    @Test
    public void testParseBeaconShape() {
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S201EnumParser.parseBeaconShape("stake"));
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S201EnumParser.parseBeaconShape("pole"));
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S201EnumParser.parseBeaconShape("perch"));
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, S201EnumParser.parseBeaconShape("post"));
        assertEquals(BeaconShapeType.BEACON_TOWER, S201EnumParser.parseBeaconShape("tower"));
        assertEquals(BeaconShapeType.LATTICE_BEACON, S201EnumParser.parseBeaconShape("lattice"));
        assertEquals(BeaconShapeType.PILE_BEACON, S201EnumParser.parseBeaconShape("pile"));
        assertNull(S201EnumParser.parseBeaconShape("error"));
    }

    /**
     * Test that we can successfully parse the buoy shape enum.
     */
    @Test
    public void testParseBuoyShape() {
        assertEquals(BuoyShapeType.CONICAL_NUN_OGIVAL, S201EnumParser.parseBuoyShape("conical"));
        assertEquals(BuoyShapeType.CAN_CYLINDRICAL, S201EnumParser.parseBuoyShape("can"));
        assertEquals(BuoyShapeType.SPHERICAL, S201EnumParser.parseBuoyShape("spherical"));
        assertEquals(BuoyShapeType.SUPER_BUOY, S201EnumParser.parseBuoyShape("super-buoy"));
        assertEquals(BuoyShapeType.PILLAR, S201EnumParser.parseBuoyShape("pillar"));
        assertEquals(BuoyShapeType.SPAR_SPINDLE, S201EnumParser.parseBuoyShape("spar"));
        assertEquals(BuoyShapeType.BARREL_TUN, S201EnumParser.parseBuoyShape("barrel"));
        assertEquals(BuoyShapeType.ICE_BUOY, S201EnumParser.parseBuoyShape("ice-buoy"));
        assertNull(S201EnumParser.parseBuoyShape("error"));
    }

    /**
     * Test that we can successfully parse the light category enum.
     */
    @Test
    public void testParseLightCategory() {
        assertEquals(CategoryOfLightType.LEADING_LIGHT, S201EnumParser.parseLightCategory("leading"));
        assertEquals(CategoryOfLightType.AERO_LIGHT, S201EnumParser.parseLightCategory("aero"));
        assertEquals(CategoryOfLightType.AIR_OBSTRUCTION_LIGHT, S201EnumParser.parseLightCategory("air_obstruction"));
        assertEquals(CategoryOfLightType.FOG_DETECTOR_LIGHT, S201EnumParser.parseLightCategory("fog_detector"));
        assertEquals(CategoryOfLightType.FLOOD_LIGHT, S201EnumParser.parseLightCategory("floodlight"));
        assertEquals(CategoryOfLightType.STRIP_LIGHT, S201EnumParser.parseLightCategory("strip_light"));
        assertEquals(CategoryOfLightType.SUBSIDIARY_LIGHT, S201EnumParser.parseLightCategory("subsidiary"));
        assertEquals(CategoryOfLightType.SPOTLIGHT, S201EnumParser.parseLightCategory("spotlight"));
        assertEquals(CategoryOfLightType.FRONT, S201EnumParser.parseLightCategory("front"));
        assertEquals(CategoryOfLightType.REAR, S201EnumParser.parseLightCategory("rear"));
        assertEquals(CategoryOfLightType.LOWER, S201EnumParser.parseLightCategory("lower"));
        assertEquals(CategoryOfLightType.UPPER, S201EnumParser.parseLightCategory("upper"));
        assertEquals(CategoryOfLightType.EMERGENCY, S201EnumParser.parseLightCategory("emergency"));
        assertEquals(CategoryOfLightType.HORIZONTALLY_DISPOSED, S201EnumParser.parseLightCategory("horizontal"));
        assertEquals(CategoryOfLightType.VERTICALLY_DISPOSED, S201EnumParser.parseLightCategory("vertical"));
        assertEquals(CategoryOfLightType.BRIDGE_LIGHT, S201EnumParser.parseLightCategory("bridge_light"));
        assertNull(S201EnumParser.parseLightCategory("error"));
    }

    /**
     * Test that we can successfully parse the light character enum.
     */
    @Test
    public void testParseLightCharacter() {
        assertEquals(LightCharacteristicType.FIXED, S201EnumParser.parseLightCharacter("F"));
        assertEquals(LightCharacteristicType.FLASHING, S201EnumParser.parseLightCharacter("Fl"));
        assertEquals(LightCharacteristicType.LONG_FLASHING, S201EnumParser.parseLightCharacter("LFl"));
        assertEquals(LightCharacteristicType.QUICK_FLASHING, S201EnumParser.parseLightCharacter("Q"));
        assertEquals(LightCharacteristicType.VERY_QUICK_FLASHING, S201EnumParser.parseLightCharacter("VQ"));
        assertEquals(LightCharacteristicType.ULTRA_QUICK_FLASHING, S201EnumParser.parseLightCharacter("UQ"));
        assertEquals(LightCharacteristicType.ISOPHASED, S201EnumParser.parseLightCharacter("Iso"));
        assertEquals(LightCharacteristicType.OCCULTING, S201EnumParser.parseLightCharacter("Oc"));
        assertEquals(LightCharacteristicType.INTERRUPTED_QUICK_FLASHING, S201EnumParser.parseLightCharacter("IQ"));
        assertEquals(LightCharacteristicType.INTERRUPTED_VERY_QUICK_FLASHING, S201EnumParser.parseLightCharacter("IVQ"));
        assertEquals(LightCharacteristicType.INTERRUPTED_ULTRA_QUICK_FLASHING, S201EnumParser.parseLightCharacter("IUQ"));
        assertEquals(LightCharacteristicType.MORSE, S201EnumParser.parseLightCharacter("Mo"));
        assertEquals(LightCharacteristicType.FIXED_AND_FLASH, S201EnumParser.parseLightCharacter("FFl"));
        assertEquals(LightCharacteristicType.FLASH_AND_LONG_FLASH, S201EnumParser.parseLightCharacter("FlLFl"));
        assertEquals(LightCharacteristicType.OCCULTING_AND_FLASH, S201EnumParser.parseLightCharacter("OcFl"));
        assertEquals(LightCharacteristicType.FIXED_AND_LONG_FLASH, S201EnumParser.parseLightCharacter("FLFl"));
        assertEquals(LightCharacteristicType.OCCULTING_ALTERNATING, S201EnumParser.parseLightCharacter("Al.Oc"));
        assertEquals(LightCharacteristicType.LONG_FLASH_ALTERNATING, S201EnumParser.parseLightCharacter("Al.LFl"));
        assertEquals(LightCharacteristicType.FLASH_ALTERNATING, S201EnumParser.parseLightCharacter("Al.Fl"));
        assertEquals(LightCharacteristicType.FLASH_ALTERNATING, S201EnumParser.parseLightCharacter("Al.FFl"));
        assertEquals(LightCharacteristicType.FLASH_ALTERNATING, S201EnumParser.parseLightCharacter("Al.Gr"));
        assertEquals(LightCharacteristicType.QUICK_FLASH_PLUS_LONG_FLASH, S201EnumParser.parseLightCharacter("Q+LFl"));
        assertEquals(LightCharacteristicType.VERY_QUICK_FLASH_PLUS_LONG_FLASH, S201EnumParser.parseLightCharacter("VQ+LFl"));
        assertEquals(LightCharacteristicType.ULTRA_QUICK_FLASH_PLUS_LONG_FLASH, S201EnumParser.parseLightCharacter("UQ+LFl"));
        assertEquals(LightCharacteristicType.ALTERNATING, S201EnumParser.parseLightCharacter("Al"));
        assertNull(S201EnumParser.parseLightCharacter("error"));
    }

    /**
     * Test that we can successfully parse the colour pattern enum.
     */
    @Test
    public void testParseColourPattern() {
        assertEquals(ColourPatternType.HORIZONTAL_STRIPES, S201EnumParser.parseColourPattern("horizontal"));
        assertEquals(ColourPatternType.VERTICAL_STRIPES, S201EnumParser.parseColourPattern("vertical"));
        assertEquals(ColourPatternType.DIAGONAL_STRIPES, S201EnumParser.parseColourPattern("diagonal"));
        assertEquals(ColourPatternType.SQUARED, S201EnumParser.parseColourPattern("squared"));
        assertEquals(ColourPatternType.STRIPES_DIRECTION_UNKNOWN, S201EnumParser.parseColourPattern("stripes"));
        assertEquals(ColourPatternType.BORDER_STRIPE, S201EnumParser.parseColourPattern("border"));
        assertEquals(ColourPatternType.SINGLE_COLOUR, S201EnumParser.parseColourPattern("single"));
        assertNull(S201EnumParser.parseColourPattern("error"));
    }

    /**
     * Test that we can successfully parse the colour enum.
     */
    @Test
    public void testParseColour() {
        assertEquals(ColourType.WHITE, S201EnumParser.parseColour("white"));
        assertEquals(ColourType.BLACK, S201EnumParser.parseColour("black"));
        assertEquals(ColourType.RED, S201EnumParser.parseColour("red"));
        assertEquals(ColourType.GREEN, S201EnumParser.parseColour("green"));
        assertEquals(ColourType.BLUE, S201EnumParser.parseColour("blue"));
        assertEquals(ColourType.YELLOW, S201EnumParser.parseColour("yellow"));
        assertEquals(ColourType.GREY, S201EnumParser.parseColour("grey"));
        assertEquals(ColourType.BROWN, S201EnumParser.parseColour("brown"));
        assertEquals(ColourType.FLUORESCENT_WHITE, S201EnumParser.parseColour("fluorescent_white"));
        assertEquals(ColourType.FLUORESCENT_RED, S201EnumParser.parseColour("fluorescent_red"));
        assertEquals(ColourType.FLUORESCENT_GREEN, S201EnumParser.parseColour("fluorescent_green"));
        assertEquals(ColourType.FLUORESCENT_ORANGE, S201EnumParser.parseColour("fluorescent_orange"));
        assertNull(S201EnumParser.parseColour("error"));
    }

    /**
     * Test that we can successfully parse the function enum.
     */
    @Test
    public void testParseFunction() {
        assertEquals(FunctionType.CUSTOMS_OFFICE, S201EnumParser.parseFunction("customs"));
        assertEquals(FunctionType.HOSPITAL, S201EnumParser.parseFunction("hospital"));
        assertEquals(FunctionType.POST_OFFICE, S201EnumParser.parseFunction("post_office"));
        assertEquals(FunctionType.HOTEL, S201EnumParser.parseFunction("hotel"));
        assertEquals(FunctionType.RAILWAY_STATION, S201EnumParser.parseFunction("railway_station"));
        assertEquals(FunctionType.POLICE_STATION, S201EnumParser.parseFunction("police_station"));
        assertEquals(FunctionType.WATER_POLICE_STATION, S201EnumParser.parseFunction("water-police_station"));
        assertEquals(FunctionType.BANK_OFFICE, S201EnumParser.parseFunction("bank"));
        assertEquals(FunctionType.POWER_STATION, S201EnumParser.parseFunction("power_station"));
        assertEquals(FunctionType.EDUCATIONAL_FACILITY, S201EnumParser.parseFunction("educational"));
        assertEquals(FunctionType.CHURCH, S201EnumParser.parseFunction("church"));
        assertEquals(FunctionType.TEMPLE, S201EnumParser.parseFunction("temple"));
        assertEquals(FunctionType.TELEVISION, S201EnumParser.parseFunction("television"));
        assertEquals(FunctionType.RADIO, S201EnumParser.parseFunction("radio"));
        assertEquals(FunctionType.RADAR, S201EnumParser.parseFunction("radar"));
        assertEquals(FunctionType.LIGHT_SUPPORT, S201EnumParser.parseFunction("light_support"));
        assertEquals(FunctionType.BUS_STATION, S201EnumParser.parseFunction("bus_station"));
        assertNull(S201EnumParser.parseFunction("error"));
    }

    /**
     * Test that we can successfully parse the fog signal category enum.
     */
    @Test
    public void testParseFogSignalCategory() {
        assertEquals(CategoryOfFogSignalType.BELL, S201EnumParser.parseFogSignalCategory("bell"));
        assertEquals(CategoryOfFogSignalType.HORN, S201EnumParser.parseFogSignalCategory("horm"));
        assertEquals(CategoryOfFogSignalType.SIREN, S201EnumParser.parseFogSignalCategory("siren"));
        assertEquals(CategoryOfFogSignalType.WHISTLE, S201EnumParser.parseFogSignalCategory("whistle"));
        assertNull(S201EnumParser.parseFogSignalCategory("error"));
    }

    /**
     * Test that we can successfully parse the radio station enum.
     */
    @Test
    public void testParseRadioStationCategory() {
        assertEquals(CategoryOfRadioStationType.AIS_BASE_STATION, S201EnumParser.parseRadioStationCategory("ais"));
        assertEquals(CategoryOfRadioStationType.DIFFERENTIAL_GNSS, S201EnumParser.parseRadioStationCategory("differential"));
        assertNull(S201EnumParser.parseRadioStationCategory("error"));
    }

    /**
     * Test that we can successfully parse the radio transponder beacon category enum.
     */
    @Test
    public void testParseRadioTransponderBeaconCategory() {
        assertEquals(CategoryOfRadarTransponderBeaconType.RAMARK_RADAR_BEACON_TRANSMITTING_CONTINUOUSLY, S201EnumParser.parseRadioTransponderBeaconCategory("ramark"));
        assertEquals(CategoryOfRadarTransponderBeaconType.RACON_RADAR_TRANSPONDER_BEACON, S201EnumParser.parseRadioTransponderBeaconCategory("racon"));
        assertNull(S201EnumParser.parseRadioTransponderBeaconCategory("error"));
    }

    /**
     * Test that we can successfully parse the building shape enum.
     */
    @Test
    public void testParseBuildingShape() {
        assertEquals(BuildingShapeType.CUBIC, S201EnumParser.parseBuildingShape("cubic"));
        assertEquals(BuildingShapeType.SPHERICAL, S201EnumParser.parseBuildingShape("spherical"));
        assertEquals(BuildingShapeType.HIGH_RISE_BUILDING, S201EnumParser.parseBuildingShape("high rise building"));
        assertEquals(BuildingShapeType.CYLINDRICAL, S201EnumParser.parseBuildingShape("cylindrical"));
        assertEquals(BuildingShapeType.PYRAMID, S201EnumParser.parseBuildingShape("pyramid"));
        assertNull(S201EnumParser.parseBuildingShape("error"));
    }

    /**
     * Test that we can successfully parse the silo talk category enum.
     */
    @Test
    public void testParseSiloTankCategory() {
        assertEquals(CategoryOfSiloTankType.SILO_IN_GENERAL, S201EnumParser.parseSiloTankCategory("silo"));
        assertEquals(CategoryOfSiloTankType.TANK_IN_GENERAL, S201EnumParser.parseSiloTankCategory("tank"));
        assertNull(S201EnumParser.parseSiloTankCategory("error"));
    }
}