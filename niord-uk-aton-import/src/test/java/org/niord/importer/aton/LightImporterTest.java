/*
 * Copyright 2016 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.niord.importer.aton;

import org.junit.Assert;
import org.junit.Test;
import org.niord.importer.aton.batch.*;

import java.util.Arrays;
import java.util.regex.Matcher;

/**
 * Test light import functionality
 */
public class LightImporterTest {

    String[] LIGHT_CHARATERISTICS = {
            "Mo(U)15s",
            "Fl(2+1)W.10s",
            "Iso.WRG.4s",
            "Al Fl.WR.4s",
            "Fl.G.3s",
            "F.R",
            "VQ+LFl.R",
            "Oc.WRG.5s",
            "2 Oc.W.R G.1,5s",
            "Q(6)+LFl W 15s",
            "Fl G 2s"
    };

    String[] FOG_SIGNALS = {
            "HORN(3)30s   (2+2+2+2+2+20)",
            "SIREN(1)30s   (5+25)",
            "BELL.15s   (2,5+12,5)",
            "HORN   MO(U)30s   (0,75+1+0,75+1+2,5+24)",
            "HORN",
            "Horn(2)60s (5+5+5+45)",
            "Horn (2) 60s",
            "Horn (2) 30s"

    };

    String[] DESIGN_CODES = {
            "2S5NC/B",
            "1S7EC/RA",
            "2S5EC",
            "+1S7SC/R",
            "2S1SWSP/B",
            "1S10SM",
            "NS1SL",
            "1S9SC-AIS/R/MH",
            "3S4.5NC",
            "4B4SL"

    };

    @Test
    public void testLightFormat() throws Exception {
        Arrays.stream(LIGHT_CHARATERISTICS).forEach(c -> {
            Matcher m = UkLightParser.LIGHT_CHARACTER_FORMAT.matcher(c);
            System.out.println("===== " + c + "=====");
            if (m.find()) {
                try {
                    if (m.group("multiple") != null) System.out.println("\tmultiple " + m.group("multiple"));
                    if (m.group("phase") != null) System.out.println("\tphase " + m.group("phase"));
                    if (m.group("group") != null) System.out.println("\tgroup " + m.group("group"));
                    if (m.group("additional") != null) System.out.println("\tadditional " + m.group("additional"));
                    if (m.group("colors") != null) System.out.println("\tcolors " + m.group("colors"));
                    if (m.group("period") != null) System.out.println("\tperiod " + m.group("period"));
                } catch (Exception e) {
                    System.out.println("\t-> " + e.getMessage());
                }
            }
        });
    }

    @Test
    public void testLightParsing() throws Exception {
        Arrays.stream(LIGHT_CHARATERISTICS).forEach(c -> {
            LightSeamark light = UkLightParser.newInstance();
            UkLightParser.parseLightCharacteristics(light, c);
            Assert.assertTrue(light.isValid());

            System.out.println("===== " + c + " =====");
            light.toOsm().forEach(t -> System.out.printf("<tag k='%s' v='%s'/>%n", t.getK(), t.getV()));
        });
    }

    @Test
    public void testFogSignalFormat() throws Exception {
        Arrays.stream(FOG_SIGNALS).forEach(f -> {
            Matcher m = UkFogSignalParser.FOG_SIGNAL_FORMAT.matcher(f);
            System.out.println("===== " + f + " =====");
            if (m.find()) {
                try {
                    if (m.group("category") != null) System.out.println("\tcategory " + m.group("category"));
                    if (m.group("morse") != null) System.out.println("\tmorse " + m.group("morse"));
                    if (m.group("group") != null) System.out.println("\tgroup " + m.group("group"));
                    if (m.group("period") != null) System.out.println("\tperiod " + m.group("period"));
                } catch (Exception e) {
                    System.out.println("\t-> " + e.getMessage());
                }
            }
        });
    }

    @Test
    public void testFogSignalParsing() throws Exception {
        Arrays.stream(FOG_SIGNALS).forEach(f -> {
            FogSignalSeamark fogSignal = UkFogSignalParser.newInstance();
            UkFogSignalParser.parseFogSignal(fogSignal, f);
            Assert.assertTrue(fogSignal.isValid());

            System.out.println("===== " + f + "=====");
            fogSignal.toOsm().forEach(t -> System.out.printf("<tag k='%s' v='%s'/>%n", t.getK(), t.getV()));
        });
    }

    @Test
    public void testDesignCodeFormat() throws Exception {
        Arrays.stream(DESIGN_CODES).forEach(f -> {
            Matcher m = UkDesignCodeParser.DESIGN_CODE_FORMAT.matcher(f);
            System.out.println("===== " + f + " =====");
            if (m.find()) {
                try {
                    if (m.group("glatype") != null) System.out.println("\tglatype " + m.group("glatype"));
                    if (m.group("power") != null) System.out.println("\tpower " + m.group("power"));
                    if (m.group("range") != null) System.out.println("\trange " + m.group("range"));
                    if (m.group("unlit") != null) System.out.println("\tunlit " + m.group("unlit"));
                    if (m.group("type") != null) System.out.println("\ttype " + m.group("type"));
                    if (m.group("shape") != null) System.out.println("\tshape " + m.group("shape"));
                    if (m.group("aids") != null) System.out.println("\taids " + m.group("aids"));
                } catch (Exception e) {
                    System.out.println("\t-> " + e.getMessage());
                }
            }
        });
    }

    @Test
    public void testDesignCodeParsing() throws Exception {
        Arrays.stream(DESIGN_CODES).forEach(d -> {
            DesignCode designCode = UkDesignCodeParser.newInstance();

            System.out.println("===== " + d + " =====");
            UkDesignCodeParser.parseDesignCode(designCode, d);
            Assert.assertTrue(designCode.isValid());
            System.out.println(designCode);
        });
    }

}
