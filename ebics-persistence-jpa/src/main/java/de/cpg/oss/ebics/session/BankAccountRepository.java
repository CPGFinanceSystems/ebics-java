package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.BankAccountInformation;

public interface BankAccountRepository extends EbicsRepository<BankAccountInformation> {

    @Override
    default Class<BankAccountInformation> getEntityType() {
        return BankAccountInformation.class;
    }
}
