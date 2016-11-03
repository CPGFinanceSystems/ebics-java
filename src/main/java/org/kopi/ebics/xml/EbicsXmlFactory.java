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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.kopi.ebics.schema.h004.DataEncryptionInfoType.EncryptionPubKeyDigest;
import org.kopi.ebics.schema.h004.*;
import org.kopi.ebics.schema.h004.DataTransferRequestType.DataEncryptionInfo;
import org.kopi.ebics.schema.h004.DataTransferRequestType.SignatureData;
import org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest;
import org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest;
import org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body.TransferReceipt;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body;
import org.kopi.ebics.schema.h004.EbicsUnsecuredRequestDocument.EbicsUnsecuredRequest.Body.DataTransfer;
import org.kopi.ebics.schema.h004.FDLOrderParamsType.DateRange;
import org.kopi.ebics.schema.h004.MutableHeaderType.SegmentNumber;
import org.kopi.ebics.schema.h004.ParameterDocument.Parameter;
import org.kopi.ebics.schema.h004.ParameterDocument.Parameter.Value;
import org.kopi.ebics.schema.h004.StaticHeaderOrderDetailsType.OrderType;
import org.kopi.ebics.schema.h004.StaticHeaderType.BankPubKeyDigests;
import org.kopi.ebics.schema.h004.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.kopi.ebics.schema.h004.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.kopi.ebics.schema.h004.StaticHeaderType.Product;
import org.kopi.ebics.schema.h004.TransactionPhaseType.Enum;
import org.kopi.ebics.schema.s001.OrderSignatureDataType;
import org.kopi.ebics.schema.s001.UserSignatureDataDocument;
import org.kopi.ebics.schema.s001.UserSignatureDataSigBookType;
import org.kopi.ebics.schema.xmldsig.*;
import org.kopi.ebics.schema.xmldsig.SignatureType;

import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.Date;


/**
 * A factory to produce XML object for EBICS requests.
 * This factory is based on xmlbeans object generated
 * from EBICS XML schemas.
 *
 * @author hachani
 * @see XmlObject
 */
public class EbicsXmlFactory {

    /**
     * Creates a new <code>SignatureType</code> XML object
     *
     * @param signedInfo the <code>SignedInfoType</code> element
     * @return the <code>SignatureType</code> XML object
     */
    public static SignatureType createSignatureType(final SignedInfoType signedInfo) {
        final SignatureType newSignatureType = SignatureType.Factory.newInstance();
        newSignatureType.setSignedInfo(signedInfo);

        return newSignatureType;
    }

    /**
     * Creates a new <code>SignedInfoType</code> XML object
     *
     * @param canonicalizationMethod the <code>CanonicalizationMethod</code> element
     * @param signatureMethod        the <code>SignatureMethod</code> element
     * @param referenceArray         the <code>ReferenceType</code> array element
     * @return the <code>SignedInfoType</code> XML object
     */
    public static SignedInfoType createSignedInfoType(final CanonicalizationMethodType canonicalizationMethod,
                                                      final SignatureMethodType signatureMethod,
                                                      final ReferenceType[] referenceArray) {
        final SignedInfoType newSignedInfoType = SignedInfoType.Factory.newInstance();
        newSignedInfoType.setSignatureMethod(signatureMethod);
        newSignedInfoType.setCanonicalizationMethod(canonicalizationMethod);
        newSignedInfoType.setReferenceArray(referenceArray);

        return newSignedInfoType;
    }

    /**
     * Creates a new <code>SignatureValueType</code> XML object
     *
     * @param signatureValue the <code>SignatureMethod</code> element
     * @return the <code>SignatureValueType</code> XML object
     */
    public static SignatureValueType createSignatureValueType(final byte[] signatureValue) {
        final SignatureValueType newSignatureValueType = SignatureValueType.Factory.newInstance();
        newSignatureValueType.setByteArrayValue(signatureValue);

        return newSignatureValueType;
    }

    /**
     * Creates a new <code>SignatureValueType</code> XML object
     *
     * @param algorithm the signature algorithm
     * @return the <code>SignatureValueType</code> XML object
     */
    public static SignatureMethodType createSignatureMethodType(final String algorithm) {
        final SignatureMethodType newSignatureMethodType = SignatureMethodType.Factory.newInstance();
        newSignatureMethodType.setAlgorithm(algorithm);

        return newSignatureMethodType;
    }

    /**
     * Creates a new <code>CanonicalizationMethodType</code> XML object
     *
     * @param algorithm the canonicalization algorithm
     * @return the <code>CanonicalizationMethodType</code> XML object
     */
    public static CanonicalizationMethodType createCanonicalizationMethodType(final String algorithm) {
        final CanonicalizationMethodType newCanonicalizationMethodType = CanonicalizationMethodType.Factory.newInstance();
        newCanonicalizationMethodType.setAlgorithm(algorithm);

        return newCanonicalizationMethodType;
    }

