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

import lombok.Getter;

/**
 * A BCS order type.
 *
 * @author Hachani
 */
public enum OrderType {
    INI(Transmission.UPLOAD, "Send password initialisation", Presence.MANDATORY),
    HIA(Transmission.UPLOAD, "Transmission of the subscriber key for identification and authentication and encryption within the framework of subscriber initialisation", Presence.MANDATORY),
    HPB(Transmission.DOWNLOAD, "Transfer the public bank key", Presence.MANDATORY),
    // HPD, // Return bank parameters
    // HTD, // Fetch user information
    FUL(Transmission.UPLOAD, "Upload file with any format"),
    FDL(Transmission.DOWNLOAD, "Download file with any format"),
    SPR(Transmission.UPLOAD, "Suspension of access authorisation", Presence.MANDATORY),

    CDD(Transmission.UPLOAD, "Upload direct debit initiation (SEPA core direct debit)");

    OrderType(final Transmission transmission, final String description, final Presence presence) {
        this.transmission = transmission;
        this.description = description;
        this.presence = presence;
    }

    OrderType(final Transmission transmission, final String description) {
        this(transmission, description, Presence.OPTIONAL);
    }

    public enum Transmission {
        DOWNLOAD,
        UPLOAD
    }

    public boolean isMandatory(final boolean germanBank) {
        return Presence.MANDATORY.equals(presence) || (Presence.CONDITIONAL.equals(presence) && germanBank);
    }

    @Getter
    private final Transmission transmission;
    @Getter
    private final String description;
    private final Presence presence;

    private enum Presence {
        MANDATORY,
        OPTIONAL,
        CONDITIONAL
    }
}
