package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;

public interface EbicsClient {
    void init();

    EbicsSession createSession(URI uri,
                               String bankName,
                               String hostId,
                               String partnerId,
                               String userId,
                               String userName,
                               PasswordCallback passwordCallback) throws EbicsException;

    EbicsSession loadSession(String hostId,
                             String partnerId,
                             String userId,
                             PasswordCallback passwordCallback) throws EbicsException;

    EbicsUser sendINIRequest(EbicsSession session) throws EbicsException;

    EbicsUser sendHIARequest(EbicsSession session) throws EbicsException;

    EbicsBank sendHPBRequest(EbicsSession session) throws EbicsException;

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
