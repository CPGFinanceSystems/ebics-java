package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.EbicsUser;

@JsonDeserialize(builder = EbicsUser.EbicsUserBuilder.class)
class EbicsUserMixin {
}
