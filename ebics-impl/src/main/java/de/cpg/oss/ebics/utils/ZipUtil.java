package de.cpg.oss.ebics.utils;

import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

public abstract class ZipUtil {
    /**
     * Compresses an input of byte array
     * <p>
     * <p>The Decompression is ensured via Universal compression
     * algorithm (RFC 1950, RFC 1951) As specified in the EBICS
     * specification (16 Appendix: Standards and references)
     *
     * @param inputStream the input to be compressed
     * @return the compressed input data
     */
    public static InputStream compress(final InputStream inputStream) {
        return new DeflaterInputStream(inputStream, new Deflater(Deflater.BEST_COMPRESSION));
    }

    /**
     * Uncompress a given byte array input.
     * <p>
     * <p>The Decompression is ensured via Universal compression
     * algorithm (RFC 1950, RFC 1951) As specified in the EBICS
     * specification (16 Appendix: Standards and references)
     *
     * @param inputStream the zipped input.
     * @return the uncompressed data.
     */
    public static InputStream uncompress(final InputStream inputStream) {
        return new InflaterInputStream(inputStream);
    }
}
