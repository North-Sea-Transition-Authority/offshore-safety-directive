package uk.co.nstauthority.offshoresafetydirective.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import uk.co.fivium.energyportalmessagequeue.message.EpmqMessage;
import uk.co.nstauthority.offshoresafetydirective.correlationid.CorrelationIdUtil;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;

@Service
public class SqsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsService.class);

  private final SqsClient sqsClient;
  private final String environmentSuffix;
  private final ObjectMapper objectMapper;

  @Autowired
  public SqsService(
      SqsClient sqsClient,
      SnsSqsConfigurationProperties snsSqsConfigurationProperties,
      ObjectMapper objectMapper
  ) {
    this.sqsClient = sqsClient;
    environmentSuffix = snsSqsConfigurationProperties.environmentSuffix();
    this.objectMapper = objectMapper;
  }

  public SqsQueueUrl getOrCreateQueue(String baseName) {
    var name = baseName + environmentSuffix + ".fifo";
    var createQueueResponse = sqsClient.createQueue(CreateQueueRequest.builder()
        .queueName(name)
        .attributes(Map.of(QueueAttributeName.FIFO_QUEUE, "true"))
        .build());
    var queueUrl = new SqsQueueUrl(createQueueResponse.queueUrl());
    var queueArn = getQueueArnByUrl(queueUrl);
    LOGGER.info("Created SQS queue: {} (ARN: {})", name, queueArn.arn());
    return queueUrl;
  }

  public SqsQueueArn getQueueArnByUrl(SqsQueueUrl queueUrl) {
    var getQueueAttributesResponse = sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
        .queueUrl(queueUrl.url())
        .attributeNames(QueueAttributeName.QUEUE_ARN)
        .build());
    return new SqsQueueArn(getQueueAttributesResponse.attributes().get(QueueAttributeName.QUEUE_ARN));
  }

  public void grantSnsTopicAccessToQueue(SqsQueueUrl queueUrl, SqsQueueArn queueArn, SnsTopicArn topicArn) {
    // v2 SDK doesn't have a policy builder API, so construct the json manually.
    // See https://github.com/aws/aws-sdk-java-v2/issues/39
    var policy = """
        {
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {
                "Service": "sns.amazonaws.com"
              },
              "Action": "sqs:SendMessage",
              "Resource": "%s",
              "Condition": {
                "ArnEquals": {
                  "aws:SourceArn": "%s"
                }
              }
            }
          ]
        }
        """.formatted(queueArn.arn(), topicArn.arn());

    sqsClient.setQueueAttributes(
        SetQueueAttributesRequest.builder()
            .queueUrl(queueUrl.url())
            .attributes(Map.of(QueueAttributeName.POLICY, policy))
            .build()
    );
  }

  public <T extends EpmqMessage> void receiveQueueMessages(
      SqsQueueUrl queueUrl,
      Class<T> messageClass,
      Consumer<T> onMessage
  ) {
    var receiveMessagesResponse = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
        .queueUrl(queueUrl.url())
        .maxNumberOfMessages(10)
        .waitTimeSeconds(20)
        .build());

    receiveMessagesResponse.messages().forEach(sqsMessage -> {
      T message;
      try {
        message = objectMapper.readValue(sqsMessage.body(), messageClass);
      } catch (JsonProcessingException exception) {
        LOGGER.error(
            "Error deserializing SQS message {} to {}",
            sqsMessage.messageId(),
            messageClass.getSimpleName(),
            exception
        );
        return;
      }

      CorrelationIdUtil.setCorrelationIdOnMdc(message.getCorrelationId());

      try {
        try {
          onMessage.accept(message);
        } catch (Exception exception) {
          LOGGER.error("Error handling {} (SQS message ID: {})", messageClass.getSimpleName(), sqsMessage.messageId());
          return;
        }

        try {
          sqsClient.deleteMessage(
              DeleteMessageRequest.builder()
                  .queueUrl(queueUrl.url())
                  .receiptHandle(sqsMessage.receiptHandle())
                  .build()
          );
        } catch (SdkException exception) {
          LOGGER.error(
              "Error deleting SQS message {} by receipt handle {} from queue {}",
              sqsMessage.messageId(),
              sqsMessage.receiptHandle(),
              queueUrl.url(),
              exception
          );
        }
      } finally {
        CorrelationIdUtil.clearCorrelationIdOnMdc();
      }
    });
  }
}
