package de.cpg.oss.ebics.api;

import lombok.*;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EbicsRsaKey implements Serializable {

    private static final long serialVersionUID = 3L;

    @NonNull
    private final PublicKey publicKey;
    @NonNull
    private final String version;
    @NonNull
    private final byte[] digest;

    private final OffsetDateTime creationTime;
    private final PrivateKey privateKey;

    // We all love JPA, don't we?
    protected EbicsRsaKey() {
        this.publicKey = null;
        this.version = null;
        this.digest = null;
        this.creationTime = null;
        this.privateKey = null;
    }
}
