package de.cpg.oss.ebics.api;

import lombok.Builder;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

public final class EbicsEncryptionKey extends EbicsRsaKey<EncryptionVersion> {
    private static final long serialVersionUID = 1L;

    @Builder
    EbicsEncryptionKey(final PublicKey publicKey, final EncryptionVersion version, final byte[] digest, final LocalDateTime creationTime, final PrivateKey privateKey) {
        super(publicKey, version, digest, creationTime, privateKey);
    }
}
