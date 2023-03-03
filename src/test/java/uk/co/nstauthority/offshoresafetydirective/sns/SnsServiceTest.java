package uk.co.nstauthority.offshoresafetydirective.sns;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;

@ExtendWith(MockitoExtension.class)
class SnsServiceTest {

  private static final String TOPIC_SUFFIX = "-test";

  @Mock
  private SnsClient snsClient;

  private SnsService snsService;

  @BeforeEach
  void setUp() {
    snsService = new SnsService(snsClient, new SnsConfigurationProperties(null, null, null, TOPIC_SUFFIX));
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
}
