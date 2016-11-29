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

import javaslang.control.Either;
import lombok.*;
import lombok.experimental.Wither;

import java.util.Collection;
import java.util.stream.Collectors;

@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EbicsUser implements Identifiable {

    private static final long serialVersionUID = 4L;

    private final EbicsSignatureKey signatureKey;
    private final EbicsEncryptionKey encryptionKey;
    private final EbicsAuthenticationKey authenticationKey;

    private final String securityMedium;

    @NonNull
    private final String userId;
    private final String name;

    @NonNull
    private final UserStatus status;
    private final Collection<String> permittedOrderTypes;

    private final transient PasswordCallback passwordCallback;

    @Override
    public String getId() {
        return getUserId();
    }

    public Collection<Either<OrderType, String>> getPermittedOrderTypes() {
        return permittedOrderTypes.stream()
                .map(OrderType::ofRaw)
                .collect(Collectors.toList());
    }

    // We all love JPA, don't we?
    private EbicsUser() {
        this(null, null, null, null, "", null, UserStatus.NEW, null, null);
    }
}
