package de.cpg.oss.ebics.api;

import de.cpg.oss.ebics.api.exception.EbicsException;

import java.io.OutputStream;


/**
 * The <code>InitLetter</code> is an abstract initialization
 * letter. The INI, HIA and HPB letters should be an implementation
 * of the <code>InitLetter</code>
 *
 * @author Hachani
 */
public interface InitLetter {

    /**
     * Creates an <code>InitLetter</code> for a given <code>EbicsUser</code>
     *
     * @param session the ebics session.
     */
    OutputStream create(EbicsSession session) throws EbicsException;
}
