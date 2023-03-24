package uk.co.nstauthority.offshoresafetydirective.sns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqMessage;

@Service
public class SnsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SnsService.class);

  private final SnsClient snsClient;
  private final String topicSuffix;
  private final ObjectMapper objectMapper;

  @Autowired
  SnsService(SnsClient snsClient, SnsConfigurationProperties snsConfigurationProperties, ObjectMapper objectMapper) {
    this.snsClient = snsClient;
    this.topicSuffix = snsConfigurationProperties.topicSuffix();
    this.objectMapper = objectMapper;
  }

  public SnsTopicArn getOrCreateTopic(String baseName) {
    var name = baseName + topicSuffix + ".fifo";
    var createTopicResponse = snsClient.createTopic(CreateTopicRequest.builder()
        .name(name)
        .attributes(Map.of("FifoTopic", "true"))
        .build());
    var topicArn = createTopicResponse.topicArn();
    LOGGER.info("Created SNS topic: {} (ARN: {})", name, topicArn);
    return new SnsTopicArn(topicArn);
  }

  public void publishMessage(SnsTopicArn topicArn, OsdEpmqMessage epmqMessage) {
    String message;
    try {
      message = objectMapper.writeValueAsString(epmqMessage);
    } catch (JsonProcessingException exception) {
      throw new RuntimeException(exception);
    }

    try {
      snsClient.publish(PublishRequest.builder()
          .topicArn(topicArn.arn())
          .message(message)
          .messageDeduplicationId(UUID.randomUUID().toString())
          .messageGroupId(topicArn.arn())
          .build());
      LOGGER.info("SNS message published to topic {}: {}", topicArn.arn(), message);
    } catch (SdkException exception) {
      LOGGER.error("SNS message failed to publish to topic {}: {}", topicArn.arn(), message, exception);
    }
  }
}
