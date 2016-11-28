package de.cpg.oss.ebics.session;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

class PublicKeyDeserializer extends StdDeserializer<PublicKey> {

    private static final long serialVersionUID = 1L;

    PublicKeyDeserializer(final Class<?> clazz) {
        super(clazz);
    }

    @Override
    public PublicKey deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        try {
            return publicKey(keyFactory(), jsonParser.readValueAs(byte[].class));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new JsonParseException(jsonParser, e.getLocalizedMessage(), e);
        }
    }

    static KeyFactory keyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

    private static PublicKey publicKey(final KeyFactory keyFactory, final byte[] encoded) throws InvalidKeySpecException {
        return keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
    }
}
