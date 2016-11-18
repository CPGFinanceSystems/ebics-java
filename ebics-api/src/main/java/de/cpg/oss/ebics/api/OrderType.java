package de.cpg.oss.ebics.api;

import javaslang.collection.Stream;
import javaslang.control.Either;
import lombok.Getter;

public enum OrderType {
    INI(Transmission.UPLOAD, "Send password initialisation", Presence.MANDATORY),
    HIA(Transmission.UPLOAD, "Transmission of the subscriber key for identification and authentication and encryption within the framework of subscriber initialisation", Presence.MANDATORY),
    HPB(Transmission.DOWNLOAD, "Transfer the public bank key", Presence.MANDATORY),
    HPD(Transmission.DOWNLOAD, "Return bank parameters", Presence.MANDATORY),
    HSA(Transmission.UPLOAD, "Transmission of the subscriber key for identification and authentication and encryption within the framework of subscriber initialisation for subscribers that have remote access data transmission via FTAM"),
    HAA(Transmission.DOWNLOAD, "Download retrievable order types"),
    HKD(Transmission.DOWNLOAD, "Download customer’s customer and subscriber data"),
    HTD(Transmission.DOWNLOAD, "Download subscriber’s customer and subscriber data"),
    HAC(Transmission.DOWNLOAD, "Download customer acknowledgement (XML-format)", Presence.MANDATORY),
    HCA(Transmission.UPLOAD, "Send amendment of the subscriber key for identification and authentication and encryption", Presence.MANDATORY),
    HCS(Transmission.UPLOAD, "Transmission of the subscriber key for ES, identification and authentication and encryption", Presence.MANDATORY),
    HEV(Transmission.DOWNLOAD, "Download supported EBICS versions", Presence.MANDATORY),
    FUL(Transmission.UPLOAD, "Upload file with any format"),
    FDL(Transmission.DOWNLOAD, "Download file with any format"),
    HVU(Transmission.DOWNLOAD, "Download VEU overview", Presence.CONDITIONAL),
    HVZ(Transmission.DOWNLOAD, "Download VEU overview with additional information", Presence.CONDITIONAL),
    HVD(Transmission.DOWNLOAD, "Retrieve VEU state", Presence.CONDITIONAL),
    HVT(Transmission.DOWNLOAD, "Retrieve VEU transaction details", Presence.CONDITIONAL),
    HVE(Transmission.UPLOAD, "Add VEU signature", Presence.CONDITIONAL),
    HVS(Transmission.UPLOAD, "VEU cancellation", Presence.CONDITIONAL),
    SPR(Transmission.UPLOAD, "Suspension of access authorisation", Presence.MANDATORY),

    CDD(Transmission.UPLOAD, "Upload direct debit initiation (SEPA core direct debit)");

    OrderType(final Transmission transmission, final String description, final Presence presence) {
        this.transmission = transmission;
        this.description = description;
        this.presence = presence;
    }

    OrderType(final Transmission transmission, final String description) {
        this(transmission, description, Presence.OPTIONAL);
    }

    public enum Transmission {
        DOWNLOAD,
        UPLOAD
    }

    public boolean isMandatory(final boolean germanBank) {
        return Presence.MANDATORY.equals(presence) || (Presence.CONDITIONAL.equals(presence) && germanBank);
    }

    public static Either<OrderType, String> ofRaw(final String rawValue) {
        return Stream.of(values()).find(orderType -> orderType.name().equals(rawValue))
                .map(Either::<OrderType, String>left)
                .getOrElse(Either.right(rawValue));
    }

    @Getter
    private final Transmission transmission;
    @Getter
    private final String description;
    private final Presence presence;

    private enum Presence {
        MANDATORY,
        OPTIONAL,
        CONDITIONAL
    }
}
