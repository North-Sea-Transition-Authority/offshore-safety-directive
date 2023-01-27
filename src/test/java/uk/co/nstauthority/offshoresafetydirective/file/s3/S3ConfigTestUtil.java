package uk.co.nstauthority.offshoresafetydirective.file.s3;

class S3ConfigTestUtil {

  public static final String ACCESS_KEY = "access-key";
  public static final String SECRET_KEY = "secret-key";
  public static final String ENDPOINT = "endpoint";
  public static final String REGION_NAME = "region-name";
  static final String BUCKET_NAME = "test-bucket";

  private S3ConfigTestUtil() {
  }

  public static S3Config getDefaultS3Config() {
    return getS3Config(false, null, null);
  }

  public static S3Config getS3ConfigWithSslDisabled() {
    return getS3Config(true, null, null);
  }

  public static S3Config getS3ConfigWithProxyConfigured(String proxyHost, int proxyPort) {
    return getS3Config(false, proxyHost, proxyPort);
  }

  public static S3Config getS3Config(boolean disableSsl, String proxyHost, Integer proxyPort) {
    return new S3Config(ACCESS_KEY, SECRET_KEY, ENDPOINT, REGION_NAME, BUCKET_NAME, disableSsl, proxyHost, proxyPort);
  }
}
