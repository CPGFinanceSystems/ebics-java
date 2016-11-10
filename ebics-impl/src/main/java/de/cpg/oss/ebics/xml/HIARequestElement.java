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

package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.session.EbicsSession;
import de.cpg.oss.ebics.session.OrderType;
import de.cpg.oss.ebics.utils.ZipUtil;
import org.ebics.h004.EbicsUnsecuredRequest;
import org.ebics.h004.HIARequestOrderDataType;

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
                ZipUtil.compress(XmlUtils.prettyPrint(HIARequestOrderDataType.class, orderDataType)));
        return unsecuredRequest.build();
    }
}
