package uk.co.nstauthority.offshoresafetydirective.file;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision.NominationDecisionFileController;

@ExtendWith(MockitoExtension.class)
class FileAssociationServiceTest {

  private static final String PURPOSE = "purpose";

  @Mock
  private FileAssociationRepository fileAssociationRepository;

  @Mock
  private FileUploadService fileUploadService;

  @Mock
  private Clock clock;

  @InjectMocks
  private FileAssociationService fileAssociationService;

  @Test
  void createDraftDetail() {
    var nominationDetailId = UUID.randomUUID();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();
    var uploadedFile = UploadedFileTestUtil.builder().build();

    var uploadedInstant = Instant.now();
    when(clock.instant()).thenReturn(uploadedInstant);

    fileAssociationService.createDraftAssociation(
        uploadedFile,
        new TestFileAssociationReference(nominationDetail),
        NominationDecisionFileController.PURPOSE
    );

    var captor = ArgumentCaptor.forClass(FileAssociation.class);

    verify(fileAssociationRepository).save(captor.capture());

    assertThat(captor.getValue())
        .hasOnlyFields("uuid", "referenceType", "referenceId", "purpose", "fileStatus", "uploadedInstant",
            "uploadedFile")
        .extracting(
            FileAssociation::getReferenceType,
            FileAssociation::getReferenceId,
            FileAssociation::getPurpose,
            FileAssociation::getFileStatus,
            FileAssociation::getUploadedInstant,
            FileAssociation::getUploadedFile
        ).containsExactly(
            FileAssociationType.NOMINATION_DETAIL,
            String.valueOf(nominationDetailId),
            NominationDecisionFileController.PURPOSE,
            FileStatus.DRAFT,
            uploadedInstant,
            uploadedFile
        );
  }

  @Test
  void findFileAssociation() {
    var nominationDetailId = UUID.randomUUID();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();
    var uploadedFile = UploadedFileTestUtil.builder().build();
    var fileAssociation = FileAssociationTestUtil.builder().build();

    when(fileAssociationRepository.findByReferenceTypeAndReferenceIdAndUploadedFile_Id(
        FileAssociationType.NOMINATION_DETAIL,
        String.valueOf(nominationDetailId),
        uploadedFile.getId()
    ))
        .thenReturn(Optional.of(fileAssociation));

    var result = fileAssociationService.findFileAssociation(
        new TestFileAssociationReference(nominationDetail),
        new UploadedFileId(uploadedFile.getId())
    );

    assertThat(result).contains(fileAssociation);
  }

  @Test
  void getFileAssociationsByFileAssociationReference() {
    var nominationDetailId = UUID.randomUUID();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();
    var fileAssociation = FileAssociationTestUtil.builder().build();

    var nominationDetailIdAsString = String.valueOf(nominationDetailId);
    when(fileAssociationRepository.findAllByReferenceTypeAndReferenceIdIn(
        FileAssociationType.NOMINATION_DETAIL,
        List.of(nominationDetailIdAsString)
    ))
        .thenReturn(List.of(fileAssociation));

    var result = fileAssociationService.getFileAssociationByFileAssociationReference(
        new TestFileAssociationReference(nominationDetail)
    );

    assertThat(result).containsExactly(fileAssociation);
  }

  @Test
  void getFileAssociationsByFileReference_whenNoMatch_thenEmptyList() {
    var nominationDetailId = UUID.randomUUID();
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(nominationDetailId)
        .build();

    var nominationDetailIdString = String.valueOf(nominationDetailId);
    when(fileAssociationRepository.findAllByReferenceTypeAndReferenceIdIn(
        FileAssociationType.NOMINATION_DETAIL,
        List.of(nominationDetailIdString)
    ))
        .thenReturn(List.of());

    var result = fileAssociationService.getFileAssociationByFileAssociationReference(
        new TestFileAssociationReference(nominationDetail)
    );

    assertThat(result).isEmpty();
  }

  @Test
  void deleteDetail() {
    var fileAssociation = FileAssociationTestUtil.builder().build();
    fileAssociationService.deleteFileAssociation(fileAssociation);
    verify(fileAssociationRepository).delete(fileAssociation);
  }

