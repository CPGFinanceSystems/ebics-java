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

package de.cpg.oss.ebics.letter;

import de.cpg.oss.ebics.api.EbicsSession;
import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.KeyUtil;


/**
 * The <code>E002Letter</code> is the initialization letter
 * for the encryption util.
 *
 * @author Hachani
 */
public class E002Letter extends AbstractInitLetter {

    /**
     * Constructs a new <code>E002Letter</code>
     */
    public E002Letter(final MessageProvider messageProvider) {
        super(messageProvider);
    }

    @Override
    public void create(final EbicsSession session) throws EbicsException {
        build(session.getHostId(),
                session.getBank().getName(),
                session.getUser().getId(),
                session.getUser().getName(),
                session.getPartner().getId(),
                getString("HIALetter.e002.version"),
                getString("HIALetter.e002.certificate"),
                session.getUser().getE002Key().getPublic(),
                getString("HIALetter.e002.digest"),
                KeyUtil.getKeyDigest(session.getUser().getE002Key().getPublic()));
    }

    @Override
    public String getTitle() {
        return getString("HIALetter.e002.title");
    }

    @Override
    public String getName() {
        return getString("HIALetter.e002.name") + ".txt";
    }
}
