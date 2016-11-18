package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.SignatureVersion;
import de.cpg.oss.ebics.api.VEUOrderDetails;
import de.cpg.oss.ebics.api.VEUOrderDetails.VEUOrderDetailsBuilder;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.HVUOriginatorInfoType;
import org.ebics.h004.HVUResponseOrderDataType;
import org.ebics.h004.HVUSigningInfoType;
import org.ebics.h004.HVZResponseOrderDataType;

import java.io.InputStream;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class VEUResponseOrderDataElement<T> implements ResponseOrderDataElement<T> {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Standard extends VEUResponseOrderDataElement<HVUResponseOrderDataType> {

        @Getter
        private final HVUResponseOrderDataType responseOrderData;

        public static Standard parse(final InputStream orderDataXml) throws EbicsException {
            return new Standard(XmlUtil.parse(HVUResponseOrderDataType.class, orderDataXml));
        }

        @Override
        public Class<HVUResponseOrderDataType> getResponseOrderDataClass() {
            return HVUResponseOrderDataType.class;
        }

        @Override
        public Collection<VEUOrderDetails> getVEUOrderDetailsList() {
            return responseOrderData.getOrderDetails().stream()
                    .map(orderDetails -> withSigningInfo(withOriginatorInfo(VEUOrderDetails.builder(),
                            orderDetails.getOriginatorInfo()),
                            orderDetails.getSigningInfo())
                            .type(OrderType.ofRaw(orderDetails.getOrderType()))
                            .id(orderDetails.getOrderID())
                            .dataSize(orderDetails.getOrderDataSize().intValue())
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detailed extends VEUResponseOrderDataElement<HVZResponseOrderDataType> {

        @Getter
        private final HVZResponseOrderDataType responseOrderData;

        public static Detailed parse(final InputStream orderDataXml) throws EbicsException {
            return new Detailed(XmlUtil.parse(HVZResponseOrderDataType.class, orderDataXml));
        }

        @Override
        public Class<HVZResponseOrderDataType> getResponseOrderDataClass() {
            return HVZResponseOrderDataType.class;
        }

        @Override
        public Collection<VEUOrderDetails> getVEUOrderDetailsList() {
            return responseOrderData.getOrderDetails().stream()
                    .map(orderDetails -> withSigningInfo(withOriginatorInfo(VEUOrderDetails.builder(),
                            orderDetails.getOriginatorInfo()),
                            orderDetails.getSigningInfo())
                            .type(OrderType.ofRaw(orderDetails.getOrderType()))
                            .id(orderDetails.getOrderID())
                            .dataSize(orderDetails.getOrderDataSize().intValue())
                            .dataDigest(orderDetails.getDataDigest().getValue())
                            .dataSignatureVersion(SignatureVersion.ofRaw(orderDetails.getDataDigest().getSignatureVersion()))
                            .build())
                    .collect(Collectors.toList());
        }
    }

    public abstract Collection<VEUOrderDetails> getVEUOrderDetailsList();

    private static VEUOrderDetailsBuilder withSigningInfo(final VEUOrderDetailsBuilder orderDetailsBuilder,
                                                          final HVUSigningInfoType signingInfo) {
        return orderDetailsBuilder.readyToBeSigned(signingInfo.isReadyToBeSigned())
                .requiredNumberOfSignatures(signingInfo.getNumSigRequired().intValue())
                .doneNumberOfSignatures(signingInfo.getNumSigDone().intValue());
    }

    private static VEUOrderDetailsBuilder withOriginatorInfo(final VEUOrderDetailsBuilder orderDetailsBuilder,
                                                             final HVUOriginatorInfoType originatorInfo) {
        return orderDetailsBuilder.partnerId(originatorInfo.getPartnerID())
                .userId(originatorInfo.getUserID())
                .timestamp(originatorInfo.getTimestamp());
    }
}
