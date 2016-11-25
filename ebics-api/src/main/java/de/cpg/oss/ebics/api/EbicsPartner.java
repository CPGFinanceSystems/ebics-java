package de.cpg.oss.ebics.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Collection;

@Value
@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EbicsPartner implements Identifiable {

    private static final long serialVersionUID = 2L;

    private final String partnerId;

    private final Collection<BankAccountInformation> bankAccounts;

    @Override
    public String getId() {
        return getPartnerId();
    }

    // We all love JPA, don't we?
    private EbicsPartner() {
        this(null, null);
    }
}
