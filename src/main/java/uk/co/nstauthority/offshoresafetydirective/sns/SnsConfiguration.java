package uk.co.nstauthority.offshoresafetydirective.sns;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
class SnsConfiguration {

  @Bean
  SnsClient snsClient(SnsConfigurationProperties snsConfigurationProperties) {
    return SnsClient.builder()
        .region(Region.of(snsConfigurationProperties.regionId()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    snsConfigurationProperties.accessKeyId(),
                    snsConfigurationProperties.secretAccessKey()
                )
            )
        )
        .build();
  }
}
