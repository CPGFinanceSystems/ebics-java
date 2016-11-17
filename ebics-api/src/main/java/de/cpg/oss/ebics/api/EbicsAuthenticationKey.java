package de.cpg.oss.ebics.api;

import lombok.Builder;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;

public final class EbicsAuthenticationKey extends EbicsRsaKey<AuthenticationVersion> {
    private static final long serialVersionUID = 1L;

    @Builder
    EbicsAuthenticationKey(final PublicKey publicKey, final AuthenticationVersion version, final byte[] digest, final OffsetDateTime creationTime, final PrivateKey privateKey) {
        super(publicKey, version, digest, creationTime, privateKey);
    }
}
