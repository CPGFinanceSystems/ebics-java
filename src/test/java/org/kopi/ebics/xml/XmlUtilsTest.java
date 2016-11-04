package org.kopi.ebics.xml;

import org.junit.Test;

public class XmlUtilsTest {

    @Test
    public void testValidateUnsecuredRequest() throws Exception {
        XmlUtils.validate(XmlUtilsTest.class.getResourceAsStream("/ebicsUnsecuredRequest.xml"));
    }
}
