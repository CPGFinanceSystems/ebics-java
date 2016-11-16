package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.session.BinarySerializaionManager;
import de.cpg.oss.ebics.session.DefaultPasswordCallback;
import de.cpg.oss.ebics.session.DefaultTraceManager;
import de.cpg.oss.ebics.utils.Constants;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .withSerializationManager(Optional.ofNullable(sessionParameter.getSerializationManager())
                        .orElseGet(() -> new BinarySerializaionManager(configuration.getSerializationDirectory())));
        EbicsSession ebicsSession;
        try {
            ebicsSession = loadSession(parameter.getHostId(),
                    parameter.getPartnerId(),
                    parameter.getUserId(),
                    parameter.getSerializationManager());
        } catch (final FileNotFoundException e) {
            log.info("No previous session data found. Creating a new session");
            ebicsSession = createSession(parameter);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        return ebicsSession
                .withTraceManager(Optional.ofNullable(sessionParameter.getTraceManager())
                        .orElseGet(() -> new DefaultTraceManager(configuration)))
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
            final EbicsUser userInitSent = sendINIRequest(session.withUser(userWithKeys));
            sessionWithUserWithKeys = session.withUser(userInitSent);
        } else {
            sessionWithUserWithKeys = session;
        }

        final EbicsSession sessionWithUserWithInitialized;
        if (!sessionWithUserWithKeys.getUser().isInitializedHIA()) {
            InitLetter.createHIA(sessionWithUserWithKeys);
            final EbicsUser initializedUser = sendHIARequest(sessionWithUserWithKeys);
            sessionWithUserWithInitialized = sessionWithUserWithKeys.withUser(initializedUser);
        } else {
            sessionWithUserWithInitialized = sessionWithUserWithKeys;
        }

        if (!sessionWithUserWithInitialized.getUser().equals(session.getUser())) {
            try {
                session.getSerializationManager().serialize(sessionWithUserWithInitialized.getUser());
            } catch (final IOException e) {
                throw new EbicsException(e);
            }
        }
        return sessionWithUserWithInitialized;
    }

    @Override
    public EbicsSession getBankInformation(final EbicsSession session) throws EbicsException {
        final EbicsBank bankWithKeys;
        if (null == session.getBank().getEncryptionKey() || null == session.getBank().getAuthenticationKey()) {
            bankWithKeys = sendHPBRequest(session);
        } else {
            bankWithKeys = session.getBank();
        }

        if (!bankWithKeys.equals(session.getBank())) {
            try {
                session.getSerializationManager().serialize(bankWithKeys);
            } catch (final IOException e) {
                throw new EbicsException(e);
            }
        }
        return session.withBank(bankWithKeys);
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

    /**
     * Sends a file to the ebics bank sever
     *
     * @param path the file path to send
     */
    @Override
    public void uploadSepaDirectDebit(final String path, final EbicsSession session) throws EbicsException {
        try {
            session.addSessionParam("FORMAT", "pain.008.001.02");
            //TODO: send file via streaming
            FileTransfer.sendFile(session, IOUtil.getFileContent(path), OrderType.CDD);
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    @Override
    public void fetchFile(final String path,
                          final EbicsSession session,
                          final OrderType orderType,
                          final boolean isTest,
                          final LocalDate start,
                          final LocalDate end) {
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        try {
            FileTransfer.fetchFile(session, orderType, start, end, new FileOutputStream(path));
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
    public void save(final EbicsSession session) throws IOException {
        log.info(configuration.getMessageProvider().getString(
                "app.quit.users",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getUser().getId()));
        session.getSerializationManager().serialize(session.getUser());

        log.info(configuration.getMessageProvider().getString(
                "app.quit.partners",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getPartner().getId()));
        session.getSerializationManager().serialize(session.getPartner());

        log.info(configuration.getMessageProvider().getString(
                "app.quit.banks",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getBank().getId()));
        session.getSerializationManager().serialize(session.getBank());
    }

    public static void init(final EbicsConfiguration configuration) {
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
                                     final SerializationManager serializationManager) throws IOException {
        log.info(configuration.getMessageProvider().getString(
                "user.load.info",
                Constants.APPLICATION_BUNDLE_NAME,
                userId));

        final EbicsBank bank = serializationManager.deserialize(EbicsBank.class, hostId);
        final EbicsPartner partner = serializationManager.deserialize(EbicsPartner.class, partnerId);
        final EbicsUser user = serializationManager.deserialize(EbicsUser.class, userId);

        log.info(configuration.getMessageProvider().getString(
                "user.load.success",
                Constants.APPLICATION_BUNDLE_NAME,
                userId));
        return EbicsSession.builder()
                .bank(bank)
                .partner(partner)
                .user(user)
                .configuration(configuration)
                .serializationManager(serializationManager)
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
            sessionParameter.getSerializationManager().serialize(bank);

            final EbicsPartner partner = EbicsPartner.builder()
                    .partnerId(sessionParameter.getPartnerId())
                    .build();
            sessionParameter.getSerializationManager().serialize(partner);

            final EbicsUser user = EbicsUser.builder()
                    .userId(sessionParameter.getUserId())
                    .name(sessionParameter.getUserName())
                    .securityMedium("0100")
                    .build();
            sessionParameter.getSerializationManager().serialize(user);

            log.info(configuration.getMessageProvider().getString(
                    "user.create.success",
                    Constants.APPLICATION_BUNDLE_NAME,
                    sessionParameter.getUserId()));
            return EbicsSession.builder()
                    .user(user)
                    .partner(partner)
                    .bank(bank)
                    .configuration(configuration)
                    .serializationManager(sessionParameter.getSerializationManager())
                    .build();
        } catch (final IOException e) {
            throw new EbicsException(configuration.getMessageProvider().getString(
                    "user.create.error",
                    Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    public static void createUserDirectories(final EbicsConfiguration configuration, final EbicsUser user) {
        log.info(configuration.getMessageProvider().getString(
                "user.create.directories",
                Constants.APPLICATION_BUNDLE_NAME,
                user.getUserId()));
        IOUtil.createDirectories(configuration.getUserDirectory(user));
        IOUtil.createDirectories(configuration.getTransferTraceDirectory(user));
        IOUtil.createDirectories(configuration.getLettersDirectory(user));
    }

    private EbicsUser sendINIRequest(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString(
                "ini.request.send",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getUser().getId()));

        if (session.getUser().isInitializedINI()) {
            log.info(configuration.getMessageProvider().getString(
                    "user.already.initialized",
                    Constants.APPLICATION_BUNDLE_NAME,
                    session.getUser().getId()));
            return session.getUser();
        }

        try {
            log.info(configuration.getMessageProvider().getString(
                    "ini.send.success",
                    Constants.APPLICATION_BUNDLE_NAME,
                    session.getUser().getId()));
            return KeyManagement.sendINI(session);
        } catch (final IOException e) {
            throw new EbicsException(
                    configuration.getMessageProvider().getString(
                            "ini.send.error",
                            Constants.APPLICATION_BUNDLE_NAME,
                            session.getUser().getId()),
                    e);
        }
    }

    private EbicsUser sendHIARequest(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString(
                "hia.request.send",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getUser().getId()));
        if (session.getUser().isInitializedHIA()) {
            log.info(configuration.getMessageProvider().getString(
                    "user.already.hia.initialized",
                    Constants.APPLICATION_BUNDLE_NAME,
                    session.getUser().getId()));
            return session.getUser();
        }

        try {
            log.info(configuration.getMessageProvider().getString(
                    "hia.send.success",
                    Constants.APPLICATION_BUNDLE_NAME,
                    session.getUser().getId()));
            return KeyManagement.sendHIA(session);
        } catch (final IOException e) {
            throw new EbicsException(
                    configuration.getMessageProvider().getString(
                            "hia.send.error",
                            Constants.APPLICATION_BUNDLE_NAME,
                            session.getUser().getId()),
                    e);
        }
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
                    .creationTime(LocalDateTime.now())
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
                    .creationTime(LocalDateTime.now())
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
                    .creationTime(LocalDateTime.now())
                    .digest(KeyUtil.getKeyDigest(keyPair.getPublic()))
                    .version(version)
                    .build();
        } catch (final NoSuchAlgorithmException e) {
            throw new EbicsException(e);
        }
    }

    private EbicsBank sendHPBRequest(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString(
                "hpb.request.send",
                Constants.APPLICATION_BUNDLE_NAME,
                session.getUser().getId()));

        try {
            log.info(configuration.getMessageProvider().getString(
                    "hpb.send.success",
                    Constants.APPLICATION_BUNDLE_NAME,
                    session.getUser().getId()));
            return KeyManagement.sendHPB(session);
        } catch (final Exception e) {
            throw new EbicsException(
                    configuration.getMessageProvider().getString(
                            "hpb.send.error",
                            Constants.APPLICATION_BUNDLE_NAME,
                            session.getUser().getId()),
                    e);
        }
    }
}
