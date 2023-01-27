package uk.co.nstauthority.offshoresafetydirective.file.s3;

import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class S3ClientProvider {
  @Bean
  AmazonS3 s3Client(S3Config s3Config) {
    return getAmazonS3ClientBuilder(s3Config).build();
  }

  AmazonS3ClientBuilder getAmazonS3ClientBuilder(S3Config s3Config) {
    var credentials = new BasicAWSCredentials(s3Config.accessKey(), s3Config.secretKey());

    var credentialsProvider = new AWSStaticCredentialsProvider(credentials);

    var endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(s3Config.endpoint(), s3Config.regionName());

    var clientConfiguration = PredefinedClientConfigurations.defaultConfig();

    if (s3Config.isProxyConfigured()) {
      clientConfiguration
          .withProxyHost(s3Config.proxyHost())
          .withProxyPort(s3Config.proxyPort());
    }

    if (s3Config.disableSsl()) {
      clientConfiguration.withProtocol(Protocol.HTTP);
    }

    return AmazonS3ClientBuilder
        .standard()
        .withEndpointConfiguration(endpointConfiguration)
        .withPathStyleAccessEnabled(true)
        .withCredentials(credentialsProvider)
        .withClientConfiguration(clientConfiguration);
  }
}
