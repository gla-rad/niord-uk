package org.niord.s125.utils;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * The S-125 Utilities Class.
 *
 * A utility class some static helper methods to be used anywhere the S-125
 * module.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class XmlUtils {

    /**
     * An string processor to try and prettify the input XML into something
     * more easily readable.
     *
     * Arghh, for some insane reason, this function does not work properly :-(
     *
     * @param input     The XML string input
     * @return The prettified output
     */
    public static String xmlPrettyPrint(String input) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, xmlOutput);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    /**
     * An string processor to try and minity the input XML into something
     * more compact.
     *
     * @param input     The XML string input
     * @return The minified output
     */
    public static String xmlMinifiedPrint(String input) {
        try {
            BufferedReader br = new BufferedReader(new StringReader(input));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while((line=br.readLine())!= null){
                stringBuilder.append(line.trim());
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

}
