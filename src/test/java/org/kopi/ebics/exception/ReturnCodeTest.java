package org.kopi.ebics.exception;

import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class ReturnCodeTest {

    @Test
    public void testNoDuplicateReturnCodes() {
        assertThat(Stream.of(ReturnCode.values()).map(ReturnCode::getCode).distinct().count())
                .isEqualTo(ReturnCode.values().length);
    }
}
