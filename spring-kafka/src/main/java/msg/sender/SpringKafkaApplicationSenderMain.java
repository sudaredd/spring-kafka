package msg.sender;

import java.util.StringJoiner;
import java.util.Date; // Added for explicit type usage in formatTweetForKafka if needed, though status.getCreatedAt() is already Date

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import msg.common.Constants;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.User; // Added for explicit type usage in formatTweetForKafka

/**
 * Main entry point for running the Kafka message sender application.
 * This application connects to Twitter, fetches tweets based on configured filters,
 * and sends them as messages to a Kafka topic.
 */
@EnableAutoConfiguration
@SpringBootApplication
public class SpringKafkaApplicationSenderMain implements CommandLineRunner {

	private static final Logger logger = LoggerFactory
            .getLogger(SpringKafkaApplicationSenderMain.class);
	
	@Autowired
	private KafkaSender kafkaSender;
	 
    @Value("${kafka.topic.name}")
    private String topicName;
	
	@Autowired
	private TwitterStream twitterStream;
	 
    public static void main(String[] args)  {
        SpringApplication.run(SpringKafkaApplicationSenderMain.class, args);
    }

    /**
     * Formats a Twitter {@link twitter4j.Status} object into a string suitable for sending via Kafka.
     * The fields (creation date, username, tweet text) are joined using {@link msg.common.Constants#MESSAGE_DELIMITER}.
     * <p>
     * This method performs null checks on the status object itself and its critical fields:
     * {@code getCreatedAt()}, {@code getUser()}, {@code getUser().getName()}, and {@code getText()}.
     * If any of these essential fields are null, the method logs a warning (using the class's static logger)
     * and returns {@code null}.
     *
     * @param status The {@link twitter4j.Status} object to format.
     * @return A delimited string representation of the tweet, or {@code null} if the input status object
     *         or any of its critical fields (createdAt, user, user.name, text) are null.
     */
    public static String formatTweetForKafka(Status status) {
        if (status == null) {
            logger.warn("formatTweetForKafka: Received null status object.");
            return null;
        }
        Date createdAt = status.getCreatedAt();
        User user = status.getUser();
        String text = status.getText();

        if (createdAt == null) {
            logger.warn("formatTweetForKafka: Status ID {} has null creation date.", status.getId());
            return null;
        }
        if (user == null) {
            logger.warn("formatTweetForKafka: Status ID {} has null user object.", status.getId());
            return null;
        }
        String userName = user.getName();
        if (userName == null) {
            logger.warn("formatTweetForKafka: Status ID {} from user ID {} has null user name.", status.getId(), user.getId());
            return null;
        }
        if (text == null) {
            // Text can be null for some types of tweets (e.g. deletions not normally streamed here, but good check)
            logger.warn("formatTweetForKafka: Status ID {} has null text.", status.getId());
            return null; 
        }

        StringJoiner joiner = new StringJoiner(Constants.MESSAGE_DELIMITER);
        joiner.add(createdAt.toString()) // Consider more specific date formatting if required downstream
              .add(userName)
              .add(text);
        return joiner.toString();
    }

	private void writeToKafka(String msg) {
		kafkaSender.sendMessage(this.topicName, msg);
	}

	@Override
	public void run(String... args) throws Exception {
		readTwitterStream();
	}

	private void readTwitterStream() {
		StatusListener statusListener = new StatusListener() {
			@Override
			public void onException(Exception ex) {
				logger.error("Exception in Twitter stream listener", ex);
			}
			
			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				logger.warn("Twitter track limitation notice: {}", numberOfLimitedStatuses);
			}
			
			@Override
			public void onStatus(Status status) {
                // Refactored formatting logic
                String formattedMessage = formatTweetForKafka(status);

                if (formattedMessage != null) {
                    logger.info("Received status, formatted message: {}", formattedMessage);
                    writeToKafka(formattedMessage);
                } else {
                    // Logging for null formattedMessage is handled within formatTweetForKafka
                    // or could add a specific log here if required for the listener context.
                    logger.warn("Skipping message for status ID {} due to formatting issues (null output).", status != null ? status.getId() : "unknown (null status)");
                }
			}
			
			@Override
			public void onStallWarning(StallWarning warning) {
				logger.warn("Twitter stall warning: {}", warning);
			}
			
			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				logger.info("Twitter scrub_geo event for userId {} upToStatusId {}", userId, upToStatusId);
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				logger.info("Twitter status deletion notice: {}", statusDeletionNotice);
			}
		};
			
		twitterStream.addListener(statusListener);
		twitterStream.filter("#java","#java8","#Black Friday","#Ashes cricket","#python","#Donald Trump");
	}
}
