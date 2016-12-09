package de.cpg.oss.ebics.api.exception;

import de.cpg.oss.ebics.api.MessageProvider;
import lombok.Getter;

/**
 * EBICS exception wrapping a {@link ReturnCode}
 */
public class EbicsException extends Exception {

    private static final long serialVersionUID = 2L;

    @Getter
    private final ReturnCode returnCode;

    /**
     * A means to construct a server error with no additional message.
     *
     * @param returnCode the EBICS return code.
     */
    public EbicsException(final ReturnCode returnCode, final MessageProvider messageProvider) {
        this(returnCode, returnCode.getText(messageProvider));
    }

    /**
     * A means to construct a server error with an additional message.
     *
     * @param returnCode the EBICS return code.
     * @param message    the additional message.
     */
    EbicsException(final ReturnCode returnCode, final String message) {
        super(message);
        this.returnCode = returnCode;
    }
}
