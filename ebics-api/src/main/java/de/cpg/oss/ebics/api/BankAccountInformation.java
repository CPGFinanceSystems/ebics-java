package de.cpg.oss.ebics.api;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class BankAccountInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String accountHolder;
    private final String currency;
    private final String description;
    private final String accountNmber;
    private final String bankCode;
    private final String iban;
    private final String bic;
}
