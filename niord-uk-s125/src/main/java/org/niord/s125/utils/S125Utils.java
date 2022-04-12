package org.niord.s125.utils;

import _int.iho.s100.gml.base._1_0.PointProperty;
import _int.iho.s100.gml.base._1_0.PointType;
import _int.iho.s125.gml._0.*;
import _net.opengis.gml.profiles.Pos;
import org.locationtech.jts.geom.Point;
import org.niord.core.aton.AtonNode;

import static org.niord.core.aton.AtonTag.TAG_ATON_TYPE;

public class S125Utils {

    /**
     * This is the entry method static function of the utility. It will examine
     * the provided AtoN Node from Niord and generate a standardised S-125
     * format messages based on the specifications specified by the IHO/IALA
     * NIPWG.
     *
     * @param atonNode      The Niord AtoN node object
     * @return The generated S-125 data message
     */
    public static S125AidsToNavigationType generateAidsToNavigation(AtonNode atonNode) {
        // First read the AtoN type information from the input
        String atonType = atonNode.getTagValue(TAG_ATON_TYPE);
        S125AidsToNavigationType s125AidsToNavigationType = null;
        // Handle each possible type, cause a different object should be created
        switch(atonType) {
            case "beacon_cardinal":
                s125AidsToNavigationType = generateBeaconCardinal(atonNode);
                break;
            case "beacon_lateral":
                s125AidsToNavigationType = generateBeaconLateral(atonNode);
                break;
            case "beacon_isolated_danger":
                s125AidsToNavigationType = generateBeaconIsolatedDanger(atonNode);
                break;
            case "beacon_safe_water":
                s125AidsToNavigationType = generateBeaconSafeWater(atonNode);
                break;
            case "beacon_special_purpose":
                s125AidsToNavigationType = generateBeaconSpecialPurpose(atonNode);
                break;
            case "buoy_cardinal":
                s125AidsToNavigationType = generateBuoyCardinal(atonNode);
                break;
            case "buoy_lateral":
                s125AidsToNavigationType = generateBuoyLateral(atonNode);
                break;
            case "buoy_installation":
                s125AidsToNavigationType = generateBuoyInstallation(atonNode);
                break;
            case "buoy_isolated_danger":
                s125AidsToNavigationType = generateBuoyIsolatedDanger(atonNode);
                break;
            case "buoy_safe_water":
                s125AidsToNavigationType = generateBuoySafeWater(atonNode);
                break;
            case "buoy_special_purpose":
                s125AidsToNavigationType = generateBuoySpecialPurpose(atonNode);
                break;
            case "light":
                s125AidsToNavigationType = generateLight(atonNode);
                break;
            case "light_vessel":
                s125AidsToNavigationType = generateLightVessel(atonNode);
                break;
            case "virtual_aton":
                s125AidsToNavigationType = generateVirtualAtoN(atonNode);
                break;
        }
        // And return what was generated
        return s125AidsToNavigationType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBeaconCardinal(AtonNode atonNode) {
        S125BeaconCardinalType beaconCardinalType = new S125BeaconCardinalType();
        return beaconCardinalType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBeaconLateral(AtonNode atonNode) {
        S125BeaconLateralType beaconLateralType = new S125BeaconLateralType();
        return  beaconLateralType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBeaconIsolatedDanger(AtonNode atonNode) {
        S125BeaconIsolatedDangerType beaconIsolatedDangerType = new S125BeaconIsolatedDangerType();
        return beaconIsolatedDangerType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBeaconSafeWater(AtonNode atonNode) {
        S125BeaconSafeWaterType beaconSafeWaterType = new S125BeaconSafeWaterType();
        return beaconSafeWaterType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBeaconSpecialPurpose(AtonNode atonNode) {
        S125BeaconSpecialPurposeGeneralType beaconSpecialPurposeGeneralType = new S125BeaconSpecialPurposeGeneralType();
        return beaconSpecialPurposeGeneralType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBuoyCardinal(AtonNode atonNode) {
        S125BuoyCardinalType buoyCardinalType = new S125BuoyCardinalType();
        return buoyCardinalType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBuoyLateral(AtonNode atonNode) {
        S125BuoyLateralType buoyLateralType = new S125BuoyLateralType();
        return buoyLateralType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBuoyInstallation(AtonNode atonNode) {
        S125BuoyInstallationType buoyInstallationType = new S125BuoyInstallationType();
        return buoyInstallationType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBuoyIsolatedDanger(AtonNode atonNode) {
        S125BuoyIsolatedDangerType buoyIsolatedDangerType = new S125BuoyIsolatedDangerType();
        return buoyIsolatedDangerType;
    }

    private static S125AidsToNavigationType generateBuoySafeWater(AtonNode atonNode) {
        S125BuoySafeWaterType buoySafeWaterType = new S125BuoySafeWaterType();
        return buoySafeWaterType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateBuoySpecialPurpose(AtonNode atonNode) {
        S125BuoySpecialPurposeGeneralType buoySpecialPurposeGeneralType = new S125BuoySpecialPurposeGeneralType();
        return buoySpecialPurposeGeneralType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateLight(AtonNode atonNode) {
        S125LightType lightType = new S125LightType();
        return lightType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateLightVessel(AtonNode atonNode) {
        S125LightVesselType lightVesselType = new S125LightVesselType();
        return lightVesselType;
    }

    /**
     *
     * @param atonNode
     * @return
     */
    private static S125AidsToNavigationType generateVirtualAtoN(AtonNode atonNode) {
        S125VirtualAISAidToNavigationType virtualAISAidToNavigationType = new S125VirtualAISAidToNavigationType();
        return virtualAISAidToNavigationType;
    }

    /**
     * Populates and return an S-125 point property based on a point geometry
     *
     * @param point     The point geometry
     * @return The populated point property
     */
    public static PointProperty generatePointProperty(Point point) {
        // Generate the elements
        PointProperty pointProperty = new PointProperty();
        PointType pointType = new PointType();
        Pos pos = new Pos();

        // Populate with the geometry data
        pos.setSrsName("EPSG:4326");
        pos.getValues().add(point.getY());
        pos.getValues().add(point.getX());

        // Populate the elements
        pointType.setPos(pos);
        pointProperty.setPoint(pointType);

        // And return the output
        return pointProperty;
    }

}
