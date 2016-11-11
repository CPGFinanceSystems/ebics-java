package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.EbicsAuthenticationKey;

@JsonDeserialize(builder = EbicsAuthenticationKey.EbicsAuthenticationKeyBuilder.class)
class EbicsAuthenticationKeyMixin {
}
