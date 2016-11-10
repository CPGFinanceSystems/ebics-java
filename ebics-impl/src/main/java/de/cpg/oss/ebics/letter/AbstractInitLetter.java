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

package de.cpg.oss.ebics.letter;

import de.cpg.oss.ebics.api.InitLetter;
import de.cpg.oss.ebics.api.Messages;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


abstract class AbstractInitLetter implements InitLetter {

    /**
     * Constructs a new initialization letter.
     *
     * @param locale the application locale
     */
    public AbstractInitLetter(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public void save(final OutputStream output) throws IOException {
        output.write(letter.getLetter());
        output.flush();
        output.close();
    }

    /**
     * Builds an initialization letter.
     *
     * @param hostId      the host ID
     * @param bankName    the bank name
     * @param userId      the user ID
     * @param username    the user name
     * @param partnerId   the partner ID
     * @param version     the signature version
     * @param pubKeyTitle the public key title
     * @param publicKey   the public key
     * @param hashTitle   the hash title
     * @param hash        the hash value
     * @throws IOException
     */
    protected void build(final String hostId,
                         final String bankName,
                         final String userId,
                         final String username,
                         final String partnerId,
                         final String version,
                         final String pubKeyTitle,
                         final PublicKey publicKey,
                         final String hashTitle,
                         final byte[] hash)
            throws IOException {
        letter = new Letter(getTitle(),
                hostId,
                bankName,
                userId,
                username,
                partnerId,
                version);
        letter.build(pubKeyTitle, publicKey, hashTitle, hash);
    }

    /**
     * Returns the value of the property key.
     *
     * @param key    the property key
     * @param locale the bundle locale
     * @return the property value
     */
    protected String getString(final String key, final Locale locale) {
        return Messages.getString(key, AbstractInitLetter.BUNDLE_NAME, locale);
    }

    // --------------------------------------------------------------------
    // INNER CLASS
    // --------------------------------------------------------------------

    /**
     * The <code>Letter</code> object is the common template
     * for all initialization letter.
     *
     * @author Hachani
     */
    private class Letter {

        /**
         * Constructs new <code>Letter</code> template
         *
         * @param title     the letter title
         * @param hostId    the host ID
         * @param bankName  the bank name
         * @param userId    the user ID
         * @param partnerId the partner ID
         * @param version   the signature version
         */
        public Letter(final String title,
                      final String hostId,
                      final String bankName,
                      final String userId,
                      final String username,
                      final String partnerId,
                      final String version) {
            this.title = title;
            this.hostId = hostId;
            this.bankName = bankName;
            this.userId = userId;
            this.username = username;
            this.partnerId = partnerId;
            this.version = version;
        }

        /**
         * Builds the letter content.
         *
         * @param pubKeyTitle the public key title
         * @param publicKey   the public key
         * @param hashTitle   the hash title
         * @param hash        the hash content
         * @throws IOException
         */
        public void build(final String pubKeyTitle,
                          final PublicKey publicKey,
                          final String hashTitle,
                          final byte[] hash)
                throws IOException {
            out = new ByteArrayOutputStream();
            writer = new PrintWriter(out, true);
            buildTitle();
            buildHeader();
            buildPubKey(pubKeyTitle, publicKey);
            buildHash(hashTitle, hash);
            buildFooter();
            writer.close();
            out.flush();
            out.close();
        }

        /**
         * Builds the letter title.
         *
         * @throws IOException
         */
        public void buildTitle() throws IOException {
            emit(title);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
        }

        /**
         * Builds the letter header
         *
         * @throws IOException
         */
        public void buildHeader() throws IOException {
            emit(Messages.getString("Letter.date", BUNDLE_NAME, locale));
            appendSpacer();
            emit(formatDate(new Date()));
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.time", BUNDLE_NAME, locale));
            appendSpacer();
            emit(formatTime(new Date()));
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.hostId", BUNDLE_NAME, locale));
            appendSpacer();
            emit(hostId);
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.bank", BUNDLE_NAME, locale));
            appendSpacer();
            emit(bankName);
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.userId", BUNDLE_NAME, locale));
            appendSpacer();
            emit(userId);
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.username", BUNDLE_NAME, locale));
            appendSpacer();
            emit(username);
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.partnerId", BUNDLE_NAME, locale));
            appendSpacer();
            emit(partnerId);
            emit(LINE_SEPARATOR);
            emit(Messages.getString("Letter.version", BUNDLE_NAME, locale));
            appendSpacer();
            emit(version);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
        }

        public void buildPubKey(final String title, final PublicKey publicKey) throws IOException {
            final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            emit(title);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit("Exponent" + LINE_SEPARATOR);
            emit(Hex.encodeHexString(rsaPublicKey.getPublicExponent().toByteArray()).toUpperCase());
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit("Modulus" + LINE_SEPARATOR);
            emit(Hex.encodeHexString(rsaPublicKey.getModulus().toByteArray()).toUpperCase());
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
        }

        /**
         * Builds the hash section.
         *
         * @param title the title
         * @param hash  the hash value
         * @throws IOException
         */
        public void buildHash(final String title, final byte[] hash)
                throws IOException {
            emit(title);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(Hex.encodeHexString(hash).toUpperCase());
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
            emit(LINE_SEPARATOR);
        }

        /**
         * Builds the footer section
         *
         * @throws IOException
         */
        public void buildFooter() throws IOException {
            emit(Messages.getString("Letter.date", BUNDLE_NAME, locale));
            emit("                                  ");
            emit(Messages.getString("Letter.signature", BUNDLE_NAME, locale));
        }

        /**
         * Appends a spacer
         *
         * @throws IOException
         */
        public void appendSpacer() throws IOException {
            emit("        ");
        }

        /**
         * Emits a text to the writer
         *
         * @param text the text to print
         * @throws IOException
         */
        public void emit(final String text) throws IOException {
            writer.write(text);
        }

        /**
         * Formats the input date
         *
         * @param date the input date
         * @return the formatted date
         */
        public String formatDate(final Date date) {
            final SimpleDateFormat formatter;

            formatter = new SimpleDateFormat(Messages.getString("Letter.dateFormat", BUNDLE_NAME, locale), locale);
            return formatter.format(date);
        }

        /**
         * Formats the input time
         *
         * @param time the input time
         * @return the formatted time
         */
        public String formatTime(final Date time) {
            final SimpleDateFormat formatter;

            formatter = new SimpleDateFormat(Messages.getString("Letter.timeFormat", BUNDLE_NAME, locale), locale);
            return formatter.format(time);
        }

        /**
         * Returns the letter content
         *
         * @return
         */
        public byte[] getLetter() {
            return out.toByteArray();
        }

        // --------------------------------------------------------------------
        // DATA MEMBERS
        // --------------------------------------------------------------------

        private ByteArrayOutputStream out;
        private Writer writer;
        private final String title;
        private final String hostId;
        private final String bankName;
        private final String userId;
        private final String username;
        private final String partnerId;
        private final String version;
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private Letter letter;
    protected Locale locale;

    protected static final String BUNDLE_NAME = "de.cpg.oss.ebics.letter.messages";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
}
