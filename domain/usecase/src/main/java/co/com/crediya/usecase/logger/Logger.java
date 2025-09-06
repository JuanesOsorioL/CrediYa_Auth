package co.com.crediya.usecase.logger;

public interface Logger {
    void info(String message);

    void warn(String message);

    void error(String message, Throwable exception);
}
