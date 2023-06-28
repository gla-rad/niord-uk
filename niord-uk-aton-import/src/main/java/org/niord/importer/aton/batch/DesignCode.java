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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class DesignCode {

    /*************************/
    /** Enums               **/
    /*************************/

    /** Power Types **/
    public enum PowerType {
        SOLAR("S"),
        BATTERY("B"),
        GENERATOR("G");

        private String code;

        PowerType(String code) {
            this.code = code;
        }

        public static PowerType valueOfPowerType(String code) {
            return Arrays.stream(PowerType.values())
                    .filter(v -> Objects.equals(v.code, code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** Types **/
    public enum Type {
        PORT_PILLAR("PP","port"),
        PORT_LATERAL("PL","port"),
        STARBOARD_LATERAL("SL","starboard"),
        NORTH_CARDINAL("NC","north"),
        SOUTH_CARDINAL("SC","south"),
        WEST_CARDINAL("WC","west"),
        EAST_CARDINAL("EC","east"),
        SAFE_WATER("SW",""),
        SPECIAL_MARK("SM","warning"),
        ISOLATED_DANGER("ID",""),
        INSTALLATION("IN","floating");

        private String code;
        private String category;

        Type(String code, String category) {
            this.code = code;
            this.category = category;
        }

        public String getCode() {
            return code;
        }

        public String getCategory() {
            return category;
        }

        public String getBuoyType() {
            switch(this) {
                case PORT_PILLAR:
                case PORT_LATERAL:
                case STARBOARD_LATERAL:
                    return "buoy_lateral";
                case EAST_CARDINAL:
                case WEST_CARDINAL:
                case NORTH_CARDINAL:
                case SOUTH_CARDINAL:
                    return "buoy_cardinal";
                case SAFE_WATER:
                    return "buoy_safe_water";
                case SPECIAL_MARK:
                    return "buoy_special_purpose";
                case INSTALLATION:
                    return "buoy_installation";
                case ISOLATED_DANGER:
                    return "buoy_isolated_danger";
                default:
                    return null;
            }
        }

        public String getBeaconType() {
            switch(this) {
                case PORT_PILLAR:
                case PORT_LATERAL:
                case STARBOARD_LATERAL:
                    return "beacon_lateral";
                case EAST_CARDINAL:
                case WEST_CARDINAL:
                case NORTH_CARDINAL:
                case SOUTH_CARDINAL:
                    return "beacon_cardinal";
                case SAFE_WATER:
                    return "beacon_safe_water";
                case SPECIAL_MARK:
                    return "beacon_special_purpose";
                case INSTALLATION:
                case ISOLATED_DANGER:
                    return "beacon_isolated_danger";
                default:
                    return null;
            }
        }

        public String getColour() {
            switch(this) {
                case PORT_PILLAR:
                case PORT_LATERAL:
                    return "red";
                case STARBOARD_LATERAL:
                    return "green";
                case EAST_CARDINAL:
                    return "black;yellow;black";
                case WEST_CARDINAL:
                    return "yellow;black;yellow";
                case NORTH_CARDINAL:
                    return "black;yellow";
                case SOUTH_CARDINAL:
                    return "yellow;black";
                case SAFE_WATER:
                    return "red;white;red;white";
                case SPECIAL_MARK:
                    return "yellow";
                case INSTALLATION:
                    return "white";
                case ISOLATED_DANGER:
                    return "black;red;black";
                default:
                    return null;
            }
        }

        public String getColourPattern() {
            // For one colour don't bother
            if(this.getColour().split(";").length <= 1) {
                return "";
            }

            // Otherwise check by type
            switch(this) {
                case PORT_PILLAR:
                case PORT_LATERAL:
                case STARBOARD_LATERAL:
                case EAST_CARDINAL:
                case WEST_CARDINAL:
                case NORTH_CARDINAL:
                case SOUTH_CARDINAL:
                case ISOLATED_DANGER:
                    return "stripes";
                case SAFE_WATER:
                case SPECIAL_MARK:
                case INSTALLATION:
                    return "vertical";
                default:
                    return null;
            }
        }

        public static Type valueOfType(String code) {
            return Arrays.stream(Type.values())
                    .filter(v -> Objects.equals(v.code, code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** Shape **/
    public enum Shape {
        SPHERICAL("SP"),
        CONICAL("CO"), // Laterals are conicals
        CAN("CA"),
        PILLAR("PP"), // Cardinals are pillars
        SPAR("SR"),
        BARREL("BA");

        private String code;

        Shape(String code) {
            this.code = code;
        }

        public static Shape valueOfShape(String code) {
            return Arrays.stream(Shape.values())
                    .filter(v -> Objects.equals(v.code, code))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** Additional Aids **/
    public enum Aids {
        BELL("B"),
        HORN("H"),
        AIS("A"),
        RACON("R"),
        POSITION_MONITORED_ONLY("X"),
        SYNC("Sy");

        private String code;

        Aids(String code) {
            this.code = code;
        }

        public static Aids valueOfAids(String code) {
            return Arrays.stream(Aids.values())
                    .filter(v -> Objects.equals(v.code, code))
                    .findFirst()
                    .orElse(null);
        }
    }
    /*************************/
    /** Variables           **/
    /*************************/

    private String glaType;
    private boolean unlit;
    private PowerType powerType;
    private Double range;
    private Type type;
    private Shape shape;
    private Collection<Aids> aids;

    /*************************/
    /** Functions           **/
    /*************************/

    /** Checks if the fog signal is valid */
    public boolean isValid() {
        return glaType != null && (unlit || (powerType != null && range != null)) && type != null;
    }


    /** Overrides the design code toString function */
    @Override
    public String toString() {
        return String.format("GLA-Type: %s, " +
                        "Unlit: %s, " +
                        "Power: %s, " +
                        "Range: %f, " +
                        "Type: %s, " +
                        "Shape: %s, " +
                        "Aids: %s, ",
                this.glaType, this.unlit, this.powerType, this.range, this.type, this.shape, this.aids);
    }

    /*************************/
    /** Getters and Setters **/
    /*************************/

    public String getGlaType() {
        return glaType;
    }

    public void setGlaType(String glaType) {
        this.glaType = glaType;
    }

    public boolean isUnlit() {
        return unlit;
    }

    public void setUnlit(boolean unlit) {
        this.unlit = unlit;
    }

    public PowerType getPowerType() {
        return powerType;
    }

    public void setPowerType(PowerType powerType) {
        this.powerType = powerType;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public Collection<Aids> getAids() {
        return aids;
    }

    public void setAids(Collection<Aids> aids) {
        this.aids = aids;
    }
}
