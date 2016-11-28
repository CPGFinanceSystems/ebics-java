package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
public final class EbicsSignatureKey extends EbicsRsaKey {
    private static final long serialVersionUID = 3L;

    @Builder
    private EbicsSignatureKey(final PublicKey publicKey, final SignatureVersion version, final byte[] digest, final OffsetDateTime creationTime, final PrivateKey privateKey) {
        super(publicKey, version.name(), digest, creationTime, privateKey);
    }

    public SignatureVersion getSignatureVersion() {
        return SignatureVersion.valueOf(getVersion());
    }

    // We all love JPA, don't we?
    private EbicsSignatureKey() {
        super();
    }
}
