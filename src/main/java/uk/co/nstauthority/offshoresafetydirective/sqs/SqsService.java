package uk.co.nstauthority.offshoresafetydirective.sqs;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;

@Service
public class SqsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqsService.class);

  private final SqsClient sqsClient;
  private final String environmentSuffix;

  public SqsService(SqsClient sqsClient, SnsSqsConfigurationProperties snsSqsConfigurationProperties) {
    this.sqsClient = sqsClient;
    environmentSuffix = snsSqsConfigurationProperties.environmentSuffix();
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
}
