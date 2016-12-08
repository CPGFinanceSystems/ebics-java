package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.session.DefaultFileTransferManager;
import de.cpg.oss.ebics.session.DefaultPasswordCallback;
import de.cpg.oss.ebics.session.NoOpXmlMessageTracer;
import de.cpg.oss.ebics.utils.KeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
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
        org.apache.xml.security.Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public EbicsSession loadOrCreateSession(final EbicsSessionParameter sessionParameter) throws EbicsException {
        EbicsSession ebicsSession;
        try {
            ebicsSession = loadSession(sessionParameter.getHostId(),
                    sessionParameter.getPartnerId(),
                    sessionParameter.getUserId(),
                    sessionParameter.getPersistenceProvider());
        } catch (final IOException e) {
            log.info("No previous session data found. Creating a new session");
            ebicsSession = createSession(sessionParameter);
        }

        return ebicsSession
                .withXmlMessageTracer(Optional.ofNullable(sessionParameter.getXmlMessageTracer())
                        .orElseGet(() -> NoOpXmlMessageTracer.INSTANCE))
                .withFileTransferManager(new DefaultFileTransferManager(sessionParameter.getPersistenceProvider()))
                .withUser(ebicsSession.getUser()
                        .withPasswordCallback(Optional.ofNullable(sessionParameter.getPasswordCallback())
                                .orElseGet(() -> new DefaultPasswordCallback(sessionParameter.getUserId(), ""))));
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
    public void generateIniLetter(final EbicsSession session, final OutputStream pdfOutput) throws EbicsException {
        InitLetter.createINI(session, pdfOutput);
    }

    @Override
    public void generateHiaLetter(final EbicsSession session, final OutputStream pdfOutput) throws EbicsException {
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
        try {
            return session.withUser(KeyManagement.lockAccess(session));
        } catch (final IOException e) {
            throw new EbicsException(e);
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
                          final LocalDate end) throws EbicsException {
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        try {
            final FileTransfer transaction = FileTransaction.createFileDownloadTransaction(session, orderType, start, end);
            FileTransaction.downloadFile(session, transaction, new File(path));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    @Override
    public EbicsSession save(final EbicsSession session) throws IOException {
        session.getPersistenceProvider().save(EbicsUser.class, session.getUser());
        session.getPersistenceProvider().save(EbicsPartner.class, session.getPartner());
        session.getPersistenceProvider().save(EbicsBank.class, session.getBank());
        return session;
    }

    private EbicsSession loadSession(final String hostId,
                                     final String partnerId,
                                     final String userId,
                                     final PersistenceProvider persistenceProvider) throws IOException {
        final EbicsBank bank = persistenceProvider.load(EbicsBank.class, hostId);
        final EbicsPartner partner = persistenceProvider.load(EbicsPartner.class, partnerId);
        final EbicsUser user = persistenceProvider.load(EbicsUser.class, userId);

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
                    .status(UserStatus.NEW)
                    .build();
            sessionParameter.getPersistenceProvider().save(EbicsUser.class, user);

            return EbicsSession.builder()
                    .user(user)
                    .partner(partner)
                    .bank(bank)
                    .configuration(configuration)
                    .persistenceProvider(sessionParameter.getPersistenceProvider())
                    .build();
        } catch (final IOException e) {
            throw new EbicsException(e);
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
