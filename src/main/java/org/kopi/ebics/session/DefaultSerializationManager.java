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

import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.Savable;
import org.kopi.ebics.interfaces.SerializationManager;
import org.kopi.ebics.io.IOUtils;

import java.io.*;


/**
 * A simple implementation of the <code>SerializationManager</code>.
 * The serialization process aims to save object on the user disk
 * using a separated file for each object to serialize.
 *
 * @author hachani
 */
public class DefaultSerializationManager implements SerializationManager {

    /**
     * Constructs a new <code>SerializationManage</code>
     *
     * @param serializationDir the serialization directory
     */
    public DefaultSerializationManager(final File serializationDir) {
        this.serializationDir = serializationDir;
    }

    /**
     * Constructs a new <code>SerializationManage</code>
     */
    public DefaultSerializationManager() {
        this(null);
    }

    @Override
    public void serialize(final Savable object) throws EbicsException {
        try {
            final ObjectOutputStream out;

            out = new ObjectOutputStream(new FileOutputStream(IOUtils.createFile(serializationDir, object.getSaveName())));
            object.save(out);
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage());
        }
    }

    @Override
    public ObjectInputStream deserialize(final String name) throws EbicsException {
        try {
            final ObjectInputStream input;

            input = new ObjectInputStream(new FileInputStream(IOUtils.createFile(serializationDir, name + ".cer")));
            return input;
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage());
        }
    }

    @Override
    public void setSerializationDirectory(final String serializationDir) {
        this.serializationDir = new File(serializationDir);
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private File serializationDir;
}