  @Test
  void submitFiles_whenNoFilesToSubmit_verifyNoInteractions() {
    fileAssociationService.submitFiles(List.of());
    verifyNoInteractions(fileUploadService, fileAssociationRepository);
  }

  @Test
  void submitFiles_whenHasFilesToSubmit_verifyInteractions() {
    var fileUuid = UUID.randomUUID();
    var fileUploadForm = new FileUploadForm();
    fileUploadForm.setUploadedFileId(fileUuid);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withId(fileUuid)
        .build();
    var fileAssociation = FileAssociationTestUtil.builder()
        .withUploadedFile(uploadedFile)
        .build();

    when(fileAssociationRepository.findAllByUploadedFile_IdIn(List.of(fileUuid)))
        .thenReturn(List.of(fileAssociation));

    fileAssociationService.submitFiles(List.of(fileUploadForm));

    verify(fileUploadService).updateFileUploadDescriptions(List.of(fileUploadForm));
    verify(fileAssociationRepository).saveAll(List.of(fileAssociation));
    assertThat(fileAssociation.getFileStatus()).isEqualTo(FileStatus.SUBMITTED);
  }

  @Test
  void getUploadedFileAssociationDtosByReferenceTypeAndReferenceIds() {
    var nominationDetailId = 123;

    var firstUploadedFileByName = UploadedFileTestUtil.builder()
        .withFilename("file_a")
        .build();
    var firstFileAssociationByName = FileAssociationTestUtil.builder()
        .withUploadedFile(firstUploadedFileByName)
        .build();

    var secondUploadedFileByName = UploadedFileTestUtil.builder()
        .withFilename("file_B") // Ensure sort is not case-sensitive
        .build();
    var secondFileAssociationByName = FileAssociationTestUtil.builder()
        .withUploadedFile(secondUploadedFileByName)
        .build();

    var thirdUploadedFileByName = UploadedFileTestUtil.builder()
        .withFilename("file_c")
        .build();
    var thirdFileAssociationByName = FileAssociationTestUtil.builder()
        .withUploadedFile(thirdUploadedFileByName)
        .build();

    when(fileAssociationRepository.findAllByReferenceTypeAndReferenceIdIn(
        FileAssociationType.NOMINATION_DETAIL,
        List.of(String.valueOf(nominationDetailId))
    )).thenReturn(
        List.of(secondFileAssociationByName, thirdFileAssociationByName, firstFileAssociationByName)
    );

    var result = fileAssociationService.getUploadedFileAssociationDtosByReferenceTypeAndReferenceIds(
        FileAssociationType.NOMINATION_DETAIL,
        List.of(String.valueOf(nominationDetailId))
    );

    assertThat(result)
        .containsExactly(
            FileAssociationDto.from(firstFileAssociationByName),
            FileAssociationDto.from(secondFileAssociationByName),
            FileAssociationDto.from(thirdFileAssociationByName)
        );
  }

  @Test
  void getSubmittedUploadedFileAssociations() {
    var referenceIds = List.of("123");

    var fileAssociation = FileAssociationTestUtil.builder()
        .withUploadedFile(UploadedFileTestUtil.builder().build())
        .build();

    when(fileAssociationRepository
        .findAllByReferenceTypeAndFileStatusAndReferenceIdIn(FileAssociationType.APPOINTMENT, FileStatus.SUBMITTED, referenceIds))
        .thenReturn(List.of(fileAssociation));

    var result = fileAssociationService.getSubmittedUploadedFileAssociations(
        FileAssociationType.APPOINTMENT,
        referenceIds
    );

    assertThat(result).containsExactly(FileAssociationDto.from(fileAssociation));
  }

  @Test
  void getAllByFileReferenceAndUploadedFileIds() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nominationDetailReference = new TestFileAssociationReference(nominationDetail);
    var fileAssociation = FileAssociationTestUtil.builder().build();
    var uploadedFileId = new UploadedFileId(fileAssociation.getUploadedFile().getId());

    when(fileAssociationRepository.findAllByReferenceTypeAndReferenceIdAndUploadedFile_IdIn(
        FileAssociationType.NOMINATION_DETAIL,
        nominationDetailReference.getReferenceId(),
        List.of(uploadedFileId.uuid())
    )).thenReturn(List.of(fileAssociation));

