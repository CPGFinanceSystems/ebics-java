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
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.io.Splitter;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import org.ebics.h004.*;
import org.ebics.s001.UserSignatureDataSigBookType;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;


/**
 * The <code>UInitializationRequestElement</code> is the common initialization
 * element for all ebics file uploads.
 *
 * @author Hachani
 */
public class UInitializationRequestElement extends InitializationRequestElement {

    private final OrderType orderType;
    private final byte[] userData;
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

        final StaticHeaderOrderDetailsType orderDetails = OBJECT_FACTORY.createStaticHeaderOrderDetailsType();
        if (orderType.equals(OrderType.FUL)) {
            final FileFormatType fileFormat = OBJECT_FACTORY.createFileFormatType();
            fileFormat.setCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase());
            fileFormat.setValue(session.getSessionParam("FORMAT"));

            final FULOrderParamsType fULOrderParams = OBJECT_FACTORY.createFULOrderParamsType();
            fULOrderParams.setFileFormat(fileFormat);
            if (parameters.size() > 0) {
                fULOrderParams.getParameters().addAll(parameters);
            }

            orderDetails.setOrderAttribute(OrderAttributeType.DZHNN);
            orderDetails.setOrderType(EbicsXmlFactory.orderType(orderType));
            orderDetails.setOrderParams(OBJECT_FACTORY.createFULOrderParams(fULOrderParams));
        } else {
            final StandardOrderParamsType standardOrderParamsType = OBJECT_FACTORY.createStandardOrderParamsType();

            orderDetails.setOrderAttribute(OrderAttributeType.OZHNN);
            orderDetails.setOrderType(EbicsXmlFactory.orderType(orderType));
            orderDetails.setOrderParams(OBJECT_FACTORY.createStandardOrderParams(standardOrderParamsType));
        }

        final DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest = OBJECT_FACTORY.createDataEncryptionInfoTypeEncryptionPubKeyDigest();
        encryptionPubKeyDigest.setVersion(session.getBank().getEncryptionKey().getVersion().name());
        encryptionPubKeyDigest.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        encryptionPubKeyDigest.setValue(session.getBank().getEncryptionKey().getDigest());

        final UserSignature userSignature = new UserSignature(session,
                session.getUser().getSignatureKey().getVersion(),
                userData);
        final JAXBElement<UserSignatureDataSigBookType> userSignatureElement = userSignature.build();

        final DataTransferRequestType.SignatureData signatureData = OBJECT_FACTORY.createDataTransferRequestTypeSignatureData();
        signatureData.setAuthenticate(true);
        signatureData.setValue(CryptoUtil.encrypt(ZipUtil.compress(XmlUtils.prettyPrint(userSignatureElement)), keySpec));

        final DataTransferRequestType.DataEncryptionInfo dataEncryptionInfo = OBJECT_FACTORY.createDataTransferRequestTypeDataEncryptionInfo();
        dataEncryptionInfo.setAuthenticate(true);
        dataEncryptionInfo.setEncryptionPubKeyDigest(encryptionPubKeyDigest);
        dataEncryptionInfo.setTransactionKey(generateTransactionKey(nonce));

        final DataTransferRequestType dataTransfer = OBJECT_FACTORY.createDataTransferRequestType();
        dataTransfer.setDataEncryptionInfo(dataEncryptionInfo);
        dataTransfer.setSignatureData(signatureData);

        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();
        body.setDataTransfer(dataTransfer);

        return EbicsXmlFactory.request(
                session.getConfiguration(),
                EbicsXmlFactory.header(
                        EbicsXmlFactory.mutableHeader(TransactionPhaseType.INITIALISATION),
                        EbicsXmlFactory.staticHeader(session, nonce, splitter.getNumSegments(), orderDetails)),
                body);
    }

    /**
     * Returns the content of a given segment.
     *
     * @param segment the segment number
     * @return the content of the given segment
     */
    public ContentFactory getContent(final int segment) {
        return splitter.getContent(segment);
    }

    /**
     * Returns the total segment number.
     *
     * @return the total segment number.
     */
    public int getSegmentNumber() {
        return splitter.getNumSegments();
    }
}
