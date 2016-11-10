/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.IOUtils;
import de.cpg.oss.ebics.utils.Constants;
import de.cpg.oss.ebics.utils.KeyUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.LocalDate;

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
    }

    /**
     * Initiates the application by creating the
     * application root directories and its children
     */
    @Override
    public void init() {
        log.info(configuration.getMessageProvider().getString(
                "init.configuration",
                Constants.APPLICATION_BUNDLE_NAME));
        configuration.init();
        org.apache.xml.security.Init.init();
    }

    /**
     * Creates a new ebics user and generates its certificates.
     *
     * @param uri              the bank url
     * @param bankName         the bank name
     * @param hostId           the bank host ID
     * @param partnerId        the partner ID
     * @param userId           UserId as obtained from the bank.
     * @param userName         the user name,
     * @param passwordCallback a callback-handler that supplies us with the password.
     *                         This parameter can be null, in this case no password is used.
     */
    @Override
    public EbicsSession createSession(final URI uri,
                                      final String bankName,
                                      final String hostId,
                                      final String partnerId,
                                      final String userId,
                                      final String userName,
                                      final PasswordCallback passwordCallback) throws EbicsException {
        try {
            log.info(configuration.getMessageProvider().getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId));

            final EbicsBank bank = EbicsBank.builder()
                    .uri(uri)
                    .name(bankName)
                    .hostId(hostId)
                    .build();
            final EbicsPartner partner = EbicsPartner.builder()
                    .partnerId(partnerId)
                    .build();
            final EbicsUser user = EbicsUser.builder()
                    .userId(userId)
                    .name(userName)
                    .passwordCallback(passwordCallback)
                    .a005Key(KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE))
                    .e002Key(KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE))
                    .x002Key(KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE))
                    .securityMedium("0100")
                    .build();

            createUserDirectories(user);

            configuration.getSerializationManager().serialize(bank);
            configuration.getSerializationManager().serialize(partner);
            configuration.getSerializationManager().serialize(user);

            final EbicsSession session = EbicsSession.builder()
                    .user(user)
                    .partner(partner)
                    .bank(bank)
                    .configuration(configuration)
                    .build();

            final InitLetter a005Letter = configuration.getLetterManager().createA005Letter(session);
            final InitLetter x002Letter = configuration.getLetterManager().createE002Letter(session);
            final InitLetter e002Letter = configuration.getLetterManager().createX002Letter(session);

            a005Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + a005Letter.getName()));
            e002Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + e002Letter.getName()));
            x002Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + x002Letter.getName()));

            log.info(configuration.getMessageProvider().getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return session;
        } catch (GeneralSecurityException | IOException e) {
            throw new EbicsException(configuration.getMessageProvider().getString(
                    "user.create.error",
                    Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Loads a user knowing its ID
     *
     * @param hostId    the host ID
     * @param partnerId the partner ID
     * @param userId    the user ID
     */
    @Override
    public EbicsSession loadSession(final String hostId,
                                    final String partnerId,
                                    final String userId,
                                    final PasswordCallback passwordCallback) throws EbicsException {
        try {
            log.info(configuration.getMessageProvider().getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId));

            final EbicsBank bank = configuration.getSerializationManager().deserialize(EbicsBank.class, hostId);
            final EbicsPartner partner = configuration.getSerializationManager().deserialize(EbicsPartner.class, partnerId);
            final EbicsUser user = configuration.getSerializationManager().deserialize(EbicsUser.class, userId)
                    .withPasswordCallback(passwordCallback);

            log.info(configuration.getMessageProvider().getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return EbicsSession.builder()
                    .bank(bank)
                    .partner(partner)
                    .user(user)
                    .configuration(configuration)
                    .build();
        } catch (final IOException e) {
            throw new EbicsException(configuration.getMessageProvider().getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     */
    @Override
    public EbicsUser sendINIRequest(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString("ini.request.send", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));

        if (session.getUser().isInitializedINI()) {
            log.info(configuration.getMessageProvider().getString("user.already.initialized", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
            return session.getUser();
        }

        try {
            log.info(configuration.getMessageProvider().getString("ini.send.success", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
            return KeyManagement.sendINI(session);
        } catch (final IOException e) {
            throw new EbicsException(configuration.getMessageProvider().getString("ini.send.error", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()), e);
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     */
    @Override
    public EbicsUser sendHIARequest(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString("hia.request.send", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
        if (session.getUser().isInitializedHIA()) {
            log.info(configuration.getMessageProvider().getString("user.already.hia.initialized", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
            return session.getUser();
        }

        try {
            log.info(configuration.getMessageProvider().getString("hia.send.success", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
            return KeyManagement.sendHIA(session);
        } catch (final IOException e) {
            throw new EbicsException(configuration.getMessageProvider().getString("hia.send.error", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()), e);
        }
    }

    /**
     * Sends a HPB request to the ebics server.
     */
    @Override
    public EbicsBank sendHPBRequest(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString("hpb.request.send", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));

        try {
            log.info(configuration.getMessageProvider().getString("hpb.send.success", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
            return KeyManagement.sendHPB(session);
        } catch (final Exception e) {
            throw new EbicsException(configuration.getMessageProvider().getString("hpb.send.error", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()), e);
        }
    }

    /**
     * Sends the SPR order to the bank.
     */
    @Override
    public EbicsUser revokeSubscriber(final EbicsSession session) throws EbicsException {
        log.info(configuration.getMessageProvider().getString("spr.request.send", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));

        try {
            log.info(configuration.getMessageProvider().getString("spr.send.success", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
            return KeyManagement.lockAccess(session);
        } catch (final Exception e) {
            throw new EbicsException(configuration.getMessageProvider().getString("spr.send.error", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()), e);
        }

    }

    /**
     * Sends a file to the ebics bank sever
     *
     * @param path the file path to send
     */
    @Override
    public void uploadSepaDirectDebit(final String path, final EbicsSession session) throws EbicsException {
        session.addSessionParam("FORMAT", "pain.008.001.02");
        FileTransfer.sendFile(session, IOUtils.getFileContent(path), OrderType.CDD);
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
            log.error(configuration.getMessageProvider().getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    @Override
    public void quit(final EbicsSession session) throws IOException {
        log.info(configuration.getMessageProvider().getString("app.quit.users", Constants.APPLICATION_BUNDLE_NAME, session.getUser().getId()));
        configuration.getSerializationManager().serialize(session.getUser());

        log.info(configuration.getMessageProvider().getString("app.quit.partners", Constants.APPLICATION_BUNDLE_NAME, session.getPartner().getId()));
        configuration.getSerializationManager().serialize(session.getPartner());

        log.info(configuration.getMessageProvider().getString("app.quit.banks", Constants.APPLICATION_BUNDLE_NAME, session.getBank().getId()));
        configuration.getSerializationManager().serialize(session.getBank());
    }

    /**
     * Creates the user necessary directories
     *
     * @param user the concerned user
     */
    private void createUserDirectories(final EbicsUser user) {
        log.info(configuration.getMessageProvider().getString("user.create.directories", Constants.APPLICATION_BUNDLE_NAME, user.getUserId()));
        //Create the user directory
        IOUtils.createDirectories(configuration.getUserDirectory(user));
        //Create the traces directory
        IOUtils.createDirectories(configuration.getTransferTraceDirectory(user));
        //Create the letters directory
        IOUtils.createDirectories(configuration.getLettersDirectory(user));
    }
}
