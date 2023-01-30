package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.VirtualFolder;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;

@ExtendWith(MockitoExtension.class)
class NominationDetailFileServiceTest {

  @Mock
  private NominationDetailFileRepository nominationDetailFileRepository;

  @Mock
  private FileUploadService fileUploadService;

  @InjectMocks
  private NominationDetailFileService nominationDetailFileService;

  @Test
  void createNominationDetailFile_assertResult() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder().build();

    nominationDetailFileService.createNominationDetailFile(nominationDetail, uploadedFile);

    var captor = ArgumentCaptor.forClass(NominationDetailFile.class);

    verify(nominationDetailFileRepository).save(captor.capture());

    assertThat(captor.getValue())
        .hasOnlyFields("uuid", "nominationDetail", "uploadedFile", "fileStatus")
        .extracting(
            NominationDetailFile::getNominationDetail,
            NominationDetailFile::getUploadedFile,
            NominationDetailFile::getFileStatus
        ).containsExactly(
            nominationDetail,
            uploadedFile,
            FileStatus.DRAFT
        );
  }

  @Test
  void getNominationDetailFilesForNomination_assertResult() {
    var nomination = NominationTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder().build();
    var nominationDetailFile = NominationDetailFileTestUtil.builder().build();

    when(nominationDetailFileRepository.findAllByUploadedFileAndNominationDetail_Nomination(
        uploadedFile, nomination))
        .thenReturn(List.of(nominationDetailFile));

    var result = nominationDetailFileService.getNominationDetailFileForNomination(nomination, uploadedFile);

    assertThat(result).containsExactly(nominationDetailFile);
  }

  @Test
  void getNominationDetailFileForNominationDetail_assertResult() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder().build();
    var nominationDetailFile = NominationDetailFileTestUtil.builder().build();

    when(nominationDetailFileRepository.findByUploadedFileAndNominationDetail(
        uploadedFile, nominationDetail))
        .thenReturn(Optional.of(nominationDetailFile));

    var result = nominationDetailFileService.getNominationDetailFileForNominationDetail(nominationDetail, uploadedFile);

    assertThat(result).contains(nominationDetailFile);
  }

  @Test
  void deleteNominationDetailFile_verifyCall() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var uploadedFile = UploadedFileTestUtil.builder().build();

    nominationDetailFileService.deleteNominationDetailFile(nominationDetail, uploadedFile);

    verify(nominationDetailFileRepository).deleteByUploadedFileAndNominationDetail(
        uploadedFile,
        nominationDetail
    );
  }

  @Test
  void submitAndCleanFiles_whenNoFiles_thenVerifyNoFurtherInteractions() {

    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(nominationDetailVersion)
        .build();

    nominationDetailFileService.submitAndCleanFiles(
        nominationDetail,
        List.of(),
        VirtualFolder.NOMINATION_DECISION
    );

    verifyNoMoreInteractions(nominationDetailFileRepository);
    verifyNoMoreInteractions(fileUploadService);
  }

  @Test
  void submitAndCleanFiles_whenFiles_thenVerifyCalls() {
    var fileUuid = UUID.randomUUID();
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);
    var nominationFileUuid = UUID.randomUUID();
    var nominationDetailVersion = 2;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withVersion(2)
        .build();

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();
    var nominationDetailFile = NominationDetailFileTestUtil.builder()
        .withUuid(nominationFileUuid)
        .withUploadedFile(uploadedFile)
        .build();

    when(nominationDetailFileRepository.findAllNominationDetailFilesByNominationAndStatusAndVirtualFolder(
        nominationDetail.getNomination(), nominationDetailVersion, FileStatus.DRAFT, VirtualFolder.NOMINATION_DECISION
    ))
        .thenReturn(List.of(nominationDetailFile));

    when(nominationDetailFileRepository.getOnlySingleReferencedNominationDetailFilesFromCollection(
        List.of(fileUuid)))
        .thenReturn(List.of(nominationDetailFile));

    when(nominationDetailFileRepository.findAllByNominationDetailAndUploadedFile_IdIn(nominationDetail,
        List.of(fileUuid)))
        .thenReturn(List.of(nominationDetailFile));

    nominationDetailFileService.submitAndCleanFiles(
        nominationDetail,
        List.of(fileUploadForm),
        VirtualFolder.NOMINATION_DECISION
    );

    verify(fileUploadService).updateFileUploadDescriptions(List.of(fileUploadForm));
    verify(nominationDetailFileRepository).deleteAll(List.of(nominationDetailFile));
    verify(fileUploadService).deleteFile(uploadedFile);
    verify(nominationDetailFileRepository).saveAll(List.of(nominationDetailFile));

    assertThat(nominationDetailFile.getFileStatus()).isEqualTo(FileStatus.SUBMITTED);
  }
}