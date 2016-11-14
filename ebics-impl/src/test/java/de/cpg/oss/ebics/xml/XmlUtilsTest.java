package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.utils.IOUtils;
import org.junit.Test;

public class XmlUtilsTest {

    @Test
    public void testValidateUnsecuredRequest() throws Exception {
        XmlUtils.validate(IOUtils.read(XmlUtilsTest.class.getResourceAsStream("/ebicsUnsecuredRequest.xml")));
    }
}
