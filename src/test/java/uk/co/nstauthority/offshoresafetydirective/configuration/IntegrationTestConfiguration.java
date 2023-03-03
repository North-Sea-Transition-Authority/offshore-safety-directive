package uk.co.nstauthority.offshoresafetydirective.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;

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
}
