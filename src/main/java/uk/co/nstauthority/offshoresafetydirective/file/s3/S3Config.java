package uk.co.nstauthority.offshoresafetydirective.file.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ObjectUtils;

@ConfigurationProperties(prefix = "s3")
public record S3Config(String accessKey,
                       String secretKey,
                       String endpoint,
                       String regionName,
                       String bucketName,
                       boolean disableSsl,
                       String proxyHost,
                       Integer proxyPort
                       ) {
  boolean isProxyConfigured() {
    return !ObjectUtils.isEmpty(proxyHost) && !ObjectUtils.isEmpty(proxyPort);
  }
}