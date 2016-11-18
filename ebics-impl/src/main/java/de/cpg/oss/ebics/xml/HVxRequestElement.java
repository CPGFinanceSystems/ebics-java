package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsPartner;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.HVDOrderParamsType;
import org.ebics.h004.HVTOrderParamsType;
import org.ebics.h004.TransactionPhaseType;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class HVxRequestElement implements EbicsRequestElement {

    public static class HVD extends HVxRequestElement {
        @Builder
        private HVD(final String orderType, final EbicsPartner partner, final String orderId) {
            super(orderType, partner, orderId);
        }

        @Override
        protected OrderType requestOrderType() {
            return OrderType.HVD;
        }

        @Override
        protected JAXBElement<?> orderParams() {
            final HVDOrderParamsType orderParams = OBJECT_FACTORY.createHVDOrderParamsType();
            orderParams.setOrderType(orderType);
            orderParams.setPartnerID(partner.getId());
            orderParams.setOrderID(orderId);
            return OBJECT_FACTORY.createHVDOrderParams(orderParams);
        }
    }

    public static class HVT extends HVxRequestElement {
        private final boolean completeOrderData;
        private final int fetchOffset;
        private final int fetchLimit;

        @Builder
        private HVT(final HVxRequestElement hvdRequestElement, final boolean completeOrderData, final int fetchOffset,
                    final int fetchLimit) {
            super(hvdRequestElement);
            this.completeOrderData = completeOrderData;
            this.fetchOffset = fetchOffset;
            this.fetchLimit = fetchLimit;
        }

        @Override
        protected OrderType requestOrderType() {
            return OrderType.HVT;
        }

        @Override
        protected JAXBElement<?> orderParams() {
            final HVTOrderParamsType.OrderFlags orderFlags = OBJECT_FACTORY.createHVTOrderParamsTypeOrderFlags();
            orderFlags.setCompleteOrderData(completeOrderData);
            orderFlags.setFetchOffset(BigInteger.valueOf(fetchOffset));
            orderFlags.setFetchLimit(BigInteger.valueOf(fetchLimit));

            final HVTOrderParamsType orderParams = OBJECT_FACTORY.createHVTOrderParamsType();
            orderParams.setOrderType(orderType);
            orderParams.setPartnerID(partner.getId());
            orderParams.setOrderID(orderId);
            orderParams.setOrderFlags(orderFlags);

            return OBJECT_FACTORY.createHVTOrderParams(orderParams);
        }
    }

    private HVxRequestElement(final HVxRequestElement requestElement) {
        this(requestElement.orderType, requestElement.partner, requestElement.orderId);
    }

    @NonNull
    protected final String orderType;
    @NonNull
    protected final EbicsPartner partner;
    @NonNull
    protected final String orderId;

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        return request(session.getConfiguration(),
                header(mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session,
                                CryptoUtil.generateNonce(),
                                orderDetails(EbicsRequestElement.orderAttribute(requestOrderType()), requestOrderType(),
                                        orderParams()))));
    }

    protected abstract OrderType requestOrderType();

    protected abstract JAXBElement<?> orderParams();
}
