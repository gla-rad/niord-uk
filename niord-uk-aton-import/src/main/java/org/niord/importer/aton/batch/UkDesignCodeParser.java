package org.niord.importer.aton.batch;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UkDesignCodeParser {

    public static Pattern DESIGN_CODE_FORMAT = Pattern.compile(
            "^" +
                    "(?<glatype>\\+?[\\dN])" +
                    "((?<power>[S])(?<range>\\d+)|(?<unlit>UL))" +
                    "(?<type>\\S\\S)" +
                    "(?<shape>[A-Z][A-Z])?" +
                    "(/?(?<aids>([ABHR]+)))?" +
                    ".*$",
            Pattern.CASE_INSENSITIVE
    );

    public static Pattern AIDS_FORMAT = Pattern.compile(
            "(?<aids>[ABHR])"
    );

    /**
     * No public initialization
     */
    private UkDesignCodeParser() {
    }

    /**
     * Creates and initializes a new instance
     *
     * @return the newly created light instance
     */
    public static DesignCode newInstance() {
        DesignCode designCode = new DesignCode();
        return designCode;
    }

    /**
     * Parses the design code string and updates the design code object.
     *
     * @param designCode        the design code to update
     * @param designCodeString  the design code string
     * @return the updated design code object
     */
    public static DesignCode parseDesignCode(DesignCode designCode, String designCodeString) {

        Matcher m = DESIGN_CODE_FORMAT.matcher(designCodeString);

        if (m.find()) {
            String glaTypeSpec = m.group("glatype");
            String powerSpec = m.group("power");
            String rangeSpec = m.group("range");
            String unlitSpec = m.group("unlit");
            String typeSpec = m.group("type");
            String shapeSpec = m.group("shape");
            String aidsSpec = m.group("aids");

            // GLA Type
            if (StringUtils.isNotBlank(glaTypeSpec)) {
                designCode.setGlaType(glaTypeSpec.trim());
            }

            // Power
            if (StringUtils.isNotBlank(powerSpec)) {
                designCode.setPowerType(DesignCode.PowerType.valueOfPowerType(powerSpec.trim()));
            }

            // Range
            if (StringUtils.isNotBlank(rangeSpec)) {
                designCode.setRange(Integer.valueOf(rangeSpec.trim()));
            }

            // Unlit
            if (StringUtils.isNotBlank(unlitSpec)) {
                designCode.setUnlit(true);
            }

            // Type
            if (StringUtils.isNotBlank(typeSpec)) {
                designCode.setType(DesignCode.Type.valueOfType(typeSpec.trim()));
            }

            // Shape
            if (StringUtils.isNotBlank(shapeSpec)) {
                designCode.setShape(DesignCode.Shape.valueOfShape(shapeSpec.trim()));
            } else {
                switch (designCode.getType()) {
                    case SOUTH_CARDINAL:
                    case NORTH_CARDINAL:
                    case WEST_CARDINAL:
                    case EAST_CARDINAL:
                        designCode.setShape(DesignCode.Shape.PILLAR);
                    case PORT_PILLAR:
                    case STARBOARD_LATERAL:
                        designCode.setShape(DesignCode.Shape.CONICAL);
                    default:
                        designCode.setShape(DesignCode.Shape.CAN);
                }
            }

            // Aids
            if (StringUtils.isNotBlank(aidsSpec)) {
                Matcher cm = AIDS_FORMAT.matcher(aidsSpec);
                designCode.setAids(new ArrayList<>());
                while (cm.find()) {
                    designCode.getAids().add(DesignCode.Aids.valueOfAids(cm.group("aids").trim()));
                }
            }
        }

        // Return the populated object
        return designCode;
    }
}
