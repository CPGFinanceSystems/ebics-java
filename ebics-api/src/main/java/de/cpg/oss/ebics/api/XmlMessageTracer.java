package de.cpg.oss.ebics.api;

import java.io.InputStream;

public interface XmlMessageTracer {

    void trace(InputStream xml, String elementName);

    <T> void trace(Class<T> clazz, T object, String elementName);
}
