package de.cpg.oss.ebics.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public abstract class EbicsRsaKey<T extends Enum> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final PublicKey publicKey;
    @NonNull
    private final T version;
    @NonNull
    private final byte[] digest;

    private final LocalDateTime creationTime;
    private final PrivateKey privateKey;
}
