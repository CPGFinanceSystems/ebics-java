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

        final StaticHeaderOrderDetailsType orderDetails;
        if (orderType.equals(OrderType.FUL)) {
            FULOrderParamsType.Builder<Void> fULOrderParamsBuilder = FULOrderParamsType.builder()
                    .withFileFormat(FileFormatType.builder()
                            .withCountryCode(session.getConfiguration().getLocale().getCountry().toUpperCase())
                            .withValue(session.getSessionParam("FORMAT"))
                            .build());
            if (Boolean.valueOf(session.getSessionParam("TEST"))) {
                fULOrderParamsBuilder = fULOrderParamsBuilder.addParameters(stringParameter("TEST", "TRUE"));
            }
            if (Boolean.valueOf(session.getSessionParam("EBCDIC"))) {
                fULOrderParamsBuilder = fULOrderParamsBuilder.addParameters(stringParameter("EBCDIC", "TRUE"));

            }
            orderDetails = orderDetails(
                    OrderAttributeType.DZHNN,
                    orderType,
                    OBJECT_FACTORY.createFULOrderParams(fULOrderParamsBuilder.build()));
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
