package de.cpg.oss.ebics.api;

import lombok.Value;
import lombok.experimental.Wither;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;

@Value
@Wither
public class EbicsConfiguration {

    private static final String EBICS_PROPERTY_ROOT = "ebics.client";

    private final MessageProvider messageProvider;

    private final SignatureVersion signatureVersion = SignatureVersion.A006;
    private final AuthenticationVersion authenticationVersion = AuthenticationVersion.X002;
    private final EncryptionVersion encryptionVersion = EncryptionVersion.E002;
    private final int revision = 1;
    private final EbicsVersion version = EbicsVersion.H004;
    private final Charset veuDisplayFileCharset = Charset.forName("ISO-8859-1");

    public EbicsConfiguration() {
        this(Locale.getDefault());
    }

    public EbicsConfiguration(final Locale locale) {
        this(new MessageProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }
        });
    }

    private EbicsConfiguration(final MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    public Locale getLocale() {
        return getMessageProvider().getLocale();
    }
}
