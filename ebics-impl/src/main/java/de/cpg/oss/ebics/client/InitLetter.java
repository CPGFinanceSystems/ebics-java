package de.cpg.oss.ebics.client;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.utils.TemplateUtil;
import fr.opensagres.xdocreport.core.XDocReportException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

abstract class InitLetter {

    static void createINI(final EbicsSession session, final OutputStream outputStream) {
        try {
            TemplateUtil.createPdfFromOdt(
                    template("iniletter", session.getMessageProvider().getLocale()),
                    templateParameters(session),
                    outputStream);
        } catch (XDocReportException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void createHIA(final EbicsSession session, final OutputStream outputStream) {
        try {
            TemplateUtil.createPdfFromOdt(
                    template("hialetter", session.getMessageProvider().getLocale()),
                    templateParameters(session),
                    outputStream);
        } catch (XDocReportException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream template(final String name, final Locale locale) {
        return Optional.ofNullable(InitLetter.class.getResourceAsStream(templateResourceLocation(name, locale)))
                .orElseGet(() -> InitLetter.class.getResourceAsStream(templateResourceLocation(name, Locale.US)));
    }

    private static String templateResourceLocation(final String name, final Locale locale) {
        return "/".concat(name).concat("_").concat(locale.getLanguage()).concat(".odt");
    }

    private static Map<String, Object> templateParameters(final EbicsSession session) {
        final Map<String, Object> templateParameters = new HashMap<>();

        templateParameters.put("dateTime", LocalDateTime.now()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        .withLocale(session.getMessageProvider().getLocale())));
        templateParameters.put("user", session.getUser()
                .withName(Optional.ofNullable(session.getUser().getName())
                        .orElse("")));
        templateParameters.put("bank", session.getBank()
                .withName(Optional.ofNullable(session.getBank().getName())
                        .orElseGet(() -> session.getBank().getId())));
        templateParameters.put("partner", session.getPartner());
        templateParameters.put("signatureKey", TemplateUtil.keyInfo(session.getUser().getSignatureKey()));
        templateParameters.put("authenticationKey", TemplateUtil.keyInfo(session.getUser().getAuthenticationKey()));
        templateParameters.put("encryptionKey", TemplateUtil.keyInfo(session.getUser().getEncryptionKey()));

        return templateParameters;
    }
}
