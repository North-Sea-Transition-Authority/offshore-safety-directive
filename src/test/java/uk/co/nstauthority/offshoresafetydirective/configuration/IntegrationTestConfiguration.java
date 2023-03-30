package uk.co.nstauthority.offshoresafetydirective.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@TestConfiguration
public class IntegrationTestConfiguration {

  @Bean
  @Primary
  SnsClient testSnsClient() {
    var mockClient = mock(SnsClient.class);

    var createTopicRequestArgumentCaptor = ArgumentCaptor.forClass(CreateTopicRequest.class);

    when(mockClient.createTopic(createTopicRequestArgumentCaptor.capture())).thenAnswer(
        invocation ->
            CreateTopicResponse.builder()
                .topicArn("%s-arn".formatted(createTopicRequestArgumentCaptor.getValue().name()))
                .build()
    );

    return mockClient;
  }

  @Bean
  @Primary
  SqsClient testSqsClient() {
    var mockClient = mock(SqsClient.class);

    var createQueueRequestArgumentCaptor = ArgumentCaptor.forClass(CreateQueueRequest.class);

    when(mockClient.createQueue(createQueueRequestArgumentCaptor.capture())).thenAnswer(
        invocation ->
            CreateQueueResponse.builder()
                .queueUrl("%s-url".formatted(createQueueRequestArgumentCaptor.getValue().queueName()))
                .build()
    );

    var getQueueAttributesRequestArgumentCaptor = ArgumentCaptor.forClass(GetQueueAttributesRequest.class);

    when(mockClient.getQueueAttributes(getQueueAttributesRequestArgumentCaptor.capture())).thenAnswer(
        invocation ->
            GetQueueAttributesResponse.builder()
                .attributes(
                    Map.of(
                        QueueAttributeName.QUEUE_ARN,
                        "%s-arn".formatted(getQueueAttributesRequestArgumentCaptor.getValue().queueUrl())
                    )
                )
                .build()
    );

    return mockClient;
  }
}
