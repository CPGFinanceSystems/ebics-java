package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.EbicsEncryptionKey;

@JsonDeserialize(builder = EbicsEncryptionKey.EbicsEncryptionKeyBuilder.class)
class EbicsEncryptionKeyMixin {
}
