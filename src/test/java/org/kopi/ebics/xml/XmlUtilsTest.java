package org.kopi.ebics.xml;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class XmlUtilsTest {

    @Test
    public void testValidateUnsecuredRequest() throws Exception {
        XmlUtils.validate(XmlUtilsTest.class.getResourceAsStream("/ebicsUnsecuredRequest.xml"));
    }

    @Test
    public void testXPath() throws Exception {
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = b.parse(XmlUtilsTest.class.getResourceAsStream("/ebicsUnsecuredRequest.xml"));

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xPath.evaluate("//*[@authenticate='true']", doc.getDocumentElement(), XPathConstants.NODESET);

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            System.out.println(node.getNodeName());
            for (int j = 0; j < node.getChildNodes().getLength(); ++j) {
                System.out.println("  " + node.getChildNodes().item(j).getNodeName());
            }
        }
    }
}
