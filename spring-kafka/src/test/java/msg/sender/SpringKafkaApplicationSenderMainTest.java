package msg.sender;

import msg.common.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import twitter4j.Status;
import twitter4j.User;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpringKafkaApplicationSenderMainTest {

    @Mock
    private Status mockStatus;

    @Mock
    private User mockUser;

    private Date testDate;
    private String testDateString;
    private final String testUserName = "testUser";
    private final String testTweetText = "This is a test tweet.";
    private final long testStatusId = 12345L;
    private final long testUserId = 67890L;


    @BeforeEach
    void setUp() {
        testDate = new Date();
        testDateString = testDate.toString(); // As per current formatting logic
        // Resetting mocks is handled by MockitoExtension, but good to re-initialize shared test data
    }

    @Test
    void formatTweetForKafka_validStatus_returnsCorrectlyFormattedString() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate);
        when(mockStatus.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(testUserName);
        when(mockStatus.getText()).thenReturn(testTweetText);
        when(mockStatus.getId()).thenReturn(testStatusId); // For logging within the method

        String expected = String.join(Constants.MESSAGE_DELIMITER, testDateString, testUserName, testTweetText);
        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);

        assertEquals(expected, actual);
    }

    @Test
    void formatTweetForKafka_nullStatus_returnsNull() {
        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(null);
        assertNull(actual);
    }

    @Test
    void formatTweetForKafka_nullCreatedAt_returnsNull() {
        when(mockStatus.getCreatedAt()).thenReturn(null);
        // Needed for logging inside formatTweetForKafka if an error occurs
        when(mockStatus.getId()).thenReturn(testStatusId); 
        // No need to mock other fields if the first check fails

        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);
        assertNull(actual);
    }

    @Test
    void formatTweetForKafka_nullUser_returnsNull() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate);
        when(mockStatus.getUser()).thenReturn(null);
        when(mockStatus.getId()).thenReturn(testStatusId);

        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);
        assertNull(actual);
    }

    @Test
    void formatTweetForKafka_nullUserName_returnsNull() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate);
        when(mockStatus.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(null);
        when(mockStatus.getId()).thenReturn(testStatusId);
        when(mockUser.getId()).thenReturn(testUserId); // For logging within the method

        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);
        assertNull(actual);
    }

    @Test
    void formatTweetForKafka_nullText_returnsNull() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate);
        when(mockStatus.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(testUserName);
        when(mockStatus.getText()).thenReturn(null);
        when(mockStatus.getId()).thenReturn(testStatusId);

        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);
        assertNull(actual);
    }

    @Test
    void formatTweetForKafka_emptyUserName_returnsFormattedStringWithEmptyUserName() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate);
        when(mockStatus.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(""); // Empty user name
        when(mockStatus.getText()).thenReturn(testTweetText);
        when(mockStatus.getId()).thenReturn(testStatusId);

        String expected = String.join(Constants.MESSAGE_DELIMITER, testDateString, "", testTweetText);
        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);

        assertEquals(expected, actual);
    }

    @Test
    void formatTweetForKafka_emptyText_returnsFormattedStringWithEmptyText() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate);
        when(mockStatus.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(testUserName);
        when(mockStatus.getText()).thenReturn(""); // Empty text
        when(mockStatus.getId()).thenReturn(testStatusId);

        String expected = String.join(Constants.MESSAGE_DELIMITER, testDateString, testUserName, "");
        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);

        assertEquals(expected, actual);
    }
    
    @Test
    void formatTweetForKafka_allFieldsEmpty_returnsFormattedStringWithEmptyFields() {
        when(mockStatus.getCreatedAt()).thenReturn(testDate); // Date is mandatory for current logic to proceed past first check
        when(mockStatus.getUser()).thenReturn(mockUser);
        when(mockUser.getName()).thenReturn(""); 
        when(mockStatus.getText()).thenReturn(""); 
        when(mockStatus.getId()).thenReturn(testStatusId);

        String expected = String.join(Constants.MESSAGE_DELIMITER, testDateString, "", "");
        String actual = SpringKafkaApplicationSenderMain.formatTweetForKafka(mockStatus);

        assertEquals(expected, actual);
    }
}
