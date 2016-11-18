package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.VEUOrderDetails;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.xml.*;
import org.ebics.h004.EbicsRequest;

import java.util.Collection;

abstract class DistributedElectronicSignature {

    static Collection<VEUOrderDetails> getOrdersForVEU(final EbicsSession session) throws EbicsException {
        final EbicsRequest ebicsRequest = new HVURequestElement().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HVUResponseOrderDataElement orderDataElement = ClientUtil.orderDataElement(session, responseElement,
                HVUResponseOrderDataElement::parse, EbicsRequestElement.orderType(ebicsRequest).name());

        return orderDataElement.getVEUOrderDetails();
    }

    static Collection<VEUOrderDetails> getDetailedOrdersForVEU(final EbicsSession session) throws EbicsException {
        final EbicsRequest ebicsRequest = new HVZRequestElement().create(session);

        final EbicsResponseElement responseElement = ClientUtil.requestExchange(session, ebicsRequest);
        final HVZResponseOrderDataElement orderDataElement = ClientUtil.orderDataElement(session, responseElement,
                HVZResponseOrderDataElement::parse, EbicsRequestElement.orderType(ebicsRequest).name());

        return orderDataElement.getVEUOrderDetails();
    }
}
