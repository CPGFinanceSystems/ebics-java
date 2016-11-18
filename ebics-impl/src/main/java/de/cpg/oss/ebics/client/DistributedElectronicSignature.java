package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.VEUOrderDetails;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.xml.*;
import org.ebics.h004.EbicsRequest;

import java.util.Collection;

abstract class DistributedElectronicSignature {

    static Collection<VEUOrderDetails> getOrdersForVEU(final EbicsSession session) throws EbicsException {
        final EbicsRequest ebicsRequest = new VEUOrderDetailsRequestElement().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final VEUResponseOrderDataElement.Standard orderDataElement = ClientUtil.orderDataElement(session,
                responseElement, VEUResponseOrderDataElement.Standard::parse,
                EbicsRequestElement.orderType(ebicsRequest));

        return orderDataElement.getVEUOrderDetailsList();
    }

    static Collection<VEUOrderDetails> getDetailedOrdersForVEU(final EbicsSession session) throws EbicsException {
        final EbicsRequest ebicsRequest = new VEUOrderDetailsRequestElement().withDetails(true).create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final VEUResponseOrderDataElement.Detailed orderDataElement = ClientUtil.orderDataElement(session,
                responseElement, VEUResponseOrderDataElement.Detailed::parse,
                EbicsRequestElement.orderType(ebicsRequest));

        return orderDataElement.getVEUOrderDetailsList();
    }

    static VEUOrderDetails getOrderDetails(final EbicsSession session, final VEUOrderDetails orderDetails)
            throws EbicsException {
        final HVxRequestElement.HVD hvdRequestElement = HVxRequestElement.HVD.builder()
                .orderType(orderDetails.getType().getOrElseGet(OrderType::name))
                .partner(session.getPartner())
                .orderId(orderDetails.getId())
                .build();
        final EbicsRequest hvdRequest = hvdRequestElement.create(session);

        final EbicsResponseElement hvdResponse = ClientUtil.requestExchange(session, hvdRequest);
        final HVDResponseOrderDataElement hvdResponseOrderData = ClientUtil.orderDataElement(session, hvdResponse,
                HVDResponseOrderDataElement::parse, EbicsRequestElement.orderType(hvdRequest));

        return hvdResponseOrderData.enrichVEUOrderDetails(session.getConfiguration(), orderDetails);
    }
}
