package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.FileTransfer;

public interface FileTransferRepository extends EbicsRepository<FileTransfer> {

    @Override
    default Class<FileTransfer> getEntityType() {
        return FileTransfer.class;
    }
}
