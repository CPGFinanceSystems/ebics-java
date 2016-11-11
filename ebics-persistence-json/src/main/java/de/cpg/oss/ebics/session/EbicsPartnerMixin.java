package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.EbicsPartner;

@JsonDeserialize(builder = EbicsPartner.EbicsPartnerBuilder.class)
class EbicsPartnerMixin {
}
