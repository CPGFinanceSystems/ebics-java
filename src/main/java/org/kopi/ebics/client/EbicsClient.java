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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.*;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.messages.Messages;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.session.Product;
import org.kopi.ebics.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Hashtable;
import java.util.Map;

/**
 * The ebics client application. Performs necessary tasks to contact
 * the ebics bank server like sending the INI, HIA and HPB requests
 * for keys retrieval and also performs the files transfer including
 * uploads and downloads.
 *
 * @author hachani
 */
public class EbicsClient {

    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     */
    public EbicsClient(final EbicsConfiguration configuration) {
        this.configuration = configuration;
        users = new Hashtable<>();
        partners = new Hashtable<>();
        banks = new Hashtable<>();
        Messages.setLocale(configuration.getLocale());
    }

    /**
     * Constructs a new ebics client application.
     */
    public EbicsClient() {
    }

    /**
     * Initiates the application by creating the
     * application root directories and its children
     */
    public void init() {
        configuration.getLogger().info(Messages.getString("init.configuration", Constants.APPLICATION_BUNDLE_NAME));
        configuration.init();
    }

    /**
     * Creates the user necessary directories
     *
     * @param user the concerned user
     */
    private void createUserDirectories(final EbicsUser user) {
        configuration.getLogger().info(Messages.getString("user.create.directories", Constants.APPLICATION_BUNDLE_NAME, user.getUserId()));
        //Create the user directory
        IOUtils.createDirectories(configuration.getUserDirectory(user));
        //Create the traces directory
        IOUtils.createDirectories(configuration.getTransferTraceDirectory(user));
        //Create the key stores directory
        IOUtils.createDirectories(configuration.getKeystoreDirectory(user));
        //Create the letters directory
        IOUtils.createDirectories(configuration.getLettersDirectory(user));
    }

    /**
     * Creates a new EBICS bank with the data you should have obtained from the bank.
     *
     * @param url    the bank URL
     * @param url    the bank name
     * @param hostId the bank host ID
     * @return the created ebics bank
     */
    public EbicsBank createBank(final URL url, final String name, final String hostId) {
        final Bank bank;

        bank = new Bank(url, name, hostId);
        banks.put(hostId, bank);
        return bank;
    }

    /**
     * Creates a new ebics partner
     *
     * @param bank      the bank
     * @param partnerId the partner ID
     */
    public EbicsPartner createPartner(final EbicsBank bank, final String partnerId) {
        final Partner partner;

        partner = new Partner(bank, partnerId);
        partners.put(partnerId, partner);
        return partner;
    }

