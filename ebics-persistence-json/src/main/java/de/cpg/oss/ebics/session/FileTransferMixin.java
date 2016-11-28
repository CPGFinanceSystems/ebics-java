package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cpg.oss.ebics.api.FileTransfer;

@JsonDeserialize(builder = FileTransfer.FileTransferBuilder.class)
@JsonIgnoreProperties("lastSegment")
class FileTransferMixin {
}
