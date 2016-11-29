package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.XmlMessageTracer;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.Charset;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Slf4jXmlMessageTracer implements XmlMessageTracer {

    public static final XmlMessageTracer INSTANCE = new Slf4jXmlMessageTracer();

    @Override
    public void trace(final InputStream xml, final String elementName) {
        log.trace("{}\n{}", elementName, new String(IOUtil.read(xml), Charset.forName("UTF-8")));
    }

    @Override
    public <T> void trace(final Class<T> clazz, final T object, final String elementName) {
        trace(XmlUtil.prettyPrint(clazz, object), elementName);
    }
}
