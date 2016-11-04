package org.kopi.ebics.xml;

import lombok.extern.slf4j.Slf4j;
import org.ebics.h004.HIARequestOrderDataType;
import org.kopi.ebics.exception.EbicsException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.Optional;

@Slf4j
public class XmlUtils {

    private static final Schema XML_SCHEMAS;

    static {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setErrorHandler(LoggingErrorHandler.INSTANCE);
            factory.setResourceResolver(new LSResourceResolver() {
                @Override
                public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId, final String baseURI) {
                    final InputStream resourceAsStream = this.getClass().getResourceAsStream("/xsd/".concat(systemId));
                    return new CustomLSInput(publicId, systemId, resourceAsStream);
                }
            });
            XML_SCHEMAS = factory.newSchema(new StreamSource(XmlUtils.class.getResourceAsStream("/xsd/ebics_H004.xsd")));
        } catch (final SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> byte[] prettyPrint(final Class<T> clazz, final T object) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        prettyPrint(clazz, object, elementNameFrom(clazz), outputStream);
        return outputStream.toByteArray();
    }

    public static void validate(final byte[] xml) throws EbicsException {
        validate(new ByteArrayInputStream(xml));
    }

    public static void validate(final InputStream inputStream) throws EbicsException {
        try {
            final Validator validator = XML_SCHEMAS.newValidator();
            validator.setErrorHandler(LoggingErrorHandler.INSTANCE);
            validator.validate(new StreamSource(inputStream));
        } catch (IOException | SAXException e) {
            throw new EbicsException(e.getMessage(), e);
        }
    }

    public static <T> T parse(final Class<T> clazz, final InputStream inputStream) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (T) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void prettyPrint(final Class<T> clazz, final T object, final String elementName, final OutputStream outputStream) {
        try {
            final JAXBContext ctx = JAXBContext.newInstance(clazz);
            final Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(
                    new JAXBElement<>(new QName(namespaceFromPackageAnnotation(clazz), elementName), clazz, object),
                    outputStream);
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> String namespaceFromPackageAnnotation(final Class<T> clazz) {
        return clazz.getPackage().getAnnotation(XmlSchema.class).namespace();
    }

    private static <T> String elementNameFrom(final Class<T> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(XmlRootElement.class))
                .map(XmlRootElement::name)
                .orElseGet(() -> elementNameFrom(clazz.getSimpleName()));
    }

    private static String elementNameFrom(final String clazzName) {
        if (HIARequestOrderDataType.class.getSimpleName().equals(clazzName)) {
            return "HIARequestOrderData";
        } else {
            return clazzName;
        }
    }

    private static class LoggingErrorHandler implements ErrorHandler {

        static final LoggingErrorHandler INSTANCE = new LoggingErrorHandler();

        @Override
        public void warning(final SAXParseException exception) throws SAXException {
            log.warn(exception.toString());
        }

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            log.error(exception.toString());
            throw exception;
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            error(exception);
        }
    }
}
