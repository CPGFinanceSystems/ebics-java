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

import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.session.EbicsSession;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import org.ebics.h004.*;
import org.ebics.s001.UserSignatureDataSigBookType;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.time.LocalDateTime;


/**
 * The <code>SPRRequestElement</code> is the request element
 * for revoking a subscriber
 *
 * @author Hachani
 */
public class SPRRequestElement extends InitializationRequestElement {

    private final SecretKeySpec keySpec;

    /**
     * Constructs a new SPR request element.
     *
     * @param session the current ebic session.
     */
    public SPRRequestElement(final EbicsSession session) throws EbicsException {
        super(session, OrderType.SPR);
        this.keySpec = new SecretKeySpec(nonce, "AES");
    }

    @Override
    public EbicsRequest buildInitialization() throws EbicsException {
        final MutableHeaderType mutable = OBJECT_FACTORY.createMutableHeaderType();
        mutable.setTransactionPhase(TransactionPhaseType.INITIALISATION);

        final StaticHeaderType.Product product = OBJECT_FACTORY.createStaticHeaderTypeProduct();
        product.setLanguage(session.getProduct().getLanguage());
        product.setValue(session.getProduct().getName());

        final StaticHeaderType.BankPubKeyDigests.Authentication authentication = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsAuthentication();
        authentication.setVersion(session.getConfiguration().getAuthenticationVersion());
        authentication.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        authentication.setValue(session.getUser().getPartner().getBank().getX002Digest());

        final StaticHeaderType.BankPubKeyDigests.Encryption encryption = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigestsEncryption();
        encryption.setVersion(session.getConfiguration().getEncryptionVersion());
        encryption.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        encryption.setValue(session.getUser().getPartner().getBank().getE002Digest());

        final StaticHeaderType.BankPubKeyDigests bankPubKeyDigests = OBJECT_FACTORY.createStaticHeaderTypeBankPubKeyDigests();
        bankPubKeyDigests.setAuthentication(authentication);
        bankPubKeyDigests.setEncryption(encryption);

        final StaticHeaderOrderDetailsType.OrderType orderType = OBJECT_FACTORY.createStaticHeaderOrderDetailsTypeOrderType();
        orderType.setValue(type.name());

        final StandardOrderParamsType standardOrderParamsType = OBJECT_FACTORY.createStandardOrderParamsType();

        final StaticHeaderOrderDetailsType orderDetails = OBJECT_FACTORY.createStaticHeaderOrderDetailsType();
        orderDetails.setOrderAttribute(OrderAttributeType.UZHNN);
        orderDetails.setOrderType(orderType);
        orderDetails.setOrderParams(OBJECT_FACTORY.createStandardOrderParams(standardOrderParamsType));

        final StaticHeaderType xstatic = OBJECT_FACTORY.createStaticHeaderType();
        xstatic.setHostID(session.getBankID());
        xstatic.setNonce(nonce);
        xstatic.setNumSegments(BigInteger.ZERO);
        xstatic.setPartnerID(session.getUser().getPartner().getPartnerId());
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
        encryptionPubKeyDigest.setVersion(session.getConfiguration().getEncryptionVersion());
        encryptionPubKeyDigest.setAlgorithm(XmlUtils.SIGNATURE_METHOD);
        encryptionPubKeyDigest.setValue(session.getUser().getPartner().getBank().getE002Digest());

        final UserSignature userSignature = new UserSignature(session.getUser(),
                DefaultEbicsRootElement.generateName("SIG"),
                session.getConfiguration().getSignatureVersion(),
                " ".getBytes());
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
        request.setVersion(session.getConfiguration().getVersion());
        request.setHeader(header);
        request.setBody(body);

        return request;
    }
}
