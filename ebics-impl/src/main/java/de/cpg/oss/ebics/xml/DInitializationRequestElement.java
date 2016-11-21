package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.Builder;
import lombok.NonNull;
import org.ebics.h004.*;

import java.time.LocalDate;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@Builder
public class DInitializationRequestElement implements EbicsRequestElement {

    @NonNull
    private final OrderType orderType;
    @NonNull
    private final LocalDate startRange;
    @NonNull
    private final LocalDate endRange;

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        final StaticHeaderOrderDetailsType orderDetails;

        if (orderType.equals(OrderType.FDL)) {
            FDLOrderParamsType.Builder<Void> fDLOrderParamsBuilder = FDLOrderParamsType.builder()
                    .withFileFormat(FileFormatType.builder()
                            .withCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase())
                            .withValue(session.getSessionParam("FORMAT"))
                            .build())
                    .withDateRange(FDLOrderParamsType.DateRange.builder()
                            .withStart(startRange)
                            .withEnd(endRange)
                            .build());
            if (Boolean.getBoolean(session.getSessionParam("TEST"))) {
                fDLOrderParamsBuilder = fDLOrderParamsBuilder.addParameters(stringParameter("TEST", "TRUE"));
            }
            orderDetails = orderDetails(OrderAttributeType.DZHNN, orderType,
                    OBJECT_FACTORY.createFDLOrderParams(fDLOrderParamsBuilder.build()));
        } else {
            orderDetails = orderDetails(OrderAttributeType.DZHNN, orderType);
        }

        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session, CryptoUtil.generateNonce(), orderDetails)));
    }
}
