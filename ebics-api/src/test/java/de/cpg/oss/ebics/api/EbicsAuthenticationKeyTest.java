package de.cpg.oss.ebics.api;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class EbicsAuthenticationKeyTest {

    @Test
    public void testEquals() throws Exception {
        final Instant creationDateTime = Instant.now();
        final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        final byte[] digest = MessageDigest.getInstance("SHA-256").digest(keyPair.getPublic().getEncoded());

        final EbicsAuthenticationKey first = EbicsAuthenticationKey.builder()
                .creationTime(creationDateTime)
                .publicKey(keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .digest(digest)
                .version(AuthenticationVersion.X002)
                .build();

        final EbicsAuthenticationKey second = EbicsAuthenticationKey.builder()
                .creationTime(creationDateTime)
                .publicKey(keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .digest(digest)
                .version(AuthenticationVersion.X002)
                .build();

        assertThat(first).isNotSameAs(second);
        assertThat(first).isEqualTo(second);
    }
}
