package uk.co.nstauthority.offshoresafetydirective.file.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3ClientServiceTest {
  private static final String S3_KEY = "TEST-KEY";
  @Mock
  private AmazonS3 amazonS3Mock;
  private S3ClientService s3ClientService;

  @BeforeEach
  void setUp() {
    var s3Config = S3ConfigTestUtil.getDefaultS3Config();
    s3ClientService = new S3ClientService(amazonS3Mock, s3Config);
  }

  @Test
  void getBucketName() {
    assertThat(s3ClientService.getBucketName()).isEqualTo(
        S3ConfigTestUtil.BUCKET_NAME);
  }

  @Test
  void doesBucketExist_exists() {
    when(amazonS3Mock.doesBucketExistV2(S3ConfigTestUtil.BUCKET_NAME)).thenReturn(true);
    assertThat(s3ClientService.doesBucketExist(S3ConfigTestUtil.BUCKET_NAME)).isTrue();
  }

  @Test
  void doesBucketExist_doesntExist() {
    when(amazonS3Mock.doesBucketExistV2(S3ConfigTestUtil.BUCKET_NAME)).thenReturn(false);
    assertThat(s3ClientService.doesBucketExist(S3ConfigTestUtil.BUCKET_NAME)).isFalse();
  }

  @Test
  void doesObjectExist_exists() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(true);
    assertThat(s3ClientService.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).isTrue();
  }

  @Test
  void doesObjectExist_doesntExist() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(false);
    assertThat(s3ClientService.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).isFalse();
  }

  @Test
  void storeFile_VerifyInteractions() {
    when(amazonS3Mock.doesBucketExistV2(S3ConfigTestUtil.BUCKET_NAME)).thenReturn(true);
    var inputStream = mock(InputStream.class);
    ObjectMetadata objectMetadata = mock(ObjectMetadata.class);

    s3ClientService.storeFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY, inputStream, objectMetadata);

    verify(amazonS3Mock).putObject(S3ConfigTestUtil.BUCKET_NAME, S3_KEY, inputStream, objectMetadata);
  }

  @Test
  void storeFile_bucketDoesntExist_VerifyInteractions() {
    when(amazonS3Mock.doesBucketExistV2(S3ConfigTestUtil.BUCKET_NAME)).thenReturn(false);

    var inputStream = mock(InputStream.class);
    var objectMetadata = mock(ObjectMetadata.class);

    assertThatThrownBy(() -> s3ClientService.storeFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY, inputStream, objectMetadata))
        .isInstanceOf(S3BucketNotFoundException.class)
        .hasMessage("Could not find bucket " + S3ConfigTestUtil.BUCKET_NAME);

    verify(amazonS3Mock, never()).putObject(any(), any(), any(), any());
  }

  @Test
  void fetchFile_FileFound() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(true);

    var inputStream = mock(S3ObjectInputStream.class);
    var s3Object = mock(S3Object.class);
    when(s3Object.getObjectContent()).thenReturn(inputStream);
    when(amazonS3Mock.getObject(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(s3Object);

    var file = s3ClientService.fetchFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY);

    assertThat(file).isEqualTo(inputStream);
  }

  @Test
  void fetchFile_FileNotFound() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(false);

    assertThatExceptionOfType(S3ObjectNotFoundException.class).isThrownBy(() ->
        s3ClientService.fetchFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)
    );
  }

  @Test
  void deleteFile_FileFound() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(true);

    s3ClientService.deleteFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY);
    verify(amazonS3Mock).deleteObject(S3ConfigTestUtil.BUCKET_NAME, S3_KEY);
  }

  @Test
  void deleteFile_FileNotFound() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(false);

    assertThatExceptionOfType(S3ObjectNotFoundException.class).isThrownBy(() ->
        s3ClientService.deleteFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)
    );
  }

  @Test
  void deleteFile_OperationFailed() {
    when(amazonS3Mock.doesObjectExist(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)).thenReturn(true);
    doThrow(SdkClientException.class).when(amazonS3Mock).deleteObject(S3ConfigTestUtil.BUCKET_NAME, S3_KEY);

    assertThatExceptionOfType(S3DeleteFileFailedException.class).isThrownBy(() ->
        s3ClientService.deleteFile(S3ConfigTestUtil.BUCKET_NAME, S3_KEY)
    );
  }
}