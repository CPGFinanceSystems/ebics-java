package de.cpg.oss.ebics.session;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@Slf4j
@Converter(autoApply = true)
public class PublicKeyConverter implements AttributeConverter<PublicKey, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(final PublicKey publicKey) {
        final byte[] result = Optional.ofNullable(publicKey).map(PublicKey::getEncoded).orElse(null);
        log.trace("Serialized {} to {}", publicKey, result);
        return result;
    }

    @Override
    public PublicKey convertToEntityAttribute(final byte[] bytes) {
        final PublicKey result = Optional.ofNullable(bytes).map(b -> {
            try {
                return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);

        log.trace("Deserialized {} from {}", result, bytes);
        return result;
    }
}