    var result = fileAssociationService.getAllByFileReferenceAndUploadedFileIds(
        nominationDetailReference,
        List.of(uploadedFileId)
    );

    assertThat(result)
        .containsExactly(fileAssociation);
  }

  @Test
  void updateFileReferences() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileAssociationReference(nominationDetail);
    var fileAssociation = FileAssociationTestUtil.builder()
        .withReferenceType(FileAssociationType.CASE_EVENT)
        .withReferenceId(UUID.randomUUID().toString())
        .build();
    fileAssociationService.updateFileReferences(List.of(fileAssociation), fileReference);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<FileAssociation>> detailListCaptor = ArgumentCaptor.forClass(List.class);

    verify(fileAssociationRepository).saveAll(detailListCaptor.capture());

    assertThat(detailListCaptor.getValue())
        .extracting(
            FileAssociation::getReferenceId,
            FileAssociation::getReferenceType
        ).containsExactly(
            Tuple.tuple(fileReference.getReferenceId(), fileReference.getFileReferenceType())
        );
  }

  @Test
  void getSubmittedUploadedFileViewsForReferenceAndPurposes() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var fileReference = new TestFileAssociationReference(nominationDetail);
    var decisionFilePurpose = new FilePurpose("decision_purpose");
    var appendixFilePurpose = new FilePurpose("appendix_purpose");

    var decisionFile = UploadedFileTestUtil.builder().build();
    var decisionFileView = UploadedFileViewTestUtil.fromUploadedFile(decisionFile);
    var fileAssociationForDecision = FileAssociationTestUtil.builder()
        .withReferenceType(FileAssociationType.NOMINATION_DETAIL)
        .withPurpose(decisionFilePurpose)
        .withFileStatus(FileStatus.SUBMITTED)
        .withUploadedFile(decisionFile)
        .build();

    var appendixFile = UploadedFileTestUtil.builder().build();
    var appendixFileView = UploadedFileViewTestUtil.fromUploadedFile(appendixFile);
    var fileAssociationForAppendixDocument = FileAssociationTestUtil.builder()
        .withReferenceType(FileAssociationType.NOMINATION_DETAIL)
        .withPurpose(appendixFilePurpose)
        .withFileStatus(FileStatus.SUBMITTED)
        .withUploadedFile(appendixFile)
        .build();

    var draftFile = UploadedFileTestUtil.builder().build();
    var fileAssociationForDraftFile = FileAssociationTestUtil.builder()
        .withReferenceType(FileAssociationType.NOMINATION_DETAIL)
        .withPurpose(appendixFilePurpose)
        .withFileStatus(FileStatus.DRAFT)
        .withUploadedFile(draftFile)
        .build();

    when(fileAssociationRepository.findAllByReferenceTypeAndReferenceIdInAndPurposeIn(
        fileReference.getFileReferenceType(),
        List.of(fileReference.getReferenceId()),
        List.of(PURPOSE)
    )).thenReturn(List.of(
        fileAssociationForDecision,
        fileAssociationForAppendixDocument,
        fileAssociationForDraftFile
    ));

    when(fileUploadService.getUploadedFileViewList(List.of(
        new UploadedFileId(decisionFile.getId()),
        new UploadedFileId(appendixFile.getId())
    ))).thenReturn(List.of(decisionFileView, appendixFileView));

    var purposeAndFileViewMap = fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReference,
        List.of(PURPOSE)
    );

    assertThat(purposeAndFileViewMap).containsExactlyInAnyOrderEntriesOf(Map.of(
        decisionFilePurpose, List.of(decisionFileView),
        appendixFilePurpose, List.of(appendixFileView)
    ));
  }

  static class TestFileAssociationReference implements FileAssociationReference {

    private final NominationDetailId nominationDetailId;

    public TestFileAssociationReference(NominationDetail nominationDetail) {
      this.nominationDetailId = NominationDetailDto.fromNominationDetail(nominationDetail).nominationDetailId();
    }

    @Override
    public FileAssociationType getFileReferenceType() {
      return FileAssociationType.NOMINATION_DETAIL;
    }

    @Override
    public String getReferenceId() {
      return nominationDetailId.toString();
    }

  }
}