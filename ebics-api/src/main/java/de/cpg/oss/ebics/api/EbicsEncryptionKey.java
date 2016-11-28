package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
public final class EbicsEncryptionKey extends EbicsRsaKey {
    private static final long serialVersionUID = 3L;

    @Builder
    private EbicsEncryptionKey(final PublicKey publicKey, final EncryptionVersion version, final byte[] digest, final OffsetDateTime creationTime, final PrivateKey privateKey) {
        super(publicKey, version.name(), digest, creationTime, privateKey);
    }

    public EncryptionVersion getEncryptionVersion() {
        return EncryptionVersion.valueOf(getVersion());
    }

    // We all love JPA, don't we?
    private EbicsEncryptionKey() {
        super();
    }
}
