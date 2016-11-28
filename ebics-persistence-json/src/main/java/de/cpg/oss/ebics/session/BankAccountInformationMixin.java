package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.BankAccountInformation;

@JsonDeserialize(builder = BankAccountInformation.BankAccountInformationBuilder.class)
class BankAccountInformationMixin {
}
