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
import org.kopi.ebics.security.UserPasswordHandler;
import org.kopi.ebics.session.DefaultConfiguration;
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
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * The ebics client application. Performs necessary tasks to contact
 * the ebics bank server like sending the INI, HIA and HPB requests
 * for keys retrieval and also performs the files transfer including
 * uploads and downloads.
 *
 * @author hachani
 */
public class Application {

    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     */
    public Application(final Configuration configuration) {
        this.configuration = configuration;
        users = new Hashtable<String, User>();
        partners = new Hashtable<String, Partner>();
        banks = new Hashtable<String, Bank>();
        Messages.setLocale(configuration.getLocale());
    }

    /**
     * Constructs a new ebics client application.
     */
    public Application() {
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
    public void createUserDirectories(final EbicsUser user) {
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
    public Bank createBank(final URL url, final String name, final String hostId) {
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
    public Partner createPartner(final EbicsBank bank, final String partnerId) {
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
    public User createUser(final URL url,
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
        final Bank bank;
        final Partner partner;
        final User user;
        final InitLetter a005Letter;
        final InitLetter x002Letter;
        final InitLetter e002Letter;

        configuration.getLogger().info(Messages.getString("user.create.info", Constants.APPLICATION_BUNDLE_NAME, userId));

        bank = createBank(url, bankName, hostId);
        partner = createPartner(bank, partnerId);
        try {
            user = new User(partner, userId, name, email, country, organization, passwordCallback);
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
    public User loadUser(final String hostId,
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
    public User sendINIRequest(final String userId, final Product product) throws EbicsException {
        final User user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("ini.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        user = users.get(userId);
        user.setInitialized(false);

        if (user.isInitialized()) {
            configuration.getLogger().info(Messages.getString("user.already.initialized", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        }

        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            keyManager.sendINI(null);
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("ini.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
        }

        user.setInitialized(true);
        configuration.getLogger().info(Messages.getString("ini.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
        return user;
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param userId  the user ID.
     * @param product the application product.
     */
    public User sendHIARequest(final String userId, final Product product) throws EbicsException {
        final User user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("hia.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));
        user = users.get(userId);
        user.setInitializedHIA(false);
        if (user.isInitializedHIA()) {
            configuration.getLogger().info(Messages.getString("user.already.hia.initialized", Constants.APPLICATION_BUNDLE_NAME, userId));
            return user;
        }
        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);
        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            keyManager.sendHIA(null);
        } catch (final IOException e) {
            throw new EbicsException(Messages.getString("hia.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
        }

        user.setInitializedHIA(true);
        configuration.getLogger().info(Messages.getString("hia.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
        return user;
    }

    /**
     * Sends a HPB request to the ebics server.
     *
     * @param userId  the user ID.
     * @param product the application product.
     */
    public void sendHPBRequest(final String userId, final Product product) {
        final User user;
        final EbicsSession session;
        final KeyManagement keyManager;

        configuration.getLogger().info(Messages.getString("hpb.request.send", Constants.APPLICATION_BUNDLE_NAME, userId));

        user = users.get(userId);
        session = new EbicsSession(user, configuration);
        session.setProduct(product);
        keyManager = new KeyManagement(session);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(user));

        try {
            keyManager.sendHPB();
        } catch (final Exception e) {
            configuration.getLogger().error(Messages.getString("hpb.send.error", Constants.APPLICATION_BUNDLE_NAME, userId), e);
            return;
        }

        configuration.getLogger().info(Messages.getString("hpb.send.success", Constants.APPLICATION_BUNDLE_NAME, userId));
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param userId  the user ID
     * @param product the session product
     */
    public void revokeSubscriber(final String userId, final Product product) {
        final User user;
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
     * @param userId  the user ID that sends the file.
     * @param product the application product.
     */
    public void sendFile(final String path, final String userId, final Product product) {
        final FileTransfer transferManager;
        final EbicsSession session;

        session = new EbicsSession(users.get(userId), configuration);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        session.addSessionParam("TEST", "true");
        session.addSessionParam("EBCDIC", "false");
        session.setProduct(product);
        transferManager = new FileTransfer(session);

        configuration.getTraceManager().setTraceDirectory(configuration.getTransferTraceDirectory(users.get(userId)));

        try {
            transferManager.sendFile(IOUtils.getFileContent(path), OrderType.FUL);
        } catch (final IOException e) {
            configuration.getLogger().error(Messages.getString("upload.file.error", Constants.APPLICATION_BUNDLE_NAME, path), e);
        } catch (final EbicsException e) {
            configuration.getLogger().error(Messages.getString("upload.file.error", Constants.APPLICATION_BUNDLE_NAME, path), e);
        }
    }

    public void fetchFile(final String path,
                          final String userId,
                          final Product product,
                          final OrderType orderType,
                          final boolean isTest,
                          final Date start,
                          final Date end) {
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
        } catch (final IOException e) {
            configuration.getLogger().error(Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
        } catch (final EbicsException e) {
            configuration.getLogger().error(Messages.getString("download.file.error", Constants.APPLICATION_BUNDLE_NAME), e);
        }
    }

    /**
     * Sets the application configuration
     *
     * @param configuration the configuration
     */
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Performs buffers save before quitting the client application.
     */
    public void quit() {
        try {
            for (final User user : users.values()) {
                if (user.needsSave()) {
                    configuration.getLogger().info(Messages.getString("app.quit.users", Constants.APPLICATION_BUNDLE_NAME, user.getUserId()));
                    configuration.getSerializationManager().serialize(user);
                }
            }

            for (final Partner partner : partners.values()) {
                if (partner.needsSave()) {
                    configuration.getLogger().info(Messages.getString("app.quit.partners", Constants.APPLICATION_BUNDLE_NAME, partner.getPartnerId()));
                    configuration.getSerializationManager().serialize(partner);
                }
            }

            for (final Bank bank : banks.values()) {
                if (bank.needsSave()) {
                    configuration.getLogger().info(Messages.getString("app.quit.banks", Constants.APPLICATION_BUNDLE_NAME, bank.getHostId()));
                    configuration.getSerializationManager().serialize(bank);
                }
            }
        } catch (final EbicsException e) {
            configuration.getLogger().info(Messages.getString("app.quit.error", Constants.APPLICATION_BUNDLE_NAME));
        }

        configuration.getLogger().info(Messages.getString("app.cache.clear", Constants.APPLICATION_BUNDLE_NAME));
        //configuration.getTraceManager().clear();
    }

    // --------------------------------------------------------------------
    // JVM ENTRY POINT
    // --------------------------------------------------------------------

    /**
     * JVM program entry point
     *
     * @param args program arguments
     */
    public static void main(final String[] args) throws Exception {
        final DefaultConfiguration configuration;
        final PasswordCallback pwdHandler;
        final Application appli;
        final String userId;

        userId = "HEDI";
        configuration = new DefaultConfiguration();
        pwdHandler = new UserPasswordHandler(userId, "2012");
        appli = new Application(configuration);
        appli.init();

//    appli.createUser(new URL("https://server-ebics.webank.fr:28103/WbkPortalFileTransfert/EbicsProtocol"),
//                     "VALERIAN",
//	             "EBIXQUAL",
//	             "EBICS",
//	             userId,
//	             "pebics",
//	             "pebics@domaine.fr",
//	             "org",
//	             "Euro-Information",
//	             true,
//	             pwdHandler);
        final Product product = new Product("kopiLeft Dev 1.0", Locale.FRANCE, null);
        appli.loadUser("EBIXQUAL", "EBIX", userId, pwdHandler);
//    appli.sendINIRequest(userId, product);
//    appli.sendHIARequest(userId, product);
//    appli.sendHPBRequest(userId, product);
        appli.sendFile(System.getProperty("user.home") + File.separator + "test.txt", userId, product);
        //for (int i = 0; i < 10000; i++) {
//    appli.fetchFile(System.getProperty("user.home") + File.separator + "download.txt",
//	            userId,
//	            product,
//	            OrderType.FDL,
//	            true,
//	            null,
//	            null);
//    appli.revokeSubscriber(userId, product);
        appli.quit();
        //}
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private Configuration configuration;
    private Map<String, User> users;
    private Map<String, Partner> partners;
    private Map<String, Bank> banks;

    static {
        org.apache.xml.security.Init.init();
        java.security.Security.addProvider(new BouncyCastleProvider());
    }
}
