package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.ebics.h004.EbicsRequest;
import org.ebics.h004.HVUOrderParamsType;
import org.ebics.h004.HVZOrderParamsType;
import org.ebics.h004.TransactionPhaseType;

import javax.xml.bind.JAXBElement;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VEUOrderDetailsRequestElement implements EbicsRequestElement {

    private final Collection<OrderType> orderTypes;
    private final boolean details;

    public VEUOrderDetailsRequestElement() {
        this.orderTypes = Collections.emptyList();
        this.details = false;
    }

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        final Collection<String> orderTypeStrings = orderTypes.stream().map(OrderType::name).collect(Collectors.toList());
        final JAXBElement<?> orderParams;
        final OrderType orderType;
        if (details) {
            orderParams = OBJECT_FACTORY.createHVZOrderParams(HVZOrderParamsType.builder()
                    .withOrderTypes(orderTypeStrings)
                    .build());
            orderType = OrderType.HVZ;
        } else {
            orderParams = OBJECT_FACTORY.createHVUOrderParams(HVUOrderParamsType.builder()
                    .withOrderTypes(orderTypeStrings)
                    .build());
            orderType = OrderType.HVU;
        }

        return request(session.getConfiguration(),
                header(mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session,
                                CryptoUtil.generateNonce(),
                                orderDetails(EbicsRequestElement.orderAttribute(orderType), orderType, orderParams))));
    }
}
