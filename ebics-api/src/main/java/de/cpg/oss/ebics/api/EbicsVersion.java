package de.cpg.oss.ebics.api;

import javaslang.collection.Stream;
import javaslang.control.Either;


public enum EbicsVersion {
    H004;

    public static Either<EbicsVersion, String> ofRaw(final String rawVersion) {
        return Stream.of(values()).find(version -> version.name().equals(rawVersion))
                .map(Either::<EbicsVersion, String>left)
                .getOrElse(Either.right(rawVersion));
    }
}