    /**
     * Creates a new ebics user and generates its certificates.
     *
     * @param url              the bank url
     * @param bankName         the bank name
     * @param hostId           the bank host ID
     * @param partnerId        the partner ID
     * @param userId           UserId as obtained from the bank.
     * @param name             the user name,
     * @param email            the user email
     * @param country          the user country
     * @param organization     the user organization or company
     * @param saveCertificates save generated certificates?
     * @param passwordCallback a callback-handler that supplies us with the password.
     *                         This parameter can be null, in this case no password is used.
     */
    public EbicsUser createUser(final URL url,
                                final String bankName,
                                final String hostId,
                                final String partnerId,
                                final String userId,
                                final String name,
                                final String email,
                                final String country,
                                final String organization,
                                final boolean saveCertificates,
                                final PasswordCallback passwordCallback) throws EbicsException {
        final InitLetter a005Letter;
        final InitLetter x002Letter;
        final InitLetter e002Letter;

        configuration.getLogger().info(Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        final EbicsBank bank = createBank(url, bankName, hostId);
        final EbicsPartner partner = createPartner(bank, partnerId);
        try {
            final User user = new User(partner, userId, name, email, country, organization, passwordCallback);
            createUserDirectories(user);
            if (saveCertificates) {
                user.saveUserCertificates(configuration.getKeystoreDirectory(user));
            }
            configuration.getSerializationManager().serialize(bank);
            configuration.getSerializationManager().serialize(partner);
            configuration.getSerializationManager().serialize(user);
            a005Letter = configuration.getLetterManager().createA005Letter(user);
            e002Letter = configuration.getLetterManager().createE002Letter(user);
            x002Letter = configuration.getLetterManager().createX002Letter(user);
            a005Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + a005Letter.getName()));
            e002Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + e002Letter.getName()));
            x002Letter.save(new FileOutputStream(configuration.getLettersDirectory(user) + File.separator + x002Letter.getName()));
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);

            configuration.getLogger().info(Messages.getString("user.create.success", Constants.APPLICATION_BUNDLE_NAME, userId));
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
        configuration.getLogger().info(Messages.getString("user.load.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        try {
            final Bank bank;
            final Partner partner;
            final User user;
            ObjectInputStream input;

            input = configuration.getSerializationManager().deserialize(hostId);
            bank = (Bank) input.readObject();
            input = configuration.getSerializationManager().deserialize(partnerId);
            partner = new Partner(bank, input);
            input = configuration.getSerializationManager().deserialize(userId);
            user = new User(partner, input, passwordCallback);
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);

            configuration.getLogger().info(Messages.getString("user.load.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        } catch (GeneralSecurityException | IOException | ClassNotFoundException e) {
            throw new EbicsException(Messages.getString("user.load.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param userId  the user ID
     * @param product the application product
     */
    public EbicsUser sendINIRequest(final String userId, final Product product) throws EbicsException {
        final EbicsUser user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("ini.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        user = users.get(userId);

        if (user.isInitializedINI()) {
            configuration.getLogger().info(Messages.getString("user.already.initialized", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        }

        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            configuration.getLogger().info(Messages.getString("ini.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return keyManager.sendINI();
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("ini.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param userId  the user ID.
     * @param product the application product.
     */
    public EbicsUser sendHIARequest(final String userId, final Product product) throws EbicsException {
        final EbicsUser user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("hia.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));
        user = users.get(userId);
        if (user.isInitializedHIA()) {
            configuration.getLogger().info(Messages.getString("user.already.hia.initialized", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        }
        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            configuration.getLogger().info(Messages.getString("hia.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return keyManager.sendHIA();
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("hia.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
        }
    }

    /**
     * Sends a HPB request to the ebics server.
     *
     * @param userId  the user ID.
     * @param product the application product.
     */
    public EbicsUser sendHPBRequest(final String userId, final Product product) throws EbicsException {
        final EbicsUser user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("hpb.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        user = users.get(userId);
        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            configuration.getLogger().info(Messages.getString("hpb.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
            return keyManager.sendHPB();
        } catch (final Exception e) {
            throw new EbicsException(Messages.getString("hpb.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
        }
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param userId  the user ID
     * @param product the session product
     */
    public void revokeSubscriber(final String userId, final Product product) {
        final EbicsUser user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("spr.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        user = users.get(userId);
        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            keyManager.lockAccess();
        } catch (final Exception e) {
            configuration.getLogger().error(Messages.getString("spr.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
            return;
        }

        configuration.getLogger().info(Messages.getString("spr.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
    }

    /**
     * Sends a file to the ebics bank sever
     *
     * @param path    the file path to send
     * @param user    the user that sends the file.
     * @param product the application product.
     */
    public void uploadSepaDirectDebit(final String path, final EbicsUser user, final Product product) throws EbicsException {
        final EbicsSession session = new EbicsSession(user, configuration);
        session.addSessionParam("FORMAT", "pain.008.001.02");
        //session.addSessionParam("TEST", "true");
        //session.addSessionParam("EBCDIC", "false");
        session.setProduct(product);
        final FileTransfer transferManager = new FileTransfer(session);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        transferManager.sendFile(IOUtils.getFileContent(path), OrderType.CDD);
    }

    public void fetchFile(final String path,
                          final String userId,
                          final Product product,
                          final OrderType orderType,
                          final boolean isTest,
                          final LocalDate start,
                          final LocalDate end) {
        final FileTransfer transferManager;
        final EbicsSession session;

        session = new EbicsSession(users.get(userId), configuration);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        session.setProduct(product);
        transferManager = new FileTransfer(session);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(users.get(userId)));

        try {
            transferManager.fetchFile(orderType, start, end, new FileOutputStream(path));
        } catch (final IOException | EbicsException e) {
            configuration.getLogger().error(Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Sets the application configuration
     *
     * @param configuration the configuration
     */
    public void setConfiguration(final EbicsConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    public void quit() {
        try {
            for (final EbicsUser user : users.values()) {
                if (user.needsSave()) {
                    configuration.getLogger().info(Messages.getString("app.quit.users", Constants.APPLICATION_BUNDLE_NAME, user.getUserId()));
                    configuration.getSerializationManager().serialize(user);
                }
            }

            for (final EbicsPartner partner : partners.values()) {
                if (partner.needsSave()) {
                    configuration.getLogger().info(Messages.getString("app.quit.partners", Constants.APPLICATION_BUNDLE_NAME, partner.getPartnerId()));
                    configuration.getSerializationManager().serialize(partner);
                }
            }

            for (final EbicsBank bank : banks.values()) {
                if (bank.needsSave()) {
                    configuration.getLogger().info(Messages.getString("app.quit.banks", Constants.APPLICATION_BUNDLE_NAME, bank.getHostId()));
                    configuration.getSerializationManager().serialize(bank);
                }
            }
        } catch (final EbicsException e) {
            configuration.getLogger().info(Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME));
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private EbicsConfiguration configuration;
    private Map<String, EbicsUser> users;
    private Map<String, EbicsPartner> partners;
    private Map<String, EbicsBank> banks;

    static {
        org.apache.xml.security.Init.init();
        java.security.Security.addProvider(new BouncyCastleProvider());
    }
}
