package uk.co.nstauthority.offshoresafetydirective.sns;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueArn;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueUrl;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsService;

@ExtendWith(MockitoExtension.class)
class SnsServiceTest {

  private static final String ENVIRONMENT_SUFFIX = "-test";

  @Mock
  private SnsClient snsClient;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private SqsService sqsService;

  private SnsService snsService;

  @BeforeEach
  void setUp() {
    snsService = new SnsService(
        snsClient,
        new SnsSqsConfigurationProperties(null, null, null, ENVIRONMENT_SUFFIX),
        objectMapper,
        sqsService
    );
  }

  @Test
  void getOrCreateTopic() {
    var topicBaseName = "test-topic";
    var topicArn = "test-topic-arn";

    when(
        snsClient.createTopic(
            CreateTopicRequest.builder()
                .name(topicBaseName + ENVIRONMENT_SUFFIX + ".fifo")
                .attributes(Map.of("FifoTopic", "true"))
                .build()
        )
    ).thenReturn(CreateTopicResponse.builder().topicArn(topicArn).build());

    assertThat(snsService.getOrCreateTopic(topicBaseName)).isEqualTo(new SnsTopicArn(topicArn));
  }

  @Test
  void publishMessage() throws JsonProcessingException {
    var topicArn = new SnsTopicArn("test-topic-arn");
    var epmqMessage = mock(OsdEpmqMessage.class);

    var message = "{ \"service\": \"OSD\" }";

    when(objectMapper.writeValueAsString(epmqMessage)).thenReturn(message);

    snsService.publishMessage(topicArn, epmqMessage);

    verify(snsClient).publish(
        PublishRequest.builder()
            .topicArn(topicArn.arn())
            .message(message)
            .messageDeduplicationId(any())
            .messageGroupId(topicArn.arn())
            .build()
    );
  }

  @Test
  void subscribeTopicToSqsQueue() {
    var topicArn = new SnsTopicArn("test-topic-arn");
    var queueUrl = new SqsQueueUrl("test-queue-url");
    var queueArn = new SqsQueueArn("test-queue-arn");

    when(sqsService.getQueueArnByUrl(queueUrl)).thenReturn(queueArn);

    snsService.subscribeTopicToSqsQueue(topicArn, queueUrl);

    verify(snsClient).subscribe(SubscribeRequest.builder()
        .topicArn(topicArn.arn())
        .protocol("sqs")
        .endpoint(queueArn.arn())
        .attributes(Map.of("RawMessageDelivery", "true"))
        .build());

    verify(sqsService).grantSnsTopicAccessToQueue(queueUrl, queueArn, topicArn);
  }
}
