package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.EbicsBank;

@JsonDeserialize(builder = EbicsBank.EbicsBankBuilder.class)
class EbicsBankMixin {
}
