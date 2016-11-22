package de.cpg.oss.ebics.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.*;

public abstract class ZipUtil {
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
    public static byte[] compress(final byte[] toZip) {

        if (toZip == null) {
            throw new IllegalArgumentException("The input to be zipped cannot be null");
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
            throw new RuntimeException(e);
        }
        compressor.end();

        return output.toByteArray();
    }

    public static InputStream compress(final InputStream inputStream) {
        return new DeflaterInputStream(inputStream, new Deflater(Deflater.BEST_COMPRESSION));
    }

    public static InputStream uncompress(final InputStream inputStream) {
        return new InflaterInputStream(inputStream, new Inflater());
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
