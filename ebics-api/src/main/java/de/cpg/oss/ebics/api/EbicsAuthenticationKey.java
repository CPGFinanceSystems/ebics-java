package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
public final class EbicsAuthenticationKey extends EbicsRsaKey {
    private static final long serialVersionUID = 2L;

    @Builder
    private EbicsAuthenticationKey(final PublicKey publicKey, final AuthenticationVersion version, final byte[] digest, final Instant creationTime, final PrivateKey privateKey) {
        super(publicKey, version.name(), digest, creationTime, privateKey);
    }

    public AuthenticationVersion getAuthenticationVersion() {
        return AuthenticationVersion.valueOf(getVersion());
    }

    // We all love JPA, don't we?
    private EbicsAuthenticationKey() {
        super();
    }
}
