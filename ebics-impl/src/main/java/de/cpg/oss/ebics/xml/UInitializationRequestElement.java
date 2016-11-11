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
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * The <code>UInitializationRequestElement</code> is the common initialization
 * element for all ebics file uploads.
 *
 * @author Hachani
 */
public class UInitializationRequestElement extends InitializationRequestElement {

    private final byte[] userData;
    private final SecretKeySpec keySpec;
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
        super(session, orderType);
        this.userData = userData;
        keySpec = new SecretKeySpec(nonce, "AES");
        splitter = new Splitter(userData);
    }

    @Override
    public EbicsRequest buildInitialization() throws EbicsException {
        splitter.readInput(session.getConfiguration().isCompressionEnabled(), keySpec);

        final MutableHeaderType mutable = OBJECT_FACTORY.createMutableHeaderType();
        mutable.setTransactionPhase(TransactionPhaseType.INITIALISATION);

        final StaticHeaderType.Product product = OBJECT_FACTORY.createStaticHeaderTypeProduct();
        product.setLanguage(session.getProduct().getLanguage());
        product.setValue(session.getProduct().getName());

        final StaticHeaderType.BankPubKeyDigests.Authentication authentication = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsAuthentication();
        authentication.setVersion(session.getConfiguration().getAuthenticationVersion().name());
        authentication.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        authentication.setValue(session.getBank().getAuthenticationKey().getDigest());

        final StaticHeaderType.BankPubKeyDigests.Encryption encryption = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsEncryption();
        encryption.setVersion(session.getConfiguration().getEncryptionVersion().name());
        encryption.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        encryption.setValue(session.getBank().getEncryptionKey().getDigest());

        final StaticHeaderType.BankPubKeyDigests bankPubKeyDigests = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigests();
        bankPubKeyDigests.setAuthentication(authentication);
        bankPubKeyDigests.setEncryption(encryption);

        final StaticHeaderOrderDetailsType.OrderType orderType = OBJECT_FACTORY.createStaticHeaderOrderDetailsTypeOrderType();
        orderType.setValue(type.name());

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
        if (type.equals(OrderType.FUL)) {
            final FileFormatType fileFormat = OBJECT_FACTORY.createFileFormatType();
            fileFormat.setCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase());
            fileFormat.setValue(session.getSessionParam("FORMAT"));

            final FULOrderParamsType fULOrderParams = OBJECT_FACTORY.createFULOrderParamsType();
            fULOrderParams.setFileFormat(fileFormat);
            if (parameters.size() > 0) {
                fULOrderParams.getParameters().addAll(parameters);
            }

            orderDetails.setOrderAttribute(OrderAttributeType.DZHNN);
            orderDetails.setOrderType(orderType);
            orderDetails.setOrderParams(OBJECT_FACTORY.createFULOrderParams(fULOrderParams));
        } else {
            final StandardOrderParamsType standardOrderParamsType = OBJECT_FACTORY.createStandardOrderParamsType();

            orderDetails.setOrderAttribute(OrderAttributeType.OZHNN);
            orderDetails.setOrderType(orderType);
            orderDetails.setOrderParams(OBJECT_FACTORY.createStandardOrderParams(standardOrderParamsType));
        }

        final StaticHeaderType xstatic = OBJECT_FACTORY.createStaticHeaderType();
        xstatic.setHostID(session.getHostId());
        xstatic.setNonce(nonce);
        xstatic.setNumSegments(BigInteger.valueOf(splitter.getNumSegments()));
        xstatic.setPartnerID(session.getPartner().getId());
        xstatic.setProduct(OBJECT_FACTORY.createStaticHeaderTypeProduct(product));
        xstatic.setSecurityMedium(session.getUser().getSecurityMedium());
        xstatic.setUserID(session.getUser().getUserId());
        xstatic.setTimestamp(LocalDateTime.now());
        xstatic.setOrderDetails(orderDetails);
        xstatic.setBankPubKeyDigests(bankPubKeyDigests);

        final EbicsRequest.Header header = OBJECT_FACTORY.createEbicsRequestHeader();
        header.setAuthenticate(true);
        header.setMutable(mutable);
        header.setStatic(xstatic);

        final DataEncryptionInfoType.EncryptionPubKeyDigest encryptionPubKeyDigest = OBJECT_FACTORY.createDataEncryptionInfoTypeEncryptionPubKeyDigest();
        encryptionPubKeyDigest.setVersion(session.getConfiguration().getEncryptionVersion().name());
        encryptionPubKeyDigest.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        encryptionPubKeyDigest.setValue(session.getBank().getEncryptionKey().getDigest());

        final UserSignature userSignature = new UserSignature(session,
                DefaultEbicsRootElement.generateName("UserSignature"),
                session.getConfiguration().getSignatureVersion(),
                userData);
        final JAXBElement<UserSignatureDataSigBookType> userSignatureElement = userSignature.build();

        final DataTransferRequestType.SignatureData signatureData = OBJECT_FACTORY.createDataTransferRequestTypeSignatureData();
        signatureData.setAuthenticate(true);
        signatureData.setValue(CryptoUtil.encrypt(ZipUtil.compress(XmlUtils.prettyPrint(userSignatureElement)), keySpec));

        final DataTransferRequestType.DataEncryptionInfo dataEncryptionInfo = OBJECT_FACTORY.createDataTransferRequestTypeDataEncryptionInfo();
        dataEncryptionInfo.setAuthenticate(true);
        dataEncryptionInfo.setEncryptionPubKeyDigest(encryptionPubKeyDigest);
        dataEncryptionInfo.setTransactionKey(generateTransactionKey());

        final DataTransferRequestType dataTransfer = OBJECT_FACTORY.createDataTransferRequestType();
        dataTransfer.setDataEncryptionInfo(dataEncryptionInfo);
        dataTransfer.setSignatureData(signatureData);

        final EbicsRequest.Body body = OBJECT_FACTORY.createEbicsRequestBody();
        body.setDataTransfer(dataTransfer);

        final EbicsRequest request = OBJECT_FACTORY.createEbicsRequest();
        request.setRevision(session.getConfiguration().getRevision());
        request.setVersion(session.getConfiguration().getVersion().name());
        request.setHeader(header);
        request.setBody(body);

        return request;
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
