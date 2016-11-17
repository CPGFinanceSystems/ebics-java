package de.cpg.oss.ebics.io;

import de.cpg.oss.ebics.api.EbicsUser;
import de.cpg.oss.ebics.api.exception.EbicsException;
import de.cpg.oss.ebics.utils.CryptoUtil;
import de.cpg.oss.ebics.utils.ZipUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple mean to join downloaded segments from the
 * bank ebics server.
 *
 * @author Hachani
 */
public class Joiner {

    /**
     * Constructs a new <code>Joiner</code> object.
     *
     * @param user the ebics user.
     */
    public Joiner(final EbicsUser user) {
        this.user = user;
        buffer = new ByteArrayOutputStream();
    }

    public void append(final byte[] data) throws EbicsException {
        try {
            buffer.write(data);
            buffer.flush();
        } catch (final IOException e) {
            throw new EbicsException(e);
        }
    }

    /**
     * Writes the joined part to an output stream.
     *
     * @param output         the output stream.
     * @param transactionKey the transaction key
     */
    public void writeTo(final OutputStream output, final byte[] transactionKey) {
        try {

            buffer.close();
            output.write(ZipUtil.uncompress(CryptoUtil.decrypt(
                    buffer.toByteArray(),
                    transactionKey,
                    user.getEncryptionKey().getPrivateKey())));
            output.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private final EbicsUser user;
    private final ByteArrayOutputStream buffer;
}
