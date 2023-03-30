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
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import uk.co.nstauthority.offshoresafetydirective.epmqmessage.OsdEpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsQueueUrl;
import uk.co.nstauthority.offshoresafetydirective.sqs.SqsService;

@Service
public class SnsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SnsService.class);

  private final SnsClient snsClient;
  private final String environmentSuffix;
  private final ObjectMapper objectMapper;
  private final SqsService sqsService;

  @Autowired
  SnsService(
      SnsClient snsClient,
      SnsSqsConfigurationProperties snsSqsConfigurationProperties,
      ObjectMapper objectMapper,
      SqsService sqsService) {
    this.snsClient = snsClient;
    environmentSuffix = snsSqsConfigurationProperties.environmentSuffix();
    this.objectMapper = objectMapper;
    this.sqsService = sqsService;
  }

  public SnsTopicArn getOrCreateTopic(String baseName) {
    var name = baseName + environmentSuffix + ".fifo";
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

  public void subscribeTopicToSqsQueue(SnsTopicArn topicArn, SqsQueueUrl queueUrl) {
    var queueArn = sqsService.getQueueArnByUrl(queueUrl);
    snsClient.subscribe(SubscribeRequest.builder()
        .topicArn(topicArn.arn())
        .protocol("sqs")
        .endpoint(queueArn.arn())
        .build());
    sqsService.grantSnsTopicAccessToQueue(queueUrl, queueArn, topicArn);
    LOGGER.info("Subscribed SQS queue {} to SNS topic {}", queueArn.arn(), topicArn.arn());
  }
}
