package uk.co.nstauthority.offshoresafetydirective.file.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class S3ClientService {

  private final AmazonS3 s3Client;
  private final S3Config s3Config;

  @Autowired
  public S3ClientService(AmazonS3 s3Client, S3Config s3Config) {
    this.s3Client = s3Client;
    this.s3Config = s3Config;
  }

  public String getBucketName() {
    return s3Config.bucketName();
  }

  boolean doesBucketExist(String bucketName) {
    return s3Client.doesBucketExistV2(bucketName);
  }

  boolean doesObjectExist(String bucketName, String s3Key) {
    return s3Client.doesObjectExist(bucketName, s3Key);
  }

  public void storeFile(String bucketName, String s3Key, InputStream fileInputStream, ObjectMetadata objectMetadata) {
    doesBucketExistOrThrow(bucketName);

    s3Client.putObject(bucketName, s3Key, fileInputStream, objectMetadata);
  }

  public InputStream fetchFile(String bucketName, String s3Key) {
    doesObjectExistOrThrow(bucketName, s3Key);

    return s3Client.getObject(bucketName, s3Key).getObjectContent();
  }

  public void deleteFile(String bucketName, String s3Key) {
    doesObjectExistOrThrow(bucketName, s3Key);

    try {
      s3Client.deleteObject(bucketName, s3Key);
    } catch (SdkClientException e) {
      throw new S3DeleteFileFailedException(bucketName, s3Key, e);
    }
  }

  private void doesBucketExistOrThrow(String bucketName) {
    if (!doesBucketExist(bucketName)) {
      throw new S3BucketNotFoundException(bucketName);
    }
  }

  private void doesObjectExistOrThrow(String bucketName, String s3Key) {
    if (!doesObjectExist(bucketName, s3Key)) {
      throw new S3ObjectNotFoundException(bucketName, s3Key);
    }
  }

}