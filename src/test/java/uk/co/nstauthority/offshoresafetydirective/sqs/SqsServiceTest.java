package uk.co.nstauthority.offshoresafetydirective.sqs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SetQueueAttributesRequest;
import uk.co.nstauthority.offshoresafetydirective.sns.SnsTopicArn;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class SqsServiceTest {

  private static final String ENVIRONMENT_SUFFIX = "-test";

  @Mock
  private SqsClient sqsClient;

  private SqsService sqsService;

  @BeforeEach
  void setUp() {
    sqsService = spy(new SqsService(
        sqsClient,
        new SnsSqsConfigurationProperties(null, null, null, ENVIRONMENT_SUFFIX)
    ));
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
}
