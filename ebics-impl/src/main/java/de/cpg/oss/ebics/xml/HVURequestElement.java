package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.HVUOrderParamsType;
import org.ebics.h004.OrderAttributeType;
import org.ebics.h004.TransactionPhaseType;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HVURequestElement implements EbicsRequestElement {

    private final Collection<OrderType> orderTypes;

    public HVURequestElement() {
        this(Collections.emptyList());
    }

    public static HVURequestElement withOrderTypes(final Collection<OrderType> orderTypes) {
        return new HVURequestElement(orderTypes);
    }

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        final HVUOrderParamsType orderParams = OBJECT_FACTORY.createHVUOrderParamsType();
        orderParams.getOrderTypes().addAll(orderTypes.stream().map(OrderType::name).collect(Collectors.toList()));

        return request(session.getConfiguration(),
                header(mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session,
                                CryptoUtil.generateNonce(),
                                orderDetails(OrderAttributeType.DZHNN, OrderType.HVU,
                                        OBJECT_FACTORY.createHVUOrderParams(orderParams)))));
    }
}
