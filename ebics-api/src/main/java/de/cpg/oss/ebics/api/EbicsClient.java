package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.IOException;
import java.time.LocalDate;

public interface EbicsClient {
    void init();

    EbicsSession loadOrCreateSession(EbicsSessionParameter sessionParameter) throws EbicsException;

    EbicsUser initializeUser(EbicsSession session) throws EbicsException;

    EbicsBank getBankInformation(EbicsSession session) throws EbicsException;

    EbicsUser revokeSubscriber(EbicsSession session) throws EbicsException;

    void uploadSepaDirectDebit(String path, EbicsSession session) throws EbicsException;

    void fetchFile(String path,
                   EbicsSession session,
                   OrderType orderType,
                   boolean isTest,
                   LocalDate start,
                   LocalDate end);

    void quit(EbicsSession session) throws IOException;
}
