package uk.co.nstauthority.offshoresafetydirective.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

public class LoggerUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtil.class);
  private static final String LOG_PROHIBITED_CHARACTERS_REGEX = "[\n\r\t]";

  private LoggerUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  public static void error(String message) {
    log(LogLevel.ERROR, message);
  }

  public static void warn(String message) {
    log(LogLevel.WARN, message);
  }

  public static void info(String message) {
    log(LogLevel.INFO, message);
  }

  public static void debug(String message) {
    log(LogLevel.DEBUG, message);
  }

  private static void log(LogLevel logLevel, String logMessage) {

    if (logLevel == null) {
      throw new IllegalArgumentException("LogLevel must be passed to logging service");
    }

    String sanitisedLogMessage = logMessage.replaceAll(LOG_PROHIBITED_CHARACTERS_REGEX, "_");

    switch (logLevel) {
      case ERROR -> LOGGER.error(sanitisedLogMessage);
      case WARN -> LOGGER.warn(sanitisedLogMessage);
      case INFO -> LOGGER.info(sanitisedLogMessage);
      case DEBUG -> LOGGER.debug(sanitisedLogMessage);
      default -> throw new IllegalArgumentException(
          "Invalid LogLevel passed into logging service: %s".formatted(logLevel.name())
      );
    }
  }
}