package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
public final class EbicsSignatureKey extends EbicsRsaKey<SignatureVersion> {
    private static final long serialVersionUID = 2L;

    @Builder
    EbicsSignatureKey(final PublicKey publicKey, final SignatureVersion version, final byte[] digest, final OffsetDateTime creationTime, final PrivateKey privateKey) {
        super(publicKey, version, digest, creationTime, privateKey);
    }
}
