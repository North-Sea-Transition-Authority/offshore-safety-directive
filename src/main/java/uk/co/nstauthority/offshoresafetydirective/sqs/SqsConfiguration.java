package uk.co.nstauthority.offshoresafetydirective.sqs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import uk.co.nstauthority.offshoresafetydirective.snssqs.SnsSqsConfigurationProperties;

@Configuration
class SqsConfiguration {

  @Bean
  SqsClient sqsClient(SnsSqsConfigurationProperties snsSqsConfigurationProperties) {
    return SqsClient.builder()
        .region(Region.of(snsSqsConfigurationProperties.regionId()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    snsSqsConfigurationProperties.accessKeyId(),
                    snsSqsConfigurationProperties.secretAccessKey()
                )
            )
        )
        .build();
  }
}