    /**
     * Creates a new <code>ReferenceType</code> XML object
     *
     * @param uri          the reference uri
     * @param transforms   the <code>TransformsType</code> element
     * @param digestMethod the <code>DigestMethodType</code> element
     * @param digestValue  the digest value
     * @return the <code>ReferenceType</code> XML object
     */
    public static ReferenceType createReferenceType(final String uri,
                                                    final TransformsType transforms,
                                                    final DigestMethodType digestMethod,
                                                    final byte[] digestValue) {
        final ReferenceType newReferenceType = ReferenceType.Factory.newInstance();
        newReferenceType.setURI(uri);
        newReferenceType.setTransforms(transforms);
        newReferenceType.setDigestMethod(digestMethod);
        newReferenceType.setDigestValue(digestValue);

        return newReferenceType;
    }

    /**
     * Creates a new <code>TransformsType</code> XML object
     *
     * @param transformArray the <code>TransformsType</code> array element
     * @return the <code>TransformsType</code> XML object
     */
    public static TransformsType createTransformsType(final TransformType[] transformArray) {
        final TransformsType newTransformsType = TransformsType.Factory.newInstance();
        newTransformsType.setTransformArray(transformArray);

        return newTransformsType;
    }

    /**
     * Creates a new <code>TransformType</code> XML object
     *
     * @param algorithm the transformation algorithm
     * @return the <code>TransformType</code> XML object
     */
    public static TransformType createTransformType(final String algorithm) {
        final TransformType newTransformType = TransformType.Factory.newInstance();
        newTransformType.setAlgorithm(algorithm);

        return newTransformType;
    }

