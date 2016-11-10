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

package de.cpg.oss.ebics.api.exception;

import de.cpg.oss.ebics.api.Messages;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Representation of EBICS return codes.
 * The return codes are described in chapter 13
 * of EBICS specification.
 *
 * @author hachani
 */
@Slf4j
public enum ReturnCode {

    // technical return codes
    EBICS_OK("000000"),
    EBICS_DOWNLOAD_POSTPROCESS_DONE("011000"),
    EBICS_DOWNLOAD_POSTPROCESS_SKIPPED("011001"),
    EBICS_TX_SEGMENT_NUMBER_UNDERRUN("011101"),
    EBICS_ORDER_PARAMS_IGNORED("031001"),
    EBICS_AUTHENTICATION_FAILED("061001"),
    EBICS_INVALID_REQUEST("061002"),
    EBICS_INTERNAL_ERROR("061099"),
    EBICS_TX_RECOVERY_SYNC("061101"),
    EBICS_INVALID_USER_OR_USER_STATE("091002"),
    EBICS_USER_UNKNOWN("091003"),
    EBICS_INVALID_USER_STATE("091004"),
    EBICS_INVALID_ORDER_TYPE("091005"),
    EBICS_UNSUPPORTED_ORDER_TYPE("091006"),
    EBICS_DISTRIBUTED_SIGNATURE_AUTHORISATION_FAILED("091007"),
    EBICS_BANK_PUBKEY_UPDATE_REQUIRED("091008"),
    EBICS_SEGMENT_SIZE_EXCEEDED("091009"),
    EBICS_INVALID_XML("091010"),
    EBICS_INVALID_HOST_ID("091011"),
    EBICS_TX_UNKNOWN_TXID("091101"),
    EBICS_TX_ABORT("091102"),
    EBICS_TX_MESSAGE_REPLAY("091103"),
    EBICS_TX_SEGMENT_NUMBER_EXCEEDED("091104"),
    EBICS_INVALID_ORDER_PARAMS("091112"),
    EBICS_INVALID_REQUEST_CONTENT("091113"),
    EBICS_MAX_ORDER_DATA_SIZE_EXCEEDED("091117"),
    EBICS_MAX_SEGMENTS_EXCEEDED("091118"),
    EBICS_MAX_TRANSACTIONS_EXCEEDED("091119"),
    EBICS_PARTNER_ID_MISMATCH("091120"),
    EBICS_INCOMPATIBLE_ORDER_ATTRIBUTE("091121"),

    // processing return codes
    EBICS_NO_ONLINE_CHECKS("011301"),
    EBICS_DOWNLOAD_SIGNED_ONLY("091001"),
    EBICS_DOWNLOAD_UNSIGNED_ONLY("091002"),
    EBICS_AUTHORISATION_ORDER_TYPE_FAILED("090003"),
    EBICS_INVALID_ORDER_DATA_FORMAT("090004"),
    EBICS_NO_DOWNLOAD_DATA_AVAILABLE("090005"),
    EBICS_UNSUPPORTED_REQUEST_FOR_ORDER_INSTANCE("090006"),
    EBICS_RECOVERY_NOT_SUPPORTED("091105"),
    EBICS_INVALID_SIGNATURE_FILE_FORMAT("091111"),
    EBICS_ORDERID_UNKNOWN("091114"),
    EBICS_ORDERID_ALREADY_EXISTS("091115"),
    EBICS_PROCESSING_ERROR("091116"),
    EBICS_KEYMGMT_UNSUPPORTED_VERSION_SIGNATURE("091201"),
    EBICS_KEYMGMT_UNSUPPORTED_VERSION_AUTHENTICATION("091202"),
    EBICS_KEYMGMT_UNSUPPORTED_VERSION_ENCRYPTION("091203"),
    EBICS_KEYMGMT_KEYLENGTH_ERROR_SIGNATURE("091204"),
    EBICS_KEYMGMT_KEYLENGTH_ERROR_AUTHENTICATION("091205"),
    EBICS_KEYMGMT_KEYLENGTH_ERROR_ENCRYPTION("091206"),
    EBICS_KEYMGMT_NO_X509_SUPPORT("091207"),
    EBICS_X509_CERTIFICATE_EXPIRED("091208"),
    EBICS_X509_CERTIFICATE_NOT_VALID_YET("091209"),
    EBICS_X509_WRONG_KEY_USAGE("091210"),
    EBICS_X509_WRONG_ALGORITHM("091211"),
    EBICS_X509_INVALID_THUMBPRINT("091212"),
    EBICS_X509_CTL_INVALID("091213"),
    EBICS_X509_UNKNOWN_CERTIFICATE_AUTHORITY("091214"),
    EBICS_X509_INVALID_POLICY("091215"),
    EBICS_X509_INVALID_BASIC_CONSTRAINTS("091216"),
    EBICS_ONLY_X509_SUPPORT("091217"),
    EBICS_KEYMGMT_DUPLICATE_KEY("091218"),
    EBICS_CERTIFICATES_VALIDATION_ERROR("091219"),
    EBICS_SIGNATURE_VERIFICATION_FAILED("091301"),
    EBICS_ACCOUNT_AUTHORISATION_FAILED("091302"),
    EBICS_AMOUNT_CHECK_FAILED("091303"),
    EBICS_SIGNER_UNKNOWN("091304"),
    EBICS_INVALID_SIGNER_STATE("091305"),
    EBICS_DUPLICATE_SIGNATURE("091306");

    private static final String BUNDLE_NAME = "de.cpg.oss.ebics.exception.messages";
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
        assert code.length() == 6;
        this.code = code;
    }

    /**
     * Throws an equivalent <code>EbicsException</code>
     *
     * @throws EbicsException
     */
    public void throwException() throws EbicsException {
        throw new EbicsException(this, MessageFormat.format("{0} [{1}]: {2}", getCode(), getSymbolicName(), getText()));
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
        final ReturnCode returnCode = Stream.of(values()).filter(v -> v.getCode().equals(code)).findFirst()
                .orElseGet(() -> EBICS_INVALID_REQUEST_CONTENT);

        returnCode.text = text;
        if (!EBICS_OK.equals(returnCode)) {
            log.warn("Got return code {} with text '{}'", returnCode, text);
        }
        return returnCode;
    }
}
