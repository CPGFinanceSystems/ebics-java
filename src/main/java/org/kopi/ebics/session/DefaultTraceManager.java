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
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.interfaces.EbicsRootElement;
import org.kopi.ebics.interfaces.TraceManager;
import org.kopi.ebics.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * The <code>DefaultTraceManager</code> aims to trace an ebics
 * transferable element in an instance of <code>java.io.File</code>
 * then saved to a trace directory.
 * The manager can delete all traces file if the configuration does
 * not offer tracing support.
 *
 * @author hachani
 */
@Slf4j
public class DefaultTraceManager implements TraceManager {

    @Override
    public void trace(final EbicsRootElement element) throws EbicsException {
        log.trace("\n{}", element.toString());
        if (null != traceDir) {
            try {
                final FileOutputStream out;
                final File file;

                file = IOUtils.createFile(traceDir, MessageFormat.format("{0}_{1}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), element.getName()));
                out = new FileOutputStream(file);
                element.save(out);
            } catch (final IOException e) {
                throw new EbicsException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void trace(final byte[] xml, final String elementName) {
        log.trace("\n{}", new String(xml));
        if (null != traceDir) {
            try {
                final FileOutputStream out;
                final File file;

                file = IOUtils.createFile(traceDir, MessageFormat.format("{0}_{1}", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), elementName));
                out = new FileOutputStream(file);
                out.write(xml);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setTraceDirectory(final String traceDir) {
        this.traceDir = IOUtils.createDirectory(traceDir);
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private File traceDir;
}
