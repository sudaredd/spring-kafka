package msg.common;

/**
 * Holds common constants used throughout the Kafka application,
 * such as message delimiters.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    public static final String MESSAGE_DELIMITER = "\u0001"; // SOH character
}
