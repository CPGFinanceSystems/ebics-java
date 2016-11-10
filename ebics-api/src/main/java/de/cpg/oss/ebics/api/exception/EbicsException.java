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

/**
 * Common exception for all EBICS errors.
 *
 * @author hachani
 */
public class EbicsException extends Exception {

    private static final long serialVersionUID = 1L;

    private ReturnCode returnCode;

    public EbicsException(final Throwable cause) {
        super(cause);
    }

    public EbicsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * A means to construct a server error with no additional message.
     *
     * @param returnCode the EBICS return code.
     */
    public EbicsException(final ReturnCode returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * A means to construct a server error with an additional message.
     *
     * @param returnCode the EBICS return code.
     * @param message    the additional message.
     */
    public EbicsException(final ReturnCode returnCode, final String message) {
        super(message);
        this.returnCode = returnCode;
    }

    /**
     * Returns the standardized error code.
     */
    public ReturnCode getReturnCode() {
        return returnCode;
    }
}
