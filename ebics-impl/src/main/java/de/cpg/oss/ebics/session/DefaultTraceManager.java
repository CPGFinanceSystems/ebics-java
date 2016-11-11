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

package de.cpg.oss.ebics.session;

import de.cpg.oss.ebics.api.EbicsConfiguration;
import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.TraceManager;
import de.cpg.oss.ebics.xml.XmlUtils;
import lombok.extern.slf4j.Slf4j;

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

    private final EbicsConfiguration ebicsConfiguration;

    public DefaultTraceManager(final EbicsConfiguration configuration) {
        this.ebicsConfiguration = configuration;
    }

    @Override
    public void trace(final byte[] xml, final String elementName, final EbicsUser user) {
        final File file = new File(
                ebicsConfiguration.getTransferTraceDirectory(user),
                MessageFormat.format(
                        "{0}_{1}.xml",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        elementName));
        try (final FileOutputStream out = new FileOutputStream(file)) {
            out.write(xml);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void trace(final Class<T> clazz, final T object, final EbicsUser user) {
        trace(XmlUtils.prettyPrint(clazz, object), XmlUtils.elementNameFrom(clazz), user);
    }
}
