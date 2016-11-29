package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.XmlMessageTracer;
import de.cpg.oss.ebics.utils.IOUtil;
import de.cpg.oss.ebics.utils.XmlUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@AllArgsConstructor
public final class FileXmlMessageTracer implements XmlMessageTracer {

    private final File traceDirectory;

    @Override
    public void trace(final InputStream xml, final String elementName) {
        final File file = new File(
                traceDirectory,
                MessageFormat.format(
                        "{0}_{1}.xml",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        elementName));
        try (final FileOutputStream out = new FileOutputStream(file)) {
            out.write(IOUtil.read(xml));
        } catch (final IOException e) {
            log.error("Exception from " + FileXmlMessageTracer.class.getSimpleName(), e);
        }
    }

    @Override
    public <T> void trace(final Class<T> clazz, final T object, final String elementName) {
        trace(XmlUtil.prettyPrint(clazz, object), elementName);
    }
}
