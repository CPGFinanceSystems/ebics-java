package de.cpg.oss.ebics.api.exception;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.stream.Stream;


public class ReturnCodeTest {

    @Test
    public void testNoDuplicateReturnCodes() {
        Assertions.assertThat(Stream.of(ReturnCode.values()).map(ReturnCode::getCode).distinct().count())
                .isEqualTo(ReturnCode.values().length - 1); // one code is duplicated by specification
    }
}
