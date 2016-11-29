package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.XmlMessageTracer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NoOpXmlMessageTracer implements XmlMessageTracer {

    public static final XmlMessageTracer INSTANCE = new NoOpXmlMessageTracer();

    @Override
    public void trace(final InputStream xml, final String elementName) {
        // no-op
    }

    @Override
    public <T> void trace(final Class<T> clazz, final T object, final String elementName) {
        // no-op
    }
}
