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

import org.niord.s125.models.S125AtonTypes;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/** Each AtoN will have a combination of these values in the "Type" field **/
public enum AtonType {
    LIGHT("LIGHT", new S125AtonTypes[]{S125AtonTypes.LIGHT}),                           // 0: Light
    SECTOR_LIGHT("SECTOR LIGHT", new S125AtonTypes[]{S125AtonTypes.LIGHT}),             // 1: Sector Light (Basically a Light)
    LIGHTHOUSE("LIGHTHOUSE", new S125AtonTypes[]{S125AtonTypes.LIGHTHOUSE}),            // 2: Lighthouse
    LIGHT_VESSEL("LIGHT VESSEL", new S125AtonTypes[]{S125AtonTypes.LIGHT_VESSEL}),      // 3: Light Vessel
    LIGHT_FLOAT("LIGHT FLOAT", new S125AtonTypes[]{S125AtonTypes.LIGHT_VESSEL}),        // 4: Light Float (Basically a Light Vessel)
    BUOY("BUOY", new S125AtonTypes[]{                                                   // 5: Buoy
            S125AtonTypes.LATERAL_BUOY,
            S125AtonTypes.CARDINAL_BUOY,
            S125AtonTypes.INSTALLATION_BUOY,
            S125AtonTypes.ISOLATED_DANGER_BUOY,
            S125AtonTypes.SAFE_WATER_BUOY,
            S125AtonTypes.SPECIAL_PURPOSE_BUOY
    }),
    BEACON("BEACON", new S125AtonTypes[]{                                               // 6: BEACON
            S125AtonTypes.LATERAL_BEACON,
            S125AtonTypes.CARDINAL_BEACON,
            S125AtonTypes.ISOLATED_DANGER_BEACON,
            S125AtonTypes.SAFE_WATER_BEACON,
            S125AtonTypes.SPECIAL_PURPOSE_BEACON
    }),
    AIS("AIS", new S125AtonTypes[]{S125AtonTypes.RADIO_STATION}),                       // 7: AIS
    DGPS("DGPS", new S125AtonTypes[]{S125AtonTypes.RADIO_STATION}),                     // 8: DGPS
    LORAN("LORAN", new S125AtonTypes[]{S125AtonTypes.RADIO_STATION}),                   // 9: E-LORAN
    RACON("RACON", new S125AtonTypes[]{S125AtonTypes.RADIO_STATION}),                   // 10: RACON
    FOG_SIGNAL("FOG SIGNAL", new S125AtonTypes[]{S125AtonTypes.FOG_SIGNAL}),            // 11: Fog Signal
    VIRTUAL_ATON("VIRTUAL ATON", new S125AtonTypes[]{S125AtonTypes.VIRTUAL_ATON});             // 12: Virtual AtoN

    String type;
    S125AtonTypes[] s125AtonTypes;
    AtonType(String type, S125AtonTypes[] s125AtonTypes) {
        this.type = type;
        this.s125AtonTypes = s125AtonTypes;
    }

    static AtonType findByCode(String type) {
        return Optional.ofNullable(type)
                .map(String::toUpperCase)
                .map(str -> str.replaceAll("\\(.*\\)", ""))
                .map(String::trim)
                .map(str -> str.replaceAll("\\s", "_"))
                .map(AtonType::valueOf)
                .map(t -> t.isLighthouse() ? AtonType.LIGHTHOUSE : t)
                .map(t -> t.isVaton() ? AtonType.VIRTUAL_ATON : t)
                .orElse(null);
    }

    public boolean isLighthouse() {
        return this == LIGHT || this == SECTOR_LIGHT || this == LIGHTHOUSE;
    }

    public boolean isLightVessel() {
        return this == LIGHT_VESSEL || this == LIGHT_FLOAT;
    }

    public boolean isBuoy() {
        return this == BUOY;
    }

    public boolean isBeacon() {
        return this == BEACON;
    }

    public boolean isFogSignal() {
        return this == FOG_SIGNAL;
    }

    public boolean isRadio() {
         return this == DGPS || this == RACON;
    }

    public boolean isVaton() {
        return this == AIS || this == VIRTUAL_ATON;
    }

    public S125AtonTypes getS125AtonType(String subType) {
        // Sanity check
        if(this.s125AtonTypes == null) {
            return null;
        }
        // Otherwise search for a matching subtype of simply the first
        return Arrays.stream(this.s125AtonTypes)
                .filter(t -> Objects.isNull(subType) || Objects.equals(subType, t.getName()))
                .findFirst()
                .orElse(null);

    }
}
