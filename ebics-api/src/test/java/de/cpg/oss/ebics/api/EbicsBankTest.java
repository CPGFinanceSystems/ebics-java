package de.cpg.oss.ebics.api;

import org.junit.Test;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class EbicsBankTest {

    @Test
    public void testEquals() throws Exception {
        final OffsetDateTime creationDateTime = OffsetDateTime.now();
        final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        final byte[] digest = MessageDigest.getInstance("SHA-256").digest(keyPair.getPublic().getEncoded());

        final EbicsBank first = EbicsBank.builder()
                .supportedOrderTypes(Collections.singleton(OrderType.AEA.name()))
                .uri(URI.create("http://example.com"))
                .authenticationKey(
                        EbicsAuthenticationKey.builder()
                                .creationTime(creationDateTime)
                                .publicKey(keyPair.getPublic())
                                .privateKey(keyPair.getPrivate())
                                .digest(digest)
                                .version(AuthenticationVersion.X002)
                                .build())
                .hostId("HOSTID")
                .name("Testbank")
                .supportedEbicsVersions(Collections.singleton(EbicsVersion.H004.name()))
                .build();

        final EbicsBank second = EbicsBank.builder()
                .supportedOrderTypes(Collections.singleton(OrderType.AEA.name()))
                .uri(URI.create("http://example.com"))
                .authenticationKey(
                        EbicsAuthenticationKey.builder()
                                .creationTime(creationDateTime)
                                .publicKey(keyPair.getPublic())
                                .privateKey(keyPair.getPrivate())
                                .digest(digest)
                                .version(AuthenticationVersion.X002)
                                .build())
                .hostId("HOSTID")
                .name("Testbank")
                .supportedEbicsVersions(Collections.singleton(EbicsVersion.H004.name()))
                .build();

        assertThat(first).isNotSameAs(second);
        assertThat(first).isEqualTo(second);
    }
}
