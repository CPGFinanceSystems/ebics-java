package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.io.ContentFactory;
import de.cpg.oss.ebics.io.InputStreamContentFactory;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.HttpUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import de.cpg.oss.ebics.utils.ZipUtil;
import de.cpg.oss.ebics.xml.EbicsResponseElement;
import de.cpg.oss.ebics.xml.ResponseElement;
import de.cpg.oss.ebics.xml.ResponseOrderDataElement;
import org.apache.http.HttpEntity;
import org.ebics.h004.EbicsRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

abstract class ClientUtil {

    interface ResponseElementParser<T extends ResponseElement<?>> {
        T parse(ContentFactory contentFactory) throws EbicsException;
    }

    interface ResponseOrderDataElementParser<T extends ResponseOrderDataElement> {
        T parse(InputStream orderDataXml) throws EbicsException;
    }

    static EbicsResponseElement requestExchange(final EbicsSession session, final EbicsRequest request)
            throws EbicsException {
        return requestExchange(session, request, EbicsResponseElement::parse);
    }

    static <T, O extends ResponseElement<T>> O requestExchange(
            final EbicsSession session,
            final EbicsRequest request,
            final ResponseElementParser<O> responseElementParser) throws EbicsException {
        return requestExchange(session, EbicsRequest.class, request, responseElementParser,
                Optional.ofNullable(request.getHeader().getStatic().getOrderDetails())
                        .map(orderDetails -> orderDetails.getOrderType().getValue())
                        .orElse("Ebics"));
    }

    static <I, T, O extends ResponseElement<T>> O requestExchange(
            final EbicsSession session,
            final Class<I> requestClass, final I request,
            final ResponseElementParser<O> responseElementParser,
            final String baseElementName) throws EbicsException {
        final byte[] xml = XmlUtil.prettyPrint(requestClass, request);
        session.getTraceManager().trace(xml, baseElementName.concat("Request"), session.getUser());
        XmlUtil.validate(xml);

        final HttpEntity httpEntity = HttpUtil.sendAndReceive(session.getBank(), new ByteArrayInputStream(xml),
                session.getMessageProvider());
        final O response;
        try {
            response = responseElementParser.parse(InputStreamContentFactory.of(httpEntity));
        } catch (final IOException e) {
            throw new EbicsException(e);
        }

        session.getTraceManager().trace(response.getResponseClass(), response.getResponse(),
                baseElementName.concat("Response"), session.getUser());
        response.report(session.getMessageProvider());
        return response;
    }

    static <O extends ResponseOrderDataElement, I extends ResponseElement> O orderDataElement(
            final EbicsSession session,
            final I responseElement,
            final ResponseOrderDataElementParser<O> responseOrderDataElementParser,
            final String baseElementName) throws EbicsException {
        final byte[] orderDataXml = ZipUtil.uncompress(CryptoUtil.decrypt(
                responseElement.getOrderData(),
                responseElement.getTransactionKey(),
                session.getUser().getEncryptionKey().getPrivateKey()));
        session.getTraceManager().trace(orderDataXml, baseElementName.concat("ResponseOrderData"), session.getUser());
        return responseOrderDataElementParser.parse(new ByteArrayInputStream(orderDataXml));
    }
}
