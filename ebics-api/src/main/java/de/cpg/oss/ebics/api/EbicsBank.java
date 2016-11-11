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

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.net.URI;

/**
 * Details about EBICS communication with a given bank.
 *
 * @author Hachani
 */
@Value
@Wither
@Builder
public class EbicsBank implements Identifiable {

    private static final long serialVersionUID = 2L;

    @NonNull
    private final URI uri;

    private final EbicsRsaKey<AuthenticationVersion> authenticationKey;
    private final EbicsRsaKey<EncryptionVersion> encryptionKey;

    @NonNull
    private final String hostId;
    private final String name;

    @Override
    public String getId() {
        return getHostId();
    }
}
