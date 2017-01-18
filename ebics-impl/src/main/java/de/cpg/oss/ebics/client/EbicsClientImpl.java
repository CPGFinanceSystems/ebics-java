package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.session.*;
import de.cpg.oss.ebics.utils.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The ebics client application. Performs necessary tasks to contact
 * the ebics bank server like sending the INI, HIA and HPB requests
 * for keys retrieval and also performs the files transfer including
 * uploads and downloads.
 *
 * @author hachani
 */
@Slf4j
public class EbicsClientImpl implements EbicsClient {

    private final EbicsConfiguration configuration;

    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     */
    public EbicsClientImpl(final EbicsConfiguration configuration) {
        this.configuration = configuration;
        org.apache.xml.security.Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public Collection<String> bankSupportedEbicsVersions(final String hostId, final URI endpoint) throws EbicsException {
        return KeyManagement.sendHEV(EbicsSession.builder()
                .user(EbicsUser.builder()
                        .userId("DUMMY")
                        .status(UserStatus.NEW)
                        .build())
                .partner(EbicsPartner.builder()
                        .partnerId("DUMMY")
                        .build())
                .bank(EbicsBank.builder()
                        .hostId(hostId)
                        .uri(endpoint)
                        .build())
                .configuration(new EbicsConfiguration())
                .persistenceProvider(InMemoryPersistenceProvider.INSTANCE)
                .xmlMessageTracer(Slf4jXmlMessageTracer.INSTANCE)
                .fileTransferManager(new DefaultFileTransferManager(InMemoryPersistenceProvider.INSTANCE))
                .build()).getSupportedEbicsVersions().stream()
                .map(v -> v.right().getOrElse(v.getLeft().name())).collect(Collectors.toList());
    }

    @Override
    public EbicsSession loadOrCreateSession(final EbicsSessionParameter sessionParameter) {
        EbicsSession.EbicsSessionBuilder ebicsSessionBuilder;
        try {
            ebicsSessionBuilder = loadSession(sessionParameter);
        } catch (final IOException e) {
            log.info("No previous session data found. Creating a new session");
            ebicsSessionBuilder = createSession(sessionParameter);
        }

        return ebicsSessionBuilder
                .configuration(configuration)
                .persistenceProvider(sessionParameter.getPersistenceProvider())
                .xmlMessageTracer(sessionParameter.getXmlMessageTracer().orElse(NoOpXmlMessageTracer.INSTANCE))
                .fileTransferManager(new DefaultFileTransferManager(sessionParameter.getPersistenceProvider()))
                .build();
    }

    @Override
    public EbicsSession initializeUser(final EbicsSession session) throws EbicsException {
        final EbicsSession sessionWithUserKeys;
        if (UserStatus.NEW.equals(session.getUser().getStatus())) {
            sessionWithUserKeys = session.withUser(KeyManagement.sendINI(session.withUser(createUserKeys(session))));
        } else {
            sessionWithUserKeys = session;
        }

        if (UserStatus.PARTLY_INITIALIZED_INI.equals(sessionWithUserKeys.getUser().getStatus())) {
            return sessionWithUserKeys.withUser(KeyManagement.sendHIA(sessionWithUserKeys));
        }
        return sessionWithUserKeys;
    }

    @Override
    public void generateIniLetter(final EbicsSession session, final OutputStream pdfOutput) {
        InitLetter.createINI(session, pdfOutput);
    }

    @Override
    public void generateHiaLetter(final EbicsSession session, final OutputStream pdfOutput) {
        InitLetter.createHIA(session, pdfOutput);
    }

    @Override
    public EbicsSession collectInformation(final EbicsSession session) throws EbicsException {
        final EbicsBank bankWithKeys;
        if (null == session.getBank().getEncryptionKey() || null == session.getBank().getAuthenticationKey()) {
            bankWithKeys = KeyManagement.getBankPublicKeys(session);
        } else {
            bankWithKeys = session.getBank();
        }

        if (!bankWithKeys.equals(session.getBank())) {
            try {
                session.getPersistenceProvider().save(EbicsBank.class, bankWithKeys);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return save(KeyManagement.collectInformation(session.withBank(bankWithKeys)));
    }

    /**
     * Sends the SPR order to the bank.
     */
    @Override
    public EbicsSession revokeSubscriber(final EbicsSession session) throws EbicsException {
        return session.withUser(KeyManagement.lockAccess(session));
    }

    @Override
    public Collection<VEUOrder> getOrdersForVEU(final EbicsSession session) throws EbicsException {
        return DistributedElectronicSignature.getOrdersForVEU(session);
    }

    @Override
    public Collection<DetailedVEUOrder> getDetailedOrdersForVEU(final EbicsSession session) throws EbicsException {
        return DistributedElectronicSignature.getDetailedOrdersForVEU(session);
    }

    @Override
    public DetailedVEUOrder detailedVEUOrderFor(final EbicsSession session,
                                                final VEUOrder orderDetails) throws EbicsException {
        return DistributedElectronicSignature.getOrderDetails(session, orderDetails);
    }

    @Override
    public void signDetailedOrder(final EbicsSession session, final DetailedVEUOrder detailedVEUOrder)
            throws EbicsException {
        DistributedElectronicSignature.signDetailedOrder(session, detailedVEUOrder);
    }

    @Override
    public void cancelSignature(final EbicsSession session, final DetailedVEUOrder detailedVEUOrder) throws EbicsException {
        DistributedElectronicSignature.cancelSignature(session, detailedVEUOrder);
    }

    @Override
    public FileTransfer createFileUploadTransaction(
            final EbicsSession session,
            final File inputFile,
            final OrderType orderType) {
        return FileTransaction.createFileUploadTransaction(session, inputFile, orderType);
    }

    @Override
    public FileTransfer uploadFile(final EbicsSession session,
                                   final FileTransfer transaction) throws EbicsException {
        return FileTransaction.uploadFile(session, transaction);
    }

    @Override
    public void fetchFile(final String path,
                          final EbicsSession session,
                          final OrderType orderType,
                          final boolean isTest,
                          final LocalDate start,
                          final LocalDate end) throws EbicsException {
        final FileTransfer transaction = FileTransaction.createFileDownloadTransaction(session, orderType, isTest, start, end);
        FileTransaction.downloadFile(session, transaction, new File(path));
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    @Override
    public EbicsSession save(final EbicsSession session) {
        try {
            final PersistenceProvider persistenceProvider = session.getPersistenceProvider();
            persistenceProvider.save(EbicsUser.class, session.getUser());
            persistenceProvider.save(EbicsPartner.class, session.getPartner());
            persistenceProvider.save(EbicsBank.class, session.getBank());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return session;
    }

    private EbicsSession.EbicsSessionBuilder loadSession(final EbicsSessionParameter sessionParameter) throws IOException {
        final PersistenceProvider persistenceProvider = sessionParameter.getPersistenceProvider();

        final EbicsBank bank = persistenceProvider.load(EbicsBank.class, sessionParameter.getHostId());
        final EbicsPartner partner = persistenceProvider.load(EbicsPartner.class, sessionParameter.getPartnerId());
        final EbicsUser user = persistenceProvider.load(EbicsUser.class, sessionParameter.getUserId());

        return EbicsSession.builder()
                .bank(bank)
                .partner(partner)
                .user(user.withPasswordCallback(sessionParameter.getPasswordCallback()
                        .orElseGet(() -> new DefaultPasswordCallback(sessionParameter.getUserId(), ""))));
    }

    private EbicsSession.EbicsSessionBuilder createSession(final EbicsSessionParameter sessionParameter) {
        try {
            final EbicsBank bank = EbicsBank.builder()
                    .uri(sessionParameter.getBankUri())
                    .hostId(sessionParameter.getHostId())
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsBank.class, bank);

            final EbicsPartner partner = EbicsPartner.builder()
                    .partnerId(sessionParameter.getPartnerId())
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsPartner.class, partner);

            final EbicsUser user = EbicsUser.builder()
                    .userId(sessionParameter.getUserId())
                    .securityMedium("0100")
                    .status(UserStatus.NEW)
                    .passwordCallback(sessionParameter.getPasswordCallback()
                            .orElseGet(() -> new DefaultPasswordCallback(sessionParameter.getUserId(), "")))
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsUser.class, user);

            return EbicsSession.builder()
                    .user(user)
                    .partner(partner)
                    .bank(bank);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private EbicsUser createUserKeys(final EbicsSession session) {
        return session.getUser()
                .withSignatureKey(createSignatureKey(configuration.getSignatureVersion()))
                .withEncryptionKey(createEncryptionKey(configuration.getEncryptionVersion()))
                .withAuthenticationKey(createAuthenticationKey(configuration.getAuthenticationVersion()));
    }

    private EbicsSignatureKey createSignatureKey(final SignatureVersion version) {
        try {
            final KeyPair keyPair = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
            return EbicsSignatureKey.builder()
                    .privateKey(keyPair.getPrivate())
                    .publicKey(keyPair.getPublic())
                    .creationTime(Instant.now())
                    .digest(KeyUtil.getKeyDigest(keyPair.getPublic()))
                    .version(version)
                    .build();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private EbicsEncryptionKey createEncryptionKey(final EncryptionVersion version) {
        try {
            final KeyPair keyPair = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
            return EbicsEncryptionKey.builder()
                    .privateKey(keyPair.getPrivate())
                    .publicKey(keyPair.getPublic())
                    .creationTime(Instant.now())
                    .digest(KeyUtil.getKeyDigest(keyPair.getPublic()))
                    .version(version)
                    .build();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private EbicsAuthenticationKey createAuthenticationKey(final AuthenticationVersion version) {
        try {
            final KeyPair keyPair = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
            return EbicsAuthenticationKey.builder()
                    .privateKey(keyPair.getPrivate())
                    .publicKey(keyPair.getPublic())
                    .creationTime(Instant.now())
                    .digest(KeyUtil.getKeyDigest(keyPair.getPublic()))
                    .version(version)
                    .build();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
