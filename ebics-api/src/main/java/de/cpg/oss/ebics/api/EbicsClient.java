package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;

public interface EbicsClient {
    void init();

    EbicsUser createUser(URI uri,
                         String bankName,
                         String hostId,
                         String partnerId,
                         String userId,
                         String userName,
                         PasswordCallback passwordCallback) throws EbicsException;

    EbicsUser loadUser(String hostId,
                       String partnerId,
                       String userId,
                       PasswordCallback passwordCallback) throws EbicsException;

    EbicsUser sendINIRequest(EbicsUser user, Product product) throws EbicsException;

    EbicsUser sendHIARequest(EbicsUser user, Product product) throws EbicsException;

    EbicsUser sendHPBRequest(EbicsUser user, Product product) throws EbicsException;

    EbicsUser revokeSubscriber(EbicsUser user, Product product) throws EbicsException;

    void uploadSepaDirectDebit(String path, EbicsUser user, Product product) throws EbicsException;

    void fetchFile(String path,
                   EbicsUser user,
                   Product product,
                   OrderType orderType,
                   boolean isTest,
                   LocalDate start,
                   LocalDate end);

    void quit(EbicsUser user) throws IOException;
}
