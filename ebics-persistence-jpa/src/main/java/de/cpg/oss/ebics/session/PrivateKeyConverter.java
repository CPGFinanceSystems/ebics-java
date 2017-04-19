package de.cpg.oss.ebics.session;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Optional;

@Slf4j
@Converter(autoApply = true)
public class PrivateKeyConverter implements AttributeConverter<PrivateKey, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(final PrivateKey privateKey) {
        final byte[] result = Optional.ofNullable(privateKey).map(PrivateKey::getEncoded).orElse(null);
        log.trace("Serialized {} to {}", privateKey, result);
        return result;
    }

    @Override
    public PrivateKey convertToEntityAttribute(final byte[] bytes) {
        final PrivateKey result = Optional.ofNullable(bytes).map(b -> {
            try {
                return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);

        log.trace("Deserialized {} from {}", result, bytes);
        return result;
    }
}
