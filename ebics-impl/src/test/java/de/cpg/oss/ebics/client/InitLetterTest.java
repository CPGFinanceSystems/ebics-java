package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.session.BinarySerializaionManager;
import de.cpg.oss.ebics.utils.KeyUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.security.KeyPair;
import java.time.OffsetDateTime;

public class InitLetterTest {

    private static EbicsSession session;

    @Test
    public void testCreateINI() throws Exception {
        InitLetter.createINI(session).close();
    }

    @Test
    public void testCreateHIA() throws Exception {
        InitLetter.createHIA(session).close();
    }

    @BeforeClass
    public static void createEbicsSession() throws Exception {
        final EbicsConfiguration configuration = new EbicsConfiguration(new File("target/test"));
        final KeyPair signatureKey = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
        final KeyPair authenticationKey = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
        final KeyPair encryptionKey = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);

        session = EbicsSession.builder()
                .configuration(configuration)
                .serializationManager(new BinarySerializaionManager(new File("serialized")))
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
                        .signatureKey(EbicsSignatureKey.builder()
                                .digest(KeyUtil.getKeyDigest(signatureKey.getPublic()))
                                .publicKey(signatureKey.getPublic())
                                .creationTime(OffsetDateTime.now())
                                .version(configuration.getSignatureVersion())
                                .build())
                        .authenticationKey(EbicsAuthenticationKey.builder()
                                .digest(KeyUtil.getKeyDigest(authenticationKey.getPublic()))
                                .publicKey(authenticationKey.getPublic())
                                .creationTime(OffsetDateTime.now())
                                .version(configuration.getAuthenticationVersion())
                                .build())
                        .encryptionKey(EbicsEncryptionKey.builder()
                                .digest(KeyUtil.getKeyDigest(encryptionKey.getPublic()))
                                .publicKey(encryptionKey.getPublic())
                                .creationTime(OffsetDateTime.now())
                                .version(configuration.getEncryptionVersion())
                                .build())
                        .build())
                .build();
        EbicsClientImpl.init(session.getConfiguration());
        EbicsClientImpl.createUserDirectories(session.getConfiguration(), session.getUser());
    }
}
