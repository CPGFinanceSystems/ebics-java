package de.cpg.oss.ebics.api;

import lombok.*;
import lombok.experimental.Wither;

import java.util.List;

@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EbicsPartner implements Identifiable {

    private static final long serialVersionUID = 2L;

    @NonNull
    private final String partnerId;

    private final List<BankAccountInformation> bankAccounts;

    @Override
    public String getId() {
        return getPartnerId();
    }

    // We all love JPA, don't we?
    private EbicsPartner() {
        this("", null);
    }
}
