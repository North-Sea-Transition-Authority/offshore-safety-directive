package uk.co.nstauthority.offshoresafetydirective.file.s3;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class S3ClientProviderTest {
  private S3ClientProvider s3ClientProvider;

  @BeforeEach
  void setUp() {
    s3ClientProvider = new S3ClientProvider();
  }

  @Test
  void getAmazonS3ClientBuilder_DefaultConfig() {
    var s3Config = S3ConfigTestUtil.getDefaultS3Config();
    var amazonS3 = s3ClientProvider.getAmazonS3ClientBuilder(s3Config);

    assertThat(amazonS3.getCredentials().getCredentials())
        .extracting(
            AWSCredentials::getAWSAccessKeyId,
            AWSCredentials::getAWSSecretKey
        )
        .containsExactly(
            S3ConfigTestUtil.ACCESS_KEY,
            S3ConfigTestUtil.SECRET_KEY
        );

    assertThat(amazonS3.getEndpoint())
        .extracting(
            AwsClientBuilder.EndpointConfiguration::getServiceEndpoint,
            AwsClientBuilder.EndpointConfiguration::getSigningRegion
        )
        .containsExactly(
            S3ConfigTestUtil.ENDPOINT,
            S3ConfigTestUtil.REGION_NAME
        );

  }

  @Test
  void getAmazonS3ClientBuilder_SSLEnabledAndProxyDisabled() {
    var s3Config = S3ConfigTestUtil.getDefaultS3Config();
    var amazonS3 = s3ClientProvider.getAmazonS3ClientBuilder(s3Config);

    assertThat(amazonS3.getClientConfiguration())
        .extracting(
            ClientConfiguration::getProxyHost,
            ClientConfiguration::getProxyPort,
            ClientConfiguration::getProtocol
        )
        .containsExactly(
            null,
            -1,
            Protocol.HTTPS
        );
  }

  @Test
  void getAmazonS3ClientBuilder_SSLDisabled() {
    var s3Config = S3ConfigTestUtil.getS3ConfigWithSslDisabled();
    var amazonS3 = s3ClientProvider.getAmazonS3ClientBuilder(s3Config);

    assertThat(amazonS3.getClientConfiguration().getProtocol()).isEqualTo(Protocol.HTTP);
  }

  @Test
  void getAmazonS3ClientBuilder_ProxyEnabled() {
    var proxyHost = "test-proxy-host";
    var proxyPort = 1000;
    var s3Config = S3ConfigTestUtil.getS3ConfigWithProxyConfigured(proxyHost, proxyPort);
    var amazonS3 = s3ClientProvider.getAmazonS3ClientBuilder(s3Config);

    assertThat(amazonS3.getClientConfiguration())
        .extracting(
            ClientConfiguration::getProxyHost,
            ClientConfiguration::getProxyPort
        )
        .containsExactly(
            proxyHost,
            proxyPort
        );
  }
}