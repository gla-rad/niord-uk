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
import _int.iho.s100.gml.base._1_0.CurveType;
import _int.iho.s100.gml.base._1_0.PointType;
import _int.iho.s100.gml.base._1_0.*;
import _int.iho.s100.gml.base._1_0.SurfaceType;
import _net.opengis.gml.profiles.*;
import org.niord.core.aton.AtonNode;
import org.niord.core.aton.AtonTag;
import org.niord.core.geojson.GeoJsonUtils;
import org.niord.core.geojson.JtsConverter;
import org.niord.model.geojson.CrsVo;
import org.niord.model.geojson.GeoJsonVo;
import org.niord.s125.models.S125DatasetInfo;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.niord.core.aton.AtonTag.TAG_ATON_TYPE;

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
        // Calculate the bounding by envelope
        List<GeoJsonVo> geoJsonAList = atonNodes.stream()
                .map(AtonNode::getGeometry)
                .map(JtsConverter::fromJts)
                .collect(Collectors.toList());
        String srsName = geoJsonAList.stream()
                .findFirst()
                .map(GeoJsonVo::getCrs)
                .map(CrsVo::getType)
                .orElse("EPSG:4326");
        double bbox[] = GeoJsonUtils.computeBBox(geoJsonAList.toArray(GeoJsonVo[]::new));
        Pos lowerCorner = new Pos();
        lowerCorner.getValues().add(bbox[1]);
        lowerCorner.getValues().add(bbox[0]);
        Pos upperCorner = new Pos();
        upperCorner.getValues().add(bbox[3]);
        upperCorner.getValues().add(bbox[2]);

        // And create the bounding by envelope
        BoundingShapeType boundingShapeType = new BoundingShapeType();
        EnvelopeType envelopeType = new EnvelopeType();
        envelopeType.setSrsName(srsName);
        envelopeType.setLowerCorner(lowerCorner);
        envelopeType.setUpperCorner(upperCorner);
        boundingShapeType.setEnvelope(envelopeType);
        s125Dataset.setBoundedBy(boundingShapeType);

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
        dataSetIdentificationType.setDatasetReferenceDate(new Date());
        dataSetIdentificationType.setDatasetLanguage(ISO6391.EN);
        dataSetIdentificationType.setDatasetAbstract(datasetInfo.getAbstractText());
        s125Dataset.setDatasetIdentificationInformation(dataSetIdentificationType);

        //====================================================================//
        //              DATASET STRUCTURE INFORMATION SECTION                 //
        //====================================================================//
        DataSetStructureInformationType dataSetStructureInformationType = new DataSetStructureInformationType();
        dataSetStructureInformationType.setCoordMultFactorX(BigInteger.ONE);
        dataSetStructureInformationType.setCoordMultFactorY(BigInteger.ONE);
        dataSetStructureInformationType.setCoordMultFactorZ(BigInteger.ONE);

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
        String atonType = atonNode.getTagValue(TAG_ATON_TYPE);
        // Now initialise the JAXB object factory to generate the member
        JAXBElement<? extends S125AidsToNavigationType> jaxbElement =  null;
        // Handle each possible type, cause a different object should be created
        switch(atonType) {
            case "beacon_cardinal":
                jaxbElement = this.s125GMLFactory.createS125BeaconCardinal(this.generateBeaconCardinal(datasetInfo, atonNode));
                break;
            case "beacon_lateral":
                jaxbElement = this.s125GMLFactory.createS125BeaconLateral(this.generateBeaconLateral(datasetInfo, atonNode));
                break;
            case "beacon_isolated_danger":
                jaxbElement = this.s125GMLFactory.createS125BeaconIsolatedDanger(this.generateBeaconIsolatedDanger(datasetInfo, atonNode));
                break;
            case "beacon_safe_water":
                jaxbElement = this.s125GMLFactory.createS125BeaconSafeWater(this.generateBeaconSafeWater(datasetInfo, atonNode));
                break;
            case "beacon_special_purpose":
                jaxbElement = this.s125GMLFactory.createS125BeaconSpecialPurposeGeneral(this.generateBeaconSpecialPurpose(datasetInfo, atonNode));
                break;
            case "buoy_cardinal":
                jaxbElement = this.s125GMLFactory.createS125BuoyCardinal(this.generateBuoyCardinal(datasetInfo, atonNode));
                break;
            case "buoy_lateral":
                jaxbElement = this.s125GMLFactory.createS125BuoyLateral(this.generateBuoyLateral(datasetInfo, atonNode));
                break;
            case "buoy_installation":
                jaxbElement = this.s125GMLFactory.createS125BuoyInstallation(this.generateBuoyInstallation(datasetInfo, atonNode));
                break;
            case "buoy_isolated_danger":
                jaxbElement = this.s125GMLFactory.createS125BuoyIsolatedDanger(this.generateBuoyIsolatedDanger(datasetInfo, atonNode));
                break;
            case "buoy_safe_water":
                jaxbElement = this.s125GMLFactory.createS125BuoySafeWater(this.generateBuoySafeWater(datasetInfo, atonNode));
                break;
            case "buoy_special_purpose":
                jaxbElement = this.s125GMLFactory.createS125BuoySpecialPurposeGeneral(this.generateBuoySpecialPurpose(datasetInfo, atonNode));
                break;
            case "landmark":
                jaxbElement = this.s125GMLFactory.createS125Landmark(this.generateLandmark(datasetInfo, atonNode));
                break;
            case "light_major": case "light_minor":
                jaxbElement = this.s125GMLFactory.createS125Lighthouse(this.generateLighthouse(datasetInfo, atonNode));
                break;
            case "light_vessel":
                jaxbElement = this.s125GMLFactory.createS125LightVessel(this.generateLightVessel(datasetInfo, atonNode));
                break;
            case "virtual_aton":
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
        S125BeaconCardinalType member = new S125BeaconCardinalType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBeaconShape(Optional.of("seamark:beacon_cardinal:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:beacon_cardinal:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:beacon_cardinal:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of("seamark:beacon_cardinal:height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:beacon_cardinal:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of("seamark:beacon_cardinal:nature_or_construction")
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
        member.setRadarConspicuous(Optional.of("seamark:beacon_cardinal:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:beacon_cardinal:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:beacon_cardinal:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfCardinalMark(Optional.of("seamark:beacon_cardinal:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfCardinalMark)
                .orElse(null));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BeaconLateralType member = new S125BeaconLateralType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBeaconShape(Optional.of("seamark:beacon_lateral:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:beacon_lateral:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:beacon_lateral:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of("seamark:beacon_lateral:height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:beacon_lateral:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of("seamark:beacon_lateral:nature_or_construction")
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
        member.setRadarConspicuous(Optional.of("seamark:beacon_lateral:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:beacon_lateral:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:beacon_lateral:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfLateralMark(Optional.of("seamark:beacon_lateral:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfLateralMark)
                .orElse(null));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BeaconIsolatedDangerType member = new S125BeaconIsolatedDangerType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBeaconShape(Optional.of("seamark:beacon_isolated_danger:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:beacon_isolated_danger:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:beacon_isolated_danger:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of("seamark:beacon_isolated_danger:height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:beacon_isolated_danger:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of("seamark:beacon_isolated_danger:nature_or_construction")
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
        member.setRadarConspicuous(Optional.of("seamark:beacon_isolated_danger:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:beacon_isolated_danger:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:beacon_isolated_danger:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BeaconSafeWaterType member = new S125BeaconSafeWaterType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBeaconShape(Optional.of("seamark:beacon_safe_water:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:beacon_safe_water:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:beacon_safe_water:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of("seamark:beacon_safe_water:height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:beacon_safe_water:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of("seamark:beacon_safe_water:nature_or_construction")
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
        member.setRadarConspicuous(Optional.of("seamark:beacon_safe_water:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:beacon_safe_water:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:beacon_safe_water:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BeaconSpecialPurposeGeneralType member = new S125BeaconSpecialPurposeGeneralType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBeaconShape(Optional.of("seamark:beacon_special_purpose:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBeaconShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:beacon_special_purpose:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:beacon_special_purpose:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setHeight(Optional.of("seamark:beacon_special_purpose:height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:beacon_special_purpose:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfConstructions().add(Optional.of("seamark:beacon_special_purpose:nature_or_construction")
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
        member.setRadarConspicuous(Optional.of("seamark:beacon_special_purpose:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:beacon_special_purpose:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:beacon_special_purpose:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.getCategoryOfSpecialPurposeMarks().add(Optional.of("seamark:beacon_special_purpose:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfSpecialPurposeMark)
                .orElse(null));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BuoyCardinalType member = new S125BuoyCardinalType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBuoyShape(Optional.of("seamark:buoy_cardinal:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:buoy_cardinal:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:buoy_cardinal:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:buoy_cardinal:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of("seamark:buoy_cardinal:nature_or_construction")
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
        member.setRadarConspicious(Optional.of("seamark:buoy_cardinal:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:buoy_cardinal:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfCardinalMark(Optional.of("seamark:buoy_cardinal:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfCardinalMark)
                .orElse(null));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BuoyLateralType member = new S125BuoyLateralType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBuoyShape(Optional.of("seamark:buoy_lateral:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:buoy_lateral:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:buoy_lateral:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:buoy_lateral:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of("seamark:buoy_lateral:nature_or_construction")
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
        member.setRadarConspicious(Optional.of("seamark:buoy_lateral:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:buoy_lateral:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfLateralMark(Optional.of("seamark:buoy_lateral:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfLateralMark)
                .orElse(null));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BuoyInstallationType member = new S125BuoyInstallationType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBuoyShape(Optional.of("seamark:buoy_installation:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:buoy_installation:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:buoy_installation:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:buoy_installation:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of("seamark:buoy_installation:nature_or_construction")
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
        member.setRadarConspicious(Optional.of("seamark:buoy_installation:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:buoy_installation:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.setCategoryOfInstallationBuoy(Optional.of("seamark:buoy_installation:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfInstallationBuoy)
                .orElse(null));

        // Now fix the geometry...
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125BuoyIsolatedDangerType member = new S125BuoyIsolatedDangerType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBuoyShape(Optional.of("seamark:buoy_isolated_danger:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:buoy_isolated_danger:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:buoy_isolated_danger:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:buoy_isolated_danger:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of("seamark:buoy_isolated_danger:nature_or_construction")
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
        member.setRadarConspicious(Optional.of("seamark:buoy_isolated_danger:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:buoy_isolated_danger:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

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
        S125BuoySafeWaterType member = new S125BuoySafeWaterType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBuoyShape(Optional.of("seamark:buoy_safe_water:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:buoy_safe_water:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:buoy_safe_water:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:buoy_safe_water:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of("seamark:buoy_safe_water:nature_or_construction")
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
        member.setRadarConspicious(Optional.of("seamark:buoy_safe_water:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:buoy_safe_water:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

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
        S125BuoySpecialPurposeGeneralType member = new S125BuoySpecialPurposeGeneralType();
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setBuoyShape(Optional.of("seamark:buoy_special_purpose:shape")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseBuoyShape)
                .orElse(null));
        member.getColours().addAll(Optional.of("seamark:buoy_special_purpose:colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:buoy_special_purpose:colour_pattern")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.setMarksNavigationalSystemOf(Optional.of("seamark:buoy_special_purpose:marks_navigational_system_of")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseMarksNavigationalSystemOf)
                .orElse(null));
        member.getNatureOfconstuctions().add(Optional.of("seamark:buoy_special_purpose:nature_or_construction")
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
        member.setRadarConspicious(Optional.of("seamark:buoy_special_purpose:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVerticalLength(Optional.of("seamark:buoy_special_purpose:vertical_length")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));
        member.getCategoryOfSpecialPurposeMarks().add(Optional.of("seamark:buoy_special_purpose:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseCategoryOfSpecialPurposeMark)
                .orElse(null));

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
        // Figure out the sector type
        final String tagKeyPrefix = atonNode.getTags()
                .stream()
                .filter(t -> t.getK().startsWith("seamark:light:"))
                .findFirst()
                .map(AtonTag::getK)
                .map(s -> s.substring(0, s.lastIndexOf(":")+1))
                .orElse("seamark:light");

        S125LightType member = new S125LightType();
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setTextualDescriptionInNationalLanguage(String.format("Light %s", Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
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

        // Now fix the geometry... from a point to a curve???
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);
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
        // Figure out the sector type
        final String tagKeyPrefix = atonNode.getTags()
                .stream()
                .filter(t -> t.getK().startsWith("seamark:light:"))
                .findFirst()
                .map(AtonTag::getK)
                .map(s -> s.substring(0, s.lastIndexOf(":")+1))
                .orElse("seamark:light");

        S125LighthouseType member = new S125LighthouseType();
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setTextualDescriptionInNationalLanguage(String.format("Light %s", Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.getFunctions().addAll(Optional.of(tagKeyPrefix+"function")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseFunction))
                .orElse(Collections.emptyList()));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setHeight(Optional.of(tagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setRadarConspicuous(Optional.of("seamark:light_vessel:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:light_vessel:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry... from a point to a curve???
        _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty pointCurveSurfacePropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty();
        pointCurveSurfacePropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointCurveSurfacePropertyExt);
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
        // Figure out the sector type
        final String tagKeyPrefix = atonNode.getTags()
                .stream()
                .filter(t -> t.getK().startsWith("seamark:light:"))
                .findFirst()
                .map(AtonTag::getK)
                .map(s -> s.substring(0, s.lastIndexOf(":")+1))
                .orElse("seamark:light");

        S125LighthouseType member = new S125LighthouseType();
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setTextualDescriptionInNationalLanguage(String.format("Light %s", Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
        member.getColours().addAll(Optional.of(tagKeyPrefix+"colour")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of(tagKeyPrefix+"category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColourPattern))
                .orElse(Collections.emptyList()));
        member.getFunctions().addAll(Optional.of(tagKeyPrefix+"function")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseFunction))
                .orElse(Collections.emptyList()));
        member.setObjectName(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setObjectNameInNationalLanguage(Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setHeight(Optional.of(tagKeyPrefix+"height")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(BigDecimal::new)
                .orElse(null));
        member.setRadarConspicuous(Optional.of("seamark:light_vessel:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:light_vessel:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry... from a point to a curve???
        _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty pointCurveSurfacePropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointCurveSurfaceProperty();
        pointCurveSurfacePropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointCurveSurfacePropertyExt);
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
        S125LightVesselType member = new S125LightVesselType();
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setTextualDescriptionInNationalLanguage(String.format("Light Vessel %s", Optional.of("seamark:name")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse("Unknown")));
        member.getColours().addAll(Optional.of("seamark:light:colours")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseColour))
                .orElse(Collections.emptyList()));
        member.getColourPatterns().addAll(Optional.of("seamark:light:colour_pattern")
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
        member.setRadarConspicuous(Optional.of("seamark:light_vessel:radar_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseRadarConspicuous)
                .orElse(null));
        member.setVisuallyConspicuous(Optional.of("seamark:light_vessel:visually_conspicuous")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(S125EnumParser::parseVisuallyConspicuous)
                .orElse(null));
        member.getStatuses().addAll(Optional.of("seamark:status")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(t -> S125EnumParser.splitAndParse(t, S125EnumParser::parseStatus))
                .orElse(Collections.emptyList()));

        // Now fix the geometry... from a point to a curve???
        _int.iho.s100.gml.base._1_0_Ext.PointProperty pointPropertyExt = new _int.iho.s100.gml.base._1_0_Ext.PointProperty();
        pointPropertyExt.setPointProperty(this.generatePointProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(pointPropertyExt);

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
        S125VirtualAISAidToNavigationType member = new S125VirtualAISAidToNavigationType();
        member.setId(this.generateId());
        member.setIdCode(atonNode.getAtonUid());
        FeatureObjectIdentifier featureObjectIdentifier = new FeatureObjectIdentifier();
        featureObjectIdentifier.setAgency(datasetInfo.getAgency());
        member.setFeatureObjectIdentifier(featureObjectIdentifier);
        member.setAtonNumber(Optional.of("mrn")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .orElse(null));
        member.setMMSICode(Optional.of("seamark:virtual_aton:mmsi")
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
        member.setVirtualAISAidToNavigationType(Optional.of("seamark:virtual_aton:category")
                .map(atonNode::getTag)
                .map(AtonTag::getV)
                .map(v -> v.replace(" ", "_"))
                .map(v -> {try {return S125VirtualAISAidToNavigationPurposeType.fromValue(v);} catch (Exception ex) {return null;}})
                .orElse(S125VirtualAISAidToNavigationPurposeType.SPECIAL_PURPOSE));

        // Now fix the geometry... from a point to a curve???
        _int.iho.s100.gml.base._1_0_Ext.CurveProperty curvePropertyExt = new _int.iho.s100.gml.base._1_0_Ext.CurveProperty();
        curvePropertyExt.setCurveProperty(this.generateCurveProperty(Arrays.asList(atonNode.getLat(), atonNode.getLon())));
        member.setGeometry(curvePropertyExt);

        // And return the populated member
        return member;
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
        posList.getValues().addAll(coords);

        // Populate the elements
        linearRingType.setPosList(posList);
        abstractRingPropertyType.setAbstractRing(this.opengisGMLFactory.createLinearRing(linearRingType));
        polygonPatchType.setExterior(abstractRingPropertyType);
        patches.getAbstractSurfacePatches().add(this.opengisGMLFactory.createPolygonPatch(polygonPatchType));
        surfaceType.setPatches(patches);
        surfaceType.setId(this.generateId());
        surfaceProperty.setAbstractSurface(this.opengisGMLFactory.createSurface(surfaceType));

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
        posList.getValues().addAll(coords);
        
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
        pos.getValues().addAll(coords);

        // Populate the elements
        pointType.setPos(pos);
        pointType.setId(this.generateId());
        pointProperty.setPoint(pointType);

        // And return the output
        return pointProperty;
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
