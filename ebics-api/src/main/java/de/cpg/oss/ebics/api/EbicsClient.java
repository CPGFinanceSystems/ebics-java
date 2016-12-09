package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;

public interface EbicsClient {

    Collection<String> bankSupportedEbicsVersions(final String hostId, final URI endpoint) throws EbicsException;

    EbicsSession loadOrCreateSession(EbicsSessionParameter sessionParameter);

    EbicsSession initializeUser(EbicsSession session) throws EbicsException;

    void generateIniLetter(EbicsSession session, OutputStream pdfOutput);

    void generateHiaLetter(EbicsSession session, OutputStream pdfOutput);

    EbicsSession collectInformation(EbicsSession session) throws EbicsException;

    EbicsSession revokeSubscriber(EbicsSession session) throws EbicsException;

    Collection<VEUOrder> getOrdersForVEU(EbicsSession session) throws EbicsException;

    Collection<DetailedVEUOrder> getDetailedOrdersForVEU(EbicsSession session) throws EbicsException;

    DetailedVEUOrder detailedVEUOrderFor(EbicsSession session, VEUOrder orderDetails) throws EbicsException;

    void signDetailedOrder(EbicsSession session, DetailedVEUOrder detailedVEUOrder) throws EbicsException;

    void cancelSignature(final EbicsSession session, final DetailedVEUOrder detailedVEUOrder) throws EbicsException;

    FileTransfer createFileUploadTransaction(final EbicsSession session,
                                             final File fileLocation,
                                             final OrderType orderType) throws EbicsException;

    FileTransfer uploadFile(final EbicsSession session, final FileTransfer fileTransfer) throws EbicsException;

    void fetchFile(String path,
                   EbicsSession session,
                   OrderType orderType,
                   boolean isTest,
                   LocalDate start,
                   LocalDate end) throws EbicsException;

    EbicsSession save(EbicsSession session);
}
