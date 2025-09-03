package co.com.crediya.usecase.user.logger;

public interface Logger {
    void info(String message);

    void warn(String message);

    void error(String message, Throwable exception);
}
