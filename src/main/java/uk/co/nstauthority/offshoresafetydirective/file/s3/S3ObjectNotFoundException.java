package uk.co.nstauthority.offshoresafetydirective.file.s3;

public class S3ObjectNotFoundException extends RuntimeException {
  public S3ObjectNotFoundException(String bucketName, String s3Key) {
    super("Could not find file stored with bucket %s and key %s".formatted(bucketName, s3Key));
  }
}
