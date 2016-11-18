package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.DetailedVEUOrder;
import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.VEUOrder;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.xml.*;
import org.ebics.h004.EbicsRequest;

import java.util.Collection;

abstract class DistributedElectronicSignature {

    static Collection<VEUOrder> getOrdersForVEU(final EbicsSession session) throws EbicsException {
        final EbicsRequest ebicsRequest = new VEUOrderDetailsRequestElement().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final VEUResponseOrderDataElement.Standard orderDataElement = ClientUtil.orderDataElement(session,
                responseElement, VEUResponseOrderDataElement.Standard::parse,
                EbicsRequestElement.orderType(ebicsRequest));

        return orderDataElement.getVEUOrders();
    }

    static Collection<DetailedVEUOrder> getDetailedOrdersForVEU(final EbicsSession session) throws EbicsException {
        final EbicsRequest ebicsRequest = new VEUOrderDetailsRequestElement().withDetails(true).create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final VEUResponseOrderDataElement.Detailed orderDataElement = ClientUtil.orderDataElement(session,
                responseElement, VEUResponseOrderDataElement.Detailed::parse,
                EbicsRequestElement.orderType(ebicsRequest));

        return orderDataElement.getDetailedVEUOrders();
    }

    static DetailedVEUOrder getOrderDetails(final EbicsSession session, final VEUOrder veuOrder)
            throws EbicsException {
        final HVxRequestElement.HVD hvdRequestElement = HVxRequestElement.HVD.builder()
                .orderType(veuOrder.getType().getOrElseGet(OrderType::name))
                .partner(session.getPartner())
                .orderId(veuOrder.getId())
                .build();
        final EbicsRequest hvdRequest = hvdRequestElement.create(session);

        final EbicsResponseElement hvdResponse = ClientUtil.requestExchange(session, hvdRequest);
        final HVDResponseOrderDataElement hvdResponseOrderData = ClientUtil.orderDataElement(session, hvdResponse,
                HVDResponseOrderDataElement::parse, EbicsRequestElement.orderType(hvdRequest));

        return hvdResponseOrderData.detailedVEUOrder(session.getConfiguration(), veuOrder);
    }
}
