package de.cpg.oss.ebics.api;

import lombok.Getter;

import java.util.stream.Stream;

public enum UserStatus {
    READY(1),
    NEW(2),
    PARTLY_INITIALIZED_INI(3),
    PARTLY_INITIALIZED_HIA(4),
    INITIALIZED(5),
    SUSPENDED_BY_SYSTEM(6),
    NEW_FTAM(7),
    SUSPENDED_BY_CUSTOMER(8),
    SUSPENDED_BY_BANK(9);

    @Getter
    private final int ebicsStatus;

    UserStatus(final int ebicsStatus) {
        this.ebicsStatus = ebicsStatus;
    }

    public static UserStatus fromEbicsStatus(final int value) {
        return Stream.of(values()).filter(status -> status.getEbicsStatus() == value).findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
