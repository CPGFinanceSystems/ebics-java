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

package de.cpg.oss.ebics.api;

import java.util.Locale;


/**
 * EBICS client application configuration.
 *
 * @author hachani
 */
public interface EbicsConfiguration {

    /**
     * Returns the root directory of the client application.
     *
     * @return the root directory of the client application.
     */
    String getRootDirectory();

    /**
     * Returns the directory path that contains the traces
     * XML transfer files.
     *
     * @param user the ebics user
     * @return the transfer trace directory
     */
    String getTransferTraceDirectory(EbicsUser user);

    /**
     * Returns the object serialization directory.
     *
     * @return the object serialization directory.
     */
    String getSerializationDirectory();

    /**
     * Returns the users directory.
     *
     * @return the users directory.
     */
    String getUsersDirectory();

    /**
     * Returns the Ebics client serialization manager.
     *
     * @return the Ebics client serialization manager.
     */
    SerializationManager getSerializationManager();

    /**
     * Returns the Ebics client trace manager.
     *
     * @return the Ebics client trace manager.
     */
    TraceManager getTraceManager();

    /**
     * Returns the letter manager.
     *
     * @return the letter manager.
     */
    LetterManager getLetterManager();

    /**
     * Returns the initializations letters directory.
     *
     * @return the initializations letters directory.
     */
    String getLettersDirectory(EbicsUser user);

    /**
     * Returns the users directory.
     *
     * @return the users directory.
     */
    String getUserDirectory(EbicsUser user);

    /**
     * Configuration initialization.
     * Creates the necessary directories for the ebics configuration.
     */
    void init();

    /**
     * Returns the application locale.
     *
     * @return the application locale.
     */
    Locale getLocale();

    /**
     * Returns the client application signature version
     *
     * @return the signature version
     */
    String getSignatureVersion();

    /**
     * Returns the client application authentication version
     *
     * @return the authentication version
     */
    String getAuthenticationVersion();

    /**
     * Returns the client application encryption version
     *
     * @return the encryption version
     */
    String getEncryptionVersion();

    /**
     * Returns if the files to be transferred should be
     * compressed or sent without compression. This can
     * affect the time of data upload especially for big
     * files
     *
     * @return true if the file compression is enabled
     */
    boolean isCompressionEnabled();

    /**
     * Returns the default revision of sent XML.
     *
     * @return the default revision of sent XML.
     */
    int getRevision();

    /**
     * Returns the version of the EBICS protocol used by the client.
     *
     * @return the version of the EBICS protocol.
     */
    String getVersion();
}
