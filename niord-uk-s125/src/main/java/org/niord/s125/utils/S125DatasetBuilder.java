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

import _int.iala_aism.s125.gml._0_0.AggregationType;
import _int.iala_aism.s125.gml._0_0.S100TruncatedDate;
import _int.iala_aism.s125.gml._0_0.*;
import _int.iho.s100.gml.base._5_0.AbstractFeatureType;
import _int.iho.s100.gml.base._5_0.CurveType;
import _int.iho.s100.gml.base._5_0.PointType;
import _int.iho.s100.gml.base._5_0.SurfaceType;
import _int.iho.s100.gml.base._5_0.*;
import _net.opengis.gml.profiles.*;
import org.apache.commons.lang3.StringUtils;
import org.grad.eNav.s125.utils.S125Utils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.niord.core.aton.AtonLink;
import org.niord.core.aton.AtonLinkType;
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

    // Class Variables
    private String idFormat;
    private AtomicInteger idIndex;
    private Map<Integer, String> idMap;
    private HashSet<Integer> linksSet;
    private _int.iala_aism.s125.gml._0_0.ObjectFactory s125GMLFactory;
    private _net.opengis.gml.profiles.ObjectFactory opengisGMLFactory;

    /**
     * Class Constructor.
     */
    public S125DatasetBuilder() {
        this.idFormat = String.format("ID%%0%dd", 3);
        this.idIndex = new AtomicInteger(1);
        this.idMap = new HashMap<>();
        this.linksSet = new HashSet<>();
        this.s125GMLFactory = new _int.iala_aism.s125.gml._0_0.ObjectFactory();
        this.opengisGMLFactory = new _net.opengis.gml.profiles.ObjectFactory();
    }
    
    /**
     * This is the main   the provided list of AtoN nodess into an S125 Dataset as
     * dictated by the NIPWG S-125 data product specification.
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
        Dataset s125Dataset = new Dataset();
        s125Dataset.setId(DatasetInfo.getDatasetId());

        //====================================================================//
        //                       BOUNDED BY SECTION                           //
        //====================================================================//
        s125Dataset.setBoundedBy(this.generateBoundingShape(atonNodes));

        //====================================================================//
        //                  Dataset IDENTIFICATION SECTION                    //
        //====================================================================//
        DataSetIdentificationType DatasetIdentificationType = new DataSetIdentificationType();
        DatasetIdentificationType.setEncodingSpecification(DatasetInfo.getEncodingSpecification());
        DatasetIdentificationType.setEncodingSpecificationEdition(DatasetInfo.getEncodingSpecificationEdition());
        DatasetIdentificationType.setProductIdentifier(DatasetInfo.getProductionIdentifier());
        DatasetIdentificationType.setProductEdition(DatasetInfo.getProductionEdition());
        DatasetIdentificationType.setDatasetFileIdentifier(DatasetInfo.getFileIdentifier());
        DatasetIdentificationType.setDatasetTitle(DatasetInfo.getTitle());
        DatasetIdentificationType.setDatasetReferenceDate(LocalDate.now());
        DatasetIdentificationType.setDatasetLanguage(Locale.getDefault().getISO3Language());
        DatasetIdentificationType.setDatasetAbstract(DatasetInfo.getAbstractText());
        s125Dataset.setDatasetIdentificationInformation(DatasetIdentificationType);

        //====================================================================//
        //                      Dataset MEMBERS SECTION                       //
        //====================================================================//
        Optional.ofNullable(atonNodes)
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(aton ->
                    Stream.of(
                            Stream.of(this.generateAidsToNavigation(aton)),
                            aton.getChildren().stream().map(this::generateAidsToNavigation),
                            this.generateAidsToNavigationLinks(aton).stream()
                    ).flatMap(i -> i)
                )
                .map(jaxb -> { MemberType m = new MemberType(); m.setAbstractFeature(jaxb); return m; })
                .forEach(s125Dataset.getImembersAndMembers()::add);

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
    public JAXBElement<? extends AbstractFeatureType> generateAidsToNavigation(AtonNode atonNode) {
        // First read the AtoN type information from the input
        S125AtonTypes atonType = S125AtonTypes.fromSeamarkType(atonNode.getTagValue(TAG_ATON_TYPE));
        // Now initialise the JAXB object factory to generate the member
        JAXBElement<? extends AidsToNavigationType> jaxbElement =  null;
        // Handle each possible type, cause a different object should be created
        switch(atonType) {
            /***************************
             *    STRUCTURE OBJECTS    *
             ***************************/
            case CARDINAL_BEACON:
                jaxbElement = this.s125GMLFactory.createBeaconCardinal(this.generateBeaconCardinal(atonNode));
                break;
            case LATERAL_BEACON:
                jaxbElement = this.s125GMLFactory.createBeaconLateral(this.generateBeaconLateral(atonNode));
                break;
            case ISOLATED_DANGER_BEACON:
                jaxbElement = this.s125GMLFactory.createBeaconIsolatedDanger(this.generateBeaconIsolatedDanger(atonNode));
                break;
            case SAFE_WATER_BEACON:
                jaxbElement = this.s125GMLFactory.createBeaconSafeWater(this.generateBeaconSafeWater(atonNode));
                break;
            case SPECIAL_PURPOSE_BEACON:
                jaxbElement = this.s125GMLFactory.createBeaconSpecialPurposeGeneral(this.generateBeaconSpecialPurpose(atonNode));
                break;
            case CARDINAL_BUOY:
                jaxbElement = this.s125GMLFactory.createBuoyCardinal(this.generateBuoyCardinal(atonNode));
                break;
            case LATERAL_BUOY:
                jaxbElement = this.s125GMLFactory.createBuoyLateral(this.generateBuoyLateral(atonNode));
                break;
            case INSTALLATION_BUOY:
                jaxbElement = this.s125GMLFactory.createBuoyInstallation(this.generateBuoyInstallation(atonNode));
                break;
            case ISOLATED_DANGER_BUOY:
                jaxbElement = this.s125GMLFactory.createBuoyIsolatedDanger(this.generateBuoyIsolatedDanger(atonNode));
                break;
            case SAFE_WATER_BUOY:
                jaxbElement = this.s125GMLFactory.createBuoySafeWater(this.generateBuoySafeWater(atonNode));
                break;
            case SPECIAL_PURPOSE_BUOY:
                jaxbElement = this.s125GMLFactory.createBuoySpecialPurposeGeneral(this.generateBuoySpecialPurpose(atonNode));
                break;
            case LANDMARK:
                jaxbElement = this.s125GMLFactory.createLandmark(this.generateLandmark(atonNode));
                break;
            case LIGHTHOUSE_MAJOR: case LIGHTHOUSE_MINOR:
                jaxbElement = this.s125GMLFactory.createLighthouse(this.generateLighthouse(atonNode));
                break;
            case LIGHT_VESSEL:
                jaxbElement = this.s125GMLFactory.createLightVessel(this.generateLightVessel(atonNode));
                break;
            case VIRTUAL_ATON:
                jaxbElement = this.s125GMLFactory.createVirtualAISAidToNavigation(this.generateVirtualAtoN(atonNode));
                break;
            /***************************
             *    EQUIPMENT OBJECTS    *
             ***************************/
            case DAYMARK:
                jaxbElement = this.s125GMLFactory.createDaymark(this.generateDaymark(atonNode));
                break;
            case ENVIRONMENT_OBSERVATION:
                jaxbElement = this.s125GMLFactory.createEnvironmentObservationEquipment(this.generateEnvironmentObservationEquipment(atonNode));
                break;
            case FOG_SIGNAL:
                jaxbElement = this.s125GMLFactory.createFogSignal(this.generateFogSignal(atonNode));
                break;
            case LIGHT:
                jaxbElement = this.s125GMLFactory.createLight(this.generateLight(atonNode));
                break;
            case RADAR_REFLECTOR:
                jaxbElement = this.s125GMLFactory.createRadarReflector(this.generateRadarReflector(atonNode));
                break;
            case RETRO_REFLECTOR:
                jaxbElement = this.s125GMLFactory.createRetroReflector(this.generateRetroReflector(atonNode));
                break;
            case SILOS_AND_TANKS:
                jaxbElement = this.s125GMLFactory.createSiloTank(this.generateSiloTank(atonNode));
                break;
            case TOPMARK:
                jaxbElement = this.s125GMLFactory.createTopmark(this.generateTopmark(atonNode));
                break;
            case RADIO_STATION:
                jaxbElement = this.s125GMLFactory.createRadioStation(this.generateRadioStation( atonNode));
                break;
            case PHYSICAL_AIS_ATON:
                jaxbElement = this.s125GMLFactory.createPhysicalAISAidToNavigation(this.generatePhysicalAISAtoN(atonNode));
                break;
        }
        // And return what was generated
        return jaxbElement;
    }

    /**
     * This is another entry method static function of the utility. It will
     * examine the provided AtoN Node links from Niord and generate a standardised
     * list of S-125 aggregation or association entries.
     *
     * @param atonNode      The AtoN node to generate the aggregation/association links for
     * @return the generated collection of aggregation/association links
     */
    public Collection<JAXBElement<? extends AbstractFeatureType>> generateAidsToNavigationLinks(AtonNode atonNode) {
        // Sanity Check
        if(atonNode == null || atonNode.getLinks() == null || atonNode.getLinks().isEmpty()) {
            return Collections.emptyList();
        }

        // Create a new link collection
        final List<JAXBElement<? extends AbstractFeatureType>> linkCollection = new ArrayList<>();

        // Add the aggregation links
        atonNode.getLinks().stream()
                .filter(link -> link.getLinkCategory().getAtonLinkType() == AtonLinkType.AGGREGATION)
                .filter(not(link -> this.linksSet.contains(link.getId())))
                .map(this::generateAggregation)
                .filter(Objects::nonNull)
                .map(this.s125GMLFactory::createAggregation)
                .forEach(linkCollection::add);

        // Add the association links
        atonNode.getLinks().stream()
                .filter(link -> link.getLinkCategory().getAtonLinkType() == AtonLinkType.ASSOCIATION)
                .filter(not(link -> this.linksSet.contains(link.getId())))
                .map(this::generateAssociation)
                .filter(Objects::nonNull)
                .map(this.s125GMLFactory::createAssociation)
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
    protected BeaconCardinalType generateBeaconCardinal(AtonNode atonNode) {
        final BeaconCardinalType member = new BeaconCardinalType();
        final String tagKeyPrefix = "seamark:beacon_cardinal:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBeaconType.Geometry.class::isInstance)
                .map(GenericBeaconType.Geometry.class::cast)
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
    protected BeaconLateralType generateBeaconLateral(AtonNode atonNode) {
        final BeaconLateralType member = new BeaconLateralType();
        final String tagKeyPrefix = "seamark:beacon_lateral:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBeaconType.Geometry.class::isInstance)
                .map(GenericBeaconType.Geometry.class::cast)
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
    protected BeaconIsolatedDangerType generateBeaconIsolatedDanger(AtonNode atonNode) {
        final BeaconIsolatedDangerType member = new BeaconIsolatedDangerType();
        final String tagKeyPrefix = "seamark:beacon_isolated_danger:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBeaconType.Geometry.class::isInstance)
                .map(GenericBeaconType.Geometry.class::cast)
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
    protected BeaconSafeWaterType generateBeaconSafeWater(AtonNode atonNode) {
        final BeaconSafeWaterType member = new BeaconSafeWaterType();
        final String tagKeyPrefix = "seamark:beacon_safe_water:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBeaconType.Geometry.class::isInstance)
                .map(GenericBeaconType.Geometry.class::cast)
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
    protected BeaconSpecialPurposeGeneralType generateBeaconSpecialPurpose(AtonNode atonNode) {
        final BeaconSpecialPurposeGeneralType member = new BeaconSpecialPurposeGeneralType();
        final String tagKeyPrefix = "seamark:beacon_special_purpose:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_beacon:";
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBeaconType.Geometry.class::isInstance)
                .map(GenericBeaconType.Geometry.class::cast)
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
    protected BuoyCardinalType generateBuoyCardinal(AtonNode atonNode) {
        final BuoyCardinalType member = new BuoyCardinalType();
        final String tagKeyPrefix = "seamark:buoy_cardinal:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
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
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBuoyType.Geometry.class::isInstance)
                .map(GenericBuoyType.Geometry.class::cast)
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
    protected BuoyLateralType generateBuoyLateral(AtonNode atonNode) {
        final BuoyLateralType member = new BuoyLateralType();
        final String tagKeyPrefix = "seamark:buoy_lateral:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
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
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBuoyType.Geometry.class::isInstance)
                .map(GenericBuoyType.Geometry.class::cast)
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
    protected BuoyInstallationType generateBuoyInstallation(AtonNode atonNode) {
        final BuoyInstallationType member = new BuoyInstallationType();
        final String tagKeyPrefix = "seamark:buoy_installation:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
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
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBuoyType.Geometry.class::isInstance)
                .map(GenericBuoyType.Geometry.class::cast)
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
    protected BuoyIsolatedDangerType generateBuoyIsolatedDanger(AtonNode atonNode) {
        final BuoyIsolatedDangerType member = new BuoyIsolatedDangerType();
        final String tagKeyPrefix = "seamark:buoy_isolated_danger:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
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
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBuoyType.Geometry.class::isInstance)
                .map(GenericBuoyType.Geometry.class::cast)
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
    protected BuoySafeWaterType generateBuoySafeWater(AtonNode atonNode) {
        final BuoySafeWaterType member = new BuoySafeWaterType();
        final String tagKeyPrefix = "seamark:buoy_safe_water:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
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
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBuoyType.Geometry.class::isInstance)
                .map(GenericBuoyType.Geometry.class::cast)
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
    protected BuoySpecialPurposeGeneralType generateBuoySpecialPurpose(AtonNode atonNode) {
        final BuoySpecialPurposeGeneralType member = new BuoySpecialPurposeGeneralType();
        final String tagKeyPrefix = "seamark:buoy_special_purpose:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:generic_buoy:";
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
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(GenericBuoyType.Geometry.class::isInstance)
                .map(GenericBuoyType.Geometry.class::cast)
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
        final LandmarkType member = new LandmarkType();
        final String tagKeyPrefix = "seamark:landmark:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:landmark:";
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
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(tagKeyPrefix+"construction")
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
        member.setVisuallyConspicuous(Optional.of(tagKeyPrefix+"conspicuity")
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
    protected LighthouseType generateLighthouse(AtonNode atonNode) {
        final LighthouseType member = new LighthouseType();
        final String tagKeyPrefix = "seamark:landmark:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:lighthouse:";
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
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(tagKeyPrefix+"construction")
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
        member.setVisuallyConspicuous(Optional.of(tagKeyPrefix+"conspicuity")
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
                .filter(LighthouseType.Geometry.class::isInstance)
                .map(LighthouseType.Geometry.class::cast)
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
    protected LightVesselType generateLightVessel(AtonNode atonNode) {
        final LightVesselType member = new LightVesselType();
        final String tagKeyPrefix = "seamark:light_vessel:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:light_vessel:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.getNatureOfConstructions().addAll(Optional.of(s125TagKeyPrefix+"nature_of_construction")
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
                .filter(LightVesselType.Geometry.class::isInstance)
                .map(LightVesselType.Geometry.class::cast)
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
    protected VirtualAISAidToNavigationType generateVirtualAtoN(AtonNode atonNode) {
        final VirtualAISAidToNavigationType member = new VirtualAISAidToNavigationType();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:virtual_ais_aid_to_navigation:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setEstimatedRangeOfTransmission(Optional.of(s125TagKeyPrefix+"estimated_range_of_transmission")
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
                .orElse(VirtualAISAidToNavigationTypeType.SPECIAL_PURPOSE));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(VirtualAISAidToNavigationType.Geometry.class::isInstance)
                .map(VirtualAISAidToNavigationType.Geometry.class::cast)
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
    protected DaymarkType generateDaymark(AtonNode atonNode) {
        final DaymarkType member = new DaymarkType();
        final String tagKeyPrefix = "seamark:daymark:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:daymark:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setCategoryOfSpecialPurposeMark(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfSpecialPurposeMark)
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.getNatureOfConstructions().addAll(Optional.of(tagKeyPrefix+"nature_of_construction")
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
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setTopmarkDaymarkShape(Optional.of(tagKeyPrefix + "shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseTopmarkDaymarkShape)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(DaymarkType.Geometry.class::isInstance)
                .map(DaymarkType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Generate the S-125 Dataset member section for Environment Observation
     * Equipment Type AtoNs.
     *
     * @param atonNode      The AtoN node to be used for the member
     * @return The S-125 Dataset member section generated
     */
    protected EnvironmentObservationEquipmentType generateEnvironmentObservationEquipment(AtonNode atonNode) {
        final EnvironmentObservationEquipmentType member = new EnvironmentObservationEquipmentType();
        final String tagKeyPrefix = "seamark:platform:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:environment_observation_equipment:";
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
                .map(BigDecimal::new)
                .orElse(null));
        member.getTypeOfEnvironmentObservationEquipments().addAll(Optional.of(s125TagKeyPrefix + "type")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                 .map(t -> t.split(","))
                .map(Arrays::asList)
                .orElse(Collections.emptyList()));
        member.setTypeOfBattery(Optional.of(s125TagKeyPrefix + "type_of_battery")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(EnvironmentObservationEquipmentType.Geometry.class::isInstance)
                .map(EnvironmentObservationEquipmentType.Geometry.class::cast)
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
    protected FogSignalType generateFogSignal(AtonNode atonNode) {
        final FogSignalType member = new FogSignalType();
        final String tagKeyPrefix = "seamark:fog_signal:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:fog_signal:";
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
        member.setSignalSequence(Optional.of(s125TagKeyPrefix+"signal_sequence")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(FogSignalType.Geometry.class::isInstance)
                .map(FogSignalType.Geometry.class::cast)
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
    protected LightType generateLight(AtonNode atonNode) {
        final LightType member = new LightType();
        final String tagKeyPrefix = "seamark:light:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:light:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
                .map(v -> {try {return ExhibitionConditionOfLightType.fromValue(v + " light");} catch (Exception ex) {return null;}})
                .orElse(null));
        member.setHeight(Optional.of(tagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
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
                .map(v -> {try {return LightVisibilityType.fromValue(v);} catch (Exception ex) {return null;}})
                .orElse(null));
        member.setOrientation(Optional.of(s125TagKeyPrefix+"orientation")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
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
                .map(BigDecimal::new)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(LightType.Geometry.class::isInstance)
                .map(LightType.Geometry.class::cast)
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
    protected RadarReflectorType generateRadarReflector(AtonNode atonNode) {
        final RadarReflectorType member = new RadarReflectorType();
        final String tagKeyPrefix = "seamark:radar_reflector:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:radar_reflector:";
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
                .map(BigDecimal::new)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(RadarReflectorType.Geometry.class::isInstance)
                .map(RadarReflectorType.Geometry.class::cast)
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
    protected RetroReflectorType generateRetroReflector(AtonNode atonNode) {
        final RetroReflectorType member = new RetroReflectorType();
        final String tagKeyPrefix = "seamark:retro_reflector:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:retro_reflector:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.getColours().addAll(Optional.of(s125TagKeyPrefix+"colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(s125TagKeyPrefix+"colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of(s125TagKeyPrefix+"marks_navigational_system_of")
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
                .filter(RetroReflectorType.Geometry.class::isInstance)
                .map(RetroReflectorType.Geometry.class::cast)
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
    protected SiloTankType generateSiloTank(AtonNode atonNode) {
        final SiloTankType member = new SiloTankType();
        final String tagKeyPrefix = "seamark:tank:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:silo_tank:";
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
        member.setHeight(Optional.of(s125TagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
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
                .filter(SiloTankType.Geometry.class::isInstance)
                .map(SiloTankType.Geometry.class::cast)
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
    protected TopmarkType generateTopmark(AtonNode atonNode) {
        final TopmarkType member = new TopmarkType();
        final String tagKeyPrefix = "seamark:topmark:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:topmark:";
        this.populateS125AidsToNavigationFields(member, atonNode);
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
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setTopmarkDaymarkShape(Optional.of(tagKeyPrefix + "shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseTopmarkDaymarkShape)
                .orElse(null));

        // Now fix the geometry...
        S125Utils.generateS125AidsToNavigationTypeGeometriesList(
                        member.getClass(),
                        Collections.singletonList(this.generatePointProperty(Arrays.asList(atonNode.getLon(), atonNode.getLat())))
                )
                .stream()
                .filter(TopmarkType.Geometry.class::isInstance)
                .map(TopmarkType.Geometry.class::cast)
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
    protected RadioStationType generateRadioStation(AtonNode atonNode) {
        final RadioStationType member = new RadioStationType();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:radio_station:";
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
                .filter(TopmarkType.Geometry.class::isInstance)
                .map(TopmarkType.Geometry.class::cast)
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
    protected PhysicalAISAidToNavigationType generatePhysicalAISAtoN(AtonNode atonNode) {
        final PhysicalAISAidToNavigationType member = new PhysicalAISAidToNavigationType();
        final String tagKeyPrefix = "seamark:radio_station:";
        final String s125TagKeyPrefix = "s125:aidsToNavigation:ais_aid_to_navigation:";
        this.populateS125AidsToNavigationFields(member, atonNode);
        member.setEstimatedRangeOfTransmission(Optional.of(s125TagKeyPrefix+"estimated_range_of_transmission")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMMSICode(Optional.of(tagKeyPrefix+"mmsi")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .filter(StringUtils::isNotBlank)
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
                .filter(PhysicalAISAidToNavigationType.Geometry.class::isInstance)
                .map(PhysicalAISAidToNavigationType.Geometry.class::cast)
                .forEach(member.getGeometries()::add);

        // And return the populated member
        return member;
    }

    /**
     * Many of the fields in the S125 Dataset features are common, i.e. they
     * are shared between all structure and equipment types. Therefore this
     * helper function can be used to populated them easily for each type.
     *
     * @param member            The S-125 databaset member
     * @param atonNode          The AtoN node to populate the information from
     * @param <R> the type of class of the AtoN feature to be populated
     */
    public <R extends AidsToNavigationType> void populateS125AidsToNavigationFields(R member, AtonNode atonNode) {
        // First read the AtoN type information from the input
        final S125AtonTypes atonType = S125AtonTypes.fromSeamarkType(atonNode.getTagValue(TAG_ATON_TYPE));
        final String s125TagKeyPrefix = "s125:aidsToNavigation:";

        // Now populate the fields
        member.setId(this.generateId(atonNode.getId()));
        member.setBoundedBy(this.generateBoundingShape(Collections.singletonList(atonNode)));
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(atonNode.getAtonUid()));
        member.setIdCode("aton.uk." + atonNode.getAtonUid().toLowerCase());
        member.setTextualDescription(String.format("%s", Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
        member.setTextualDescriptionInNationalLanguage(String.format("%s", Optional.of("seamark:name")
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
                .filter(StringUtils::isNotBlank)
                .map(BigInteger::new)
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
    protected AidsToNavigationType processAidsToNavigationTypeRelationships(AidsToNavigationType member, AtonNode atonNode) {
        // If this is a structure try to populate its child references
        if(member instanceof StructureObjectType) {
            ((StructureObjectType)member).setchildren(atonNode.getChildren().stream()
                    .map(child -> {
                        ReferenceType referenceType = new ReferenceType();
                        referenceType.setHref("#" + generateId(child.getId()));
                        referenceType.setRole("child");
                        referenceType.setArcrole(CHILD_REF_ARCHOLE);
                        return referenceType;
                    })
                    .collect(Collectors.toList()));
        }

        // If this is an equipment try to populate its parent references
        if(member instanceof EquipmentType) {
            ReferenceType referenceType = new ReferenceType();
            referenceType.setHref("#" + generateId(atonNode.getParent().getId()));
            referenceType.setRole("parent");
            referenceType.setArcrole(PARENT_REF_ARCHOLE);
            ((EquipmentType)member).setParent(referenceType);
        }

        return member;
    }

    /**
     * Based on the provided AtoN link information this function will generate
     * the appropriate S-125 aggregation link XML entry.
     *
     * @param atonLink      The AtoN link to generate the aggregation for
     * @return the generate aggregation link entry
     */
    protected AggregationType generateAggregation(AtonLink atonLink) {
        // Sanity Check
        if(atonLink.getLinkCategory().getAtonLinkType() != AtonLinkType.AGGREGATION) {
            return null;
        }

        // Otherwise create the aggregation
        AggregationType aggregationType = new AggregationType();
        aggregationType.setCategoryOfAggregation(CategoryOfAggregationType.fromValue(atonLink.getLinkCategory().getValue()));
        aggregationType.setPeers(atonLink.getPeers().stream()
                .map(peer -> {
                    ReferenceType referenceType = new ReferenceType();
                    referenceType.setHref("#" + generateId(peer.getId()));
                    referenceType.setRole("aggregation");
                    referenceType.setArcrole(AGGREGATION_REF_ARCHOLE);
                    return referenceType;
                })
                .collect(Collectors.toList()));

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
    protected AssociationType generateAssociation(AtonLink atonLink) {
        // Sanity Check
        if(atonLink.getLinkCategory().getAtonLinkType() != AtonLinkType.ASSOCIATION) {
            return null;
        }

        // Otherwise create the association
        AssociationType associationType = new AssociationType();
        associationType.setCategoryOfAssociation(CategoryOfAssociationType.fromValue(atonLink.getLinkCategory().getValue()));
        associationType.setPeers(atonLink.getPeers().stream()
                .map(peer -> {
                    ReferenceType referenceType = new ReferenceType();
                    referenceType.setHref("#" + generateId(peer.getId()));
                    referenceType.setRole("association");
                    referenceType.setArcrole(ASSOCIATION_REF_ARCHOLE);
                    return referenceType;
                })
                .collect(Collectors.toList()));

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
        PointProperty pointProperty = new PointProperty();
        PointType pointType = new PointType();
        Pos pos = new Pos();

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
