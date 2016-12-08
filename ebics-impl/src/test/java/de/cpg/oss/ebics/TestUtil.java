package de.cpg.oss.ebics;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.utils.KeyUtil;

import java.net.URI;
import java.security.KeyPair;
import java.time.Instant;

public abstract class TestUtil {

    public static EbicsSession createEbicsSession() throws Exception {
        final EbicsConfiguration configuration = new EbicsConfiguration();
        final KeyPair signatureKey = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
        final KeyPair authenticationKey = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
        final KeyPair encryptionKey = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);

        return EbicsSession.builder()
                .configuration(configuration)
                .bank(EbicsBank.builder()
                        .hostId("HOSTID")
                        .name("Test Bank Name")
                        .uri(URI.create("https://bank.example.org"))
                        .build())
                .partner(EbicsPartner.builder()
                        .partnerId("PARTNERID")
                        .build())
                .user(EbicsUser.builder()
                        .name("User Name")
                        .userId("USERID")
                        .status(UserStatus.INITIALIZED)
                        .signatureKey(EbicsSignatureKey.builder()
                                .digest(KeyUtil.getKeyDigest(signatureKey.getPublic()))
                                .publicKey(signatureKey.getPublic())
                                .creationTime(Instant.now())
                                .version(configuration.getSignatureVersion())
                                .build())
                        .authenticationKey(EbicsAuthenticationKey.builder()
                                .digest(KeyUtil.getKeyDigest(authenticationKey.getPublic()))
                                .publicKey(authenticationKey.getPublic())
                                .creationTime(Instant.now())
                                .version(configuration.getAuthenticationVersion())
                                .build())
                        .encryptionKey(EbicsEncryptionKey.builder()
                                .digest(KeyUtil.getKeyDigest(encryptionKey.getPublic()))
                                .publicKey(encryptionKey.getPublic())
                                .creationTime(Instant.now())
                                .version(configuration.getEncryptionVersion())
                                .build())
                        .build())
                .build();
    }
}
