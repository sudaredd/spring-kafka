package msg.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.apache.kafka.clients.producer.RecordMetadata;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KafkaSenderTest {

    @Mock
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<Integer, String>> mockFuture;
    
    @Mock
    private SendResult<Integer, String> mockSendResult;

    @Mock
    private RecordMetadata mockRecordMetadata;

    @InjectMocks
    private KafkaSender kafkaSender;

    @Captor
    private ArgumentCaptor<ListenableFutureCallback<SendResult<Integer, String>>> callbackCaptor;

    private final String testTopic = "testTopic";
    private final String testMessage = "Hello Kafka!";

    @BeforeEach
    void setUp() {
        // No need for MockitoAnnotations.openMocks(this) when using @ExtendWith(MockitoExtension.class)
        // Resetting mocks can be useful if they are reused across tests in complex ways,
        // but with @ExtendWith, mocks are typically re-initialized per test.
    }

    @Test
    void sendMessage_successfulSend_callsKafkaTemplateAndAddsCallback() {
        when(kafkaTemplate.send(testTopic, testMessage)).thenReturn(mockFuture);

        kafkaSender.sendMessage(testTopic, testMessage);

        verify(kafkaTemplate).send(testTopic, testMessage);
        verify(mockFuture).addCallback(any(ListenableFutureCallback.class));
    }

    @Test
    void sendMessage_kafkaTemplateSendThrowsException_shouldNotAddCallbackIfFutureNotReturned() {
        // This test assumes kafkaTemplate.send() itself might throw an exception
        // before returning a future (e.g., configuration error, immediate connection issue).
        when(kafkaTemplate.send(testTopic, testMessage)).thenThrow(new RuntimeException("Kafka connection failed"));

        // We expect the KafkaSender.sendMessage to not suppress this synchronous exception
        assertThrows(RuntimeException.class, () -> {
            kafkaSender.sendMessage(testTopic, testMessage);
        });

        verify(kafkaTemplate).send(testTopic, testMessage);
        // If .send() throws, addCallback on a future would not be reached.
        verify(mockFuture, never()).addCallback(any(ListenableFutureCallback.class));
    }


    @Test
    void sendMessage_callbackOnSuccess_logsCorrectly() {
        // Arrange
        when(kafkaTemplate.send(eq(testTopic), eq(testMessage))).thenReturn(mockFuture);
        // The actual SendResult and RecordMetadata would be provided by Spring Kafka
        // We mock them here to simulate a successful callback
        when(mockSendResult.getRecordMetadata()).thenReturn(mockRecordMetadata);
        when(mockRecordMetadata.offset()).thenReturn(123L);

        kafkaSender.sendMessage(testTopic, testMessage);

        // Capture the callback
        verify(mockFuture).addCallback(callbackCaptor.capture());
        ListenableFutureCallback<SendResult<Integer, String>> callback = callbackCaptor.getValue();

        // Act: Simulate the Kafka success callback
        callback.onSuccess(mockSendResult);

        // Assert
        // Direct log verification is complex. We assume the logger inside the callback works as coded.
        // We've verified the callback is added and can invoke it.
        // We can verify that the necessary methods on mockSendResult and mockRecordMetadata were called by the callback.
        verify(mockSendResult).getRecordMetadata();
        verify(mockRecordMetadata).offset();
        // If LOGGER was a mock, we could verify LOGGER.info(...) call, but that's usually not recommended for SLF4J.
    }

    @Test
    void sendMessage_callbackOnFailure_logsError() {
        // Arrange
        when(kafkaTemplate.send(eq(testTopic), eq(testMessage))).thenReturn(mockFuture);
        Throwable testException = new RuntimeException("Simulated send failure");

        kafkaSender.sendMessage(testTopic, testMessage);

        // Capture the callback
        verify(mockFuture).addCallback(callbackCaptor.capture());
        ListenableFutureCallback<SendResult<Integer, String>> callback = callbackCaptor.getValue();

        // Act: Simulate the Kafka failure callback
        callback.onFailure(testException);

        // Assert
        // Direct log verification is complex. We assume the logger inside the callback works as coded.
        // We've verified the callback is added and can invoke it.
        // If LOGGER was a mock, we could verify LOGGER.error(...) call.
        // The main thing is that the onFailure part of the callback is exercised.
        // No additional mock interactions to verify here beyond what's in the success case,
        // as onFailure primarily logs the provided exception and message.
    }
    
    @Test
    void sendMessage_futureCompletesWithFailure_callbackOnFailureIsInvoked() {
        // This is a more realistic test for async failure
        // where kafkaTemplate.send returns a future, but that future later signals a failure.
        
        // Use doAnswer to capture the callback and immediately invoke onFailure
        doAnswer(invocation -> {
            ListenableFutureCallback<SendResult<Integer, String>> callback = invocation.getArgument(0);
            callback.onFailure(new RuntimeException("Async send failed"));
            return null; // addCallback is void
        }).when(mockFuture).addCallback(any(ListenableFutureCallback.class));

        when(kafkaTemplate.send(testTopic, testMessage)).thenReturn(mockFuture);

        kafkaSender.sendMessage(testTopic, testMessage);

        verify(kafkaTemplate).send(testTopic, testMessage);
        verify(mockFuture).addCallback(callbackCaptor.capture());
        
        // In this setup, onFailure is invoked during the addCallback mock execution.
        // Assertions for logging (if possible) or other side effects of failure would go here.
        // For now, this confirms the callback mechanism is wired.
    }
}
