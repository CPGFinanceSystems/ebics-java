package de.cpg.oss.ebics.api;

import java.io.InputStream;

public interface XmlMessageTracer {

    void trace(InputStream xml, String elementName, final EbicsUser user);

    <T> void trace(Class<T> clazz, T object, final String elementName, final EbicsUser user);
}
