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

package org.kopi.ebics.interfaces;


import de.cpg.oss.ebics.api.exception.ReturnCode;

/**
 * A mean to log all ebics client operations. The log process
 * ensures four functions:
 * <ol>
 * <li> info: inform the user about an operation</li>
 * <li> warn: warn the user about a risk that may affect the transfer or the key activation process</li>
 * <li> error: report an error to the user with its details and causes</li>
 * <li> report: reports an EBICS return code to the user</li>
 * </ol>
 *
 * @author hachani
 */
public interface EbicsLogger {

    /**
     * Informs a given message to the client application user.
     *
     * @param message the message to be informed.
     */
    void info(String message);

    /**
     * Warns a given message to the client application user.
     *
     * @param message the given message.
     */
    void warn(String message);

    /**
     * Warns a given message and its causes to the client application user.
     *
     * @param message   the given message.
     * @param throwable message causes.
     */
    void warn(String message, Throwable throwable);

    /**
     * Reports an error to the client application user.
     *
     * @param message the error message
     */
    void error(String message);

    /**
     * Reports an error and its causes to the client application user.
     *
     * @param message   the error message.
     * @param throwable the error causes.
     */
    void error(String message, Throwable throwable);

    /**
     * Reports an ebics return code to the client application user.
     *
     * @param returnCode the return code to report.
     */
    void report(ReturnCode returnCode);
}