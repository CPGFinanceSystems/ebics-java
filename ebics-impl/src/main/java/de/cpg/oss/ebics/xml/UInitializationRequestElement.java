package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.OrderType;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.Splitter;
import de.cpg.oss.ebics.utils.CryptoUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.ebics.h004.*;

import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

import static de.cpg.oss.ebics.xml.EbicsXmlFactory.*;

@Builder
public class UInitializationRequestElement implements EbicsRequestElement {

    @NonNull
    private final OrderType orderType;
    private final byte[] userData;
    @Getter
    @NonNull
    private final Splitter splitter;

    @Override
    public EbicsRequest createForSigning(final EbicsSession session) throws EbicsException {
        final byte[] nonce = CryptoUtil.generateNonce();
        final SecretKeySpec keySpec = new SecretKeySpec(nonce, "AES");

        splitter.readInput(session.getConfiguration().isCompressionEnabled(), keySpec);

        final List<Parameter> parameters = new ArrayList<>();
        if (Boolean.valueOf(session.getSessionParam("TEST"))) {
            final Parameter.Value value = OBJECT_FACTORY.createParameterValue();
            value.setType("String");
            value.setValue("TRUE");

            final Parameter parameter = OBJECT_FACTORY.createParameter();
            parameter.setName("TEST");
            parameter.setValue(value);

            parameters.add(parameter);
        }
        if (Boolean.valueOf(session.getSessionParam("EBCDIC"))) {
            final Parameter.Value value = OBJECT_FACTORY.createParameterValue();
            value.setType("String");
            value.setValue("TRUE");

            final Parameter parameter = OBJECT_FACTORY.createParameter();
            parameter.setName("EBCDIC");
            parameter.setValue(value);

            parameters.add(parameter);
        }

        final StaticHeaderOrderDetailsType orderDetails;
        if (orderType.equals(OrderType.FUL)) {
            final FileFormatType fileFormat = OBJECT_FACTORY.createFileFormatType();
            fileFormat.setCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase());
            fileFormat.setValue(session.getSessionParam("FORMAT"));

            final FULOrderParamsType fULOrderParams = OBJECT_FACTORY.createFULOrderParamsType();
            fULOrderParams.setFileFormat(fileFormat);
            if (parameters.size() > 0) {
                fULOrderParams.getParameters().addAll(parameters);
            }

            orderDetails = orderDetails(
                    OrderAttributeType.DZHNN,
                    orderType,
                    OBJECT_FACTORY.createFULOrderParams(fULOrderParams));
        } else {
            orderDetails = orderDetails(OrderAttributeType.OZHNN, orderType);
        }

        return request(
                session.getConfiguration(),
                header(
                        mutableHeader(TransactionPhaseType.INITIALISATION),
                        staticHeader(session, nonce, splitter.getNumSegments(), orderDetails)),
                body(
                        dataTransferRequest(
                                session,
                                userData,
                                keySpec,
                                CryptoUtil.generateTransactionKey(nonce, session.getBankEncryptionKey()))));
    }
}
