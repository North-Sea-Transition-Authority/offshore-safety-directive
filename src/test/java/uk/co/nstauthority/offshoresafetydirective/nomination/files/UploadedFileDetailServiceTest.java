package uk.co.nstauthority.offshoresafetydirective.nomination.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionFileController;
import uk.co.nstauthority.offshoresafetydirective.nomination.files.reference.FileReference;

@ExtendWith(MockitoExtension.class)
class UploadedFileDetailServiceTest {

  private static final String PURPOSE = "purpose";

  @Mock
  private UploadedFileDetailRepository uploadedFileDetailRepository;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private Clock clock;

  @InjectMocks
  private UploadedFileDetailService uploadedFileDetailService;

  @Test
  void createDraftDetail() {
    var nominationDetailId = 123;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();
    var uploadedFile = UploadedFileTestUtil.builder().build();

    var uploadedInstant = Instant.now();
    when(clock.instant()).thenReturn(uploadedInstant);

    uploadedFileDetailService.createDraftDetail(
        uploadedFile,
        new TestFileReference(nominationDetail),
        NominationDecisionFileController.PURPOSE
    );

    var captor = ArgumentCaptor.forClass(UploadedFileDetail.class);

    verify(uploadedFileDetailRepository).save(captor.capture());

    assertThat(captor.getValue())
        .hasOnlyFields("uuid", "referenceType", "referenceId", "purpose", "fileStatus", "uploadedInstant",
            "uploadedFile")
        .extracting(
            UploadedFileDetail::getReferenceType,
            UploadedFileDetail::getReferenceId,
            UploadedFileDetail::getPurpose,
            UploadedFileDetail::getFileStatus,
            UploadedFileDetail::getUploadedInstant,
            UploadedFileDetail::getUploadedFile
        ).containsExactly(
            FileReferenceType.NOMINATION_DETAIL,
            String.valueOf(nominationDetailId),
            NominationDecisionFileController.PURPOSE,
            FileStatus.DRAFT,
            uploadedInstant,
            uploadedFile
        );
  }

  @Test
  void findUploadedFileDetail() {
    var nominationDetailId = 123;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();
    var uploadedFile = UploadedFileTestUtil.builder().build();
    var uploadedFileDetail = UploadedFileDetailTestUtil.builder().build();

    when(uploadedFileDetailRepository.findByReferenceTypeAndReferenceIdAndUploadedFile_Id(
        FileReferenceType.NOMINATION_DETAIL,
        String.valueOf(nominationDetailId),
        uploadedFile.getId()
    ))
        .thenReturn(Optional.of(uploadedFileDetail));

    var result = uploadedFileDetailService.findUploadedFileDetail(
        new TestFileReference(nominationDetail),
        new UploadedFileId(uploadedFile.getId())
    );

    assertThat(result).contains(uploadedFileDetail);
  }

  @Test
  void getUploadedFileDetailsByFileReference() {
    var nominationDetailId = 123;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();
    var uploadedFileDetail = UploadedFileDetailTestUtil.builder().build();

    var nominationDetailIdAsString = String.valueOf(nominationDetailId);
    when(uploadedFileDetailRepository.findAllByReferenceTypeAndReferenceIdIn(
        FileReferenceType.NOMINATION_DETAIL,
        List.of(nominationDetailIdAsString)
    ))
        .thenReturn(List.of(uploadedFileDetail));

    var result = uploadedFileDetailService.getUploadedFileDetailsByFileReference(
        new TestFileReference(nominationDetail)
    );

    assertThat(result).containsExactly(uploadedFileDetail);
  }

  @Test
  void getUploadedFileDetailsByFileReference_whenNoMatch_thenEmptyList() {
    var nominationDetailId = 123;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();

    var nominationDetailIdString = String.valueOf(nominationDetailId);
    when(uploadedFileDetailRepository.findAllByReferenceTypeAndReferenceIdIn(
        FileReferenceType.NOMINATION_DETAIL,
        List.of(nominationDetailIdString)
    ))
        .thenReturn(List.of());

    var result = uploadedFileDetailService.getUploadedFileDetailsByFileReference(
        new TestFileReference(nominationDetail)
    );

    assertThat(result).isEmpty();
  }

