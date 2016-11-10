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

package de.cpg.oss.ebics.io;

import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A simple mean to join downloaded segments from the
 * bank ebics server.
 *
 * @author Hachani
 */
public class Joiner {

    /**
     * Constructs a new <code>Joiner</code> object.
     *
     * @param user the ebics user.
     */
    public Joiner(final EbicsUser user) {
        this.user = user;
        buffer = new ByteArrayOutputStream();
    }

    public void append(final byte[] data) throws EbicsException {
        try {
            buffer.write(data);
            buffer.flush();
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage());
        }
    }

    /**
     * Writes the joined part to an output stream.
     *
     * @param output         the output stream.
     * @param transactionKey the transaction key
     */
    public void writeTo(final OutputStream output, final byte[] transactionKey) {
        try {

            buffer.close();
            output.write(ZipUtil.uncompress(CryptoUtil.decrypt(
                    buffer.toByteArray(),
                    transactionKey,
                    user.getE002Key().getPrivate())));
            output.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final EbicsUser user;
    private final ByteArrayOutputStream buffer;
}
