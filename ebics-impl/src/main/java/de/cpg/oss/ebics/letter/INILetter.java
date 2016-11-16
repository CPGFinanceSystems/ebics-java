package de.cpg.oss.ebics.letter;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.InitLetter;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.TemplateUtil;
import fr.opensagres.xdocreport.core.XDocReportException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

class INILetter implements InitLetter {

    @Override
    public OutputStream create(final EbicsSession session) throws EbicsException {
        try {
            final OutputStream pdfOutputStream = new FileOutputStream(new File(
                    session.getConfiguration().getLettersDirectory(session.getUser()),
                    "INILetter.pdf"));

            final Map<String, Object> templateParameters = new HashMap<>();
            templateParameters.put("dateTime", LocalDateTime.now()
                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                            .withLocale(session.getMessageProvider().getLocale())));
            templateParameters.put("user", session.getUser());
            templateParameters.put("signatureKey", TemplateUtil.keyInfo(session.getUser().getSignatureKey()));
            templateParameters.put("bank", session.getBank());
            templateParameters.put("partner", session.getPartner());

            TemplateUtil.createPdfFromOdt("/iniletter.odt", templateParameters, pdfOutputStream);

            return pdfOutputStream;
        } catch (XDocReportException | IOException e) {
            throw new EbicsException(e);
        }
    }
}