  @Test
  void deleteDetail() {
    var uploadedFileDetail = UploadedFileDetailTestUtil.builder().build();
    uploadedFileDetailService.deleteDetail(uploadedFileDetail);
    verify(uploadedFileDetailRepository).delete(uploadedFileDetail);
  }

  @Test
  void submitFiles_whenNoFilesToSubmit_verifyNoInteractions() {
    uploadedFileDetailService.submitFiles(List.of());
    verifyNoInteractions(fileUploadService, uploadedFileDetailRepository);
  }

  @Test
  void submitFiles_whenHasFilesToSubmit_verifyInteractions() {
    var fileUuid = UUID.randomUUID();
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();
    var uploadedFileDetail = UploadedFileDetailTestUtil.builder()
        .withUploadedFile(uploadedFile)
        .build();

    when(uploadedFileDetailRepository.findAllByUploadedFile_IdIn(List.of(fileUuid)))
        .thenReturn(List.of(uploadedFileDetail));

    uploadedFileDetailService.submitFiles(List.of(fileUploadForm));

    verify(fileUploadService).updateFileUploadDescriptions(List.of(fileUploadForm));
    verify(uploadedFileDetailRepository).saveAll(List.of(uploadedFileDetail));
    assertThat(uploadedFileDetail.getFileStatus()).isEqualTo(FileStatus.SUBMITTED);
  }

  @Test
  void getUploadedFileDetailViewsByReferenceTypeAndReferenceIds() {
    var nominationDetailId = 123;

    var firstUploadedFileByName = UploadedFileTestUtil.builder()
        .withFilename("file_a")
        .build();
    var firstUploadedFileDetailByName = UploadedFileDetailTestUtil.builder()
        .withUploadedFile(firstUploadedFileByName)
        .build();

    var secondUploadedFileByName = UploadedFileTestUtil.builder()
        .withFilename("file_B") // Ensure sort is not case-sensitive
        .build();
    var secondUploadedFileDetailByName = UploadedFileDetailTestUtil.builder()
        .withUploadedFile(secondUploadedFileByName)
        .build();

    var thirdUploadedFileByName = UploadedFileTestUtil.builder()
        .withFilename("file_c")
        .build();
    var thirdUploadedFileDetailByName = UploadedFileDetailTestUtil.builder()
        .withUploadedFile(thirdUploadedFileByName)
        .build();

    when(uploadedFileDetailRepository.findAllByReferenceTypeAndReferenceIdIn(
        FileReferenceType.NOMINATION_DETAIL,
        List.of(String.valueOf(nominationDetailId))
    )).thenReturn(
        List.of(secondUploadedFileDetailByName, thirdUploadedFileDetailByName, firstUploadedFileDetailByName)
    );

    var result = uploadedFileDetailService.getUploadedFileDetailViewsByReferenceTypeAndReferenceIds(
        FileReferenceType.NOMINATION_DETAIL,
        List.of(String.valueOf(nominationDetailId))
    );

    assertThat(result)
        .containsExactly(
            UploadedFileDetailView.from(firstUploadedFileDetailByName),
            UploadedFileDetailView.from(secondUploadedFileDetailByName),
            UploadedFileDetailView.from(thirdUploadedFileDetailByName)
        );
  }

  @Test
  void getAllByFileReferenceAndUploadedFileIds() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailReference = new TestFileReference(nominationDetail);
    var uploadedFileDetail = UploadedFileDetailTestUtil.builder().build();
    var uploadedFileId = new UploadedFileId(uploadedFileDetail.getUploadedFile().getId());

    when(uploadedFileDetailRepository.findAllByReferenceTypeAndReferenceIdAndUploadedFile_IdIn(
        FileReferenceType.NOMINATION_DETAIL,
        nominationDetailReference.getReferenceId(),
        List.of(uploadedFileId.uuid())
    )).thenReturn(List.of(uploadedFileDetail));

    var result = uploadedFileDetailService.getAllByFileReferenceAndUploadedFileIds(
        nominationDetailReference,
        List.of(uploadedFileId)
    );

