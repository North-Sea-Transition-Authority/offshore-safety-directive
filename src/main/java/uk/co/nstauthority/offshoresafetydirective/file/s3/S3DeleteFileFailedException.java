package uk.co.nstauthority.offshoresafetydirective.file.s3;

import com.amazonaws.SdkClientException;

public class S3DeleteFileFailedException extends RuntimeException {
  public S3DeleteFileFailedException(String bucketName, String s3Key, SdkClientException e) {
    super("Failed to delete file stored with bucket %s and key %s".formatted(bucketName, s3Key), e);
  }
}
