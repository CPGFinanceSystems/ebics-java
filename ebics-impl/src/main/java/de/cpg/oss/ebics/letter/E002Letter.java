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

import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.KeyUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Locale;


/**
 * The <code>E002Letter</code> is the initialization letter
 * for the encryption util.
 *
 * @author Hachani
 */
public class E002Letter extends AbstractInitLetter {

    /**
     * Constructs a new <code>E002Letter</code>
     *
     * @param locale the application locale
     */
    public E002Letter(final MessageProvider messageProvider, final Locale locale) {
        super(messageProvider, locale);
    }

    @Override
    public void create(final EbicsUser user) throws GeneralSecurityException, IOException, EbicsException {
        build(user.getPartner().getBank().getHostId(),
                user.getPartner().getBank().getName(),
                user.getUserId(),
                user.getName(),
                user.getPartner().getPartnerId(),
                getString("HIALetter.e002.version"),
                getString("HIALetter.e002.certificate"),
                user.getE002Key().getPublic(),
                getString("HIALetter.e002.digest"),
                KeyUtil.getKeyDigest(user.getE002Key().getPublic()));
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
