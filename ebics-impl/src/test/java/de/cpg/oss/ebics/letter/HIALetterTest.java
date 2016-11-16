package de.cpg.oss.ebics.letter;

import de.cpg.oss.ebics.api.EbicsSession;
import org.junit.Test;

import static de.cpg.oss.ebics.letter.INILetterTest.createEbicsSession;

public class HIALetterTest {

    @Test
    public void testCreate() throws Exception {
        final EbicsSession session = createEbicsSession();
        final HIALetter hiaLetter = new HIALetter();
        hiaLetter.create(session).close();
    }
}
