package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.DetailedVEUOrder;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.SignatureVersion;
import de.cpg.oss.ebics.api.VEUOrder;
import de.cpg.oss.ebics.api.VEUOrder.VEUOrderBuilder;
import de.cpg.oss.ebics.utils.XmlUtil;
import javaslang.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ebics.h004.*;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class VEUResponseOrderDataElement<T> implements ResponseOrderDataElement<T> {

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Standard extends VEUResponseOrderDataElement<HVUResponseOrderDataType> {

        @Getter
        private final HVUResponseOrderDataType responseOrderData;

        public static Standard parse(final InputStream orderDataXml) {
            return new Standard(XmlUtil.parse(HVUResponseOrderDataType.class, orderDataXml));
        }

        @Override
        public Class<HVUResponseOrderDataType> getResponseOrderDataClass() {
            return HVUResponseOrderDataType.class;
        }

        public Collection<VEUOrder> getVEUOrders() {
            return responseOrderData.getOrderDetails().stream()
                    .map(orderDetails -> withSigningInfo(withOriginatorInfo(VEUOrder.builder(),
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

        public static Detailed parse(final InputStream orderDataXml) {
            return new Detailed(XmlUtil.parse(HVZResponseOrderDataType.class, orderDataXml));
        }

        @Override
        public Class<HVZResponseOrderDataType> getResponseOrderDataClass() {
            return HVZResponseOrderDataType.class;
        }

        public Collection<DetailedVEUOrder> getDetailedVEUOrders() {
            return responseOrderData.getOrderDetails().stream()
                    .map(orderDetails -> DetailedVEUOrder.builder()
                            .order(withSigningInfo(
                                    withOriginatorInfo(VEUOrder.builder(),
                                            orderDetails.getOriginatorInfo()),
                                    orderDetails.getSigningInfo())
                                    .type(OrderType.ofRaw(orderDetails.getOrderType()))
                                    .id(orderDetails.getOrderID())
                                    .dataSize(orderDetails.getOrderDataSize().intValue())
                                    .build())
                            .dataDigest(orderDetails.getDataDigest().getValue())
                            .dataSignatureVersion(SignatureVersion.ofRaw(orderDetails.getDataDigest().getSignatureVersion()))
                            .orderCount(Option.of(orderDetails.getTotalOrders())
                                    .map(BigInteger::intValue).getOrElse(0))
                            .orderSumAmount(Option.of(orderDetails.getTotalAmount())
                                    .map(HVZOrderDetailsType.TotalAmount::getValue).getOrElse(BigDecimal.ZERO))
                            .fileFormat(Option.of(orderDetails.getFileFormat())
                                    .map(FileFormatType::getValue).getOrElse(""))
                            .currency(Option.of(orderDetails.getCurrency()).getOrElse(""))
                            .build())
                    .collect(Collectors.toList());
        }
    }

    private static VEUOrderBuilder withSigningInfo(final VEUOrderBuilder orderBuilder,
                                                   final HVUSigningInfoType signingInfo) {
        return orderBuilder.readyToBeSigned(signingInfo.isReadyToBeSigned())
                .requiredNumberOfSignatures(signingInfo.getNumSigRequired().intValue())
                .doneNumberOfSignatures(signingInfo.getNumSigDone().intValue());
    }

    private static VEUOrderBuilder withOriginatorInfo(final VEUOrderBuilder orderBuilder,
                                                      final HVUOriginatorInfoType originatorInfo) {
        return orderBuilder.partnerId(originatorInfo.getPartnerID())
                .userId(originatorInfo.getUserID())
                .timestamp(originatorInfo.getTimestamp());
    }
}
