package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Collection;

public interface EbicsClient {

    EbicsSession loadOrCreateSession(EbicsSessionParameter sessionParameter) throws EbicsException;

    EbicsSession initializeUser(EbicsSession session) throws EbicsException;

    void generateIniLetter(EbicsSession session, OutputStream pdfOutput) throws EbicsException;

    void generateHiaLetter(EbicsSession session, OutputStream pdfOutput) throws EbicsException;

    EbicsSession collectInformation(EbicsSession session) throws EbicsException;

    EbicsSession revokeSubscriber(EbicsSession session) throws EbicsException;

    Collection<VEUOrder> getOrdersForVEU(EbicsSession session) throws EbicsException;

    Collection<DetailedVEUOrder> getDetailedOrdersForVEU(EbicsSession session) throws EbicsException;

    DetailedVEUOrder detailedVEUOrderFor(EbicsSession session, VEUOrder orderDetails) throws EbicsException;

    void signDetailedOrder(EbicsSession session, DetailedVEUOrder detailedVEUOrder) throws EbicsException;

    void cancelSignature(final EbicsSession session, final DetailedVEUOrder detailedVEUOrder) throws EbicsException;

    FileTransfer createFileUploadTransaction(final EbicsSession session,
                                             final File fileLocation,
                                             final OrderType orderType) throws FileNotFoundException, EbicsException;

    FileTransfer uploadFile(final EbicsSession session, final FileTransfer fileTransfer) throws EbicsException;

    void fetchFile(String path,
                   EbicsSession session,
                   OrderType orderType,
                   boolean isTest,
                   LocalDate start,
                   LocalDate end);

    EbicsSession save(EbicsSession session) throws IOException;
}
