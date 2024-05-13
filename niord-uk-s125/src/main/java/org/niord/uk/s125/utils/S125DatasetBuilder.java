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

import _int.iho.s125.s100.gml.base._5_0.CurveType;
import _int.iho.s125.s100.gml.base._5_0.PointType;
import _int.iho.s125.s100.gml.base._5_0.SurfaceType;
import _int.iho.s125.s100.gml.base._5_0.*;
import _int.iho.s125.s100.gml.base._5_0.impl.CurveTypeImpl;
import _int.iho.s125.s100.gml.base._5_0.impl.PointTypeImpl;
import _int.iho.s125.s100.gml.base._5_0.impl.SurfaceTypeImpl;
import _int.iho.s125.s100.gml.base._5_0.impl.*;
import _int.iho.s125.s100.gml.profiles._5_0.*;
import _int.iho.s125.s100.gml.profiles._5_0.impl.*;
import _int.iho.s125.gml.cs0._1.S100TruncatedDate;
import _int.iho.s125.gml.cs0._1.*;
import _int.iho.s125.gml.cs0._1.impl.*;
import _int.iho.s125.gml.cs0._1.impl.S100TruncatedDateImpl;
import org.apache.commons.lang3.StringUtils;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.niord.core.aton.AtonLink;
import org.niord.core.aton.AtonLinkType;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.uk.s125.models.S125AtonTypes;
import org.niord.uk.s125.models.S125DatasetInfo;

import java.lang.Boolean;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.niord.core.aton.AtonTag.TAG_ATON_TYPE;

