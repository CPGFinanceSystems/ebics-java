package org.kopi.ebics.utils;

import org.kopi.ebics.exception.EbicsException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZipUtil {
    /**
     * Compresses an input of byte array
     * <p>
     * <p>The Decompression is ensured via Universal compression
     * algorithm (RFC 1950, RFC 1951) As specified in the EBICS
     * specification (16 Appendix: Standards and references)
     *
     * @param toZip the input to be compressed
     * @return the compressed input data
     */
    public static byte[] compress(final byte[] toZip) throws EbicsException {

        if (toZip == null) {
            throw new EbicsException("The input to be zipped cannot be null");
        }

        final Deflater compressor;
        final ByteArrayOutputStream output;
        final byte[] buffer;

        output = new ByteArrayOutputStream(toZip.length);
        buffer = new byte[1024];
        compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(toZip);
        compressor.finish();

        while (!compressor.finished()) {
            final int count = compressor.deflate(buffer);
            output.write(buffer, 0, count);
        }

        try {
            output.close();
        } catch (final IOException e) {
            throw new EbicsException(e.getMessage());
        }
        compressor.end();

        return output.toByteArray();
    }

    /**
     * Uncompress a given byte array input.
     * <p>
     * <p>The Decompression is ensured via Universal compression
     * algorithm (RFC 1950, RFC 1951) As specified in the EBICS
     * specification (16 Appendix: Standards and references)
     *
     * @param zip the zipped input.
     * @return the uncompressed data.
     */
    public static byte[] uncompress(final byte[] zip) {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream(zip.length)) {
            final Inflater inflater = new Inflater();
            inflater.setInput(zip);
            final byte[] buf = new byte[1024];

            while (!inflater.finished()) {
                final int count = inflater.inflate(buf);
                output.write(buf, 0, count);
            }

            inflater.end(); //TODO: Check if AutoCloseable is available
            return output.toByteArray();
        } catch (IOException | DataFormatException e) {
            throw new RuntimeException(e);
        }
    }
}
