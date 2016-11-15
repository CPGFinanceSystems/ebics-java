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

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.Splitter;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.Getter;
import org.ebics.h004.*;

import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;


/**
 * The <code>UInitializationRequestElement</code> is the common initialization
 * element for all ebics file uploads.
 *
 * @author Hachani
 */
public class UInitializationRequestElement extends InitializationRequestElement {

    private final OrderType orderType;
    private final byte[] userData;
    @Getter
    private final Splitter splitter;

    /**
     * Constructs a new <code>UInitializationRequestElement</code> for uploads initializations.
     *
     * @param session   the current ebics session.
     * @param orderType the upload order type
     * @param userData  the user data to be uploaded
     */
    public UInitializationRequestElement(final EbicsSession session,
                                         final OrderType orderType,
                                         final byte[] userData) {
        super(session);
        this.orderType = orderType;
        this.userData = userData;
        this.splitter = new Splitter(userData);
    }

    @Override
    public EbicsRequest buildEbicsRequest() throws EbicsException {
        final byte[] nonce = CryptoUtil.generateNonce();
        final SecretKeySpec keySpec = new SecretKeySpec(nonce, "AES");

        splitter.readInput(session.getConfiguration().isCompressionEnabled(), keySpec);

        final List<Parameter> parameters = new ArrayList<>();
        if (Boolean.valueOf(session.getSessionParam("TEST"))) {
            final Parameter.Value value = OBJECT_FACTORY.createParameterValue();
            value.setType("String");
            value.setValue("TRUE");

            final Parameter parameter = OBJECT_FACTORY.createParameter();
            parameter.setName("TEST");
            parameter.setValue(value);

            parameters.add(parameter);
        }
        if (Boolean.valueOf(session.getSessionParam("EBCDIC"))) {
            final Parameter.Value value = OBJECT_FACTORY.createParameterValue();
            value.setType("String");
            value.setValue("TRUE");

            final Parameter parameter = OBJECT_FACTORY.createParameter();
            parameter.setName("EBCDIC");
            parameter.setValue(value);

            parameters.add(parameter);
        }

        final StaticHeaderOrderDetailsType orderDetails;
        if (orderType.equals(OrderType.FUL)) {
            final FileFormatType fileFormat = OBJECT_FACTORY.createFileFormatType();
            fileFormat.setCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase());
            fileFormat.setValue(session.getSessionParam("FORMAT"));

            final FULOrderParamsType fULOrderParams = OBJECT_FACTORY.createFULOrderParamsType();
            fULOrderParams.setFileFormat(fileFormat);
            if (parameters.size() > 0) {
                fULOrderParams.getParameters().addAll(parameters);
            }

            orderDetails = orderDetails(
                    OrderAttributeType.DZHNN,
                    orderType,
                    OBJECT_FACTORY.createFULOrderParams(fULOrderParams));
        } else {
            orderDetails = orderDetails(OrderAttributeType.OZHNN, orderType);
        }

        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session, nonce, splitter.getNumSegments(), orderDetails)),
                body(
                        dataTransferRequest(session, userData, keySpec, generateTransactionKey(nonce))));
    }
}
