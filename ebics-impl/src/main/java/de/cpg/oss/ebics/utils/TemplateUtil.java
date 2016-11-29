package de.cpg.oss.ebics.utils;

import de.cpg.oss.ebics.api.EbicsRsaKey;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

public abstract class TemplateUtil {

    public static void createPdfFromOdt(
            final InputStream template,
            final Map<String, Object> templateParameters,
            final OutputStream pdfOutputStream) throws IOException, XDocReportException {
        // Prepare the IXDocReport instance based on the template, using
        // Freemarker template engine
        final IXDocReport report = XDocReportRegistry.getRegistry().loadReport(template, TemplateEngineKind.Freemarker);

        // Define what we want to do (PDF file from ODF template)
        final Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);

        // Add properties to the context
        final IContext ctx = report.createContext();
        templateParameters.forEach(ctx::put);

        // Write the PDF file to output stream
        report.convert(ctx, options, pdfOutputStream);
    }

    public static <T extends EbicsRsaKey> KeyInfo keyInfo(final T ebicsRsaKey) {
        return KeyInfo.builder()
                .digestHex(hexBlock(ebicsRsaKey.getDigest()))
                .exponentBits(publicKey(ebicsRsaKey).getPublicExponent().bitLength())
                .exponentHex(encodeHexString(publicKey(ebicsRsaKey).getPublicExponent().toByteArray()))
                .modulusBits(publicKey(ebicsRsaKey).getModulus().bitLength())
                .modulusHex(hexBlock(publicKey(ebicsRsaKey).getModulus().toByteArray()))
                .version(ebicsRsaKey.getVersion())
                .build();
    }

    private static RSAPublicKey publicKey(final EbicsRsaKey ebicsRsaKey) {
        return (RSAPublicKey) ebicsRsaKey.getPublicKey();
    }

    private static String hexBlock(final byte[] bytes) {
        return multiline(encodeHexString(bytes).replaceFirst("^00 ", ""), 16 * 3);
    }

    private static String multiline(final String line, final int maxLineLength) {
        final StringBuilder builder = new StringBuilder(line.length() + line.length() / maxLineLength);
        int totalLines = line.length() / maxLineLength;
        if (line.length() % maxLineLength != 0) {
            totalLines++;
        }
        for (int i = 0; i < totalLines; i++) {
            int endIndex = (i + 1) * maxLineLength;
            if (endIndex > line.length()) {
                endIndex = line.length();
            }
            builder.append(line.substring(i * maxLineLength, endIndex).replaceAll(" $", ""));
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private static String encodeHexString(final byte[] bytes) {
        final String hex = Hex.encodeHexString(bytes).toUpperCase();
        final StringBuilder builder = new StringBuilder(hex.length() * 3);
        for (int i = 0; i < bytes.length; i++) {
            builder.append(hex.substring(i * 2, i * 2 + 2));
            builder.append(' ');
        }
        return builder.toString().trim();
    }
}
