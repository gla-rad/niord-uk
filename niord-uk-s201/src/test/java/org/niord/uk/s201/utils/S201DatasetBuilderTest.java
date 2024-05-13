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
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.uk.s201.models.S201DatasetInfo;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.niord.core.aton.AtonTag.TAG_ATON_TYPE;

/**
 * A testing class for the S-201 Dataset Builder utility.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S201DatasetBuilderTest {

    // Test Variable
    private S201DatasetBuilder builder;
    private S201DatasetInfo info;
    private AtonNode atonNode;
    private AtonNode equipmentNode;

    /**
     * A common setup for all tests.
     */
    @Before
    public void setup() {
        // Initialise the builder
        this.builder = new S201DatasetBuilder();

        // Initialise the S-201 Dataset Information
        this.info = new S201DatasetInfo("test", "test", Collections.emptyList());

        // Initialise some AtoN information
        this.atonNode = new AtonNode();
        this.atonNode.setId(1);
        this.atonNode.setChangeset(0);
        this.atonNode.setLat(52.98);
        this.atonNode.setLon(1.28);
        this.atonNode.setGeometry(new GeometryFactory().createPoint(new Coordinate(52.98, 1.28)));
        this.atonNode.setTimestamp(Date.from(Instant.now()));
        this.atonNode.setTags(List.of(
                new AtonTag(TAG_ATON_TYPE, "beacon_cardinal"),
                new AtonTag("seamark:beacon_cardinal:category", "north"),
                new AtonTag("seamark:beacon_cardinal:shape", "stake"),
                new AtonTag("seamark:beacon_cardinal:colour", "red"),
                new AtonTag("seamark:beacon_cardinal:colour_pattern", "horizontal"),
                new AtonTag("s100:aidsToNavigation:generic_beacon:height", "3.0"),
                new AtonTag("seamark:beacon_cardinal:system", "iala-a"),
                new AtonTag("s100:aidsToNavigation:generic_beacon:nature_of_construction", "concreted"),
                new AtonTag("s100:aidsToNavigation:generic_beacon:radar_conspicuous", "conspicuous"),
                new AtonTag("s100:aidsToNavigation:generic_beacon:visually_conspicuous", "conspicuous"),
                new AtonTag("seamark:status", "permanent")
                ));

        // Add some equipment
        this.equipmentNode = new AtonNode();
        this.equipmentNode.setId(2);
        this.equipmentNode.setChangeset(0);
        this.equipmentNode.setLat(this.atonNode.getLat());
        this.equipmentNode.setLon(this.atonNode.getLon());
        this.equipmentNode.setGeometry(this.atonNode.getGeometry());
        this.equipmentNode.setTimestamp(Date.from(Instant.now()));
        this.equipmentNode.setTags(List.of(
                new AtonTag(TAG_ATON_TYPE, "power_source"),
                new AtonTag("s100:aidsToNavigation:power_source:category_of_power_source", "battery"),
                new AtonTag("s100:aidsToNavigation:power_source:manufacturer", "test"),
                new AtonTag("seamark:status", "permanent")
                ));
        this.atonNode.setChildren(Collections.singleton(this.equipmentNode));

    }

    /**
     * Test that we can successfully package the AtoN information of Niord
     * into an S-201 dataset.
     */
    @Test
    public void testS201PackageToDataset() {
        final Dataset dataset = this.builder.packageToDataset(this.info, Collections.singletonList(this.atonNode));

        // Make sure the dataset looks alright
        assertNotNull(dataset);
        assertEquals("test", dataset.getId());

        // Now look into the dataset identification
        assertNotNull(dataset.getDatasetIdentificationInformation());
        assertEquals(this.info.getEncodingSpecification(), dataset.getDatasetIdentificationInformation().getEncodingSpecification());
        assertEquals(this.info.getEncodingSpecificationEdition(), dataset.getDatasetIdentificationInformation().getEncodingSpecificationEdition());
        assertEquals(this.info.getProductionIdentifier(), dataset.getDatasetIdentificationInformation().getProductIdentifier());
        assertEquals(this.info.getProductionEdition(), dataset.getDatasetIdentificationInformation().getProductEdition());
        assertEquals(this.info.getFileIdentifier(), dataset.getDatasetIdentificationInformation().getDatasetFileIdentifier());
        assertEquals(this.info.getTitle(), dataset.getDatasetIdentificationInformation().getDatasetTitle());
        assertEquals(this.info.getLanguage(), dataset.getDatasetIdentificationInformation().getDatasetLanguage());
        assertEquals(this.info.getAbstractText(), dataset.getDatasetIdentificationInformation().getDatasetAbstract());

        // Finally look into the dataset members
        assertNotNull(dataset.getMembers());
        assertNotNull(dataset.getMembers().getBeaconCardinal());
        assertFalse(dataset.getMembers().getBeaconCardinal().isEmpty());
        assertEquals(1, dataset.getMembers().getBeaconCardinal().size());
        assertFalse(dataset.getMembers().getPowerSource().isEmpty());
        assertEquals(1, dataset.getMembers().getPowerSource().size());

        // We expect to have a single beacon cardinal
        final BeaconCardinal result = (BeaconCardinal) dataset.getMembers().getBeaconCardinal().getFirst();
        assertEquals("ID001", result.getId());
        assertEquals(CategoryOfCardinalMarkType.NORTH_CARDINAL_MARK, result.getCategoryOfCardinalMark());
        assertEquals(BeaconShapeType.STAKE_POLE_PERCH_POST, result.getBeaconShape());
        assertNotNull(result.getColours());
        assertFalse(result.getColours().isEmpty());
        assertEquals(ColourType.RED, result.getColours().getFirst());
        assertNotNull(result.getColourPatterns());
        assertFalse(result.getColourPatterns().isEmpty());
        assertEquals(ColourPatternType.HORIZONTAL_STRIPES, result.getColourPatterns().getFirst());
        assertEquals(Double.valueOf(3.0), result.getHeight());
        assertEquals(MarksNavigationalSystemOfType.IALA_A, result.getMarksNavigationalSystemOf());
        assertNotNull(result.getNatureOfConstructions());
        assertFalse(result.getNatureOfConstructions().isEmpty());
        assertEquals(NatureOfConstructionType.CONCRETED, result.getNatureOfConstructions().getFirst());
        assertEquals(RadarConspicuousType.RADAR_CONSPICUOUS, result.getRadarConspicuous());
        assertEquals(VisualProminenceType.VISUALLY_CONSPICUOUS, result.getVisualProminence());
        assertNotNull(result.getStatuses());
        assertFalse(result.getStatuses().isEmpty());
        assertEquals(StatusType.PERMANENT, result.getStatuses().getFirst());

        // And we expect a power source
        final PowerSource resultEquipment = (PowerSource) dataset.getMembers().getPowerSource().getFirst();
        assertEquals("ID002", resultEquipment.getId());
        assertEquals(CategoryOfPowerSourceType.BATTERY, resultEquipment.getCategoryOfPowerSource());
        assertEquals("test", resultEquipment.getManufacturer());
        assertEquals(StatusType.PERMANENT, resultEquipment.getStatus());
    }

}