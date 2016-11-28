package de.cpg.oss.ebics.session;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Optional;

@Converter(autoApply = true)
public class PrivateKeyConverter implements AttributeConverter<PrivateKey, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(final PrivateKey privateKey) {
        return Optional.ofNullable(privateKey).map(PrivateKey::getEncoded).orElse(null);
    }

    @Override
    public PrivateKey convertToEntityAttribute(final byte[] bytes) {
        return Optional.ofNullable(bytes).map(b -> {
            try {
                return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);
    }
}
