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

package org.kopi.ebics.client;

import de.cpg.oss.ebics.api.*;
import de.cpg.oss.ebics.api.exception.EbicsException;
import lombok.extern.slf4j.Slf4j;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.session.Product;
import org.kopi.ebics.utils.Constants;
import org.kopi.ebics.utils.KeyUtil;

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
public class EbicsClient {

    private final EbicsConfiguration configuration;

    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     */
    public EbicsClient(final EbicsConfiguration configuration) {
        this.configuration = configuration;
        Messages.setLocale(configuration.getLocale());
    }

    /**
     * Initiates the application by creating the
     * application root directories and its children
     */
    public void init() {
        log.info(Messages.getString("init.configuration", Constants.APPLICATION_BUNDLE_NAME));
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
    public EbicsUser createUser(final URI uri,
                                final String bankName,
                                final String hostId,
                                final String partnerId,
                                final String userId,
                                final String userName,
                                final PasswordCallback passwordCallback) throws EbicsException {
        log.info(Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        final EbicsBank bank = EbicsBank.builder()
                .uri(uri)
                .name(bankName)
                .hostId(hostId)
                .build();
        final EbicsPartner partner = EbicsPartner.builder()
                .bank(bank)
                .partnerId(partnerId)
                .build();
        try {
            final EbicsUser user = EbicsUser.builder()
                    .partner(partner)
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
            final InitLetter a005Letter = configuration.getLetterManager().createA005Letter(user);
            final InitLetter x002Letter = configuration.getLetterManager().createE002Letter(user);
            final InitLetter e002Letter = configuration.getLetterManager().createX002Letter(user);
            a005Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + a005Letter.getName()));
            e002Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + e002Letter.getName()));
            x002Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + x002Letter.getName()));

            log.info(Messages.getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (GeneralSecurityException | IOException e) {
            throw new EbicsException(Messages.getString("user.create.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Loads a user knowing its ID
     *
     * @param hostId    the host ID
     * @param partnerId the partner ID
     * @param userId    the user ID
     */
    public EbicsUser loadUser(final String hostId,
                              final String partnerId,
                              final String userId,
                              final PasswordCallback passwordCallback) throws EbicsException {
        log.info(Messages.getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        try {
            final EbicsBank bank = configuration.getSerializationManager().deserialize(EbicsBank.class, hostId);

            final EbicsPartner partner = configuration.getSerializationManager()
                    .deserialize(EbicsPartner.class, partnerId)
                    .withBank(bank);

            final EbicsUser user = configuration.getSerializationManager().deserialize(EbicsUser.class, userId)
                    .withPartner(partner)
                    .withPasswordCallback(passwordCallback);

            log.info(Messages.getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param user    the user
     * @param product the application product
     */
    public EbicsUser sendINIRequest(final EbicsUser user, final Product product) throws EbicsException {
        log.info(Messages.getString("ini.request.send", Constants.APPLICATION_BUNDLE_NAME, user.getId()));

        if (user.isInitializedINI()) {
            log.info(Messages.getString("user.already.initialized", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
            return user;
        }

        final EbicsSession session = new EbicsSession(user, configuration, product);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            log.info(Messages.getString("ini.send.success", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
            return KeyManagement.sendINI(session);
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("ini.send.error", Constants.APPLICATION_BUNDLE_NAME, user.getId()), e);
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param user    the user.
     * @param product the application product.
     */
    public EbicsUser sendHIARequest(final EbicsUser user, final Product product) throws EbicsException {
        log.info(Messages.getString("hia.request.send", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
        if (user.isInitializedHIA()) {
            log.info(Messages.getString("user.already.hia.initialized", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
            return user;
        }

        final EbicsSession session = new EbicsSession(user, configuration, product);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            log.info(Messages.getString("hia.send.success", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
            return KeyManagement.sendHIA(session);
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("hia.send.error", Constants.APPLICATION_BUNDLE_NAME, user.getId()), e);
        }
    }

    /**
     * Sends a HPB request to the ebics server.
     *
     * @param user    the user.
     * @param product the application product.
     */
    public EbicsUser sendHPBRequest(final EbicsUser user, final Product product) throws EbicsException {
        log.info(Messages.getString("hpb.request.send", Constants.APPLICATION_BUNDLE_NAME, user.getId()));

        final EbicsSession session = new EbicsSession(user, configuration, product);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            log.info(Messages.getString("hpb.send.success", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
            return KeyManagement.sendHPB(session);
        } catch (final Exception e) {
            throw new EbicsException(Messages.getString("hpb.send.error", Constants.APPLICATION_BUNDLE_NAME, user.getId()), e);
        }
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param user    the user
     * @param product the session product
     */
    public EbicsUser revokeSubscriber(final EbicsUser user, final Product product) throws EbicsException {
        log.info(Messages.getString("spr.request.send", Constants.APPLICATION_BUNDLE_NAME, user.getId()));

        final EbicsSession session = new EbicsSession(user, configuration, product);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            log.info(Messages.getString("spr.send.success", Constants.APPLICATION_BUNDLE_NAME, user.getId()));
            return KeyManagement.lockAccess(session);
        } catch (final Exception e) {
            throw new EbicsException(Messages.getString("spr.send.error", Constants.APPLICATION_BUNDLE_NAME, user.getId()), e);
        }

    }

    /**
     * Sends a file to the ebics bank sever
     *
     * @param path    the file path to send
     * @param user    the user that sends the file.
     * @param product the application product.
     */
    public void uploadSepaDirectDebit(final String path, final EbicsUser user, final Product product) throws EbicsException {
        final EbicsSession session = new EbicsSession(user, configuration, product);
        session.addSessionParam("FORMAT", "pain.008.001.02");

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        FileTransfer.sendFile(session, IOUtils.getFileContent(path), OrderType.CDD);
    }

    public void fetchFile(final String path,
                          final EbicsUser user,
                          final Product product,
                          final OrderType orderType,
                          final boolean isTest,
                          final LocalDate start,
                          final LocalDate end) {
        final EbicsSession session = new EbicsSession(user, configuration, product);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            FileTransfer.fetchFile(session, orderType, start, end, new FileOutputStream(path));
        } catch (final IOException | EbicsException e) {
            log.error(Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    public void quit(final EbicsUser user) throws IOException {
        log.info(Messages.getString("app.quit.users", Constants.APPLICATION_BUNDLE_NAME, user.getUserId()));
        configuration.getSerializationManager().serialize(user);

        log.info(Messages.getString("app.quit.partners", Constants.APPLICATION_BUNDLE_NAME, user.getPartner().getId()));
        configuration.getSerializationManager().serialize(user.getPartner());

        log.info(Messages.getString("app.quit.banks", Constants.APPLICATION_BUNDLE_NAME, user.getPartner().getBank().getId()));
        configuration.getSerializationManager().serialize(user.getPartner().getBank());
    }

    /**
     * Creates the user necessary directories
     *
     * @param user the concerned user
     */
    private void createUserDirectories(final EbicsUser user) {
        log.info(Messages.getString("user.create.directories", Constants.APPLICATION_BUNDLE_NAME, user.getUserId()));
        //Create the user directory
        IOUtils.createDirectories(configuration.getUserDirectory(user));
        //Create the traces directory
        IOUtils.createDirectories(configuration.getTransferTraceDirectory(user));
        //Create the key stores directory
        IOUtils.createDirectories(configuration.getKeystoreDirectory(user));
        //Create the letters directory
        IOUtils.createDirectories(configuration.getLettersDirectory(user));
    }
}
