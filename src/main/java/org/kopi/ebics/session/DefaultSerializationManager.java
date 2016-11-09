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

import org.kopi.ebics.interfaces.Identifiable;
import org.kopi.ebics.interfaces.SerializationManager;
import org.kopi.ebics.io.IOUtils;

import java.io.*;
import java.text.MessageFormat;


/**
 * A simple implementation of the <code>SerializationManager</code>.
 * The serialization process aims to save object on the user disk
 * using a separated file for each object to serialize.
 *
 * @author hachani
 */
public class DefaultSerializationManager implements SerializationManager {

    private final File serializationDir;

    /**
     * Constructs a new <code>SerializationManage</code>
     *
     * @param serializationDir the serialization directory
     */
    public DefaultSerializationManager(final File serializationDir) {
        this.serializationDir = serializationDir;
    }

    @Override
    public void serialize(final Identifiable object) throws IOException {
        final ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(IOUtils.createFile(serializationDir, object.getId())));
        out.writeObject(object);
    }

    @Override
    public <T extends Identifiable> T deserialize(final Class<T> clazz, final String id) throws IOException {
        final ObjectInputStream objectInputStream = new ObjectInputStream(
                new FileInputStream(IOUtils.createFile(serializationDir, id)));
        try {
            final Object object = objectInputStream.readObject();
            if (clazz.isAssignableFrom(object.getClass())) {
                return clazz.cast(object);
            } else {
                throw new IOException(MessageFormat.format(
                        "Invalid object class! Expected {0}, got {1} for {2}",
                        clazz.getName(), object.getClass().getName(), id));
            }
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
