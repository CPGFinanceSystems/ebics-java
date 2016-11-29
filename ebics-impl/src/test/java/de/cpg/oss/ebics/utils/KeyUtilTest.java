package de.cpg.oss.ebics.utils;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyUtilTest {

    @Test
    public void testCreateRsaKeyPair() throws Exception {
        KeyUtil.createRsaKeyPair(4096);
    }

    @Test
    public void testGetKeyDigest() throws Exception {
        final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        final byte[] digest = KeyUtil.getKeyDigest(keyPair.getPublic());

        assertThat(digest).hasSize(256 / 8);
    }

    @Test
    public void testPublicKey() throws Exception {
        final RSAPublicKey publicKey = (RSAPublicKey) KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic();

        assertThat(KeyUtil.getPublicKey(
                publicKey.getModulus().toByteArray(),
                publicKey.getPublicExponent().toByteArray()))
                .isEqualTo(publicKey);
    }
}
