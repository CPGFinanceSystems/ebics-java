package de.cpg.oss.ebics.utils;

import de.cpg.oss.ebics.api.EbicsSignatureKey;
import de.cpg.oss.ebics.api.SignatureVersion;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Security;
import java.security.Signature;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CryptoUtilTest {

    private static byte[] AES_KEY;
    private static EbicsSignatureKey RSA_KEY;
    private static byte[] MESSAGE;

    @BeforeClass
    public static void registerBouncyCastleProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeClass
    public static void createTestData() throws Exception {
        AES_KEY = CryptoUtil.generateNonce();
        RSA_KEY = createSignatureKey(SignatureVersion.A006);
        MESSAGE = "som random test message".getBytes();
    }

    @Test
    public void testGenerateNonce() throws Exception {
        assertThat(CryptoUtil.generateNonce()).hasSize(16);
    }

    @Test
    public void testAEScrypto() throws Exception {
        final byte[] encrypted = IOUtil.read(CryptoUtil.encryptAES(IOUtil.wrap(MESSAGE), AES_KEY));
        assertThat(encrypted).isNotEqualTo(MESSAGE);

        final byte[] decrypted = IOUtil.read(CryptoUtil.decryptAES(IOUtil.wrap(encrypted), AES_KEY));
        assertThat(decrypted).isEqualTo(MESSAGE);
    }

    @Test
    public void testRSAcrypto() throws Exception {
        final byte[] encrypted = CryptoUtil.encryptRSA(MESSAGE, RSA_KEY.getPublicKey());
        assertThat(encrypted).isNotEqualTo(MESSAGE);

        final byte[] decrypted = CryptoUtil.decryptRSA(encrypted, RSA_KEY.getPrivateKey());
        assertThat(decrypted).isEqualTo(MESSAGE);
    }

    @Test
    public void testAuthenticate() throws Exception {
        final byte[] signature = CryptoUtil.authenticate(MESSAGE, RSA_KEY.getPrivateKey());
        assertThat(signature).hasSize(KeyUtil.EBICS_KEY_SIZE / 8);
    }

    @Test
    public void testSignMessage() throws Exception {
        for (final SignatureVersion signatureVersion : SignatureVersion.values()) {
            log.info("Test signature Version {}", signatureVersion);
            final EbicsSignatureKey signatureKey = createSignatureKey(signatureVersion);
            assertThat(verifySignature(
                    signatureKey,
                    CryptoUtil.signMessage(IOUtil.wrap(MESSAGE), signatureKey),
                    MESSAGE)).isTrue();
        }
    }

    @Test
    public void testDigest() throws Exception {
        final MessageDigest digester = MessageDigest.getInstance(CryptoUtil.EBICS_DIGEST_ALGORITHM);
        final byte[] message = IOUtil.read(CryptoUtil.digest(IOUtil.wrap(MESSAGE), digester));
        assertThat(message).isEqualTo(MESSAGE);
        assertThat(digester.digest()).hasSize(256 / 8);
    }

    @Test
    public void testSignHash() throws Exception {
        final byte[] signature = CryptoUtil.signHash(MESSAGE, RSA_KEY);
        assertThat(signature).hasSize(KeyUtil.EBICS_KEY_SIZE / 8);
    }

    private static EbicsSignatureKey createSignatureKey(final SignatureVersion version) throws Exception {
        final KeyPair keyPair = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
        return EbicsSignatureKey.builder()
                .privateKey(keyPair.getPrivate())
                .publicKey(keyPair.getPublic())
                .creationTime(Instant.now())
                .digest(KeyUtil.getKeyDigest(keyPair.getPublic()))
                .version(version)
                .build();
    }

    private static boolean verifySignature(final EbicsSignatureKey signatureKey, final byte[] signature, final byte[] message) throws Exception {
        final Signature signer;
        switch (signatureKey.getSignatureVersion()) {
            case A005:
                signer = Signature.getInstance("SHA256withRSA");
                signer.initVerify(signatureKey.getPublicKey());
                signer.update(removeOSSpecificChars(message));
                return signer.verify(signature);

            case A006:
                signer = Signature.getInstance("SHA256withRSAandMGF1", BouncyCastleProvider.PROVIDER_NAME);
                signer.initVerify(signatureKey.getPublicKey());
                final MessageDigest digester = MessageDigest.getInstance(CryptoUtil.EBICS_DIGEST_ALGORITHM);
                signer.update(digester.digest(removeOSSpecificChars(message)));
                return signer.verify(signature);

            default:
                throw new IllegalArgumentException("Unsupported signature version");
        }
    }

    private static byte[] removeOSSpecificChars(final byte[] buf) {
        final ByteArrayOutputStream output;

        output = new ByteArrayOutputStream();
        for (final byte aBuf : buf) {
            if (CryptoUtil.isOsSpecificChar(aBuf)) {
                continue;
            }
            output.write(aBuf);
        }

        return output.toByteArray();
    }
}
