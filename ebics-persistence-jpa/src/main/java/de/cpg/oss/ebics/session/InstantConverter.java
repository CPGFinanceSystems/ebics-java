package de.cpg.oss.ebics.session;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(final Instant instant) {
        final Timestamp result = Optional.ofNullable(instant).map(Timestamp::from).orElse(null);
        log.trace("Serialized {} to {}", instant, result);
        return result;
    }

    @Override
    public Instant convertToEntityAttribute(final Timestamp timestamp) {
        final Instant result = Optional.ofNullable(timestamp).map(Timestamp::toInstant).orElse(null);
        log.trace("Deserialized {} from {}", result, timestamp);
        return result;
    }
}
