package de.cpg.oss.ebics.api;

import javaslang.collection.Stream;
import javaslang.control.Either;

public enum SignatureVersion {
    A005,
    A006;

    public static Either<SignatureVersion, String> ofRaw(final String rawVersion) {
        return Stream.of(values()).find(version -> version.name().equals(rawVersion))
                .map(Either::<SignatureVersion, String>left)
                .getOrElse(Either.right(rawVersion));
    }
}
