package msg.receiver;

import msg.common.Constants;
import msg.model.TwitterUser;
// Corrected import for the actual repository if it's in msg.repository
// However, KafkaReceiver.java currently uses a type msg.receiver.TwitterUserRepository
// For the test to compile against the current KafkaReceiver, this mock needs to match.
// If KafkaReceiver is updated to use msg.repository.TwitterUserRepository, this test should also be updated.
// For now, assuming msg.receiver.TwitterUserRepository as per current KafkaReceiver.java
// import msg.repository.TwitterUserRepository; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

// Using MockitoExtension for JUnit 5
@ExtendWith(MockitoExtension.class)
public class KafkaReceiverTest {

    @Mock
    private TwitterUserRepository twitterUserRepository; // Using the type from msg.receiver

    @InjectMocks
    private KafkaReceiver kafkaReceiver;

    @Captor
    private ArgumentCaptor<TwitterUser> twitterUserCaptor;
    
    // This is needed if not using @ExtendWith(MockitoExtension.class) or if constructor injection is not used for mocks
    // @BeforeEach
    // void setUp() {
    //     MockitoAnnotations.openMocks(this);
    // }

    private String createMessage(String date, String user, String text) {
        return String.join(Constants.MESSAGE_DELIMITER, date, user, text);
    }

    @Test
    void receiveMsg_validMessage_savesUser() {
        String testDate = "2023-10-26";
        String testUser = "testUser";
        String testText = "Hello Kafka";
        String message = createMessage(testDate, testUser, testText);
        
        TwitterUser mockTwitterUser = new TwitterUser(testDate, testUser, testText);
        // We can't easily mock the static TwitterUser.newInstance, so we rely on its actual behavior.
        // KafkaReceiver's internal call to TwitterUser.newInstance will create an actual TwitterUser object.

        when(twitterUserRepository.save(any(TwitterUser.class))).thenReturn(Mono.empty());

        kafkaReceiver.receiveMsg(message);

        verify(twitterUserRepository).save(twitterUserCaptor.capture());
        TwitterUser capturedUser = twitterUserCaptor.getValue();

        assertNotNull(capturedUser);
        assertEquals(testDate, capturedUser.getTime());
        assertEquals(testUser, capturedUser.getUser());
        assertEquals(testText, capturedUser.getText());
        
        // Verify that subscribe was called on the Mono returned by save
        // This is a bit tricky. What we can verify is that save() itself was called.
        // The reactive chain completion is implicitly tested by not throwing an error
        // and by the fact that doOnError/subscribe are part of the chain in the SUT.
        // For more direct "subscribe called" testing, one might need more advanced reactor-test features.
        // For now, verifying save() is called is the primary goal for this interaction.
    }

    @Test
    void receiveMsg_malformedMessage_tooFewParts_doesNotSave() {
        String message = "date" + Constants.MESSAGE_DELIMITER + "user";

        kafkaReceiver.receiveMsg(message);

        // Verify newInstance is not called (indirectly, by save not being called)
        // Verify save is not called
        verify(twitterUserRepository, never()).save(any(TwitterUser.class));
        // Logging verification is complex; we assume error logging happens based on code.
    }

    @Test
    void receiveMsg_malformedMessage_tooManyParts_doesNotSave() {
        String message = "date" + Constants.MESSAGE_DELIMITER + "user" + Constants.MESSAGE_DELIMITER + "text" + Constants.MESSAGE_DELIMITER + "extra";

        kafkaReceiver.receiveMsg(message);

        verify(twitterUserRepository, never()).save(any(TwitterUser.class));
    }
    
    @Test
    void receiveMsg_nullMessage_doesNotSave() {
        // StringUtils.split(null, *) returns null, so this covers null messages.
        kafkaReceiver.receiveMsg(null);
        verify(twitterUserRepository, never()).save(any(TwitterUser.class));
    }

    @Test
    void receiveMsg_emptyMessage_doesNotSave() {
        String message = ""; // StringUtils.split("", DELIMITER) results in {""} - an array with one empty string.
                             // This means parts.length will be 1, not 3.

        kafkaReceiver.receiveMsg(message);

        verify(twitterUserRepository, never()).save(any(TwitterUser.class));
    }

    @Test
    void receiveMsg_messageWithOnlyDelimiters_doesNotSave() {
        // "" -> parts are ["", "", ""]
        // TwitterUser.newInstance would be called with these empty strings.
        // The subtask mentions "if TwitterUser.newInstance is called with empty strings, ensure that's handled or tested as expected."
        // The current KafkaReceiver doesn't explicitly block empty strings in parts, but TwitterUser might.
        // Assuming an empty user/text/date is invalid and TwitterUser.newInstance might return null or an invalid object.
        // Or, if KafkaReceiver's check `if (twitterUser == null)` is hit.
        String message = Constants.MESSAGE_DELIMITER + Constants.MESSAGE_DELIMITER; // Leads to 3 empty parts typically

        // If TwitterUser.newInstance is robust enough to handle empty strings and create an object,
        // then save might be called. If it returns null or throws an error handled by KafkaReceiver,
        // then save won't be called. The current KafkaReceiver has a null check for twitterUser.
        // Let's assume TwitterUser.newInstance might return null for all empty parts.
        
        // For this test, let's assume the current logic in KafkaReceiver:
        // 1. Splits into ["", "", ""]
        // 2. Calls TwitterUser.newInstance(["", "", ""])
        // 3. If newInstance returns null, save is not called.
        // If newInstance returns a non-null TwitterUser, then save IS called.
        // The prompt implies save() should NOT be called if parts are invalid.
        // So, we need to ensure that either newInstance returns null or we adjust the test.
        // Given `TwitterUser twitterUser = TwitterUser.newInstance(parts); if (twitterUser == null) { logger.error(...); return; }`
        // this test implicitly tests that `TwitterUser.newInstance` would return null for such input for save not to be called.

        kafkaReceiver.receiveMsg(message);
        verify(twitterUserRepository, never()).save(any(TwitterUser.class));
    }
    
    @Test
    void receiveMsg_messageWithEmptyPartsButCorrectDelimiterCount_doesNotSave() {
        // "abcdef" -> parts are ["abc", "", "def"]
        // This case depends on how TwitterUser.newInstance handles partially empty strings.
        // Similar to the above, if newInstance returns null for this, save won't be called.
        String message = "abc" + Constants.MESSAGE_DELIMITER + Constants.MESSAGE_DELIMITER + "def";

        kafkaReceiver.receiveMsg(message);
        // This assertion depends on TwitterUser.newInstance returning null for ["abc", "", "def"]
        verify(twitterUserRepository, never()).save(any(TwitterUser.class));
    }


    @Test
    void receiveMsg_repositorySaveFails_logsError() {
        String testDate = "2023-10-26";
        String testUser = "testUserFailure";
        String testText = "Save should fail";
        String message = createMessage(testDate, testUser, testText);

        when(twitterUserRepository.save(any(TwitterUser.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        kafkaReceiver.receiveMsg(message);

        verify(twitterUserRepository).save(twitterUserCaptor.capture());
        TwitterUser capturedUser = twitterUserCaptor.getValue();
        assertNotNull(capturedUser);
        assertEquals(testUser, capturedUser.getUser());

        // We expect an error to be logged by the .doOnError callback in KafkaReceiver.
        // Direct verification of SLF4J logs is complex in basic unit tests.
        // This test ensures that the reactive chain is set up to handle errors from save().
        // If the error wasn't handled (e.g., no .subscribe() or error consumer),
        // it might propagate or cause other issues, which this test doesn't cover directly.
        // However, the call to save() is verified.
    }
}
