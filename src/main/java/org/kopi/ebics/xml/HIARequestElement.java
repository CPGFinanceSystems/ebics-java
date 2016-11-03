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

import org.ebics.h004.EbicsUnsecuredRequest;
import org.ebics.h004.HIARequestOrderDataType;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.utils.Utils;

/**
 * The <code>HIARequestElement</code> is the root element used
 * to send the authentication and encryption keys to the ebics
 * bank server
 *
 * @author hachani
 */
public class HIARequestElement {

    private final EbicsSession session;

    /**
     * Constructs a new HIA Request root element
     *
     * @param session the current ebics session
     */
    public HIARequestElement(final EbicsSession session) {
        this.session = session;
    }

    public String getName() {
        return "HIARequest.xml";
    }

    public EbicsUnsecuredRequest build() throws EbicsException {
        final HIARequestOrderDataElement requestOrderData = new HIARequestOrderDataElement(session);
        final HIARequestOrderDataType orderDataType = requestOrderData.build();
        final UnsecuredRequestElement unsecuredRequest = new UnsecuredRequestElement(session,
                OrderType.HIA,
                Utils.zip(XmlUtils.prettyPrint(HIARequestOrderDataType.class, orderDataType)));
        return unsecuredRequest.build();
    }

    /*
    public byte[] toByteArray() {
        setSaveSuggestedPrefixes("urn:org:ebics:H004", "");

        return unsecuredRequest.toByteArray();
    }

    public void validate() throws EbicsException {
        unsecuredRequest.validate();
    }
    */

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

}
