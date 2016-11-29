package de.cpg.oss.ebics.utils;

import de.cpg.oss.ebics.api.EbicsRsaKey;
import de.cpg.oss.ebics.api.SignatureVersion;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateUtilTest {

    @Test
    public void testKeyInfo() throws Exception {
        final EbicsRsaKey rsaKey = CryptoUtilTest.createSignatureKey(SignatureVersion.A006);
        final KeyInfo keyInfo = TemplateUtil.keyInfo(rsaKey);
        final Pattern hexPattern = Pattern.compile("^([A-F0-9]{2} )+[A-F0-9]{2}$");

        final String[] digestHexLines = keyInfo.getDigestHex().split("\\n");
        assertThat(digestHexLines).hasSize(2);
        for (final String hex : digestHexLines) {
            assertThat(hex).matches(hexPattern);
        }
        assertThat(keyInfo.getExponentBits()).isLessThan(keyInfo.getModulusBits());
        assertThat(keyInfo.getExponentHex()).matches(hexPattern);
        assertThat(keyInfo.getModulusBits()).isEqualTo(KeyUtil.EBICS_KEY_SIZE);
        final String[] modulusHexLines = keyInfo.getModulusHex().split("\\n");
        assertThat(modulusHexLines).hasSize(KeyUtil.EBICS_KEY_SIZE / 8 / 16);
        for (final String hex : modulusHexLines) {
            assertThat(hex).matches(hexPattern);
        }
    }
}
