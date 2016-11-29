package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class JpaPersistenceProviderTest extends AbstractTransactionalJUnit4SpringContextTests {

    @SpringBootApplication
    static class TestConfig {
    }

    @Autowired
    private PersistenceProvider persistenceProvider;

    private static KeyPair KEY_PAIR;
    private static byte[] DIGEST;

    @BeforeClass
    public static void createTestData() throws Exception {
        KEY_PAIR = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        DIGEST = MessageDigest.getInstance("SHA-256").digest(KEY_PAIR.getPublic().getEncoded());
    }

    @Test
    public void testEbicsBankPersistence() throws Exception {
        final EbicsBank bank = EbicsBank.builder()
                .uri(URI.create("http://example.org"))
                .hostId("HOSTID")
                .name("Testbank")
                .supportedEbicsVersions(Collections.singleton(EbicsVersion.H004.name()))
                .supportedOrderTypes(Collections.singleton(OrderType.AEA.name()))
                .authenticationKey(EbicsAuthenticationKey.builder()
                        .creationTime(Instant.now())
                        .version(AuthenticationVersion.X002)
                        .publicKey(KEY_PAIR.getPublic())
                        .digest(DIGEST)
                        .build())
                .encryptionKey(EbicsEncryptionKey.builder()
                        .digest(DIGEST)
                        .publicKey(KEY_PAIR.getPublic())
                        .privateKey(KEY_PAIR.getPrivate())
                        .version(EncryptionVersion.E002)
                        .creationTime(Instant.now())
                        .build())
                .build();

        log.info("Saved {}", persistenceProvider.save(EbicsBank.class, bank));
        final EbicsBank saved = persistenceProvider.load(EbicsBank.class, bank.getId());
        assertThat(saved).isNotSameAs(bank);
        assertThat(saved).isEqualTo(bank);
    }

    @Test
    public void testEbicsPartnerPersistence() throws Exception {
        final EbicsPartner partner = EbicsPartner.builder()
                .partnerId("PARTNERID")
                .bankAccounts(Collections.singletonList(BankAccountInformation.builder()
                        .id("1")
                        .accountNmber("1234")
                        .bankCode("5678")
                        .build()))
                .build();

        log.info("Saved {}", persistenceProvider.save(EbicsPartner.class, partner));
        final EbicsPartner saved = persistenceProvider.load(EbicsPartner.class, partner.getId());
        assertThat(saved).isNotSameAs(partner);
        assertThat(saved).isEqualTo(partner);
    }

    @Test
    public void testEbicsUserPersistence() throws Exception {
        final EbicsUser user = EbicsUser.builder()
                .userId("USERID")
                .name("User Name")
                .securityMedium("1234")
                .status(UserStatus.INITIALIZED)
                .authenticationKey(EbicsAuthenticationKey.builder()
                        .digest(DIGEST)
                        .publicKey(KEY_PAIR.getPublic())
                        .privateKey(KEY_PAIR.getPrivate())
                        .version(AuthenticationVersion.X002)
                        .creationTime(Instant.now())
                        .build())
                .signatureKey(EbicsSignatureKey.builder()
                        .digest(DIGEST)
                        .publicKey(KEY_PAIR.getPublic())
                        .privateKey(KEY_PAIR.getPrivate())
                        .version(SignatureVersion.A006)
                        .creationTime(Instant.now())
                        .build())
                .encryptionKey(EbicsEncryptionKey.builder()
                        .digest(DIGEST)
                        .publicKey(KEY_PAIR.getPublic())
                        .privateKey(KEY_PAIR.getPrivate())
                        .version(EncryptionVersion.E002)
                        .creationTime(Instant.now())
                        .build())
                .permittedOrderTypes(Collections.singletonList(OrderType.AEA.name()))
                .build();

        log.info("Saved {}", persistenceProvider.save(EbicsUser.class, user));
        final EbicsUser saved = persistenceProvider.load(EbicsUser.class, user.getId());
        assertThat(saved).isNotSameAs(user);
        assertThat(saved).isEqualTo(user);
    }

    @Test
    public void testFileTransferPersistence() throws Exception {
        final FileTransfer fileTransfer = FileTransfer.builder()
                .digest(DIGEST)
                .nonce(DIGEST)
                .segmentIds(Collections.singletonList(UUID.randomUUID()))
                .orderType(OrderType.AIA)
                .segmentNumber(0)
                .transactionId(DIGEST)
                .transferId(UUID.randomUUID())
                .build();

        log.info("Saved {}", persistenceProvider.save(FileTransfer.class, fileTransfer));
        final FileTransfer saved = persistenceProvider.load(FileTransfer.class, fileTransfer.getId());
        assertThat(saved).isNotSameAs(fileTransfer);
        assertThat(saved).isEqualTo(fileTransfer);
    }
}
