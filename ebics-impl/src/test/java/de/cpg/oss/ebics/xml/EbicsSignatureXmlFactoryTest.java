package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.TestUtil;
import org.junit.Test;

public class EbicsSignatureXmlFactoryTest {

    @Test
    public void testSignaturePubKeyOrderData() throws Exception {
        EbicsSignatureXmlFactory.signaturePubKeyOrderData(TestUtil.createEbicsSession());
    }
}
