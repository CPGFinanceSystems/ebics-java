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

import java.io.IOException;


/**
 * A mean to serialize and deserialize <code>Object</code>.
 * The manager should ensure serialization and deserialization
 * operations
 *
 * @author hachani
 */
public interface SerializationManager {

    <T extends Identifiable> T serialize(Class<T> clazz, T object) throws IOException;

    <T extends Identifiable> T deserialize(Class<T> clazz, String id) throws IOException;

    boolean delete(Identifiable identifiable) throws IOException;
}
