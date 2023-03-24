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
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqMessage;

@ExtendWith(MockitoExtension.class)
class SnsServiceTest {

  private static final String TOPIC_SUFFIX = "-test";

  @Mock
  private SnsClient snsClient;

  @Mock
  private ObjectMapper objectMapper;

  private SnsService snsService;

  @BeforeEach
  void setUp() {
    snsService = new SnsService(
        snsClient,
        new SnsConfigurationProperties(null, null, null, TOPIC_SUFFIX),
        objectMapper
    );
  }

  @Test
  void createTopic() {
    var topicBaseName = "test-topic";
    var topicArn = "test-topic-arn";

    when(
        snsClient.createTopic(
            CreateTopicRequest.builder()
                .name(topicBaseName + TOPIC_SUFFIX + ".fifo")
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
}
