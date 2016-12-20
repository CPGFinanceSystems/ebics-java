package de.cpg.oss.ebics.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BankAccountInformation implements Identifiable {

    private static final long serialVersionUID = 2L;

    private final String id;
    private final String accountHolder;
    private final String currency;
    private final String description;
    private final String accountNumber;
    private final String bankCode;
    private final String iban;
    private final String bic;

    // We all love JPA, don't we?
    private BankAccountInformation() {
        this(null, null, null, null, null, null, null, null);
    }
}
