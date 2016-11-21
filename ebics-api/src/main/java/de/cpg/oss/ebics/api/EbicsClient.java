package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;

public interface EbicsClient {
    EbicsSession loadOrCreateSession(EbicsSessionParameter sessionParameter) throws EbicsException;

    EbicsSession initializeUser(EbicsSession session) throws EbicsException;

    EbicsSession getBankInformation(EbicsSession session) throws EbicsException;

    EbicsSession revokeSubscriber(EbicsSession session) throws EbicsException;

    Collection<VEUOrder> getOrdersForVEU(EbicsSession session) throws EbicsException;

    Collection<DetailedVEUOrder> getDetailedOrdersForVEU(EbicsSession session) throws EbicsException;

    DetailedVEUOrder detailedVEUOrderFor(EbicsSession session, VEUOrder orderDetails) throws EbicsException;

    void signDetailedOrder(EbicsSession session, DetailedVEUOrder detailedVEUOrder) throws EbicsException;

    void uploadSepaDirectDebit(String path, EbicsSession session) throws EbicsException;

    void fetchFile(String path,
                   EbicsSession session,
                   OrderType orderType,
                   boolean isTest,
                   LocalDate start,
                   LocalDate end);

    void save(EbicsSession session) throws IOException;
}
