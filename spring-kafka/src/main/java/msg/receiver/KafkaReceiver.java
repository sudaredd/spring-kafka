package msg.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.util.StringUtils;

import msg.common.Constants; // Added import
import msg.model.TwitterUser;

// Assuming TwitterUserRepository is in this package (msg.receiver) as per original file structure.
// If it's actually in msg.repository, an import for msg.repository.TwitterUserRepository would be needed,
// and the type of the field below would need to change.
// For now, sticking to the original file's direct usage of TwitterUserRepository.

public class KafkaReceiver {

    private static final Logger logger = LoggerFactory
            .getLogger(KafkaReceiver.class);

    // int soh = 0x01; // This line is removed as per requirements

    @Autowired
    private TwitterUserRepository twitterUserRepository; // Type kept as msg.receiver.TwitterUserRepository
	 
    @KafkaListener(topics = "${kafka.topic.name}") // Updated to use property placeholder
    public void receiveMsg(String message) {
        logger.info("Received message: [{}]", message); // Standardized logging

        String[] parts = StringUtils.split(message, Constants.MESSAGE_DELIMITER);

        if (parts == null || parts.length != 3) {
            logger.error("Malformed message received: [{}]. Expected 3 parts, but got {}.", 
                         message, (parts == null ? 0 : parts.length));
            return; // Skip further processing
        }

        // Assuming TwitterUser.newInstance can handle a String array of 3 parts.
        // If it expects specific types, conversions would be needed here.
        TwitterUser twitterUser = TwitterUser.newInstance(parts);

        if (twitterUser == null) {
            // Log with "(Object) parts" to ensure parts.toString() is not misinterpreted by logger varargs
            logger.error("Failed to create TwitterUser from parts: {}", (Object) parts);
            return; // Skip further processing
        }
        
        // Save to MongoDB with reactive error handling
        // Assuming twitterUserRepository.save returns a Mono or Flux (common for Spring Data Reactive Repositories)
        try {
            twitterUserRepository.save(twitterUser)
                .doOnError(e -> {
                    // Using toString() on twitterUser for logging, assuming a sensible implementation.
                    // Be cautious if twitterUser.toString() can throw exceptions or is very verbose.
                    logger.error("Failed to save TwitterUser {}: {}", twitterUser.toString(), e.getMessage());
                })
                .subscribe(
                    savedUser -> {
                        // Using toString() on savedUser for logging.
                        logger.info("TwitterUser {} saved successfully", savedUser.toString());
                    },
                    error -> {
                        // This error callback in subscribe is for errors that occur *after* doOnError
                        // or if doOnError itself fails, or for terminal errors in the reactive stream.
                        logger.error("Error during subscribe phase for TwitterUser {}: {}", twitterUser.toString(), error.getMessage());
                    }
                );
        } catch (Exception e) {
            // This catch block is for any synchronous exceptions that might occur *before*
            // the reactive chain is even established, or if .save() itself is not fully reactive.
            logger.error("Unexpected synchronous error during save operation for TwitterUser {}: {}", 
                         twitterUser.toString(), e.getMessage(), e);
        }
    }
}
