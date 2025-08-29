package co.com.crediya.api.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GlobalLoggerTest {

    private final GlobalLogger logger = new GlobalLogger();

    @Test
    void testInfo() {
        assertDoesNotThrow(() -> logger.info("This is an info message"));
    }

    @Test
    void testWarn() {
        assertDoesNotThrow(() -> logger.warn("This is a warning message"));
    }

    @Test
    void testError() {
        assertDoesNotThrow(() -> logger.error("This is an error message", new RuntimeException("boom")));
    }
}