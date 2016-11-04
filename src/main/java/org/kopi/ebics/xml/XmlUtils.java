package org.kopi.ebics.xml;

import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.ebics.h004.HIARequestOrderDataType;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsUser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.Optional;

@Slf4j
public class XmlUtils {

    private static final Schema XML_SCHEMAS;
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    public static String CANONICALIZAION_METHOD = CanonicalizationMethod.INCLUSIVE;
    public static String DIGEST_METHOD = DigestMethod.SHA256;
    public static String SIGNATURE_METHOD = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static String XPATH_SELECTOR = "//*[@authenticate='true']";

    static {
        DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);

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
        if (null == clazz.getAnnotation(XmlRootElement.class)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "{0} annotation is required for deserialization of objects with type {1}",
                    XmlRootElement.class.getSimpleName(),
                    clazz.getName()));
        }
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (T) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> byte[] sign(final Class<T> clazz, final T object, final EbicsUser user) {
        try {
            final DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final Document document = builder.parse(new ByteArrayInputStream(prettyPrint(clazz, object)));

            final XPath xPath = X_PATH_FACTORY.newXPath();
            final Node node = (Node) xPath.evaluate("//*[local-name()='SignedInfo']", document.getDocumentElement(), XPathConstants.NODE);

            final byte[] canonized = canonize(node);
            log.info("Canonized for sign: '{}'", new String(canonized));

            return user.authenticate(canonized);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> byte[] digest(final Class<T> clazz, final T object) {
        try {
            final DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final Document document = builder.parse(new ByteArrayInputStream(prettyPrint(clazz, object)));

            final XPath xPath = X_PATH_FACTORY.newXPath();
            final NodeList nodes = (NodeList) xPath.evaluate(XPATH_SELECTOR, document.getDocumentElement(), XPathConstants.NODESET);

            final MessageDigest digester = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < nodes.getLength(); ++i) {
                final Node node = nodes.item(i);
                final byte[] canonized = canonize(node);
                log.info("Canonized for digest: '{}'", new String(canonized));
                digester.update(canonized);
            }
            return digester.digest();
        } catch (final Exception e) {
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

    public static byte[] canonize(final Node node) {
        try {
            final Canonicalizer canonicalizer = Canonicalizer.getInstance(CANONICALIZAION_METHOD);
            return canonicalizer.canonicalizeSubtree(node);
        } catch (InvalidCanonicalizerException | CanonicalizationException e) {
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
