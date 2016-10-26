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

package org.kopi.ebics.session;

import lombok.extern.slf4j.Slf4j;
import org.kopi.ebics.exception.ReturnCode;
import org.kopi.ebics.interfaces.EbicsLogger;


/**
 * A simple EBICS transfers logger base on log4j framework.
 *
 * @author hacheni
 */
@Slf4j
public class DefaultEbicsLogger implements EbicsLogger {

    @Override
    public void info(final String message) {
        log.info(message);
    }

    @Override
    public void warn(final String message) {
        log.warn(message);
    }

    @Override
    public void warn(final String message, final Throwable throwable) {
        log.warn(message, throwable);
    }

    @Override
    public void error(final String message) {
        log.error(message);
    }

    @Override
    public void error(final String message, final Throwable throwable) {
        log.error(message, throwable);
    }

    @Override
    public void report(final ReturnCode returnCode) {
        if (returnCode.isOk()) {
            info(returnCode.getText());
        } else {
            error(returnCode.getText());
        }
    }
}
