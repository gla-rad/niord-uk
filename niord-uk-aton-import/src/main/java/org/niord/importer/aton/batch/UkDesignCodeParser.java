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

package org.niord.importer.aton.batch;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the fog design codes of the UK AtoN light list.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class UkDesignCodeParser {

    public static Pattern DESIGN_CODE_FORMAT = Pattern.compile(
            "^" +
                    "(?<glatype>\\+?[\\dN])" +
                    "((?<power>[S])(?<range>\\d+(\\.\\d+)?)|(?<unlit>UL))" +
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
                designCode.setRange(Double.valueOf(rangeSpec.trim()));
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
                    case PORT_PILLAR:
                        designCode.setShape(DesignCode.Shape.PILLAR);
                        break;
                    case PORT_LATERAL:
                    case STARBOARD_LATERAL:
                        designCode.setShape(DesignCode.Shape.CONICAL);
                        break;
                    default:
                        designCode.setShape(DesignCode.Shape.CAN);
                        break;
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
