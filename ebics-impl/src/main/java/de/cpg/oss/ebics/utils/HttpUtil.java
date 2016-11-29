package de.cpg.oss.ebics.utils;

import de.cpg.oss.ebics.api.EbicsBank;
import de.cpg.oss.ebics.api.exception.EbicsException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;

public abstract class HttpUtil {

    public static HttpEntity sendAndReceive(final EbicsBank ebicsBank, final InputStream xmlRequest) throws EbicsException {
        try {
            final HttpResponse httpResponse = Request.Post(ebicsBank.getUri().toString())
                    .bodyStream(xmlRequest, ContentType.APPLICATION_XML.withCharset(Charset.defaultCharset()))
                    // TODO .socketTimeout()
                    .execute()
                    .returnResponse();
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException(MessageFormat.format(
                        "Got HTTP return code {0}",
                        httpResponse.getStatusLine().getStatusCode()));
            }
            return httpResponse.getEntity();
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }
}
