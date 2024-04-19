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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A testing call for the S-125 XML Utility.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class XmlUtilsTest {

    /**
     * Test that the utility will throw errors for null inputs.
     */
    @Test(expected = RuntimeException.class)
    public void testXmlPrettyPrintNull() {
        XmlUtils.xmlPrettyPrint(null, 0);
    }

    /**
     * Test that the utility will throw errors for empty inputs.
     */
    @Test(expected = RuntimeException.class)
    public void testXmlPrettyPrintEmpty() {
        XmlUtils.xmlPrettyPrint("", 0);
    }

    /**
     * Test that the utility will throw errors for invalid inputs.
     */
    @Test(expected = RuntimeException.class)
    public void testXmlPrettyPrintInvalid() {
        XmlUtils.xmlPrettyPrint("invalid", 0);
    }

    /**
     * Test that we can use the utility to prettify XML with 0 indentation.
     */
    @Test
    public void testXmlPrettyPrint0() {
        assertEquals("<xml/>", XmlUtils.xmlPrettyPrint("<xml></xml>", 0));
        assertEquals("<xml><test/></xml>", XmlUtils.xmlPrettyPrint("<xml><test></test></xml>", 0));
        assertEquals("<xml><test>test</test></xml>", XmlUtils.xmlPrettyPrint("<xml><test>test</test></xml>", 0));
    }

    /**
     * Test that we can use the utility to prettify XML with 2 indentation.
     */
    @Test
    public void testXmlPrettyPrint2() {
        assertEquals("<xml/>"
                +System.lineSeparator(),
                XmlUtils.xmlPrettyPrint("<xml></xml>", 2)
        );
        assertEquals("<xml>" + System.lineSeparator() +
                "  <test/>" + System.lineSeparator() +
                "</xml>" + System.lineSeparator(), XmlUtils.xmlPrettyPrint("<xml><test></test></xml>", 2));
        assertEquals("<xml>" + System.lineSeparator() +
                "  <test>test</test>" + System.lineSeparator() +
                "</xml>" + System.lineSeparator(), XmlUtils.xmlPrettyPrint("<xml><test>test</test></xml>", 2));
    }

    /**
     * Test that we can use the utility to prettify XML with 4 indentation.
     */
    @Test
    public void testXmlPrettyPrint4() {
        assertEquals("<xml/>"+System.lineSeparator(), XmlUtils.xmlPrettyPrint("<xml></xml>", 4));
        assertEquals("<xml>" + System.lineSeparator() +
                "    <test/>" + System.lineSeparator() +
                "</xml>" + System.lineSeparator(), XmlUtils.xmlPrettyPrint("<xml><test></test></xml>", 4));
        assertEquals("<xml>" + System.lineSeparator() +
                "    <test>test</test>" + System.lineSeparator() +
                "</xml>" + System.lineSeparator(), XmlUtils.xmlPrettyPrint("<xml><test>test</test></xml>", 4));
    }

}