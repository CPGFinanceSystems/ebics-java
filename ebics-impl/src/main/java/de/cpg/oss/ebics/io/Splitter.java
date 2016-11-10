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

package de.cpg.oss.ebics.io;

import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;

import javax.crypto.spec.SecretKeySpec;


/**
 * A mean to split a given input file to
 * 1MB portions. this i useful to handle
 * big file uploading.
 *
 * @author Hachani
 */
public class Splitter {

    /**
     * Constructs a new <code>FileSplitter</code> with a given file.
     *
     * @param input the input byte array
     */
    public Splitter(final byte[] input) {
        this.input = input;
    }

    /**
     * Reads the input stream and splits it to segments of 1MB size.
     * <p>
     * <p>EBICS Specification 2.4.2 - 7 Segmentation of the order data:
     * <p>
     * <p>The following procedure is to be followed with segmentation:
     * <ol>
     * <li> The order data is ZIP compressed
     * <li> The compressed order data is encrypted in accordance with Chapter 6.2
     * <li> The compressed, encrypted order data is base64-coded.
     * <li> The result is to be verified with regard to the data volume:
     * <ol>
     * <li> If the resulting data volume is below the threshold of 1 MB = 1,048,576 bytes,
     * the order data can be sent complete as a data segment within one transmission step
     * <li> If the resulting data volume exceeds 1,048,576 bytes the data is to be
     * separated sequentially and in a base64-conformant manner into segments
     * that each have a maximum of 1,048,576 bytes.
     * </ol>
     *
     * @param isCompressionEnabled enable compression?
     * @param keySpec              the secret key spec
     */
    public final void readInput(final boolean isCompressionEnabled, final SecretKeySpec keySpec)
            throws EbicsException {
        try {
            if (isCompressionEnabled) {
                input = ZipUtil.compress(input);
            }
            content = CryptoUtil.encrypt(input, keySpec);
            segmentation();
        } catch (final Exception e) {
            throw new EbicsException(e);
        }
    }

    /**
     * Slits the input into 1MB portions.
     * <p>
     * <p> EBICS Specification 2.4.2 - 7 Segmentation of the order data:
     * <p>
     * <p>In Version H003 of the EBICS standard, order data that requires more than 1 MB of storage
     * space in compressed, encrypted and base64-coded form MUST be segmented before
     * transmission, irrespective of the transfer direction (upload/download).
     */
    private void segmentation() {

        numSegments = content.length / 1048576; //(1024 * 1024)

        if (content.length % 1048576 != 0) {
            numSegments++;
        }

        segmentSize = content.length / numSegments;
    }

    /**
     * Returns the content of a data segment according to
     * a given segment number.
     *
     * @param segmentNumber the segment number
     */
    public ContentFactory getContent(final int segmentNumber) {
        final byte[] segment;
        final int offset;

        offset = segmentSize * (segmentNumber - 1);
        if (content.length < segmentSize + offset) {
            segment = new byte[content.length - offset];
        } else {
            segment = new byte[segmentSize];
        }

        System.arraycopy(content, offset, segment, 0, segment.length);
        return new ByteArrayContentFactory(segment);
    }

    /**
     * Returns the hole content.
     *
     * @return the input content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns the total segment number.
     *
     * @return the total segment number.
     */
    public int getNumSegments() {
        return numSegments;
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private byte[] input;
    private byte[] content;
    private int segmentSize;
    private int numSegments;
}
