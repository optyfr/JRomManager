package jrm.xml;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class XMLTools {
    static {
        System.setProperty("jdk.xml.entityExpansionLimit", "0");
        System.setProperty("jdk.xml.maxAttributeCount", "0");
        System.setProperty("jdk.xml.maxElementCount", "0");
        System.setProperty("jdk.xml.maxElementDepth", "0");
        System.setProperty("jdk.xml.maxEntityCount", "0");
        System.setProperty("jdk.xml.maxGeneralEntityCount", "0");
        System.setProperty("jdk.xml.maxGeneralEntitySizeLimit", "0");
        System.setProperty("jdk.xml.maxParameterEntityCount", "0");
        System.setProperty("jdk.xml.maxParameterEntitySizeLimit", "0");
        System.setProperty("jdk.xml.maxOccurLimit", "0");
        System.setProperty("jdk.xml.maxXMLNameLimit", "0");
        System.setProperty("jdk.xml.totalEntitySizeLimit", "0");
    }

    public static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        final var factory = SAXParserFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        final var parser = factory.newSAXParser();
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return parser;
    }
}