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

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;
import org.kopi.ebics.interfaces.Identifiable;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;

/**
 * Details about EBICS communication with a given bank.
 *
 * @author Hachani
 */
@Value
@Builder
@Wither
public class EbicsBank implements Identifiable {

    private static final long serialVersionUID = 1L;

    private final URI uri;

    private final byte[] e002Digest;
    private final byte[] x002Digest;

    private final RSAPublicKey e002Key;
    private final RSAPublicKey x002Key;

    private final String hostId;
    private final String name;

    @Override
    public String getId() {
        return getHostId();
    }
}
