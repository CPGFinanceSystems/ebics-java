package de.cpg.oss.ebics.api;

import lombok.Value;
import lombok.experimental.Wither;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

@Value
@Wither
public class EbicsConfiguration {

    private static final String EBICS_PROPERTY_ROOT = "ebics.client";

    private final File rootDirectory;
    private final MessageProvider messageProvider;

    private final SignatureVersion signatureVersion = SignatureVersion.A005;
    private final AuthenticationVersion authenticationVersion = AuthenticationVersion.X002;
    private final EncryptionVerion encryptionVersion = EncryptionVerion.E002;
    private final boolean compressionEnabled = true;
    private final int revision = 1;
    private final EbicsVersion version = EbicsVersion.H004;

    public EbicsConfiguration(final File rootDirectory, final Locale locale) {
        this(rootDirectory, new MessageProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }
        });
    }

    public EbicsConfiguration(final File rootDirectory, final MessageProvider messageProvider) {
        this.rootDirectory = rootDirectory;
        this.messageProvider = messageProvider;
    }

    public File getSerializationDirectory() {
        return new File(getRootDirectory(), getProperty("serialization.dir.name", "serialized"));
    }

    public File getUsersDirectory() {
        return new File(getRootDirectory(), getProperty("users.dir.name", "users"));
    }

    public File getTransferTraceDirectory(final EbicsUser user) {
        return new File(getUserDirectory(user), getProperty("traces.dir.name", "traces"));
    }

    public File getLettersDirectory(final EbicsUser user) {
        return new File(getUserDirectory(user), getProperty("letters.dir.name", "letters"));
    }

    public File getUserDirectory(final EbicsUser user) {
        return new File(getUsersDirectory(), user.getId());
    }

    public Locale getLocale() {
        return getMessageProvider().getLocale();
    }

    private static String getProperty(final String key, final String defaultValue) {
        return Optional.ofNullable(System.getProperty(EBICS_PROPERTY_ROOT.concat(".").concat(key)))
                .orElse(defaultValue);
    }
}
