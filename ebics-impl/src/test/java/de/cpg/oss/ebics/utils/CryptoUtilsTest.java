package de.cpg.oss.ebics.utils;

import de.cpg.oss.ebics.api.EbicsSignatureKey;
import de.cpg.oss.ebics.api.SignatureVersion;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Security;
import java.security.Signature;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CryptoUtilsTest {

    @BeforeClass
    public static void registerBouncyCastleProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testSignMessage() throws Exception {
        final byte[] message = "This is some test message".getBytes();

        for (final SignatureVersion signatureVersion : SignatureVersion.values()) {
            log.info("Test signature Version {}", signatureVersion);
            final EbicsSignatureKey signatureKey = createSignatureKey(signatureVersion);
            assertThat(verifySignature(signatureKey, CryptoUtil.signMessage(message, signatureKey), message)).isTrue();
        }
    }

    private static EbicsSignatureKey createSignatureKey(final SignatureVersion version) throws Exception {
        final KeyPair keyPair = KeyUtil.createRsaKeyPair(KeyUtil.EBICS_KEY_SIZE);
        return EbicsSignatureKey.builder()
                .privateKey(keyPair.getPrivate())
                .publicKey(keyPair.getPublic())
                .creationTime(OffsetDateTime.now())
                .digest(KeyUtil.getKeyDigest(keyPair.getPublic()))
                .version(version)
                .build();
    }

    private static boolean verifySignature(final EbicsSignatureKey signatureKey, final byte[] signature, final byte[] message) throws Exception {
        final Signature signer;
        switch (signatureKey.getVersion()) {
            case A005:
                signer = Signature.getInstance("SHA256withRSA");
                signer.initVerify(signatureKey.getPublicKey());
                signer.update(CryptoUtil.removeOSSpecificChars(message));
                return signer.verify(signature);

            case A006:
                signer = Signature.getInstance("SHA256withRSAandMGF1", BouncyCastleProvider.PROVIDER_NAME);
                signer.initVerify(signatureKey.getPublicKey());
                final MessageDigest digester = MessageDigest.getInstance("SHA-256");
                signer.update(digester.digest(CryptoUtil.removeOSSpecificChars(message)));
                return signer.verify(signature);

            default:
                throw new IllegalArgumentException("Unsupported signature version");
        }
    }
}
