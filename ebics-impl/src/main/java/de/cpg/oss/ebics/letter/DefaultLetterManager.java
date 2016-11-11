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
import de.cpg.oss.ebics.api.InitLetter;
import de.cpg.oss.ebics.api.LetterManager;
import de.cpg.oss.ebics.api.MessageProvider;
import de.cpg.oss.ebics.api.exception.EbicsException;


/**
 * The <code>DefaultLetterManager</code> is a simple way
 * to manage initialization letters.
 *
 * @author Hachani
 */
public class DefaultLetterManager implements LetterManager {

    private final MessageProvider messageProvider;

    /**
     * Constructs a new <code>LetterManager</code>
     */
    public DefaultLetterManager(final MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public InitLetter createA005Letter(final EbicsSession session) throws EbicsException {
        final A005Letter letter;

        letter = new A005Letter(messageProvider);
        letter.create(session);
        return letter;
    }

    @Override
    public InitLetter createE002Letter(final EbicsSession session) throws EbicsException {
        final E002Letter letter;

        letter = new E002Letter(messageProvider);
        letter.create(session);
        return letter;
    }

    @Override
    public InitLetter createX002Letter(final EbicsSession session) throws EbicsException {
        final X002Letter letter;

        letter = new X002Letter(messageProvider);
        letter.create(session);
        return letter;
    }
}
