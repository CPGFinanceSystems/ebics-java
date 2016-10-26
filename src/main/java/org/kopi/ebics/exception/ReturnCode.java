/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package org.kopi.ebics.exception;

import org.kopi.ebics.messages.Messages;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * Representation of EBICS return codes.
 * The return codes are described in chapter 13
 * of EBICS specification.
 *
 * @author hachani
 */
public enum ReturnCode {

    EBICS_OK("000000"),
    EBICS_DOWNLOAD_POSTPROCESS_DONE("011000"),
    EBICS_DOWNLOAD_POSTPROCESS_SKIPPED("011001"),
    EBICS_TX_SEGMENT_NUMBER_UNDERRUN("011101"),
    EBICS_AUTHENTICATION_FAILED("061001"),
    EBICS_INVALID_REQUEST("061002"),
    EBICS_INTERNAL_ERROR("061099"),
    EBICS_TX_RECOVERY_SYNC("061101"),
    EBICS_INVALID_USER_OR_USER_STATE("091002"),
    EBICS_USER_UNKNOWN("091003"),
    EBICS_INVALID_USER_STATE("091004"),
    EBICS_INVALID_ORDER_TYPE("091005"),
    EBICS_UNSUPPORTED_ORDER_TYPE("091006"),
    EBICS_USER_AUTHENTICATION_REQUIRED("091007"),
    EBICS_BANK_PUBKEY_UPDATE_REQUIRED("091008"),
    EBICS_SEGMENT_SIZE_EXCEEDED("091009"),
    EBICS_TX_UNKNOWN_TXID("091101"),
    EBICS_TX_ABORT("091102"),
    EBICS_TX_MESSAGE_REPLAY("091103"),
    EBICS_TX_SEGMENT_NUMBER_EXCEEDED("091104"),
    EBICS_X509_CERTIFICATE_NOT_VALID_YET("091209"),
    EBICS_MAX_TRANSACTIONS_EXCEEDED("091119"),
    EBICS_SIGNATURE_VERIFICATION_FAILED("091301"),
    EBICS_NO_DOWNLOAD_DATA_AVAILABLE("090005"),
    EBICS_OTHER("100000");

    private static final String BUNDLE_NAME = "org.kopi.ebics.exception.messages";
    private static final long serialVersionUID = 1L;

    private final String code;
    private String text;

    /**
     * Constructs a new <code>ReturnCode</code> with a given
     * standard code, symbolic name and text
     *
     * @param code the given standard code.
     */
    ReturnCode(final String code) {
        this.code = code;
    }

    /**
     * Throws an equivalent <code>EbicsException</code>
     *
     * @throws EbicsException
     */
    public void throwException() throws EbicsException {
        throw new EbicsException(this, getText());
    }

    /**
     * Tells if the return code is an OK one.
     *
     * @return True if the return code is OK one.
     */
    public boolean isOk() {
        return equals(EBICS_OK);
    }

    /**
     * Returns a slightly more human readable version of this return code.
     *
     * @return a slightly more human readable version of this return code.
     */
    public String getSymbolicName() {
        return name();
    }

    /**
     * Returns a display text for the default locale.
     *
     * @return a text that can be displayed.
     */
    public String getText() {
        return Optional.ofNullable(text).orElseGet(() -> Messages.getString(code, BUNDLE_NAME));
    }

    /**
     * Returns the code.
     *
     * @return the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the equivalent <code>ReturnCode</code> of a given code
     *
     * @param code the given code
     * @param text the given code text
     * @return the equivalent <code>ReturnCode</code>
     */
    public static ReturnCode toReturnCode(final String code, final String text) {
        return Stream.of(values()).filter(v -> v.getCode().equals(code)).findFirst()
                .orElseGet(() -> {
                    final ReturnCode other = EBICS_OTHER;
                    other.text = text;
                    return other;
                });
    }
}
