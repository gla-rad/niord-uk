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
import _int.iala_aism.s125.gml._0_0.S100TruncatedDate;
import _int.iho.s100.gml.base._5_0.*;
import _int.iho.s100.gml.base._5_0.CurveType;
import _int.iho.s100.gml.base._5_0.PointType;
import _int.iho.s100.gml.base._5_0.SurfaceType;
import _net.opengis.gml.profiles.*;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.s125.models.S125AtonTypes;
import org.niord.s125.models.S125DatasetInfo;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.niord.core.aton.AtonTag.TAG_ATON_TYPE;

/**
 * The S-125 Dataset Builder Class.
 *
 * This class is entrusted with the generation of the S-125 dataset based on a
 * list of provided AtoN nodes. These are originally generated through Niord
 * and follow the Openstreet Maps Seachart extension format.
 *
 * Note that the mapping is not always an exact match, but it seems like the
 * two standards and quick close so let's do the best we can.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S125DatasetBuilder {

    // Class Variables
    private AtomicInteger idIndex;
    private _int.iala_aism.s125.gml._0_0.ObjectFactory s125GMLFactory;
    private _net.opengis.gml.profiles.ObjectFactory opengisGMLFactory;

    /**
     * Class Constructor.
     */
    public S125DatasetBuilder() {
        this.idIndex = new AtomicInteger(1);
        this.s125GMLFactory = new _int.iala_aism.s125.gml._0_0.ObjectFactory();
        this.opengisGMLFactory = new _net.opengis.gml.profiles.ObjectFactory();
    }
    
    /**
     * This is the main   the provided list of AtoN nodess into an S125 dataset as
     * dictated by the NIPWG S-125 data product specification.
     *
     * @param datasetInfo   The dataset information
     * @param atonNodes     The list of S-125 AtoN nodes
     */
    public DataSet packageToDataset(S125DatasetInfo datasetInfo,
                                    List<AtonNode> atonNodes) {
        // Initialise the dataset
        DataSet s125Dataset = new DataSet();
        s125Dataset.setId(datasetInfo.getDatasetId());

        //====================================================================//
        //                       BOUNDED BY SECTION                           //
        //====================================================================//
        s125Dataset.setBoundedBy(this.generateBoundingShape(atonNodes));

        //====================================================================//
        //                  DATASET IDENTIFICATION SECTION                    //
        //====================================================================//
        DataSetIdentificationType dataSetIdentificationType = new DataSetIdentificationType();
        dataSetIdentificationType.setEncodingSpecification(datasetInfo.getEncodingSpecification());
        dataSetIdentificationType.setEncodingSpecificationEdition(datasetInfo.getEncodingSpecificationEdition());
        dataSetIdentificationType.setProductIdentifier(datasetInfo.getProductionIdentifier());
        dataSetIdentificationType.setProductEdition(datasetInfo.getProductionEdition());
        dataSetIdentificationType.setDatasetFileIdentifier(datasetInfo.getFileIdentifier());
        dataSetIdentificationType.setDatasetTitle(datasetInfo.getTitle());
        dataSetIdentificationType.setDatasetReferenceDate(LocalDate.now());
        dataSetIdentificationType.setDatasetLanguage(Locale.getDefault().getISO3Language());
        dataSetIdentificationType.setDatasetAbstract(datasetInfo.getAbstractText());
        s125Dataset.setDatasetIdentificationInformation(dataSetIdentificationType);

        //====================================================================//
        //                      DATASET MEMBERS SECTION                       //
        //====================================================================//
        Optional.ofNullable(atonNodes)
                .orElse(Collections.emptyList())
                .stream()
                .map(aton -> this.generateAidsToNavigation(datasetInfo, aton))
                .map(jaxb -> { MemberType m = new MemberType(); m.setAbstractFeature(jaxb); return m; })
                .forEach(s125Dataset.getImembersAndMembers()::add);

        // Return the dataset
        return s125Dataset;
    }

    /**
     * This is another entry method static function of the utility. It will
     * examine the provided AtoN Node from Niord and generate a standardised
     * S-125 format messages based on the specifications specified by the
     * IHO/IALA NIPWG.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The Niord AtoN node object
     * @return The generated S-125 data message
     */
    public JAXBElement<? extends S125AidsToNavigationType> generateAidsToNavigation(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        // First read the AtoN type information from the input
        S125AtonTypes atonType = S125AtonTypes.fromSeamarkType(atonNode.getTagValue(TAG_ATON_TYPE));
        // Now initialise the JAXB object factory to generate the member
        JAXBElement<? extends S125AidsToNavigationType> jaxbElement =  null;
        // Handle each possible type, cause a different object should be created
        switch(atonType) {
            case CARDINAL_BEACON:
                jaxbElement = this.s125GMLFactory.createS125BeaconCardinal(this.generateBeaconCardinal(datasetInfo, atonNode));
                break;
            case LATERAL_BEACON:
                jaxbElement = this.s125GMLFactory.createS125BeaconLateral(this.generateBeaconLateral(datasetInfo, atonNode));
                break;
            case ISOLATED_DANGER_BEACON:
                jaxbElement = this.s125GMLFactory.createS125BeaconIsolatedDanger(this.generateBeaconIsolatedDanger(datasetInfo, atonNode));
                break;
            case SAFE_WATER_BEACON:
                jaxbElement = this.s125GMLFactory.createS125BeaconSafeWater(this.generateBeaconSafeWater(datasetInfo, atonNode));
                break;
            case SPECIAL_PURPOSE_BEACON:
                jaxbElement = this.s125GMLFactory.createS125BeaconSpecialPurposeGeneral(this.generateBeaconSpecialPurpose(datasetInfo, atonNode));
                break;
            case CARDINAL_BUOY:
                jaxbElement = this.s125GMLFactory.createS125BuoyCardinal(this.generateBuoyCardinal(datasetInfo, atonNode));
                break;
            case LATERAL_BUOY:
                jaxbElement = this.s125GMLFactory.createS125BuoyLateral(this.generateBuoyLateral(datasetInfo, atonNode));
                break;
            case INSTALLATION_BUOY:
                jaxbElement = this.s125GMLFactory.createS125BuoyInstallation(this.generateBuoyInstallation(datasetInfo, atonNode));
                break;
            case ISOLATED_DANGER_BUOY:
                jaxbElement = this.s125GMLFactory.createS125BuoyIsolatedDanger(this.generateBuoyIsolatedDanger(datasetInfo, atonNode));
                break;
            case SAFE_WATER_BUOY:
                jaxbElement = this.s125GMLFactory.createS125BuoySafeWater(this.generateBuoySafeWater(datasetInfo, atonNode));
                break;
            case SPECIAL_PURPOSE_BUOY:
                jaxbElement = this.s125GMLFactory.createS125BuoySpecialPurposeGeneral(this.generateBuoySpecialPurpose(datasetInfo, atonNode));
                break;
            case LANDMARK:
                jaxbElement = this.s125GMLFactory.createS125Landmark(this.generateLandmark(datasetInfo, atonNode));
                break;
            case LIGHTHOUSE_MAJOR: case LIGHTHOUSE_MINOR:
                jaxbElement = this.s125GMLFactory.createS125Lighthouse(this.generateLighthouse(datasetInfo, atonNode));
                break;
            case LIGHT_VESSEL:
                jaxbElement = this.s125GMLFactory.createS125LightVessel(this.generateLightVessel(datasetInfo, atonNode));
                break;
            case PHYSICAL_AIS_ATON:
                jaxbElement = this.s125GMLFactory.createS125PhysicalAISAidToNavigation(this.generatePhysicalAISAtoN(datasetInfo, atonNode));
                break;
            case VIRTUAL_ATON:
                jaxbElement = this.s125GMLFactory.createS125VirtualAISAidToNavigation(this.generateVirtualAtoN(datasetInfo, atonNode));
                break;
        }
        // And return what was generated
        return jaxbElement;
    }

    /**
     * Generate the S-125 dataset member section for Beacon Cardinal AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BeaconCardinalType generateBeaconCardinal(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BeaconCardinalType member = new S125BeaconCardinalType();
        final String tagKeyPrefix = "seamark:beacon_cardinal:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBeaconShape(Optional.of(tagKeyPrefix+"shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfCardinalMark(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfCardinalMark)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBeaconType.Geometry.class::isInstance)
                .map(S125GenericBeaconType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Beacon Lateral AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BeaconLateralType generateBeaconLateral(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BeaconLateralType member = new S125BeaconLateralType();
        final String tagKeyPrefix = "seamark:beacon_lateral:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBeaconShape(Optional.of(tagKeyPrefix+"shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix + "colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfLateralMark(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfLateralMark)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBeaconType.Geometry.class::isInstance)
                .map(S125GenericBeaconType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return  member;
    }

    /**
     * Generate the S-125 dataset member section for Beacon Isolated Danger
     * AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BeaconIsolatedDangerType generateBeaconIsolatedDanger(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BeaconIsolatedDangerType member = new S125BeaconIsolatedDangerType();
        final String tagKeyPrefix = "seamark:beacon_isolated_danger:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBeaconShape(Optional.of(tagKeyPrefix+"shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBeaconType.Geometry.class::isInstance)
                .map(S125GenericBeaconType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Beacon Safe Water AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BeaconSafeWaterType generateBeaconSafeWater(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BeaconSafeWaterType member = new S125BeaconSafeWaterType();
        final String tagKeyPrefix = "seamark:beacon_safe_water:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBeaconShape(Optional.of("seamark:beacon_safe_water:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBeaconType.Geometry.class::isInstance)
                .map(S125GenericBeaconType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Beacon Special Purpose
     * AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BeaconSpecialPurposeGeneralType generateBeaconSpecialPurpose(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BeaconSpecialPurposeGeneralType member = new S125BeaconSpecialPurposeGeneralType();
        final String tagKeyPrefix = "seamark:beacon_special_purpose:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBeaconShape(Optional.of(tagKeyPrefix+"shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"isually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.getCategoryOfSpecialPurposeMarks().add(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfSpecialPurposeMark)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBeaconType.Geometry.class::isInstance)
                .map(S125GenericBeaconType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Buoy Cardinal AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BuoyCardinalType generateBuoyCardinal(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BuoyCardinalType member = new S125BuoyCardinalType();
        final String tagKeyPrefix = "seamark:buoy_cardinal:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBuoyShape(Optional.of("seamark:buoy_cardinal:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicious(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfCardinalMark(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfCardinalMark)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBuoyType.Geometry.class::isInstance)
                .map(S125GenericBuoyType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Buoy Lateral AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BuoyLateralType generateBuoyLateral(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BuoyLateralType member = new S125BuoyLateralType();
        final String tagKeyPrefix = "seamark:buoy_lateral:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBuoyShape(Optional.of("seamark:buoy_lateral:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicious(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfLateralMark(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfLateralMark)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBuoyType.Geometry.class::isInstance)
                .map(S125GenericBuoyType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Buoy Installation AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BuoyInstallationType generateBuoyInstallation(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BuoyInstallationType member = new S125BuoyInstallationType();
        final String tagKeyPrefix = "seamark:buoy_installation:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBuoyShape(Optional.of(tagKeyPrefix+"shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicious(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfInstallationBuoy(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfInstallationBuoy)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBuoyType.Geometry.class::isInstance)
                .map(S125GenericBuoyType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Buoy Isolated Dander AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BuoyIsolatedDangerType generateBuoyIsolatedDanger(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BuoyIsolatedDangerType member = new S125BuoyIsolatedDangerType();
        final String tagKeyPrefix = "seamark:buoy_isolated_danger:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBuoyShape(Optional.of("seamark:buoy_isolated_danger:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicious(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBuoyType.Geometry.class::isInstance)
                .map(S125GenericBuoyType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Buoy Safe Water AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BuoySafeWaterType generateBuoySafeWater(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BuoySafeWaterType member = new S125BuoySafeWaterType();
        final String tagKeyPrefix = "seamark:buoy_safe_water:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBuoyShape(Optional.of("seamark:buoy_safe_water:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicious(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBuoyType.Geometry.class::isInstance)
                .map(S125GenericBuoyType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Buoy Special Purpose AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125BuoySpecialPurposeGeneralType generateBuoySpecialPurpose(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125BuoySpecialPurposeGeneralType member = new S125BuoySpecialPurposeGeneralType();
        final String tagKeyPrefix = "seamark:buoy_special_purpose:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setBuoyShape(Optional.of("seamark:buoy_special_purpose:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicious(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.getCategoryOfSpecialPurposeMarks().add(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfSpecialPurposeMark)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125GenericBuoyType.Geometry.class::isInstance)
                .map(S125GenericBuoyType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Landmark AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125LandmarkType generateLandmark(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125LandmarkType member = new S125LandmarkType();
        final String tagKeyPrefix = "seamark:landmark";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:landmark:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.getCategoryOfLandmarks().addAll(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseCategoryOfLandmark))
                .orElse(Collections.emptyList()));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.getFunctions().addAll(Optional.of(tagKeyPrefix+"function")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseFunction))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125LandmarkType.Geometry.class::isInstance)
                .map(S125LandmarkType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Lighthouse AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125LighthouseType generateLighthouse(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125LighthouseType member = new S125LighthouseType();
        final String tagKeyPrefix = "seamark:light:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:lighthouse:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.getCategoryOfLandmarks().addAll(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseCategoryOfLandmark))
                .orElse(Collections.emptyList()));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.getFunctions().addAll(Optional.of(tagKeyPrefix+"function")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseFunction))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125LandmarkType.Geometry.class::isInstance)
                .map(S125LandmarkType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Light Vessel AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125LightVesselType generateLightVessel(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125LightVesselType member = new S125LightVesselType();
        final String tagKeyPrefix = "seamark:light_vessel:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:light_vessel:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.getNatureOfConstructions().addAll(Optional.of(s125TagKeyPrefix+"nature_or_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s125TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of(s125TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125LightVesselType.Geometry.class::isInstance)
                .map(S125LightVesselType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for physical AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125PhysicalAISAidToNavigationType generatePhysicalAISAtoN(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125PhysicalAISAidToNavigationType member = new S125PhysicalAISAidToNavigationType();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:ais_aid_to_navigation:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setEstimatedRangeOfTransmission(Optional.of(s125TagKeyPrefix+"estimated_range_of_transmission")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMMSICode(Optional.of(tagKeyPrefix+"mmsi")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setStatus(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseStatus)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125PhysicalAISAidToNavigationType.Geometry.class::isInstance)
                .map(S125PhysicalAISAidToNavigationType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Virtual AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125VirtualAISAidToNavigationType generateVirtualAtoN(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125VirtualAISAidToNavigationType member = new S125VirtualAISAidToNavigationType();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:virtual_ais_aid_to_navigation:";
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setEstimatedRangeOfTransmission(Optional.of(s125TagKeyPrefix+"estimated_range_of_transmission")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigInteger::new)
                .orElse(null));
        member.setMMSICode(Optional.of(tagKeyPrefix+"mmsi")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigInteger::new)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setStatus(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseStatus)
                .orElse(null));
        member.setVirtualAISAidToNavigationType(Optional.of("seamark:virtual_aton:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> v.replace(" ", "_"))
                .map(S125EnumParser::parseVirtualAisAidToNavigationType)
                .orElse(S125CategoryOfVirtualAISAidToNavigation.SPECIAL_PURPOSE));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125VirtualAISAidToNavigationType.Geometry.class::isInstance)
                .map(S125VirtualAISAidToNavigationType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 dataset member section for Light AtoNs.
     *
     * @param datasetInfo   The dataset information
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 dataset member section generated
     */
    protected S125LightType generateLight(S125DatasetInfo datasetInfo, AtonNode atonNode) {
        final S125LightType member = new S125LightType();
        final String tagKeyPrefix = atonNode.getTags()
                .stream()
                .filter(t -> t.getK().startsWith("seamark:light:"))
                .findFirst()
                .map(AtonTag::getK)
                .map(s -> s.substring(0, s.lastIndexOf(":")+1))
                .orElse("seamark:light");
        this.populateS125AidsToNavigationFields(member, datasetInfo, atonNode);
        member.setColour(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColour)
                .orElse(null));
        member.getCategoryOfLights().add(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseLightCategory)
                .orElse(null));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setExhibitionConditionOfLight(Optional.of(tagKeyPrefix+"exhibition")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> {try {return S125ExhibitionConditionOfLight.fromValue(v + " light");} catch (Exception ex) {return null;}})
                .orElse(null));
        member.setHeight(Optional.of(tagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setLightCharacteristic(Optional.of(tagKeyPrefix+"character")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseLightCharacter)
                .orElse(null));
        member.getLightVisibilities().add(Optional.of(tagKeyPrefix+"visibility")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> {try {return S125LightVisibility.fromValue(v);} catch (Exception ex) {return null;}})
                .orElse(null));
        member.setOrientation(Optional.of(tagKeyPrefix+"orientation")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(BigDecimal.ONE));
        member.setMultiplicityOfLights(Optional.of(tagKeyPrefix+"multiple")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigInteger::new)
                .orElse(BigInteger.ONE));
        member.setSignalPeriod(Optional.of(tagKeyPrefix+"group")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setSignalPeriod(Optional.of(tagKeyPrefix+"period")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setValueOfNominalRange(Optional.of(tagKeyPrefix+"range")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(S125LightType.Geometry.class::isInstance)
                .map(S125LightType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Many of the fields in the S125 dataset features are common, i.e. they
     * are shared between all structure and equipment types. Therefore this
     * helper function can be used to populated them easily for each type.
     *
     * @param member            The S-125 databaset member
     * @param datasetInfo       The general S-125 dataset information
     * @param atonNode          The AtoN node to populate the information from
     * @param <R> the type of class of the AtoN feature to be populated
     */
    public <R extends S125AidsToNavigationType> void populateS125AidsToNavigationFields(R member, S125DatasetInfo datasetInfo, AtonNode atonNode) {
        // First read the AtoN type information from the input
        final S125AtonTypes atonType = S125AtonTypes.fromSeamarkType(atonNode.getTagValue(TAG_ATON_TYPE));
        final String s125TagKeyPrefix = "s125:aidsToNavigation:";

        // Now populate the fields
        member.setId(this.generateId());
        member.setBoundedBy(this.generateBoundingShape(Collections.singletonList(atonNode)));
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(atonNode.getAtonUid()));
        member.setIdCode("aton.uk." + atonNode.getAtonUid().toLowerCase());
        member.setTextualDescription(String.format("%s %s", atonType.getDescription(), Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
        member.setTextualDescriptionInNationalLanguage(String.format("%s", atonType.getDescription(), Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
        member.getInformations().addAll(Optional.of(s125TagKeyPrefix+"information")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, String::valueOf))
                .orElse(Collections.emptyList()));
        member.getInformationInNationalLanguages().addAll(Optional.of(s125TagKeyPrefix+"information_in_national_language")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, String::valueOf))
                .orElse(Collections.emptyList()));
        member.setDateStart(Optional.of(s125TagKeyPrefix+"date_start")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setDateEnd(Optional.of(s125TagKeyPrefix+"date_end")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setPeriodStart(Optional.of(s125TagKeyPrefix+"period_start")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setPeriodEnd(Optional.of(s125TagKeyPrefix+"period_end" )
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setScaleMinimum(Optional.of(s125TagKeyPrefix+"scale_minimum")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigInteger::new)
                .orElse(BigInteger.ONE));
    }

    /**
     * Translate the ISO Date-Time string into an S100-compatible Truncated Date
     * object.
     *
     * @param isoDateTimeString The ISO Date-Time String
     * @return the S100 Truncated Date object
     */
    private S100TruncatedDate getS100TruncatedDate(String isoDateTimeString) {
        S100TruncatedDate s100TruncatedDate = new S100TruncatedDate();
        try {
            Optional.ofNullable(isoDateTimeString)
                    .map(s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .map(LocalDateTime::toLocalDate)
                    .ifPresent(s100TruncatedDate::setDate);
        } catch (NullPointerException | DateTimeParseException ex) {
            // Ok, so we got an error..., we return an empty date
            return null;
        }
        return s100TruncatedDate;
    }

    /**
     * Populates and return an S-125 surface property based on the provided
     * surface geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    private SurfaceProperty generateSurfaceProperty(Collection<Double> coords) {
        // Generate the elements
        SurfaceProperty surfaceProperty = new SurfaceProperty();
        SurfaceType surfaceType = new SurfaceType();
        Patches patches = new Patches();
        PolygonPatchType polygonPatchType = new PolygonPatchType();
        AbstractRingPropertyType abstractRingPropertyType = new AbstractRingPropertyType();
        LinearRingType linearRingType = new LinearRingType();
        PosList posList = new PosList();

        // Populate with the geometry data
        posList.setValue(coords.toArray(new Double[0]));

        // Populate the elements
        linearRingType.setPosList(posList);
        abstractRingPropertyType.setAbstractRing(this.opengisGMLFactory.createLinearRing(linearRingType));
        polygonPatchType.setExterior(abstractRingPropertyType);
        patches.getAbstractSurfacePatches().add(this.opengisGMLFactory.createPolygonPatch(polygonPatchType));
        surfaceType.setPatches(patches);
        surfaceType.setId(this.generateId());
        surfaceProperty.setSurface(surfaceType);

        // And return the output
        return surfaceProperty;
    }

    /**
     * Populates and return an S-125 curve property based on the provided line
     * segment geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    private CurveProperty generateCurveProperty(Collection<Double> coords) {
        // Generate the elements
        CurveProperty curveProperty = new CurveProperty();
        CurveType curveType = new CurveType();
        Segments segments = new Segments();
        LineStringSegmentType lineStringSegmentType = new LineStringSegmentType();
        PosList posList = new PosList();
        
        // Populate with the geometry data
        posList.setValue(coords.toArray(new Double[0]));
        
        // Populate the elements
        lineStringSegmentType.setPosList(posList);
        segments.getAbstractCurveSegments().add(this.opengisGMLFactory.createLineStringSegment(lineStringSegmentType));
        curveType.setSegments(segments);
        curveType.setId(this.generateId());
        curveProperty.setCurve(curveType);

        // And return the output
        return curveProperty;
    }

    /**
     * Populates and return an S-125 point property based on the provided point
     * geometry coordinates.
     *
     * @param coords    The coordinates of the element to be generated
     * @return The populated point property
     */
    protected PointProperty generatePointProperty(Collection<Double> coords) {
        // Generate the elements
        PointProperty pointProperty = new PointProperty();
        PointType pointType = new PointType();
        Pos pos = new Pos();

        // Populate with the geometry data
        pos.setValue(coords.toArray(new Double[0]));

        // Populate the elements
        pointType.setPos(pos);
        pointType.setId(this.generateId());
        pointProperty.setPoint(pointType);

        // And return the output
        return pointProperty;
    }

    /**
     * For easy generation of the bounding shapes for the dataset or individual
     * features, we are using this function.
     *
     * @param atonNodes     The AtoN nodes to generate the bounding shape from
     * @return the bounding shape
     */
    protected BoundingShapeType generateBoundingShape(Collection<AtonNode> atonNodes) {
        // Calculate the bounding by envelope
        final Envelope envelope = new Envelope();
        atonNodes.stream()
                .map(AtonNode::getGeometry)
                .forEach(g -> this.enclosingEnvelopFromGeometry(envelope, g));

        Pos lowerCorner = new Pos();
        lowerCorner.setValue(new Double[]{envelope.getMinX(), envelope.getMaxY()});
        Pos upperCorner = new Pos();
        upperCorner.setValue(new Double[]{envelope.getMaxX(), envelope.getMaxY()});

        // And create the bounding by envelope
        BoundingShapeType boundingShapeType = new BoundingShapeType();
        EnvelopeType envelopeType = new EnvelopeType();
        envelopeType.setSrsName("EPSG:4326");
        envelopeType.setLowerCorner(lowerCorner);
        envelopeType.setUpperCorner(upperCorner);
        boundingShapeType.setEnvelope(envelopeType);

        // Finally, return the result
        return boundingShapeType;
    }

    /**
     * Adds the enclosing geometry boundaries to the provided envelop.
     *
     * @param envelope      The envelope to be updated
     * @param geometry      The geometry to update the envelope boundaries with
     * @return the updates envelope
     */
    protected Envelope enclosingEnvelopFromGeometry(Envelope envelope, Geometry geometry) {
        final Geometry enclosingGeometry = geometry.getEnvelope();
        final Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
        for (Coordinate c : enclosingCoordinates) {
            envelope.expandToInclude(c);
        }
        return envelope;
    }

    /**
     * A helper function that is used to generate a homogenous set of IDs for
     * the S-125 dataset.
     *
     * @return the generated ID string
     */
    protected String generateId() {
        return String.format("ID%03d", this.idIndex.getAndIncrement());
    }

}
