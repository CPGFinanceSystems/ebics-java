package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.session.BinaryPersistenceProvider;
import de.cpg.oss.ebics.session.DefaultFileTransferManager;
import de.cpg.oss.ebics.session.DefaultPasswordCallback;
import de.cpg.oss.ebics.session.DefaultTraceManager;
import de.cpg.oss.ebics.utils.Constants;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

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
        init(configuration);
    }

    @Override
    public EbicsSession loadOrCreateSession(final EbicsSessionParameter sessionParameter) throws EbicsException {
        final EbicsSessionParameter parameter = sessionParameter
                .withPersistenceProvider(Optional.ofNullable(sessionParameter.getPersistenceProvider())
                        .orElseGet(() -> new BinaryPersistenceProvider(configuration.getSerializationDirectory())));
        EbicsSession ebicsSession;
        try {
            ebicsSession = loadSession(parameter.getHostId(),
                    parameter.getPartnerId(),
                    parameter.getUserId(),
                    parameter.getPersistenceProvider());
        } catch (final FileNotFoundException e) {
            log.info("No previous session data found. Creating a new session");
            ebicsSession = createSession(parameter);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return ebicsSession
                .withTraceManager(Optional.ofNullable(sessionParameter.getTraceManager())
                        .orElseGet(() -> new DefaultTraceManager(configuration)))
                .withFileTransferManager(Optional.ofNullable(sessionParameter.getFileTransferManager())
                        .orElseGet(() -> new DefaultFileTransferManager(configuration, parameter.getPersistenceProvider())))
                .withUser(ebicsSession.getUser()
                        .withPasswordCallback(Optional.ofNullable(sessionParameter.getPasswordCallback())
                                .orElseGet(() -> new DefaultPasswordCallback(sessionParameter.getUserId(), ""))));
    }

    @Override
    public EbicsSession initializeUser(final EbicsSession session) throws EbicsException {

        final EbicsSession sessionWithUserWithKeys;
        if (!session.getUser().isInitializedINI()) {
            createUserDirectories(session.getConfiguration(), session.getUser());
            final EbicsUser userWithKeys = createUserKeys(session);
            InitLetter.createINI(session.withUser(userWithKeys));
            final EbicsUser userInitSent = KeyManagement.sendINI(session.withUser(userWithKeys));
            sessionWithUserWithKeys = session.withUser(userInitSent);
        } else {
            sessionWithUserWithKeys = session;
        }

        final EbicsSession sessionWithUserWithInitialized;
        if (!sessionWithUserWithKeys.getUser().isInitializedHIA()) {
            InitLetter.createHIA(sessionWithUserWithKeys);
            final EbicsUser initializedUser = KeyManagement.sendHIA(sessionWithUserWithKeys);
            sessionWithUserWithInitialized = sessionWithUserWithKeys.withUser(initializedUser);
        } else {
            sessionWithUserWithInitialized = sessionWithUserWithKeys;
        }

        if (!sessionWithUserWithInitialized.getUser().equals(session.getUser())) {
            try {
                session.getPersistenceProvider().save(EbicsUser.class, sessionWithUserWithInitialized.getUser());
            } catch (final IOException e) {
                throw new EbicsException(e);
            }
        }
        return sessionWithUserWithInitialized;
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
                throw new EbicsException(e);
            }
        }

        try {
            return save(KeyManagement.collectInformation(session.withBank(bankWithKeys)));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    /**
     * Sends the SPR order to the bank.
     */
    @Override
    public EbicsSession revokeSubscriber(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString(
                "spr.request.send",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getUser().getId()));

        try {
            log.info(configuration.getMessageProvider().getString(
                    "spr.send.success",
                    Constants.APPLICATION_BUNDLE_NAME,
                    session.getUser().getId()));
            return session.withUser(KeyManagement.lockAccess(session));
        } catch (final Exception e) {
            throw new EbicsException(
                    configuration.getMessageProvider().getString(
                            "spr.send.error",
                            Constants.APPLICATION_BUNDLE_NAME,
                            session.getUser().getId()),
                    e);
        }

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
            final OrderType orderType) throws FileNotFoundException, EbicsException {
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
                          final LocalDate end) {
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        try {
            final FileTransfer transaction = FileTransaction.createFileDownloadTransaction(session, orderType, start, end);
            FileTransaction.downloadFile(session, transaction, new File(path));
        } catch (final IOException | EbicsException e) {
            log.error(
                    configuration.getMessageProvider().getString(
                            "download.file.error",
                            Constants.APPLICATION_BUNDLE_NAME),
                    e);
        }
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    @Override
    public EbicsSession save(final EbicsSession session) throws IOException {
        log.info(configuration.getMessageProvider().getString(
                "app.quit.users",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getUser().getId()));
        session.getPersistenceProvider().save(EbicsUser.class, session.getUser());

        log.info(configuration.getMessageProvider().getString(
                "app.quit.partners",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getPartner().getId()));
        session.getPersistenceProvider().save(EbicsPartner.class, session.getPartner());

        log.info(configuration.getMessageProvider().getString(
                "app.quit.banks",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getBank().getId()));
        session.getPersistenceProvider().save(EbicsBank.class, session.getBank());

        return session;
    }

    static void init(final EbicsConfiguration configuration) {
        log.info(configuration.getMessageProvider().getString(
                "init.configuration",
                Constants.APPLICATION_BUNDLE_NAME));
        org.apache.xml.security.Init.init();
        Security.addProvider(new BouncyCastleProvider());
        IOUtil.createDirectories(configuration.getRootDirectory());
        IOUtil.createDirectories(configuration.getSerializationDirectory());
        IOUtil.createDirectories(configuration.getUsersDirectory());
    }

    private EbicsSession loadSession(final String hostId,
                                     final String partnerId,
                                     final String userId,
                                     final PersistenceProvider persistenceProvider) throws IOException {
        log.info(configuration.getMessageProvider().getString(
                "user.load.info",
                Constants.APPLICATION_BUNDLE_NAME,
                userId));

        final EbicsBank bank = persistenceProvider.load(EbicsBank.class, hostId);
        final EbicsPartner partner = persistenceProvider.load(EbicsPartner.class, partnerId);
        final EbicsUser user = persistenceProvider.load(EbicsUser.class, userId);

        log.info(configuration.getMessageProvider().getString(
                "user.load.success",
                Constants.APPLICATION_BUNDLE_NAME,
                userId));
        return EbicsSession.builder()
                .bank(bank)
                .partner(partner)
                .user(user)
                .configuration(configuration)
                .persistenceProvider(persistenceProvider)
                .build();
    }

    private EbicsSession createSession(final EbicsSessionParameter sessionParameter) throws EbicsException {
        try {
            log.info(configuration.getMessageProvider().getString(
                    "user.create.info",
                    Constants.APPLICATION_BUNDLE_NAME,
                    sessionParameter.getUserId()));

            final EbicsBank bank = EbicsBank.builder()
                    .uri(sessionParameter.getBankUri())
                    .name(sessionParameter.getBankName())
                    .hostId(sessionParameter.getHostId())
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsBank.class, bank);

            final EbicsPartner partner = EbicsPartner.builder()
                    .partnerId(sessionParameter.getPartnerId())
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsPartner.class, partner);

            final EbicsUser user = EbicsUser.builder()
                    .userId(sessionParameter.getUserId())
                    .name(sessionParameter.getUserName())
                    .securityMedium("0100")
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsUser.class, user);

            log.info(configuration.getMessageProvider().getString(
                    "user.create.success",
                    Constants.APPLICATION_BUNDLE_NAME,
                    sessionParameter.getUserId()));
            return EbicsSession.builder()
                    .user(user)
                    .partner(partner)
                    .bank(bank)
                    .configuration(configuration)
                    .persistenceProvider(sessionParameter.getPersistenceProvider())
                    .build();
        } catch (final IOException e) {
            throw new EbicsException(configuration.getMessageProvider().getString(
                    "user.create.error",
                    Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    static void createUserDirectories(final EbicsConfiguration configuration, final EbicsUser user) {
        log.info(configuration.getMessageProvider().getString(
                "user.create.directories",
                Constants.APPLICATION_BUNDLE_NAME,
                user.getUserId()));
        IOUtil.createDirectories(configuration.getUserDirectory(user));
        IOUtil.createDirectories(configuration.getTransferTraceDirectory(user));
        IOUtil.createDirectories(configuration.getLettersDirectory(user));
        IOUtil.createDirectories(configuration.getTransferFilesDirectory(user));
    }

    private EbicsUser createUserKeys(final EbicsSession session) throws EbicsException {
        return session.getUser()
                .withSignatureKey(createSignatureKey(configuration.getSignatureVersion()))
                .withEncryptionKey(createEncryptionKey(configuration.getEncryptionVersion()))
                .withAuthenticationKey(createAuthenticationKey(configuration.getAuthenticationVersion()));
    }

    private EbicsSignatureKey createSignatureKey(final SignatureVersion version) throws EbicsException {
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
            throw new EbicsException(e);
        }
    }

    private EbicsEncryptionKey createEncryptionKey(final EncryptionVersion version) throws EbicsException {
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
            throw new EbicsException(e);
        }
    }

    private EbicsAuthenticationKey createAuthenticationKey(final AuthenticationVersion version) throws EbicsException {
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
            throw new EbicsException(e);
        }
    }
}
