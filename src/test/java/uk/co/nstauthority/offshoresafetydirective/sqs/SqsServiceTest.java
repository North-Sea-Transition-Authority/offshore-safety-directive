package uk.co.nstauthority.offshoresafetydirective.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class SqsServiceTest {

  private static final String ENVIRONMENT_SUFFIX = "-test";

  @Mock
  private SqsClient sqsClient;

  @Mock
  private ObjectMapper objectMapper;

  private SqsService sqsService;

  @BeforeEach
  void setUp() {
    sqsService = spy(
        new SqsService(
            sqsClient,
            new SnsSqsConfigurationProperties(null, null, null, ENVIRONMENT_SUFFIX),
            objectMapper
        )
    );
  }

  @Test
  void getOrCreateQueue() {
    var queueBaseName = "test-queue";
    var queueUrl = "test-queue-url";

    when(
        sqsClient.createQueue(
            CreateQueueRequest.builder()
                .queueName(queueBaseName + ENVIRONMENT_SUFFIX + ".fifo")
                .attributes(Map.of(QueueAttributeName.FIFO_QUEUE, "true"))
                .build()
        )
    ).thenReturn(CreateQueueResponse.builder().queueUrl(queueUrl).build());

    var sqsQueueUrl = new SqsQueueUrl(queueUrl);

    doReturn(new SqsQueueArn("test-queue-arn")).when(sqsService).getQueueArnByUrl(sqsQueueUrl);

    assertThat(sqsService.getOrCreateQueue(queueBaseName)).isEqualTo(sqsQueueUrl);
  }

  @Test
  void getQueueArnByUrl() {
    var queueUrl = new SqsQueueUrl("test-queue-url");
    var queueArn = new SqsQueueArn("test-queue-arn");

    when(
        sqsClient.getQueueAttributes(
            GetQueueAttributesRequest.builder()
                .queueUrl(queueUrl.url())
                .attributeNames(QueueAttributeName.QUEUE_ARN)
                .build()
        )
    ).thenReturn(
        GetQueueAttributesResponse.builder()
            .attributes(Map.of(QueueAttributeName.QUEUE_ARN, queueArn.arn()))
            .build()
    );

    assertThat(sqsService.getQueueArnByUrl(queueUrl)).isEqualTo(queueArn);
  }

  @Test
  void grantSnsTopicAccessToQueue() {
    var queueUrl = new SqsQueueUrl("test-queue-url");
    var queueArn = new SqsQueueArn("test-queue-arn");
    var topicArn = new SnsTopicArn("test-topic-arn");

    sqsService.grantSnsTopicAccessToQueue(queueUrl, queueArn, topicArn);

    var policy = """
        {
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {
                "Service": "sns.amazonaws.com"
              },
              "Action": "sqs:SendMessage",
              "Resource": "test-queue-arn",
              "Condition": {
                "ArnEquals": {
                  "aws:SourceArn": "test-topic-arn"
                }
              }
            }
          ]
        }
        """;

    verify(sqsClient).setQueueAttributes(
        SetQueueAttributesRequest.builder()
            .queueUrl(queueUrl.url())
            .attributes(Map.of(QueueAttributeName.POLICY, policy))
            .build()
    );
  }

  @Test
  void receiveQueueMessages() throws JsonProcessingException {
    var queueUrl = new SqsQueueUrl("test-queue-url");
    var messageClass = TestEpmqMessage.class;
    Consumer<TestEpmqMessage> onMessage = mock(Consumer.class);

    var message1Body = "message-1-body";
    var message2Body = "message-2-body";

    var sqsMessages = List.of(
        Message.builder()
            .receiptHandle("test-receipt-handle-1")
            .body(message1Body)
            .build(),
        Message.builder()
            .receiptHandle("test-receipt-handle-2")
            .body(message2Body)
            .build()
    );

    when(
        sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(queueUrl.url())
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .build())
    ).thenReturn(
        ReceiveMessageResponse.builder()
            .messages(sqsMessages)
            .build()
    );

    var message1 = new TestEpmqMessage();
    var message2 = new TestEpmqMessage();

    when(objectMapper.readValue(message1Body, messageClass)).thenReturn(message1);
    when(objectMapper.readValue(message2Body, messageClass)).thenReturn(message2);

    sqsService.receiveQueueMessages(queueUrl, messageClass, onMessage);

    verify(onMessage).accept(message1);
    verify(onMessage).accept(message2);
    verifyNoMoreInteractions(onMessage);

    sqsMessages.forEach(message ->
        verify(sqsClient)
            .deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl.url())
                .receiptHandle(message.receiptHandle())
                .build())
    );
  }

  @Test
  void receiveQueueMessages_sqsMessageNotDeletedAfterConsumerAcceptThrowsError() throws JsonProcessingException {
    var queueUrl = new SqsQueueUrl("test-queue-url");
    var messageClass = TestEpmqMessage.class;
    Consumer<TestEpmqMessage> onMessage = mock(Consumer.class);

    var messageBody = "message-body";

    var sqsMessage = Message.builder()
        .receiptHandle("test-receipt-handle")
        .body(messageBody)
        .build();

    when(
        sqsClient.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(queueUrl.url())
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .build())
    ).thenReturn(
        ReceiveMessageResponse.builder()
            .messages(List.of(sqsMessage))
            .build()
    );

    var message = new TestEpmqMessage();

    when(objectMapper.readValue(messageBody, messageClass)).thenReturn(message);

    doThrow(RuntimeException.class).when(onMessage).accept(message);

    sqsService.receiveQueueMessages(queueUrl, messageClass, onMessage);

    verify(onMessage).accept(message);

    verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
  }

  private static class TestEpmqMessage extends EpmqMessage {

    private String testField;

    public TestEpmqMessage() {
      super("TEST_SERVICE", "TEST_MESSAGE", null);
    }

    public String getTestField() {
      return testField;
    }

    public void setTestField(String testField) {
      this.testField = testField;
    }
  }
}
