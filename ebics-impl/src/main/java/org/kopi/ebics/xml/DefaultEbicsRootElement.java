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

package org.kopi.ebics.xml;

import org.kopi.ebics.session.OrderType;

import java.math.BigInteger;
import java.security.SecureRandom;


public abstract class DefaultEbicsRootElement {

    /**
     * Generates a random file name with a prefix.
     *
     * @param type the order type.
     * @return the generated file name.
     */
    public static String generateName(final OrderType type) {
        return generateName(type.name());
    }

    /**
     * Generates a random file name with a prefix.
     *
     * @param prefix the prefix to use.
     * @return the generated file name.
     */
    public static String generateName(final String prefix) {
        return prefix + new BigInteger(130, new SecureRandom()).toString(32);
    }
}