    assertThat(result)
        .containsExactly(uploadedFileDetail);
  }

  @Test
  void updateFileReferences() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileReference(nominationDetail);
    var uploadedFileDetail = UploadedFileDetailTestUtil.builder()
        .withReferenceType(FileReferenceType.CASE_EVENT)
        .withReferenceId(UUID.randomUUID().toString())
        .build();
    uploadedFileDetailService.updateFileReferences(List.of(uploadedFileDetail), fileReference);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<UploadedFileDetail>> detailListCaptor = ArgumentCaptor.forClass(List.class);

    verify(uploadedFileDetailRepository).saveAll(detailListCaptor.capture());

    assertThat(detailListCaptor.getValue())
        .extracting(
            UploadedFileDetail::getReferenceId,
            UploadedFileDetail::getReferenceType
        ).containsExactly(
            Tuple.tuple(fileReference.getReferenceId(), fileReference.getFileReferenceType())
        );
  }

  @Test
  void getSubmittedUploadedFileViewsForReferenceAndPurposes() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileReference(nominationDetail);
    var decisionFilePurpose = new FilePurpose("decision_purpose");
    var appendixFilePurpose = new FilePurpose("appendix_purpose");

    var decisionFile = UploadedFileTestUtil.builder().build();
    var decisionFileView = UploadedFileViewTestUtil.fromUploadedFile(decisionFile);
    var uploadedFileDetailForDecision = UploadedFileDetailTestUtil.builder()
        .withReferenceType(FileReferenceType.NOMINATION_DETAIL)
        .withPurpose(decisionFilePurpose)
        .withFileStatus(FileStatus.SUBMITTED)
        .withUploadedFile(decisionFile)
        .build();

    var appendixFile = UploadedFileTestUtil.builder().build();
    var appendixFileView = UploadedFileViewTestUtil.fromUploadedFile(appendixFile);
    var uploadedFileDetailForAppendixDocument = UploadedFileDetailTestUtil.builder()
        .withReferenceType(FileReferenceType.NOMINATION_DETAIL)
        .withPurpose(appendixFilePurpose)
        .withFileStatus(FileStatus.SUBMITTED)
        .withUploadedFile(appendixFile)
        .build();

    var draftFile = UploadedFileTestUtil.builder().build();
    var uploadedFileDetailForDraftFile = UploadedFileDetailTestUtil.builder()
        .withReferenceType(FileReferenceType.NOMINATION_DETAIL)
        .withPurpose(appendixFilePurpose)
        .withFileStatus(FileStatus.DRAFT)
        .withUploadedFile(draftFile)
        .build();

    when(uploadedFileDetailRepository.findAllByReferenceTypeAndReferenceIdInAndPurposeIn(
        fileReference.getFileReferenceType(),
        List.of(fileReference.getReferenceId()),
        List.of(PURPOSE)
    )).thenReturn(List.of(
        uploadedFileDetailForDecision,
        uploadedFileDetailForAppendixDocument,
        uploadedFileDetailForDraftFile
    ));

    when(fileUploadService.getUploadedFileViewList(List.of(
        new UploadedFileId(decisionFile.getId()),
        new UploadedFileId(appendixFile.getId())
    ))).thenReturn(List.of(decisionFileView, appendixFileView));

    var purposeAndFileViewMap = uploadedFileDetailService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReference,
        List.of(PURPOSE)
    );

    assertThat(purposeAndFileViewMap).containsExactlyInAnyOrderEntriesOf(Map.of(
        decisionFilePurpose, List.of(decisionFileView),
        appendixFilePurpose, List.of(appendixFileView)
    ));
  }

  static class TestFileReference implements FileReference {

    private final NominationDetailId nominationDetailId;

    public TestFileReference(NominationDetail nominationDetail) {
      this.nominationDetailId = NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId();
    }

    @Override
    public FileReferenceType getFileReferenceType() {
      return FileReferenceType.NOMINATION_DETAIL;
    }

    @Override
    public String getReferenceId() {
      return nominationDetailId.toString();
    }

  }
}