package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.TestUtil;
import de.cpg.oss.ebics.api.EbicsSession;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;

public class InitLetterTest {

    private static EbicsSession session;

    @Test
    public void testCreateINI() throws Exception {
        InitLetter.createINI(session, new FileOutputStream("target/test_iniletter.pdf"));
    }

    @Test
    public void testCreateHIA() throws Exception {
        InitLetter.createHIA(session, new FileOutputStream("target/test_hialetter.pdf"));
    }

    @BeforeClass
    public static void createEbicsSession() throws Exception {
        session = TestUtil.createEbicsSession();
    }
}
