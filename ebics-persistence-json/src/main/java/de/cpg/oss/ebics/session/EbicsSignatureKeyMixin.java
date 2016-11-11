package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.EbicsSignatureKey;

@JsonDeserialize(builder = EbicsSignatureKey.EbicsSignatureKeyBuilder.class)
class EbicsSignatureKeyMixin {
}
