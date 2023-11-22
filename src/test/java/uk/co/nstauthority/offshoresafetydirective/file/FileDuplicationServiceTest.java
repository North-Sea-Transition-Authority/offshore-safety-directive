package uk.co.nstauthority.offshoresafetydirective.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class FileDuplicationServiceTest {

  @Mock
  private OldUploadedFileRepository uploadedFileRepository;

  @Mock
  private FileAssociationService fileAssociationService;

  @Mock
  private FileAssociationRepository fileAssociationRepository;

  @InjectMocks
  private FileDuplicationService fileDuplicationService;

  @Test
  void duplicateFiles() {
    var sourceNominationDetailId = UUID.randomUUID();
    var targetNominationDetailId = UUID.randomUUID();
    var sourceNominationDetail = NominationDetailTestUtil.builder()
        .withId(sourceNominationDetailId)
        .build();
    var targetNominationDetail = NominationDetailTestUtil.builder()
        .withId(targetNominationDetailId)
        .build();

    var uploadedFileA = UploadedFileTestUtil.builder()
        .withId(UUID.randomUUID())
        .withS3Key(UUID.randomUUID().toString())
        .withDescription("file a")
        .withBucketName("bucket/a")
        .withFileContentType("typeA")
        .withFileSizeBytes(100L)
        .withVirtualFolder(VirtualFolder.NOMINATION_DECISION)
        .withUploadedTimeStamp(Instant.now())
        .withFilename("filename_a.txt")
        .build();
    var uploadedFileB = UploadedFileTestUtil.builder()
        .withId(UUID.randomUUID())
        .withS3Key(UUID.randomUUID().toString())
        .withDescription("file b")
        .withBucketName("bucket/b")
        .withFileContentType("typeB")
        .withFileSizeBytes(200L)
        .withVirtualFolder(VirtualFolder.CASE_NOTES)
        .withUploadedTimeStamp(Instant.now())
        .withFilename("filename_b.txt")
        .build();
    var fileAssociationA = FileAssociationTestUtil.builder()
        .withUploadedFile(uploadedFileA)
        .withReferenceType(FileAssociationType.NOMINATION_DETAIL)
        .withReferenceId(String.valueOf(sourceNominationDetailId))
        .build();
    var fileAssociationB = FileAssociationTestUtil.builder()
        .withUploadedFile(uploadedFileB)
        .withReferenceType(FileAssociationType.NOMINATION_DETAIL)
        .withReferenceId(String.valueOf(sourceNominationDetailId))
        .build();

    when(fileAssociationService.getFileAssociationByFileAssociationReference(any(NominationDetailFileReference.class)))
        .thenReturn(List.of(fileAssociationA, fileAssociationB));

    fileDuplicationService.duplicateFiles(sourceNominationDetail, targetNominationDetail);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<OldUploadedFile>> uploadedFileCaptor = ArgumentCaptor.forClass(List.class);
    verify(uploadedFileRepository).saveAll(uploadedFileCaptor.capture());

    var duplicateUploadedFileA = uploadedFileCaptor.getValue()
        .stream()
        .filter(uploadedFile -> uploadedFile.getFileKey().equals(uploadedFileA.getFileKey()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("File [A] was not duplicated with a matching file key"));

    PropertyObjectAssert.thenAssertThat(duplicateUploadedFileA)
        .hasFieldOrPropertyWithValue("fileKey", uploadedFileA.getFileKey())
        .hasFieldOrPropertyWithValue("bucketName", uploadedFileA.getBucketName())
        .hasFieldOrPropertyWithValue("virtualFolder", uploadedFileA.getVirtualFolder())
        .hasFieldOrPropertyWithValue("filename", uploadedFileA.getFilename())
        .hasFieldOrPropertyWithValue("fileContentType", uploadedFileA.getFileContentType())
        .hasFieldOrPropertyWithValue("fileSizeBytes", uploadedFileA.getFileSizeBytes())
        .hasFieldOrPropertyWithValue("uploadedTimeStamp", uploadedFileA.getUploadedTimeStamp())
        .hasFieldOrPropertyWithValue("description", uploadedFileA.getDescription())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(duplicateUploadedFileA)
        .extracting(OldUploadedFile::getId)
        .isNotEqualTo(uploadedFileA.getId());

    var duplicateUploadedFileB = uploadedFileCaptor.getValue()
        .stream()
        .filter(uploadedFile -> uploadedFile.getFileKey().equals(uploadedFileB.getFileKey()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("File [B] was not duplicated with a matching file key"));

    PropertyObjectAssert.thenAssertThat(duplicateUploadedFileB)
        .hasFieldOrPropertyWithValue("fileKey", uploadedFileB.getFileKey())
        .hasFieldOrPropertyWithValue("bucketName", uploadedFileB.getBucketName())
        .hasFieldOrPropertyWithValue("virtualFolder", uploadedFileB.getVirtualFolder())
        .hasFieldOrPropertyWithValue("filename", uploadedFileB.getFilename())
        .hasFieldOrPropertyWithValue("fileContentType", uploadedFileB.getFileContentType())
        .hasFieldOrPropertyWithValue("fileSizeBytes", uploadedFileB.getFileSizeBytes())
        .hasFieldOrPropertyWithValue("uploadedTimeStamp", uploadedFileB.getUploadedTimeStamp())
        .hasFieldOrPropertyWithValue("description", uploadedFileB.getDescription())
        .hasAssertedAllPropertiesExcept("id");

    assertThat(duplicateUploadedFileB)
        .extracting(OldUploadedFile::getId)
        .isNotEqualTo(uploadedFileB.getId());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<FileAssociation>> fileAssociationCaptor = ArgumentCaptor.forClass(List.class);
    verify(fileAssociationRepository).saveAll(fileAssociationCaptor.capture());

    var duplicateFileAssociationA = fileAssociationCaptor.getValue()
        .stream()
        .filter(fileAssociation -> fileAssociation.getUploadedFile().equals(duplicateUploadedFileA))
        .findFirst()
        .orElseThrow(() ->
            new IllegalStateException("FileAssociation [A] was not duplicated with link to duplicate file"));

    PropertyObjectAssert.thenAssertThat(duplicateFileAssociationA)
        .hasFieldOrPropertyWithValue("uploadedFile", duplicateUploadedFileA)
        .hasFieldOrPropertyWithValue("fileStatus", fileAssociationA.getFileStatus())
        .hasFieldOrPropertyWithValue("referenceType", fileAssociationA.getReferenceType())
        .hasFieldOrPropertyWithValue("referenceId", String.valueOf(targetNominationDetailId))
        .hasFieldOrPropertyWithValue("purpose", fileAssociationA.getPurpose())
        .hasFieldOrPropertyWithValue("uploadedInstant", fileAssociationA.getUploadedInstant())
        .hasAssertedAllPropertiesExcept("uuid");

    assertThat(duplicateFileAssociationA)
        .extracting(FileAssociation::getUuid)
        .isNotEqualTo(fileAssociationA.getUuid());

    var duplicateFileAssociationB = fileAssociationCaptor.getValue()
        .stream()
        .filter(fileAssociation -> fileAssociation.getUploadedFile().equals(duplicateUploadedFileB))
        .findFirst()
        .orElseThrow(() ->
            new IllegalStateException("FileAssociation [B] was not duplicated with link to duplicate file"));

    PropertyObjectAssert.thenAssertThat(duplicateFileAssociationB)
        .hasFieldOrPropertyWithValue("uploadedFile", duplicateUploadedFileB)
        .hasFieldOrPropertyWithValue("fileStatus", fileAssociationB.getFileStatus())
        .hasFieldOrPropertyWithValue("referenceType", fileAssociationB.getReferenceType())
        .hasFieldOrPropertyWithValue("referenceId", String.valueOf(targetNominationDetailId))
        .hasFieldOrPropertyWithValue("purpose", fileAssociationB.getPurpose())
        .hasFieldOrPropertyWithValue("uploadedInstant", fileAssociationB.getUploadedInstant())
        .hasAssertedAllPropertiesExcept("uuid");

    assertThat(duplicateFileAssociationB)
        .extracting(FileAssociation::getUuid)
        .isNotEqualTo(fileAssociationB.getUuid());
  }
}