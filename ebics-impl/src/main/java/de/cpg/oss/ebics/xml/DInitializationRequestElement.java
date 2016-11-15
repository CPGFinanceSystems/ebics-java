package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.Builder;
import lombok.NonNull;
import org.ebics.h004.*;

import javax.xml.bind.JAXBElement;
import java.time.LocalDate;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.OBJECT_FACTORY;

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
            final FileFormatType fileFormat = OBJECT_FACTORY.createFileFormatType();
            fileFormat.setCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase());
            fileFormat.setValue(session.getSessionParam("FORMAT"));

            final FDLOrderParamsType fDLOrderParamsType = OBJECT_FACTORY.createFDLOrderParamsType();
            fDLOrderParamsType.setFileFormat(fileFormat);

            if (startRange != null && endRange != null) {
                final FDLOrderParamsType.DateRange range = OBJECT_FACTORY.createFDLOrderParamsTypeDateRange();
                range.setStart(startRange);
                range.setEnd(endRange);

                fDLOrderParamsType.setDateRange(range);
            }

            if (Boolean.getBoolean(session.getSessionParam("TEST"))) {
                final Parameter.Value value = OBJECT_FACTORY.createParameterValue();
                value.setType("String");
                value.setValue("TRUE");

                final Parameter parameter = OBJECT_FACTORY.createParameter();
                parameter.setName("TEST");
                parameter.setValue(value);

                fDLOrderParamsType.getParameters().add(parameter);
            }

            final JAXBElement<FDLOrderParamsType> orderParams = OBJECT_FACTORY.createFDLOrderParams(fDLOrderParamsType);
            orderDetails = EbicsXmlFactory.orderDetails(OrderAttributeType.DZHNN, orderType, orderParams);
        } else {
            orderDetails = EbicsXmlFactory.orderDetails(OrderAttributeType.DZHNN, orderType);
        }

        return EbicsXmlFactory.request(
                session.getConfiguration(),
                EbicsXmlFactory.header(
                        EbicsXmlFactory.mutableHeader(TransactionPhaseType.INITIALISATION),
                        EbicsXmlFactory.staticHeader(session, CryptoUtil.generateNonce(), orderDetails)));
    }
}
