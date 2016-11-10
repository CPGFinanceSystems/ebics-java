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

package org.kopi.ebics.session;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.kopi.ebics.client.EbicsUser;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsConfiguration;
import org.kopi.ebics.interfaces.LetterManager;
import org.kopi.ebics.interfaces.SerializationManager;
import org.kopi.ebics.interfaces.TraceManager;
import org.kopi.ebics.io.IOUtils;
import org.kopi.ebics.letter.DefaultLetterManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;


/**
 * A simple client application configuration.
 *
 * @author hachani
 */
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultEbicsConfiguration implements EbicsConfiguration {

    private static final String RESOURCE_DIR = "org.kopi.ebics.client.config";

    private final String rootDir;
    private final ResourceBundle bundle;
    private final Properties properties;
    private final SerializationManager serializationManager;
    private final TraceManager traceManager;
    private final LetterManager letterManager;

    /**
     * Creates a new application configuration.
     *
     * @param rootDir the root directory
     */
    public DefaultEbicsConfiguration(final String rootDir) {
        this.rootDir = rootDir;
        this.bundle = ResourceBundle.getBundle(RESOURCE_DIR);
        this.properties = new Properties();
        this.serializationManager = new DefaultSerializationManager(new File(getSerializationDirectory()));
        this.traceManager = new DefaultTraceManager();
        this.letterManager = new DefaultLetterManager(getLocale());
    }

    /**
     * Creates a new application configuration.
     * The root directory will be user.home/ebics/client
     */
    public DefaultEbicsConfiguration() {
        this(System.getProperty("user.home") + File.separator + "ebics" + File.separator + "client");
    }

    /**
     * Returns the corresponding property of the given key
     *
     * @param key the property key
     * @return the property value.
     */
    private String getString(final String key) {
        try {
            return bundle.getString(key);
        } catch (final MissingResourceException e) {
            return "!!" + key + "!!";
        }
    }

    /**
     * Loads the configuration
     *
     * @throws EbicsException
     */
    public void load(final String configFile) throws EbicsException {
        try {
            properties.load(new FileInputStream(new File(configFile)));
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage(), e);
        }
    }

    @Override
    public String getRootDirectory() {
        return rootDir;
    }

    @Override
    public void init() {
        //Create the root directory
        IOUtils.createDirectories(getRootDirectory());
        //Create the logs directory
        IOUtils.createDirectories(getLogDirectory());
        //Create the serialization directory
        IOUtils.createDirectories(getSerializationDirectory());
        //create the SSL trusted stores directories
        IOUtils.createDirectories(getSSLTrustedStoreDirectory());
        //create the SSL key stores directories
        IOUtils.createDirectories(getSSLKeyStoreDirectory());
        //Create the SSL bank certificates directories
        IOUtils.createDirectories(getSSLBankCertificates());
        //Create users directory
        IOUtils.createDirectories(getUsersDirectory());
    }

    @Override
    public Locale getLocale() {
        return Locale.US;
    }

    @Override
    public String getLogDirectory() {
        return rootDir + File.separator + getString("log.dir.name");
    }

    @Override
    public String getLogFileName() {
        return getString("log.file.name");
    }

    @Override
    public String getConfigurationFile() {
        return rootDir + File.separator + getString("conf.file.name");
    }

    @Override
    public String getProperty(final String key) {
        if (key == null) {
            return null;
        }

        return properties.getProperty(key);
    }

    @Override
    public String getKeystoreDirectory(final EbicsUser user) {
        return getUserDirectory(user) + File.separator + getString("keystore.dir.name");
    }

    @Override
    public String getTransferTraceDirectory(final EbicsUser user) {
        return getUserDirectory(user) + File.separator + getString("traces.dir.name");
    }

    @Override
    public String getSerializationDirectory() {
        return rootDir + File.separator + getString("serialization.dir.name");
    }

    @Override
    public String getSSLTrustedStoreDirectory() {
        return rootDir + File.separator + getString("ssltruststore.dir.name");
    }

    @Override
    public String getSSLKeyStoreDirectory() {
        return rootDir + File.separator + getString("sslkeystore.dir.name");
    }

    @Override
    public String getSSLBankCertificates() {
        return rootDir + File.separator + getString("sslbankcert.dir.name");
    }

    @Override
    public String getUsersDirectory() {
        return rootDir + File.separator + getString("users.dir.name");
    }

    @Override
    public SerializationManager getSerializationManager() {
        return serializationManager;
    }

    @Override
    public TraceManager getTraceManager() {
        return traceManager;
    }

    @Override
    public LetterManager getLetterManager() {
        return letterManager;
    }

    @Override
    public String getLettersDirectory(final EbicsUser user) {
        return getUserDirectory(user) + File.separator + getString("letters.dir.name");
    }

    @Override
    public String getUserDirectory(final EbicsUser user) {
        return getUsersDirectory() + File.separator + user.getUserId();
    }

    @Override
    public String getSignatureVersion() {
        return getString("signature.version");
    }

    @Override
    public String getAuthenticationVersion() {
        return getString("authentication.version");
    }

    @Override
    public String getEncryptionVersion() {
        return getString("encryption.version");
    }

    @Override
    public boolean isCompressionEnabled() {
        return true;
    }

    @Override
    public int getRevision() {
        return 1;
    }

    @Override
    public String getVersion() {
        return getString("ebics.version");
    }
}
