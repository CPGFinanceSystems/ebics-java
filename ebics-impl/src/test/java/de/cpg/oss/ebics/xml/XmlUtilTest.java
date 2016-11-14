package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import org.junit.Test;

public class XmlUtilTest {

    @Test
    public void testValidateUnsecuredRequest() throws Exception {
        XmlUtil.validate(IOUtil.read(XmlUtilTest.class.getResourceAsStream("/ebicsUnsecuredRequest.xml")));
    }
}