/**
 * The S-125 Dataset Builder Class.
 *
 * This class is entrusted with the generation of the S-125 Dataset based on a
 * list of provided AtoN nodes. These are originally generated through Niord
 * and follow the Openstreet Maps Seachart extension format.
 *
 * Note that the mapping is not always an exact match, but it seems like the
 * two standards and quick close so let's do the best we can.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S125DatasetBuilder {

    /*
     * The standard parent reference archole.
     */
    protected static final String PARENT_REF_ARCHOLE = "urn:IALA:S125:roles:parent";

    /*
     * The standard child reference archole.
     */
    protected static final String CHILD_REF_ARCHOLE = "urn:IALA:S125:roles:child";

    /*
    /*
     * The standard aggregation reference archole.
     */
    protected static final String AGGREGATION_REF_ARCHOLE = "urn:IALA:S125:roles:aggregation";

    /*
     * The standard association reference archole.
     */
    protected static final String ASSOCIATION_REF_ARCHOLE = "urn:IALA:S125:roles:association";

    /*
     * The standard AtoN Status reference archole.
     */
    protected static final String ATON_STATUS_REF_ARCHOLE = "urn:IALA:S125:roles:atonStatus";

    // Class Variables
    private String idFormat;
    private AtomicInteger idIndex;
    private Map<Integer, String> idMap;
    private HashSet<Integer> linksSet;
    private _int.iho.s125.s100.gml.profiles._5_0.ObjectFactory opengisGMLFactory;

    /**
     * Class Constructor.
     */
    public S125DatasetBuilder() {
        this.idFormat = String.format("ID%%0%dd", 3);
        this.idIndex = new AtomicInteger(1);
        this.idMap = new HashMap<>();
        this.linksSet = new HashSet<>();
        this.opengisGMLFactory = new _int.iho.s125.s100.gml.profiles._5_0.ObjectFactory();
    }
    
    /**
     * This is the main function, which translates the provided list of AtoN
     * nodes into an S-125 Dataset as dictated by the NIPWG S-125 data
     * product specification.
     *
     * @param DatasetInfo   The Dataset information
     * @param atonNodes     The list of S-125 AtoN nodes
     */
    public Dataset packageToDataset(S125DatasetInfo DatasetInfo,
                                    List<AtonNode> atonNodes) {
        // Update the ID format based on the number of inputs. The general
        // guideline is that it should look like "ID001" with the number of
        // digits being enough to enumerate all the AtoN node entries available.
        this.idFormat = String.format("ID%%0%dd", Math.max(1, String.format("%d", atonNodes.size()).length() + 2));

        // Initialise the Dataset
        Dataset s125Dataset = new DatasetImpl();
        s125Dataset.setId(DatasetInfo.getDatasetId());

        //====================================================================//
        //                       BOUNDED BY SECTION                           //
        //====================================================================//
        s125Dataset.setBoundedBy(this.generateBoundingShape(atonNodes));

        //====================================================================//
        //                  Dataset IDENTIFICATION SECTION                    //
        //====================================================================//
        DataSetIdentificationType datasetIdentificationType = new DataSetIdentificationTypeImpl();
        datasetIdentificationType.setEncodingSpecification(DatasetInfo.getEncodingSpecification());
        datasetIdentificationType.setEncodingSpecificationEdition(DatasetInfo.getEncodingSpecificationEdition());
        datasetIdentificationType.setProductIdentifier(DatasetInfo.getProductionIdentifier());
        datasetIdentificationType.setProductEdition(DatasetInfo.getProductionEdition());
        datasetIdentificationType.setDatasetFileIdentifier(DatasetInfo.getFileIdentifier());
        datasetIdentificationType.setDatasetTitle(DatasetInfo.getTitle());
        datasetIdentificationType.setDatasetReferenceDate(LocalDate.now());
        datasetIdentificationType.setDatasetLanguage(DatasetInfo.getLanguage());
        datasetIdentificationType.setDatasetAbstract(DatasetInfo.getAbstractText());
        s125Dataset.setDatasetIdentificationInformation(datasetIdentificationType);

        //====================================================================//
        //                      Dataset MEMBERS SECTION                       //
        //====================================================================//
        S125Utils.addDatasetMembers(s125Dataset, Optional.ofNullable(atonNodes)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(aton ->
                    Stream.of(
                            Stream.of(this.generateAidsToNavigation(aton)),
                            aton.getChildren().stream().map(this::generateAidsToNavigation),
                            this.generateAidsToNavigationLinks(aton).stream()
                    ).flatMap(i -> i)
                )
                .toList());

        // Return the Dataset
        return s125Dataset;
    }

    /**
     * This is another entry method static function of the utility. It will
     * examine the provided AtoN Node from Niord and generate a standardised
     * S-125 format messages based on the specifications specified by the
     * IHO/IALA NIPWG.
     *
     * @param atonNode      The Niord AtoN node object
     * @return The generated S-125 data message
     */
    public AbstractGMLType generateAidsToNavigation(AtonNode atonNode) {
        // First read the AtoN type information from the input
        S125AtonTypes atonType = S125AtonTypes.fromSeamarkType(atonNode.getTagValue(TAG_ATON_TYPE));
        // Fix for AIS stations
        if(S125AtonTypes.RADIO_STATION == atonType && "ais".equals(atonNode.getTagValue("seamark:radio_station:category"))) {
            atonType = S125AtonTypes.PHYSICAL_AIS_ATON;
        }
        // Now initialise the JAXB object factory to generate the member
        return switch (atonType) {
            //=========================//
            //    STRUCTURE OBJECTS    //
            //=========================//
            case CARDINAL_BEACON ->
                    this.generateBeaconCardinal(atonNode);
            case LATERAL_BEACON ->
                    this.generateBeaconLateral(atonNode);
            case ISOLATED_DANGER_BEACON ->
                    this.generateBeaconIsolatedDanger(atonNode);
            case SAFE_WATER_BEACON ->
                    this.generateBeaconSafeWater(atonNode);
            case SPECIAL_PURPOSE_BEACON ->
                    this.generateBeaconSpecialPurpose(atonNode);
            case CARDINAL_BUOY ->
                    this.generateBuoyCardinal(atonNode);
            case LATERAL_BUOY ->
                    this.generateBuoyLateral(atonNode);
            case INSTALLATION_BUOY ->
                    this.generateBuoyInstallation(atonNode);
            case ISOLATED_DANGER_BUOY ->
                    this.generateBuoyIsolatedDanger(atonNode);
            case SAFE_WATER_BUOY ->
                    this.generateBuoySafeWater(atonNode);
            case SPECIAL_PURPOSE_BUOY ->
                    this.generateBuoySpecialPurpose(atonNode);
            case LANDMARK ->
                    this.generateLandmark(atonNode);
            case LIGHTHOUSE ->
                    this.generateLighthouse(atonNode);
            case LIGHT_VESSEL ->
                    this.generateLightVessel(atonNode);
            case VIRTUAL_ATON ->
                    this.generateVirtualAtoN(atonNode);
            //=========================//
            //    EQUIPMENT OBJECTS    //
            //=========================//
            case DAYMARK ->
                    this.generateDaymark(atonNode);
            case FOG_SIGNAL ->
                    this.generateFogSignal(atonNode);
            case LIGHT ->
                    this.generateLight(atonNode);
            case RADAR_REFLECTOR ->
                    this.generateRadarReflector(atonNode);
            case RETRO_REFLECTOR ->
                    this.generateRetroReflector(atonNode);
            case SILOS_AND_TANKS ->
                    this.generateSiloTank(atonNode);
            case TOPMARK ->
                    this.generateTopmark(atonNode);
            case RADIO_STATION ->
                    this.generateRadioStation(atonNode);
            case RADAR_TRANSPONDER ->
                    this.generateRadarTransponderBeacon(atonNode);
            case PHYSICAL_AIS_ATON ->
                    this.generatePhysicalAISAtoN(atonNode);
            default -> null;
        };
    }

    /**
     * This is another entry method static function of the utility. It will
     * examine the provided AtoN Node links from Niord and generate a standardised
     * list of S-125 aggregation or association entries.
     *
     * @param atonNode      The AtoN node to generate the aggregation/association links for
     * @return the generated collection of aggregation/association links
     */
    public Collection<AbstractGMLType> generateAidsToNavigationLinks(AtonNode atonNode) {
        // Sanity Check
        if(atonNode == null || atonNode.getLinks() == null || atonNode.getLinks().isEmpty()) {
            return Collections.emptyList();
        }

        // Create a new link collection
        final List<AbstractGMLType> linkCollection = new ArrayList<>();

        // Add the aggregation links
        atonNode.getLinks().stream()
                .filter(link -> link.getLinkCategory().getAtonLinkType() == AtonLinkType.AGGREGATION)
                .filter(not(link -> this.linksSet.contains(link.getId())))
                .map(this::generateAggregation)
                .filter(Objects::nonNull)
                .forEach(linkCollection::add);

        // Add the association links
        atonNode.getLinks().stream()
                .filter(link -> link.getLinkCategory().getAtonLinkType() == AtonLinkType.ASSOCIATION)
                .filter(not(link -> this.linksSet.contains(link.getId())))
                .map(this::generateAssociation)
                .filter(Objects::nonNull)
                .forEach(linkCollection::add);

        // Add the link IDs to the link-set so that they don't get duplicated
        this.linksSet.addAll(atonNode.getLinks()
                .stream()
                .map(AtonLink::getId)
                .collect(Collectors.toSet()));

        // Return the generated list
        return linkCollection;
    }

    /**
     * Generate the S-125 Dataset member section for Beacon Cardinal AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BeaconCardinal generateBeaconCardinal(AtonNode atonNode) {
        final BeaconCardinal member = new BeaconCardinalImpl();
        final String tagKeyPrefix = "seamark:beacon_cardinal:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(BeaconCardinalImpl.Geometry.class::isInstance)
                .map(BeaconCardinalImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Beacon Lateral AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BeaconLateral generateBeaconLateral(AtonNode atonNode) {
        final BeaconLateral member = new BeaconLateralImpl();
        final String tagKeyPrefix = "seamark:beacon_lateral:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(BeaconLateralImpl.Geometry.class::isInstance)
                .map(BeaconLateralImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return  member;
    }

    /**
     * Generate the S-125 Dataset member section for Beacon Isolated Danger
     * AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BeaconIsolatedDanger generateBeaconIsolatedDanger(AtonNode atonNode) {
        final BeaconIsolatedDanger member = new BeaconIsolatedDangerImpl();
        final String tagKeyPrefix = "seamark:beacon_isolated_danger:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(BeaconIsolatedDangerImpl.Geometry.class::isInstance)
                .map(BeaconIsolatedDangerImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Beacon Safe Water AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BeaconSafeWater generateBeaconSafeWater(AtonNode atonNode) {
        final BeaconSafeWater member = new BeaconSafeWaterImpl();
        final String tagKeyPrefix = "seamark:beacon_safe_water:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(BeaconSafeWaterImpl.Geometry.class::isInstance)
                .map(BeaconSafeWaterImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Beacon Special Purpose
     * AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BeaconSpecialPurposeGeneral generateBeaconSpecialPurpose(AtonNode atonNode) {
        final BeaconSpecialPurposeGeneral member = new BeaconSpecialPurposeGeneralImpl();
        final String tagKeyPrefix = "seamark:beacon_special_purpose:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_beacon:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"isually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(BeaconSpecialPurposeGeneralImpl.Geometry.class::isInstance)
                .map(BeaconSpecialPurposeGeneralImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Buoy Cardinal AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BuoyCardinal generateBuoyCardinal(AtonNode atonNode) {
        final BuoyCardinal member = new BuoyCardinalImpl();
        final String tagKeyPrefix = "seamark:buoy_cardinal:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
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
                .filter(BuoyCardinalImpl.Geometry.class::isInstance)
                .map(BuoyCardinalImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Buoy Lateral AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BuoyLateral generateBuoyLateral(AtonNode atonNode) {
        final BuoyLateral member = new BuoyLateralImpl();
        final String tagKeyPrefix = "seamark:buoy_lateral:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
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
                .filter(BuoyLateralImpl.Geometry.class::isInstance)
                .map(BuoyLateralImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Buoy Installation AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BuoyInstallation generateBuoyInstallation(AtonNode atonNode) {
        final BuoyInstallation member = new BuoyInstallationImpl();
        final String tagKeyPrefix = "seamark:buoy_installation:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
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
                .filter(BuoyInstallationImpl.Geometry.class::isInstance)
                .map(BuoyInstallationImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Buoy Isolated Dander AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BuoyIsolatedDanger generateBuoyIsolatedDanger(AtonNode atonNode) {
        final BuoyIsolatedDanger member = new BuoyIsolatedDangerImpl();
        final String tagKeyPrefix = "seamark:buoy_isolated_danger:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
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
                .filter(BuoyIsolatedDangerImpl.Geometry.class::isInstance)
                .map(BuoyIsolatedDangerImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Buoy Safe Water AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BuoySafeWater generateBuoySafeWater(AtonNode atonNode) {
        final BuoySafeWater member = new BuoySafeWaterImpl();
        final String tagKeyPrefix = "seamark:buoy_safe_water:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
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
                .filter(BuoySafeWaterImpl.Geometry.class::isInstance)
                .map(BuoySafeWaterImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Buoy Special Purpose AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected BuoySpecialPurposeGeneral generateBuoySpecialPurpose(AtonNode atonNode) {
        final BuoySpecialPurposeGeneral member = new BuoySpecialPurposeGeneralImpl();
        final String tagKeyPrefix = "seamark:buoy_special_purpose:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:generic_buoy:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseNatureOfConstruction)
                .orElse(null));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
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
                .filter(BuoySpecialPurposeGeneralImpl.Geometry.class::isInstance)
                .map(BuoySpecialPurposeGeneralImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Landmark AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected LandmarkType generateLandmark(AtonNode atonNode) {
        final LandmarkType member = new LandmarkTypeImpl();
        final String tagKeyPrefix = "seamark:landmark:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:landmark:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setColourPattern(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColourPattern)
                .orElse(null));
        member.getFunctions().addAll(Optional.of(tagKeyPrefix+"function")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseFunction))
                .orElse(Collections.emptyList()));
        member.getHeights().add(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(tagKeyPrefix+"construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(tagKeyPrefix+"conspicuity")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(LandmarkType.Geometry.class::isInstance)
                .map(LandmarkType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Lighthouse AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected Lighthouse generateLighthouse(AtonNode atonNode) {
        final Lighthouse member = new LighthouseImpl();
        final String tagKeyPrefix = "seamark:landmark:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:lighthouse:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.setColourPattern(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColourPattern)
                .orElse(null));
        member.getFunctions().addAll(Optional.of(tagKeyPrefix+"function")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseFunction))
                .orElse(Collections.emptyList()));
        member.getHeights().add(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(tagKeyPrefix+"construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(tagKeyPrefix+"conspicuity")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(LighthouseImpl.Geometry.class::isInstance)
                .map(LighthouseImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Light Vessel AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected LightVessel generateLightVessel(AtonNode atonNode) {
        final LightVessel member = new LightVesselImpl();
        final String tagKeyPrefix = "seamark:light_vessel:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:light_vessel:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.setColourPattern(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColourPattern)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(s100TagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
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
                .filter(LightVesselImpl.Geometry.class::isInstance)
                .map(LightVesselImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Virtual AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected VirtualAISAidToNavigation generateVirtualAtoN(AtonNode atonNode) {
        final VirtualAISAidToNavigation member = new VirtualAISAidToNavigationImpl();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:virtual_ais_aid_to_navigation:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setEstimatedRangeOfTransmission(Optional.of(s100TagKeyPrefix+"estimated_range_of_transmission")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
                .orElse(null));
        member.setMMSICode(Optional.of(tagKeyPrefix+"mmsi")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
                .orElse(null));
        member.getStatuses().add(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseStatus)
                .orElse(null));
        member.setVirtualAISAidToNavigationType(Optional.of("seamark:virtual_aton:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> v.replace(" ", "_"))
                .map(S125EnumParser::parseVirtualAisAidToNavigationType)
                .orElse(VirtualAISAidToNavigationTypeType.SPECIAL_PURPOSE));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(VirtualAISAidToNavigationImpl.Geometry.class::isInstance)
                .map(VirtualAISAidToNavigationImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Daymark AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected Daymark generateDaymark(AtonNode atonNode) {
        final Daymark member = new DaymarkImpl();
        final String tagKeyPrefix = "seamark:daymark:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:daymark:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getCategoryOfSpecialPurposeMarks().add(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfSpecialPurposeMark)
                .orElse(null));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.setColourPattern(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColourPattern)
                .orElse(null));
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(tagKeyPrefix+"nature_of_construction")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseNatureOfConstruction))
                .orElse(Collections.emptyList()));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setTopmarkDaymarkShape(Optional.of(tagKeyPrefix + "shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(DaymarkImpl.Geometry.class::isInstance)
                .map(DaymarkImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Fog Signal Type AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected FogSignal generateFogSignal(AtonNode atonNode) {
        final FogSignal member = new FogSignalImpl();
        final String tagKeyPrefix = "seamark:fog_signal:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:fog_signal:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfFogSignal(Optional.of(tagKeyPrefix + "category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseFogSignalCategory)
                .orElse(null));
        member.setSignalSequence(Optional.of(s100TagKeyPrefix+"signal_sequence")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(FogSignalImpl.Geometry.class::isInstance)
                .map(FogSignalImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Light AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected Light generateLight(AtonNode atonNode) {
        final Light member = new LightImpl();
        final String tagKeyPrefix = "seamark:light:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:light:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getColours().add(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColour)
                .orElse(null));
        member.getCategoryOfLights().add(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseLightCategory)
                .orElse(null));
        member.setExhibitionConditionOfLight(Optional.of(tagKeyPrefix+"exhibition")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> {try {return ExhibitionConditionOfLightType.fromValue(v + " light");} catch (Exception ex) {return null;}})
                .orElse(null));
        member.setHeight(Optional.of(tagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setLightCharacteristic(Optional.of(tagKeyPrefix+"character")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseLightCharacter)
                .orElse(null));
        member.getLightVisibilities().add(Optional.of(tagKeyPrefix+"visibility")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> {try {return LightVisibilityType.fromValue(v);} catch (Exception ex) {return null;}})
                .orElse(null));
        member.setMultiplicityOfLights(Optional.of(tagKeyPrefix+"multiple")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
                .orElse(BigInteger.ONE));
        member.setSignalGroup(Optional.of(tagKeyPrefix+"group")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setSignalPeriod(Optional.of(tagKeyPrefix+"period")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(Double::parseDouble)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setValueOfNominalRange(Optional.of(tagKeyPrefix+"range")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(LightImpl.Geometry.class::isInstance)
                .map(LightImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Radar Reflector AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected RadarReflector generateRadarReflector(AtonNode atonNode) {
        final RadarReflector member = new RadarReflectorImpl();
        final String tagKeyPrefix = "seamark:radar_reflector:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:radar_reflector:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of(tagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(RadarReflectorImpl.Geometry.class::isInstance)
                .map(RadarReflectorImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Retro Reflector AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected RetroReflector generateRetroReflector(AtonNode atonNode) {
        final RetroReflector member = new RetroReflectorImpl();
        final String tagKeyPrefix = "seamark:retro_reflector:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:retro_reflector:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getColours().addAll(Optional.of(s100TagKeyPrefix+"colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(s100TagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(tagKeyPrefix+"system")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
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
                .filter(RetroReflectorImpl.Geometry.class::isInstance)
                .map(RetroReflectorImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Silo and Tank AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected SiloTank generateSiloTank(AtonNode atonNode) {
        final SiloTank member = new SiloTankImpl();
        final String tagKeyPrefix = "seamark:tank:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:silo_tank:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setBuildingShape(Optional.of(tagKeyPrefix+"shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuildingShape)
                .orElse(null));
        member.setCategoryOfSiloTank(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseSiloTankCategory)
                .orElse(null));
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
        member.setRadarConspicuous(Optional.of(s100TagKeyPrefix+"radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisualProminence(Optional.of(s100TagKeyPrefix+"visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisualProminence)
                .orElse(null));
        member.setHeight(Optional.of(s100TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
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
                .filter(SiloTankImpl.Geometry.class::isInstance)
                .map(SiloTankImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Topmark AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected Topmark generateTopmark(AtonNode atonNode) {
        final Topmark member = new TopmarkImpl();
        final String tagKeyPrefix = "seamark:topmark:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:topmark:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.setColourPattern(Optional.of(tagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseColourPattern)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setTopmarkDaymarkShape(Optional.of(tagKeyPrefix + "shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(TopmarkImpl.Geometry.class::isInstance)
                .map(TopmarkImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Radio Station AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected RadioStation generateRadioStation(AtonNode atonNode) {
        final RadioStation member = new RadioStationImpl();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:radio_station:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setCategoryOfRadioStation(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadioStationCategory)
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
                .filter(RadioStationImpl.Geometry.class::isInstance)
                .map(RadioStationImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Radio Station AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected RadarTransponderBeacon generateRadarTransponderBeacon(AtonNode atonNode) {
        final RadarTransponderBeacon member = new RadarTransponderBeaconImpl();
        final String tagKeyPrefix = "seamark:radar_transponder:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:radar_transponder:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setCategoryOfRadarTransponderBeacon(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadioTransponderBeaconCategory)
                .orElse(null));
        member.setRadarWaveLength(Optional.of(tagKeyPrefix+"wavelength")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setSectorLimitOne(Optional.of(tagKeyPrefix+"sector_start")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                 .map(Double::parseDouble)
                .orElse(null));
        member.setSectorLimitTwo(Optional.of(tagKeyPrefix+"sector_end")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));
        member.setSignalGroup(Optional.of(tagKeyPrefix+"group")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setSignalSequence(Optional.of(tagKeyPrefix+"period")
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
                .filter(StringUtils::isNotBlank)
                .map(Double::parseDouble)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(RadarTransponderBeaconImpl.Geometry.class::isInstance)
                .map(RadarTransponderBeaconImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for physical AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected PhysicalAISAidToNavigation generatePhysicalAISAtoN(AtonNode atonNode) {
        final PhysicalAISAidToNavigation member = new PhysicalAISAidToNavigationImpl();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s100TagKeyPrefix = "s100:aidsToNavigation:ais_aid_to_navigation:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setEstimatedRangeOfTransmission(Optional.of(s100TagKeyPrefix+"estimated_range_of_transmission")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
                .orElse(null));
        member.setMMSICode(Optional.of(tagKeyPrefix+"mmsi")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
                .orElse(null));
        member.getStatuses().add(Optional.of("seamark:status")
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
                .filter(PhysicalAISAidToNavigationImpl.Geometry.class::isInstance)
                .map(PhysicalAISAidToNavigationImpl.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Many of the fields in the S125 Dataset features are common, i.e. they
     * are shared between all structure and equipment types. Therefore, this
     * helper function can be used to populated them easily for each type.
     *
     * @param member            The S-125 databaset member
     * @param atonNode          The AtoN node to populate the information from
     * @param <R> the type of class of the AtoN feature to be populated
     */
    public <R extends AidsToNavigationType> void populateS125AidsToNavigationFields(R member, AtonNode atonNode) {
        // First read the AtoN type information from the input
        final String s100TagKeyPrefix = "s100:aidsToNavigation:";

        // Now populate the fields
        member.setId(this.generateId(atonNode.getId()));
        member.setBoundedBy(this.generateBoundingShape(Collections.singletonList(atonNode)));
        member.setIdCode(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("aton.uk." + atonNode.getAtonUid()));
        member.setDateStart(Optional.of(s100TagKeyPrefix+"date_start")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setDateEnd(Optional.of(s100TagKeyPrefix+"date_end")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setPeriodStart(Optional.of(s100TagKeyPrefix+"period_start")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setPeriodEnd(Optional.of(s100TagKeyPrefix+"period_end" )
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(this::getS100TruncatedDate)
                .orElse(null));
        member.setScaleMinimum(Optional.of(s100TagKeyPrefix+"scale_minimum")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
                .orElse(null));

        // Add the feature names
        member.getFeatureNames().add(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(value -> {
                    final FeatureNameType featureNameType = new FeatureNameTypeImpl();
                    featureNameType.setName(value);
                    featureNameType.setLanguage(Locale.UK.getISO3Language());
                    featureNameType.setDisplayName(Boolean.TRUE);
                    return featureNameType;
                })
                .orElse(null));

        // Add the information
        member.getInformations().add(Optional.of(s100TagKeyPrefix+"information")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(value -> {
                    final InformationType informationType = new InformationTypeImpl();
                    informationType.setText(value);
                    informationType.setLanguage(Locale.UK.getISO3Language());
                    return informationType;
                })
                .orElse(null));

        // Also process the child and parent links
        processAidsToNavigationTypeRelationships(member, atonNode);
    }

    /**
     * This helper function will handle the parent-child relationships between
     * the AtoN node entries included in the dataset. It will use the member
     * type to identify if this is a parent or child entry, and then with the
     * help of the ID maps will either generate a new ID for the linked
     * elements or will use the already generated ones.
     *
     * @param member        The member entry of the dataset to assign the link
     * @param atonNode      The AtoN node which contains the links
     * @return the updated dataset member entry
     */
    protected void processAidsToNavigationTypeRelationships(AidsToNavigationType member, AtonNode atonNode) {
        // If this is a structure try to populate its child references
        if(member instanceof StructureObjectType) {
            ((StructureObjectType)member).getchildren().addAll(atonNode
                    .getChildren()
                    .stream()
                    .map(child -> {
                        ReferenceType referenceType = new ReferenceTypeImpl();
                        referenceType.setTitle(child.getAtonUid());
                        referenceType.setHref("#" + generateId(child.getId()));
                        referenceType.setRole("child");
                        referenceType.setArcrole(CHILD_REF_ARCHOLE);
                        return referenceType;
                    })
                    .toList());
        }

        // If this is an equipment try to populate its parent references
        if(member instanceof EquipmentType) {
            Optional.of(atonNode)
                    .map(AtonNode::getParent)
                    .map(parent -> {
                        ReferenceType referenceType = new ReferenceTypeImpl();
                        referenceType.setTitle(parent.getAtonUid());
                        referenceType.setHref("#" + generateId(parent.getId()));
                        referenceType.setRole("parent");
                        referenceType.setArcrole(PARENT_REF_ARCHOLE);
                        return  referenceType;
                    })
                    .ifPresent(((EquipmentType)member)::setParent);
        }
    }

    /**
     * Based on the provided AtoN link information this function will generate
     * the appropriate S-125 aggregation link XML entry.
     *
     * @param atonLink      The AtoN link to generate the aggregation for
     * @return the generate aggregation link entry
     */
    protected Aggregation generateAggregation(AtonLink atonLink) {
        // Sanity Check
        if(atonLink.getLinkCategory().getAtonLinkType() != AtonLinkType.AGGREGATION) {
            return null;
        }

        // Otherwise create the aggregation
        Aggregation aggregationType = new AggregationImpl();
        aggregationType.setId(this.generateId(null));
        aggregationType.setCategoryOfAggregation(CategoryOfAggregationType.fromValue(atonLink.getLinkCategory().getValue()));
        aggregationType.getPeers().addAll(atonLink.getPeers().stream()
                .map(peer -> {
                    ReferenceType referenceType = new ReferenceTypeImpl();
                    referenceType.setTitle(peer.getAtonUid());
                    referenceType.setHref("#" + generateId(peer.getId()));
                    referenceType.setRole("aggregation");
                    referenceType.setArcrole(AGGREGATION_REF_ARCHOLE);
                    return referenceType;
                })
                .toList());

        // And return the result
        return aggregationType;
    }

    /**
     * Based on the provided AtoN link information this function will generate
     * the appropriate S-125 association link XML entry.
     *
     * @param atonLink      The AtoN link to generate the association for
     * @return the generate association link entry
     */
    protected Association generateAssociation(AtonLink atonLink) {
        // Sanity Check
        if(atonLink.getLinkCategory().getAtonLinkType() != AtonLinkType.ASSOCIATION) {
            return null;
        }

        // Otherwise create the association
        Association associationType = new AssociationImpl();
        associationType.setId(this.generateId(null));
        associationType.setCategoryOfAssociation(CategoryOfAssociationType.fromValue(atonLink.getLinkCategory().getValue()));
        associationType.getPeers().addAll(atonLink.getPeers().stream()
                .map(peer -> {
                    ReferenceType referenceType = new ReferenceTypeImpl();
                    referenceType.setTitle(peer.getAtonUid());
                    referenceType.setHref("#" + generateId(peer.getId()));
                    referenceType.setRole("association");
                    referenceType.setArcrole(ASSOCIATION_REF_ARCHOLE);
                    return referenceType;
                })
                .toList());

        // And return the result
        return associationType;
    }

    /**
     * Translate the ISO Date-Time string into an S100-compatible Truncated Date
     * object.
     *
     * @param isoDateTimeString The ISO Date-Time String
     * @return the S100 Truncated Date object
     */
    private S100TruncatedDate getS100TruncatedDate(String isoDateTimeString) {
        S100TruncatedDate s100TruncatedDate = new S100TruncatedDateImpl();
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
        SurfaceProperty surfaceProperty = new SurfacePropertyImpl();
        SurfaceType surfaceType = new SurfaceTypeImpl();
        Patches patches = new PatchesImpl();
        PolygonPatchType polygonPatchType = new PolygonPatchTypeImpl();
        AbstractRingPropertyType abstractRingPropertyType = new AbstractRingPropertyTypeImpl();
        LinearRingType linearRingType = new LinearRingTypeImpl();
        PosList posList = new PosListImpl();

        // Populate with the geometry data
        posList.setValue(coords.toArray(new Double[0]));

        // Populate the elements
        linearRingType.setPosList(posList);
        abstractRingPropertyType.setAbstractRing(this.opengisGMLFactory.createLinearRing(linearRingType));
        polygonPatchType.setExterior(abstractRingPropertyType);
        patches.getAbstractSurfacePatches().add(this.opengisGMLFactory.createPolygonPatch(polygonPatchType));
        surfaceType.setPatches(patches);
        surfaceType.setId(this.generateId(null));
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
        CurveProperty curveProperty = new CurvePropertyImpl();
        CurveType curveType = new CurveTypeImpl();
        Segments segments = new SegmentsImpl();
        LineStringSegmentType lineStringSegmentType = new LineStringSegmentTypeImpl();
        PosList posList = new PosListImpl();
        
        // Populate with the geometry data
        posList.setValue(coords.toArray(new Double[0]));
        
        // Populate the elements
        lineStringSegmentType.setPosList(posList);
        segments.getAbstractCurveSegments().add(this.opengisGMLFactory.createLineStringSegment(lineStringSegmentType));
        curveType.setSegments(segments);
        curveType.setId(this.generateId(null));
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
        PointProperty pointProperty = new PointPropertyImpl();
        PointType pointType = new PointTypeImpl();
        Pos pos = new PosImpl();

        // Populate with the geometry data
        pos.setValue(coords.toArray(new Double[0]));

        // Populate the elements
        pointType.setPos(pos);
        pointType.setId(this.generateId(null));
        pointProperty.setPoint(pointType);

        // And return the output
        return pointProperty;
    }

    /**
     * For easy generation of the bounding shapes for the Dataset or individual
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

        Pos lowerCorner = new PosImpl();
        lowerCorner.setValue(new Double[]{envelope.getMinX(), envelope.getMaxY()});
        Pos upperCorner = new PosImpl();
        upperCorner.setValue(new Double[]{envelope.getMaxX(), envelope.getMaxY()});

        // And create the bounding by envelope
        BoundingShapeType boundingShapeType = new BoundingShapeTypeImpl();
        EnvelopeType envelopeType = new EnvelopeTypeImpl();
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
     */
    protected void enclosingEnvelopFromGeometry(Envelope envelope, Geometry geometry) {
        final Geometry enclosingGeometry = geometry.getEnvelope();
        final Coordinate[] enclosingCoordinates = enclosingGeometry.getCoordinates();
        for (Coordinate c : enclosingCoordinates) {
            envelope.expandToInclude(c);
        }
    }

    /**
     * A helper function that is used to generate a homogenous set of IDs for
     * the S-125 Dataset.
     *
     * @param id    The ID of the element used to generate the dataset ID
     * @return the generated ID string
     */
    protected String generateId(Integer id) {
        // If an element ID was provided add it in the maps for a lookup
        if(id != null && this.idMap.containsKey(id)) {
            return this.idMap.get(id);
        }

        // Generate a new dataset ID
        final String datasetId = String.format(this.idFormat, this.idIndex.getAndIncrement());

        // If again we have an element ID, add the generated value to the lookup maps
        if(id != null) {
            this.idMap.put(id, datasetId);
        }

        // And return the output
        return datasetId;
    }

}