    /**
     * Creates a new <code>DigestMethodType</code> XML object
     *
     * @param algorithm the digest method algorithm
     * @return the <code>DigestMethodType</code> XML object
     */
    public static DigestMethodType createDigestMethodType(final String algorithm) {
        final DigestMethodType newDigestMethodType = DigestMethodType.Factory.newInstance();
        newDigestMethodType.setAlgorithm(algorithm);

        return newDigestMethodType;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new <code>UserSignatureDataDocument</code> XML object
     *
     * @param userSignatureData the <code>UserSignatureDataSigBookType</code> element
     * @return the <code>UserSignatureDataDocument</code> XML object
     */
    public static UserSignatureDataDocument createUserSignatureDataDocument(final UserSignatureDataSigBookType userSignatureData) {
        final UserSignatureDataDocument newUserSignatureDataDocument = UserSignatureDataDocument.Factory.newInstance();
        newUserSignatureDataDocument.setUserSignatureData(userSignatureData);

        return newUserSignatureDataDocument;
    }

    /**
     * Creates a new <code>UserSignatureDataSigBookType</code> XML object
     *
     * @param orderSignatureDataArray the <code>OrderSignatureDataType</code> array element
     * @return the <code>UserSignatureDataSigBookType</code> XML object
     */
    public static UserSignatureDataSigBookType createUserSignatureDataSigBookType(final OrderSignatureDataType[] orderSignatureDataArray) {
        final UserSignatureDataSigBookType newUserSignatureDataSigBookType = UserSignatureDataSigBookType.Factory.newInstance();
        newUserSignatureDataSigBookType.setOrderSignatureDataArray(orderSignatureDataArray);

        return newUserSignatureDataSigBookType;
    }

    /**
     * Creates a new <code>OrderSignatureDataType</code> XML object
     *
     * @param signatureVersion the signature version
     * @param partnerID        the partner id
     * @param userID           the user id
     * @param signatureValue   the signature value
     * @return the <code>OrderSignatureDataType</code> XML object
     */
    public static OrderSignatureDataType createOrderSignatureDataType(final String signatureVersion,
                                                                      final String partnerID,
                                                                      final String userID,
                                                                      final byte[] signatureValue) {
        final OrderSignatureDataType newOrderSignatureDataType = OrderSignatureDataType.Factory.newInstance();
        newOrderSignatureDataType.setSignatureVersion(signatureVersion);
        newOrderSignatureDataType.setPartnerID(partnerID);
        newOrderSignatureDataType.setUserID(userID);
        newOrderSignatureDataType.setSignatureValue(signatureValue);

        return newOrderSignatureDataType;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new <code>EmptyMutableHeaderType</code> XML object
     *
     * @return the <code>EmptyMutableHeaderType</code> XML object
     */
    public static EmptyMutableHeaderType createEmptyMutableHeaderType() {

        return EmptyMutableHeaderType.Factory.newInstance();
    }

    /**
     * Creates a new <code>ProductElementType</code> XML object
     *
     * @param language the language
     * @param product  the product name
     * @return the <code>ProductElementType</code> XML object
     */
    public static ProductElementType creatProductElementType(final String language, final String product) {
        final ProductElementType newProductElementType = ProductElementType.Factory.newInstance();
        newProductElementType.setLanguage(language);
        newProductElementType.setStringValue(product);

        return newProductElementType;
    }

    /**
     * Creates a new <code>OrderDetailsType</code> XML object
     *
     * @param orderAttribute the order attribute
     * @param orderType      the order type
     * @return the <code>OrderDetailsType</code> XML object
     */
    @SuppressWarnings("deprecation")
    public static OrderDetailsType createOrderDetailsType(final String orderAttribute, final String orderType) {
        final OrderDetailsType newOrderDetailsType = OrderDetailsType.Factory.newInstance();
        newOrderDetailsType.setOrderAttribute(orderAttribute);
        newOrderDetailsType.setOrderType(orderType);

        return newOrderDetailsType;
    }

    /**
     * Creates a new <code>Body</code> XML object
     *
     * @param dataTransfer the <code>DataTransfer</code> element
     * @return the <code>Body</code> XML object
     */
    public static Body createBody(final DataTransfer dataTransfer) {
        final Body newBody = Body.Factory.newInstance();
        newBody.setDataTransfer(dataTransfer);

        return newBody;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new <code>EbicsNoPubKeyDigestsRequest</code> XML object
     *
     * @param revision the default revision
     * @param version  the default version
     * @param header   the <code>org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header</code> element
     * @param body     the <code>org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body</code> element
     * @return the <code>EbicsNoPubKeyDigestsRequest</code> XML object
     */
    public static EbicsNoPubKeyDigestsRequest createEbicsNoPubKeyDigestsRequest(final int revision,
                                                                                final String version,
                                                                                final org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header header,
                                                                                final org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body body) {
        final EbicsNoPubKeyDigestsRequest newEbicsNoPubKeyDigestsRequest = EbicsNoPubKeyDigestsRequest.Factory.newInstance();
        newEbicsNoPubKeyDigestsRequest.setRevision(revision);
        newEbicsNoPubKeyDigestsRequest.setVersion(version);
        newEbicsNoPubKeyDigestsRequest.setHeader(header);
        newEbicsNoPubKeyDigestsRequest.setBody(body);

        return newEbicsNoPubKeyDigestsRequest;
    }

    /**
     * Creates a new <code>org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header</code> XML object
     *
     * @param authenticate should authenticate?
     * @param mutable      the <code>EmptyMutableHeaderType</code> element
     * @param xstatic      the <code>NoPubKeyDigestsRequestStaticHeaderType</code> element
     * @return the <code>org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header</code> XML object
     */
    public static org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header createDigestsRequestHeader(final boolean authenticate,
                                                                                                                                               final EmptyMutableHeaderType mutable,
                                                                                                                                               final NoPubKeyDigestsRequestStaticHeaderType xstatic) {
        final org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header newHeader = org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Header.Factory.newInstance();
        newHeader.setAuthenticate(authenticate);
        newHeader.setMutable(mutable);
        newHeader.setStatic(xstatic);

        return newHeader;
    }

    /**
     * Creates a new <code>NoPubKeyDigestsRequestStaticHeaderType</code> XML object
     *
     * @param hostId         the host ID
     * @param nonce          a random nonce
     * @param timestamp      the current time stamp
     * @param partnerId      the partner ID
     * @param userId         the user ID
     * @param product        the <code>ProductElementType</code> element
     * @param orderDetails   the <code>OrderDetailsType</code> element
     * @param securityMedium the user security medium
     * @return
     */
    public static NoPubKeyDigestsRequestStaticHeaderType createNoPubKeyDigestsRequestStaticHeaderType(final String hostId,
                                                                                                      final byte[] nonce,
                                                                                                      final Calendar timestamp,
                                                                                                      final String partnerId,
                                                                                                      final String userId,
                                                                                                      final ProductElementType product,
                                                                                                      final OrderDetailsType orderDetails,
                                                                                                      final String securityMedium) {
        final NoPubKeyDigestsRequestStaticHeaderType newNoPubKeyDigestsRequestStaticHeaderType = NoPubKeyDigestsRequestStaticHeaderType.Factory.newInstance();
        newNoPubKeyDigestsRequestStaticHeaderType.setHostID(hostId);
        newNoPubKeyDigestsRequestStaticHeaderType.setNonce(nonce);
        newNoPubKeyDigestsRequestStaticHeaderType.setTimestamp(timestamp);
        newNoPubKeyDigestsRequestStaticHeaderType.setPartnerID(partnerId);
        newNoPubKeyDigestsRequestStaticHeaderType.setUserID(userId);
        newNoPubKeyDigestsRequestStaticHeaderType.setProduct(product);
        newNoPubKeyDigestsRequestStaticHeaderType.setOrderDetails(orderDetails);
        newNoPubKeyDigestsRequestStaticHeaderType.setSecurityMedium(securityMedium);

        return newNoPubKeyDigestsRequestStaticHeaderType;
    }

    /**
     * Creates a new <code>org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body</code> XML object
     *
     * @return the <code>org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body</code> XML object
     */
    public static org.kopi.ebics.schema.h004.EbicsNoPubKeyDigestsRequestDocument.EbicsNoPubKeyDigestsRequest.Body createDigestsRequestBody() {

        return EbicsNoPubKeyDigestsRequest.Body.Factory.newInstance();
    }

    /**
     * Creates a new <code>EbicsNoPubKeyDigestsRequestDocument</code> XML object
     *
     * @param ebicsNoPubKeyDigestsRequest the <code>EbicsNoPubKeyDigestsRequest</code> element
     * @return the <code>EbicsNoPubKeyDigestsRequestDocument</code> XML object
     */
    public static EbicsNoPubKeyDigestsRequestDocument createEbicsNoPubKeyDigestsRequestDocument(final EbicsNoPubKeyDigestsRequest ebicsNoPubKeyDigestsRequest) {
        final EbicsNoPubKeyDigestsRequestDocument newEbicsNoPubKeyDigestsRequestDocument = EbicsNoPubKeyDigestsRequestDocument.Factory.newInstance();
        newEbicsNoPubKeyDigestsRequestDocument.setEbicsNoPubKeyDigestsRequest(ebicsNoPubKeyDigestsRequest);

        return newEbicsNoPubKeyDigestsRequestDocument;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new <code>EbicsRequestDocument</code> XML object
     *
     * @param ebicsRequest the <code>EbicsRequest</code> element
     * @return the <code>EbicsRequestDocument</code> XML object
     */
    public static EbicsRequestDocument createEbicsRequestDocument(final EbicsRequest ebicsRequest) {
        final EbicsRequestDocument newEbicsRequestDocument = EbicsRequestDocument.Factory.newInstance();
        newEbicsRequestDocument.setEbicsRequest(ebicsRequest);

        return newEbicsRequestDocument;
    }

    /**
     * Creates a new <code>EbicsRequest</code> XML object
     *
     * @param revision the default revision
     * @param version  the default version
     * @param header   the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header</code> element
     * @param body     the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> element
     * @return the <code>EbicsRequest</code> XML object
     */
    public static EbicsRequest createEbicsRequest(final int revision,
                                                  final String version,
                                                  final org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header header,
                                                  final org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body body) {
        final EbicsRequest newEbicsRequest = EbicsRequest.Factory.newInstance();
        newEbicsRequest.setRevision(revision);
        newEbicsRequest.setVersion(version);
        newEbicsRequest.setHeader(header);
        newEbicsRequest.setBody(body);

        return newEbicsRequest;
    }

    /**
     * Creates a new <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header</code> XML object
     *
     * @param authenticate should authenticate?
     * @param mutable      the <code>MutableHeaderType</code> element
     * @param xstatic      the <code>StaticHeaderType</code> element
     * @return the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header</code> XML object
     */
    public static org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header createEbicsRequestHeader(final boolean authenticate,
                                                                                                               final MutableHeaderType mutable,
                                                                                                               final StaticHeaderType xstatic) {
        final org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header newHeader = org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Header.Factory.newInstance();
        newHeader.setAuthenticate(authenticate);
        newHeader.setMutable(mutable);
        newHeader.setStatic(xstatic);

        return newHeader;
    }

    /**
     * Creates a new <code>MutableHeaderType</code> XML object
     *
     * @param transactionPhase the transaction phase
     * @param segmentNumber    the <code>SegmentNumber</code> element
     * @return the <code>MutableHeaderType</code> XML object
     */
    public static MutableHeaderType createMutableHeaderType(final String transactionPhase, final SegmentNumber segmentNumber) {
        final MutableHeaderType newMutableHeaderType = MutableHeaderType.Factory.newInstance();
        newMutableHeaderType.setTransactionPhase(Enum.forString(transactionPhase));
        if (segmentNumber != null) {
            newMutableHeaderType.setSegmentNumber(segmentNumber);
        }

        return newMutableHeaderType;
    }

    /**
     * Creates a new <code>SegmentNumber</code> XML object
     *
     * @param segmentNumber the segment number
     * @param lastSegment   is the last segment?
     * @return the <code>SegmentNumber</code> XML object
     */
    public static SegmentNumber createSegmentNumber(final long segmentNumber, final boolean lastSegment) {
        final SegmentNumber newSegmentNumber = SegmentNumber.Factory.newInstance();
        newSegmentNumber.setLongValue(segmentNumber);
        newSegmentNumber.setLastSegment(lastSegment);

        return newSegmentNumber;
    }

    /**
     * Creates a new <code>StaticHeaderType</code> XML object
     *
     * @param hostId            the host ID
     * @param nonce             the random nonce
     * @param numSegments       the segments number
     * @param partnerId         the partner ID
     * @param product           the <code>Product</code> element
     * @param securityMedium    the security medium
     * @param userId            the user Id
     * @param timestamp         the current time stamp
     * @param orderDetails      the <code>StaticHeaderOrderDetailsType</code> element
     * @param bankPubKeyDigests the <code>BankPubKeyDigests</code> element
     * @return the <code>StaticHeaderType</code> XML object
     */
    public static StaticHeaderType createStaticHeaderType(final String hostId,
                                                          final byte[] nonce,
                                                          final int numSegments,
                                                          final String partnerId,
                                                          final Product product,
                                                          final String securityMedium,
                                                          final String userId,
                                                          final Calendar timestamp,
                                                          final StaticHeaderOrderDetailsType orderDetails,
                                                          final BankPubKeyDigests bankPubKeyDigests) {
        final StaticHeaderType newStaticHeaderType = StaticHeaderType.Factory.newInstance();
        newStaticHeaderType.setHostID(hostId);
        newStaticHeaderType.setNonce(nonce);
        newStaticHeaderType.setNumSegments(numSegments);
        newStaticHeaderType.setPartnerID(partnerId);
        newStaticHeaderType.setProduct(product);
        newStaticHeaderType.setSecurityMedium(securityMedium);
        newStaticHeaderType.setUserID(userId);
        newStaticHeaderType.setTimestamp(timestamp);
        newStaticHeaderType.setOrderDetails(orderDetails);
        newStaticHeaderType.setBankPubKeyDigests(bankPubKeyDigests);

        return newStaticHeaderType;
    }

    /**
     * Creates a new <code>StaticHeaderType</code> XML object
     *
     * @param hostId            the host ID
     * @param nonce             the random nonce
     * @param partnerId         the partner ID
     * @param product           the <code>Product</code> element
     * @param securityMedium    the security medium
     * @param userId            the user Id
     * @param timestamp         the current time stamp
     * @param orderDetails      the <code>StaticHeaderOrderDetailsType</code> element
     * @param bankPubKeyDigests the <code>BankPubKeyDigests</code> element
     * @return the <code>StaticHeaderType</code> XML object
     */
    public static StaticHeaderType createStaticHeaderType(final String hostId,
                                                          final byte[] nonce,
                                                          final String partnerId,
                                                          final Product product,
                                                          final String securityMedium,
                                                          final String userId,
                                                          final Calendar timestamp,
                                                          final StaticHeaderOrderDetailsType orderDetails,
                                                          final BankPubKeyDigests bankPubKeyDigests) {
        final StaticHeaderType newStaticHeaderType = StaticHeaderType.Factory.newInstance();
        newStaticHeaderType.setHostID(hostId);
        newStaticHeaderType.setNonce(nonce);
        newStaticHeaderType.setPartnerID(partnerId);
        newStaticHeaderType.setProduct(product);
        newStaticHeaderType.setSecurityMedium(securityMedium);
        newStaticHeaderType.setUserID(userId);
        newStaticHeaderType.setTimestamp(timestamp);
        newStaticHeaderType.setOrderDetails(orderDetails);
        newStaticHeaderType.setBankPubKeyDigests(bankPubKeyDigests);

        return newStaticHeaderType;
    }

    /**
     * Creates a new <code>StaticHeaderOrderDetailsType</code> XML object
     *
     * @param orderId        the order ID
     * @param orderAttribute the order attribute
     * @param orderType      the order type
     * @param orderParams    the <code>FULOrderParamsType</code> element
     * @return the <code>StaticHeaderOrderDetailsType</code> XML object
     */
    public static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(final String orderId,
                                                                                  final String orderAttribute,
                                                                                  final OrderType orderType,
                                                                                  final FULOrderParamsType orderParams) {
        final StaticHeaderOrderDetailsType newStaticHeaderOrderDetailsType = StaticHeaderOrderDetailsType.Factory.newInstance();
        if (orderId != null) {
            newStaticHeaderOrderDetailsType.setOrderID(orderId);
        }
        newStaticHeaderOrderDetailsType.setOrderAttribute(org.kopi.ebics.schema.h004.OrderAttributeType.Enum.forString(orderAttribute));
        newStaticHeaderOrderDetailsType.setOrderType(orderType);
        newStaticHeaderOrderDetailsType.setOrderParams(orderParams);
        qualifySubstitutionGroup(newStaticHeaderOrderDetailsType.getOrderParams(), FULOrderParamsDocument.type.getDocumentElementName(), null);

        return newStaticHeaderOrderDetailsType;
    }


    /**
     * Creates a new <code>StaticHeaderOrderDetailsType</code> XML object
     *
     * @param orderId        the order ID
     * @param orderAttribute the order attribute
     * @param orderType      the order type
     * @param orderParams    the <code>FDLOrderParamsType</code> element
     * @return the <code>StaticHeaderOrderDetailsType</code> XML object
     */
    public static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(final String orderId,
                                                                                  final String orderAttribute,
                                                                                  final OrderType orderType,
                                                                                  final FDLOrderParamsType orderParams) {
        final StaticHeaderOrderDetailsType newStaticHeaderOrderDetailsType = StaticHeaderOrderDetailsType.Factory.newInstance();

        if (orderId != null) {
            //newStaticHeaderOrderDetailsType.setOrderID(orderId);
        }
        newStaticHeaderOrderDetailsType.setOrderAttribute(org.kopi.ebics.schema.h004.OrderAttributeType.Enum.forString(orderAttribute));
        newStaticHeaderOrderDetailsType.setOrderType(orderType);
        newStaticHeaderOrderDetailsType.setOrderParams(orderParams);
        qualifySubstitutionGroup(newStaticHeaderOrderDetailsType.getOrderParams(), FDLOrderParamsDocument.type.getDocumentElementName(), null);

        return newStaticHeaderOrderDetailsType;
    }

    /**
     * Creates a new <code>StaticHeaderOrderDetailsType</code> XML object
     *
     * @param orderId        the order ID
     * @param orderAttribute the order attribute
     * @param orderType      the order type
     * @param orderParams    the <code>StandardOrderParamsType</code> element
     * @return the <code>StaticHeaderOrderDetailsType</code> XML object
     */
    public static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(final String orderId,
                                                                                  final String orderAttribute,
                                                                                  final OrderType orderType,
                                                                                  final StandardOrderParamsType orderParams) {
        final StaticHeaderOrderDetailsType newStaticHeaderOrderDetailsType = StaticHeaderOrderDetailsType.Factory.newInstance();
        if (null != orderId) {
            newStaticHeaderOrderDetailsType.setOrderID(orderId);
        }
        newStaticHeaderOrderDetailsType.setOrderAttribute(org.kopi.ebics.schema.h004.OrderAttributeType.Enum.forString(orderAttribute));
        newStaticHeaderOrderDetailsType.setOrderType(orderType);
        newStaticHeaderOrderDetailsType.setOrderParams(orderParams);
        qualifySubstitutionGroup(newStaticHeaderOrderDetailsType.getOrderParams(), StandardOrderParamsDocument.type.getDocumentElementName(), null);

        return newStaticHeaderOrderDetailsType;
    }

    static StaticHeaderOrderDetailsType createStaticHeaderOrderDetailsType(final String orderId,
                                                                           final String orderAttribute,
                                                                           final OrderType orderType,
                                                                           final GenericOrderParamsType orderParams) {
        final StaticHeaderOrderDetailsType staticHeaderOrderDetailsType = StaticHeaderOrderDetailsType.Factory.newInstance();
        if (null != orderId) {
            staticHeaderOrderDetailsType.setOrderID(orderId);
        }
        staticHeaderOrderDetailsType.setOrderAttribute(org.kopi.ebics.schema.h004.OrderAttributeType.Enum.forString(orderAttribute));
        staticHeaderOrderDetailsType.setOrderType(orderType);
        staticHeaderOrderDetailsType.setOrderParams(orderParams);
        qualifySubstitutionGroup(staticHeaderOrderDetailsType.getOrderParams(), GenericOrderParamsDocument.type.getDocumentElementName(), null);

        return staticHeaderOrderDetailsType;
    }

    /**
     * Creates a new <code>FULOrderParamsType</code> XML object
     *
     * @param fileFormat the <code>FileFormatType</code> element
     * @return the <code>FULOrderParamsType</code> XML object
     */
    public static FULOrderParamsType createFULOrderParamsType(final FileFormatType fileFormat) {
        final FULOrderParamsType newFULOrderParamsType = FULOrderParamsType.Factory.newInstance();
        newFULOrderParamsType.setFileFormat(fileFormat);

        return newFULOrderParamsType;
    }

    static GenericOrderParamsType createGenericOrderParamsType() {
        return GenericOrderParamsType.Factory.newInstance();
    }

    /**
     * Creates a new <code>FDLOrderParamsType</code> XML object
     *
     * @param fileFormat the <code>FileFormatType</code> element
     * @return the <code>FDLOrderParamsType</code> XML object
     */
    public static FDLOrderParamsType createFDLOrderParamsType(final FileFormatType fileFormat) {
        final FDLOrderParamsType newFDLOrderParamsType = FDLOrderParamsType.Factory.newInstance();
        newFDLOrderParamsType.setFileFormat(fileFormat);

        return newFDLOrderParamsType;
    }

    /**
     * Creates a new <code>StandardOrderParamsType</code> XML object
     *
     * @return the <code>StandardOrderParamsType</code> XML object
     */
    public static StandardOrderParamsType createStandardOrderParamsType() {

        return StandardOrderParamsType.Factory.newInstance();
    }

    /**
     * Creates a new <code>DateRange</code> XML object
     *
     * @param start the start range
     * @param end   the end range
     * @return the <code>DateRange</code> XML object
     */
    public static DateRange createDateRange(final Date start, final Date end) {
        final DateRange newDateRange = DateRange.Factory.newInstance();
        final Calendar startRange = Calendar.getInstance();
        final Calendar endRange = Calendar.getInstance();

        startRange.setTime(start);
        endRange.setTime(end);
        newDateRange.setStart(startRange);
        newDateRange.setEnd(endRange);

        return newDateRange;
    }

    /**
     * Creates a new <code>FileFormatType</code> XML object
     *
     * @param countryCode the country code
     * @param value       the file format value
     * @return the <code>FileFormatType</code> XML object
     */
    public static FileFormatType createFileFormatType(final String countryCode, final String value) {
        final FileFormatType newFileFormatType = FileFormatType.Factory.newInstance();
        newFileFormatType.setCountryCode(countryCode);
        newFileFormatType.setStringValue(value);

        return newFileFormatType;
    }

    /**
     * Creates a new <code>Parameter</code> XML object
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the <code>Parameter</code> XML object
     */
    public static Parameter createParameter(final String name, final Value value) {
        final Parameter newParameter = Parameter.Factory.newInstance();
        newParameter.setName(name);
        newParameter.setValue(value);

        return newParameter;
    }

    /**
     * Creates a new <code>Value</code> XML object
     *
     * @param type  the value type
     * @param value the value
     * @return the <code>Value</code> XML object
     */
    public static Value createValue(final String type, final String value) {
        final Value newValue = Value.Factory.newInstance();
        newValue.setType(type);
        newValue.setStringValue(value);

        return newValue;
    }

    /**
     * Create the <code>OrderType</code> XML object
     *
     * @param orderType the order type
     * @return the <code>OrderType</code> XML object
     */
    public static OrderType createOrderType(final String orderType) {
        final OrderType newOrderType = OrderType.Factory.newInstance();
        newOrderType.setStringValue(orderType);

        return newOrderType;
    }

    /**
     * Create the <code>Product</code> XML object
     *
     * @param language the product language
     * @param value    the product value
     * @return the <code>Product</code> XML object
     */
    public static Product createProduct(final String language, final String value) {
        final Product newProduct = Product.Factory.newInstance();
        newProduct.setLanguage(language);
        newProduct.setStringValue(value);

        return newProduct;
    }

    /**
     * Create the <code>BankPubKeyDigests</code> XML object
     *
     * @param authentication the <code>Authentication</code> element
     * @param encryption     the <code>Encryption</code> element
     * @return the <code>BankPubKeyDigests</code> XML object
     */
    public static BankPubKeyDigests createBankPubKeyDigests(final Authentication authentication, final Encryption encryption) {
        final BankPubKeyDigests newBankPubKeyDigests = BankPubKeyDigests.Factory.newInstance();
        newBankPubKeyDigests.setAuthentication(authentication);
        newBankPubKeyDigests.setEncryption(encryption);

        return newBankPubKeyDigests;
    }

    /**
     * Create the <code>Authentication</code> XML object
     *
     * @param version   the authentication version
     * @param algorithm the authentication algorithm
     * @param value     the authentication value
     * @return the <code>Authentication</code> XML object
     */
    public static Authentication createAuthentication(final String version, final String algorithm, final byte[] value) {
        final Authentication newAuthentication = Authentication.Factory.newInstance();
        newAuthentication.setVersion(version);
        newAuthentication.setAlgorithm(algorithm);
        newAuthentication.setByteArrayValue(value);

        return newAuthentication;
    }

    /**
     * Create the <code>Encryption</code> XML object
     *
     * @param version   the encryption version
     * @param algorithm the encryption algorithm
     * @param value     the encryption value
     * @return the <code>Encryption</code> XML object
     */
    public static Encryption createEncryption(final String version, final String algorithm, final byte[] value) {
        final Encryption newEncryption = Encryption.Factory.newInstance();
        newEncryption.setVersion(version);
        newEncryption.setAlgorithm(algorithm);
        newEncryption.setByteArrayValue(value);

        return newEncryption;
    }

    /**
     * Create the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> XML object
     *
     * @param dataTransfer the <code>DataTransferRequestType</code> element
     * @return the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> XML object
     */
    public static org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body createEbicsRequestBody(final DataTransferRequestType dataTransfer) {
        final org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body newBody = org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body.Factory.newInstance();
        newBody.setDataTransfer(dataTransfer);

        return newBody;
    }

    /**
     * Create the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> XML object
     *
     * @return the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> XML object
     */
    public static org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body createEbicsRequestBody() {

        return EbicsRequest.Body.Factory.newInstance();
    }


    /**
     * Create the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> XML object
     *
     * @param transferReceipt the <code>TransferReceipt</code> element
     * @return the <code>org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body</code> XML object
     */
    public static org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body createEbicsRequestBody(final TransferReceipt transferReceipt) {
        final org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body newBody = org.kopi.ebics.schema.h004.EbicsRequestDocument.EbicsRequest.Body.Factory.newInstance();
        newBody.setTransferReceipt(transferReceipt);

        return newBody;
    }

    /**
     * Create the <code>DataTransferRequestType</code> XML object
     *
     * @param dataEncryptionInfo the <code>DataEncryptionInfo</code> element
     * @param signatureData      the <code>SignatureData</code> element
     * @return the <code>DataTransferRequestType</code> XML object
     */
    public static DataTransferRequestType createDataTransferRequestType(final DataEncryptionInfo dataEncryptionInfo,
                                                                        final SignatureData signatureData) {
        final DataTransferRequestType newDataTransferRequestType = DataTransferRequestType.Factory.newInstance();
        newDataTransferRequestType.setDataEncryptionInfo(dataEncryptionInfo);
        newDataTransferRequestType.setSignatureData(signatureData);

        return newDataTransferRequestType;
    }

    /**
     * Create the <code>StaticHeaderType</code> XML object
     *
     * @param hostId        the host ID
     * @param transactionId the transaction ID
     * @return the <code>StaticHeaderType</code> XML object
     */
    public static StaticHeaderType createStaticHeaderType(final String hostId, final byte[] transactionId) {
        final StaticHeaderType newStaticHeaderType = StaticHeaderType.Factory.newInstance();
        newStaticHeaderType.setHostID(hostId);
        newStaticHeaderType.setTransactionID(transactionId);

        return newStaticHeaderType;
    }

    /**
     * Create the <code>DataTransferRequestType</code> XML object
     *
     * @param orderData the <code>org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData</code> element
     * @return the <code>DataTransferRequestType</code> XML object
     */
    public static DataTransferRequestType createDataTransferRequestType(final org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData orderData) {
        final DataTransferRequestType newDataTransferRequestType = DataTransferRequestType.Factory.newInstance();
        newDataTransferRequestType.setOrderData(orderData);

        return newDataTransferRequestType;
    }

    /**
     * Create the <code>org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData</code> XML object
     *
     * @param orderDataValue the order data value
     * @return the <code>org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData</code> XML object
     */
    public static org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData createTransferRequestTypeOrderData(final byte[] orderDataValue) {
        final org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData newOrderData = org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData.Factory.newInstance();
        newOrderData.setByteArrayValue(orderDataValue);

        return newOrderData;
    }

    /**
     * Create the <code>DataEncryptionInfo</code> XML object
     *
     * @param authenticate           should authenticate?
     * @param encryptionPubKeyDigest the <code>EncryptionPubKeyDigest</code> element
     * @param transactionKey         the transaction key
     * @return the the <code>DataEncryptionInfo</code> XML object
     */
    public static DataEncryptionInfo createDataEncryptionInfo(final boolean authenticate,
                                                              final EncryptionPubKeyDigest encryptionPubKeyDigest,
                                                              final byte[] transactionKey) {
        final DataEncryptionInfo newDataEncryptionInfo = DataEncryptionInfo.Factory.newInstance();
        newDataEncryptionInfo.setAuthenticate(authenticate);
        newDataEncryptionInfo.setEncryptionPubKeyDigest(encryptionPubKeyDigest);
        newDataEncryptionInfo.setTransactionKey(transactionKey);

        return newDataEncryptionInfo;
    }

    /**
     * Create the <code>EncryptionPubKeyDigest</code> XML object
     *
     * @param version   the encryption version
     * @param algorithm the encryption algorithm
     * @param value     the encryption value
     * @return the <code>EncryptionPubKeyDigest</code> XML object
     */
    public static EncryptionPubKeyDigest createEncryptionPubKeyDigest(final String version, final String algorithm, final byte[] value) {
        final EncryptionPubKeyDigest newEncryptionPubKeyDigest = EncryptionPubKeyDigest.Factory.newInstance();
        newEncryptionPubKeyDigest.setVersion(version);
        newEncryptionPubKeyDigest.setAlgorithm(algorithm);
        newEncryptionPubKeyDigest.setByteArrayValue(value);

        return newEncryptionPubKeyDigest;
    }

    /**
     * Create the <code>org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData</code> XML object
     *
     * @param oderData the order data value
     * @return the the <code>org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData</code> XML object
     */
    public static org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData createEbicsRequestOrderData(final byte[] oderData) {
        final org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData newOrderData = org.kopi.ebics.schema.h004.DataTransferRequestType.OrderData.Factory.newInstance();
        newOrderData.setByteArrayValue(oderData);

        return newOrderData;
    }

    /**
     * Create the <code>SignatureData</code> XML object
     *
     * @param authenticate  should authenticate?
     * @param signatureData the signature data value
     * @return the <code>SignatureData</code> XML object
     */
    public static SignatureData createSignatureData(final boolean authenticate, final byte[] signatureData) {
        final SignatureData newSignatureData = SignatureData.Factory.newInstance();
        newSignatureData.setAuthenticate(authenticate);
        newSignatureData.setByteArrayValue(signatureData);

        return newSignatureData;
    }

    /**
     * Create the <code>TransferReceipt</code> XML object
     *
     * @param authenticate should authenticate?
     * @param receiptCode  the receipt code
     * @return the <code>TransferReceipt</code> XML object
     */
    public static TransferReceipt createTransferReceipt(final boolean authenticate, final int receiptCode) {
        final TransferReceipt newTransferReceipt = TransferReceipt.Factory.newInstance();
        newTransferReceipt.setAuthenticate(authenticate);
        newTransferReceipt.setReceiptCode(receiptCode);

        return newTransferReceipt;
    }

    /**
     * Qualifies a valid member of a substitution group. This method tries to use the
     * built-in {@link XmlObject#substitute(QName, SchemaType)} and if succesful returns
     * a valid substitution which is usable (not disconnected). If it fails, it uses
     * low-level {@link XmlCursor} manipulation to qualify the substitution group. Note
     * that if the latter is the case the resulting document is disconnected and should
     * no longer be manipulated. Thus, use it as a final step after all markup is included.
     * <p>
     * If newType is null, this method will skip {@link XmlObject#substitute(QName, SchemaType)}
     * and directly use {@link XmlCursor}. This can be used, if you are sure that the substitute
     * is not in the list of (pre-compiled) valid substitutions (this is the case if a schema
     * uses another schema's type as a base for elements. E.g. om:Observation uses gml:_Feature
     * as the base type).
     *
     * @param xobj        the abstract element
     * @param newInstance the new {@link QName} of the instance
     * @param newType     the new schemaType. if null, cursors will be used and the resulting object
     *                    will be disconnected.
     * @return if successful applied {@link XmlObject#substitute(QName, SchemaType)} a living object with a
     * type == newType is returned. Otherwise null is returned as you can no longer manipulate the object.
     */
    public static XmlObject qualifySubstitutionGroup(final XmlObject xobj, final QName newInstance, final SchemaType newType) {
        XmlObject substitute = null;

        if (newType != null) {
            substitute = xobj.substitute(newInstance, newType);
            if (substitute != null && substitute.schemaType() == newType
                    && substitute.getDomNode().getLocalName().equals(newInstance.getLocalPart())) {
                return substitute;
            }
        }

        final XmlCursor cursor = xobj.newCursor();
        cursor.setName(newInstance);
        final QName qName = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");
        cursor.removeAttribute(qName);
        cursor.toNextToken();
        if (cursor.isNamespace()) {
            cursor.removeXml();
        }

        cursor.dispose();

        return null;
    }
}
