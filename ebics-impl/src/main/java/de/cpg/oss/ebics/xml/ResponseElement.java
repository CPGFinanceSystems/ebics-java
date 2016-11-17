package de.cpg.oss.ebics.xml;

import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.api.exception.ReturnCode;

public interface ResponseElement<T> {

    Class<T> getResponseClass();

    T getResponse();

    ReturnCode getReturnCode();

    byte[] getOrderData();

    byte[] getTransactionKey();

    default void report(final MessageProvider messageProvider) throws EbicsException {
        final ReturnCode returnCode = getReturnCode();
        if (!returnCode.isOk()) {
            returnCode.throwException(messageProvider);
        }
    }
}
