package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationReference;
import uk.co.nstauthority.offshoresafetydirective.file.FileAssociationService;
import uk.co.nstauthority.offshoresafetydirective.file.FileSummaryView;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileTestUtil;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailFileReference;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationFileDownloadController;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@ExtendWith(MockitoExtension.class)
class NomineeDetailSummaryServiceTest {

  private static final SummaryValidationBehaviour VALIDATION_BEHAVIOUR = SummaryValidationBehaviour.VALIDATED;

  @Mock
  private NomineeDetailPersistenceService nomineeDetailPersistenceService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Mock
  private NomineeDetailSubmissionService nomineeDetailSubmissionService;

  @Mock
  private FileAssociationService fileAssociationService;

  @InjectMocks
  private NomineeDetailSummaryService nomineeDetailSummaryService;

  @Test
  void getNomineeDetailSummaryView_whenNomineeDetail_andAllFieldsFilled_thenAssert() {

    Integer portalOrgId = 190;
    var portalOrgName = "Portal org";

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder()
        .withId(portalOrgId)
        .withName(portalOrgName)
        .build();

    var plannedStartDate = LocalDate.now();
    var reasonForNomination = "Reason for nomination";

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.SUBMITTED)
        .build();
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(portalOrgId)
        .withPlannedStartDate(plannedStartDate)
        .withReasonForNomination(reasonForNomination)
        .withOperatorHasAuthority(true)
        .withOperatorHasCapacity(true)
        .withLicenseeAcknowledgeOperatorRequirements(true)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(portalOrgId, NomineeDetailSummaryService.NOMINEE_ORGANISATION_PURPOSE))
        .thenReturn(Optional.of(portalOrgDto));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var firstUploadedFile = UploadedFileTestUtil.builder()
        .withFilename("file_a")
        .build();
    var firstUploadedFileViewByName = UploadedFileViewTestUtil.fromUploadedFile(firstUploadedFile);
    var secondUploadedFile = UploadedFileTestUtil.builder()
        .withFilename("file_B")
        .build();
    var secondUploadedFileViewByName = UploadedFileViewTestUtil.fromUploadedFile(secondUploadedFile);
    var thirdUploadedFile = UploadedFileTestUtil.builder()
        .withFilename("file_c")
        .build();
    var thirdUploadedFileViewByName = UploadedFileViewTestUtil.fromUploadedFile(thirdUploadedFile);

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);
    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    )).thenReturn(
        Map.of(
            NomineeDetailAppendixFileController.PURPOSE,
            List.of(secondUploadedFileViewByName, thirdUploadedFileViewByName, firstUploadedFileViewByName)
        ));

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::nominatedOrganisationUnitView)
        .extracting(
            view -> view.id().id(),
            view -> view.name().name()
        ).containsExactly(
            portalOrgId,
            portalOrgName
        );

    assertThat(result)
        .extracting(NomineeDetailSummaryView::appointmentPlannedStartDate)
        .extracting(AppointmentPlannedStartDate::plannedStartDate)
        .isEqualTo("%d %s %d".formatted(
            plannedStartDate.getDayOfMonth(),
            plannedStartDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
            plannedStartDate.getYear()
        ));

    assertThat(result)
        .extracting(NomineeDetailSummaryView::nominationReason)
        .extracting(NominationReason::reason)
        .isEqualTo(reasonForNomination);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::nomineeDetailConditionsAccepted)
        .extracting(NomineeDetailConditionsAccepted::accepted)
        .isEqualTo(true);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::appendixDocuments)
        .extracting(AppendixDocuments::documents)
        .asList()
        .containsExactly(
            new FileSummaryView(
                firstUploadedFileViewByName,
                ReverseRouter.route(on(NominationFileDownloadController.class).download(
                    new NominationId(nominationDetail.getNomination().getId()),
                    UploadedFileId.valueOf(firstUploadedFileViewByName.fileId())
                ))
            ),
            new FileSummaryView(
                secondUploadedFileViewByName,
                ReverseRouter.route(on(NominationFileDownloadController.class).download(
                    new NominationId(nominationDetail.getNomination().getId()),
                    UploadedFileId.valueOf(secondUploadedFileViewByName.fileId())
                ))
            ),
            new FileSummaryView(
                thirdUploadedFileViewByName,
                ReverseRouter.route(on(NominationFileDownloadController.class).download(
                    new NominationId(nominationDetail.getNomination().getId()),
                    UploadedFileId.valueOf(thirdUploadedFileViewByName.fileId())
                ))
            )
        );

    assertThat(result).hasNoNullFieldsOrPropertiesExcept("summarySectionError");

    assertThat(fileReferenceCaptor.getValue())
        .extracting(
            FileAssociationReference::getFileReferenceType,
            FileAssociationReference::getReferenceId
        ).containsExactly(
            new NominationDetailFileReference(nominationDetail).getFileReferenceType(),
            new NominationDetailFileReference(nominationDetail).getReferenceId()
        );
  }

  @Test
  void getNomineeDetailSummaryView_whenNominationStatusIsDraft_andHasFile_thenAssertUrl() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DRAFT)
        .build();

    Integer portalOrgId = 190;
    var portalOrgName = "Portal org";

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder()
        .withId(portalOrgId)
        .withName(portalOrgName)
        .build();

    var plannedStartDate = LocalDate.now();
    var reasonForNomination = "Reason for nomination";

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(portalOrgId)
        .withPlannedStartDate(plannedStartDate)
        .withReasonForNomination(reasonForNomination)
        .withOperatorHasAuthority(true)
        .withOperatorHasCapacity(true)
        .withLicenseeAcknowledgeOperatorRequirements(true)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(portalOrgId, NomineeDetailSummaryService.NOMINEE_ORGANISATION_PURPOSE))
        .thenReturn(Optional.of(portalOrgDto));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withFilename("file_a")
        .build();
    var uploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(uploadedFile);

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);

    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    )).thenReturn(
        Map.of(
            NomineeDetailAppendixFileController.PURPOSE,
            List.of(uploadedFileView)
        ));

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::appendixDocuments)
        .extracting(AppendixDocuments::documents)
        .asList()
        .containsExactly(
            new FileSummaryView(
                uploadedFileView,
                ReverseRouter.route(on(NomineeDetailAppendixFileController.class).download(
                    new NominationId(nominationDetail.getNomination().getId()),
                    NominationDetailId.fromNominationDetail(nominationDetail),
                    UploadedFileId.valueOf(uploadedFileView.fileId())
                ))
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"DELETED", "DRAFT"})
  void getNomineeDetailSummaryView_whenNominationStatusIsPostSubmission_andHasFile_thenAssertUrl(NominationStatus nominationStatus) {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(nominationStatus)
        .build();

    Integer portalOrgId = 190;
    var portalOrgName = "Portal org";

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder()
        .withId(portalOrgId)
        .withName(portalOrgName)
        .build();

    var plannedStartDate = LocalDate.now();
    var reasonForNomination = "Reason for nomination";

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(portalOrgId)
        .withPlannedStartDate(plannedStartDate)
        .withReasonForNomination(reasonForNomination)
        .withOperatorHasAuthority(true)
        .withOperatorHasCapacity(true)
        .withLicenseeAcknowledgeOperatorRequirements(true)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(portalOrgId, NomineeDetailSummaryService.NOMINEE_ORGANISATION_PURPOSE))
        .thenReturn(Optional.of(portalOrgDto));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withFilename("file_a")
        .build();
    var uploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(uploadedFile);

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);

    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    )).thenReturn(
        Map.of(
            NomineeDetailAppendixFileController.PURPOSE,
            List.of(uploadedFileView)
        ));

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::appendixDocuments)
        .extracting(AppendixDocuments::documents)
        .asList()
        .containsExactly(
            new FileSummaryView(
                uploadedFileView,
                ReverseRouter.route(on(NominationFileDownloadController.class).download(
                    new NominationId(nominationDetail.getNomination().getId()),
                    UploadedFileId.valueOf(uploadedFileView.fileId())
                ))
            )
        );
  }

  @Test
  void getNomineeDetailSummaryView_whenNominationStatusIsDeleted_thenException() {

    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(NominationStatus.DELETED)
        .build();

    Integer portalOrgId = 190;
    var portalOrgName = "Portal org";

    var portalOrgDto = PortalOrganisationDtoTestUtil.builder()
        .withId(portalOrgId)
        .withName(portalOrgName)
        .build();

    var plannedStartDate = LocalDate.now();
    var reasonForNomination = "Reason for nomination";

    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(portalOrgId)
        .withPlannedStartDate(plannedStartDate)
        .withReasonForNomination(reasonForNomination)
        .withOperatorHasAuthority(true)
        .withOperatorHasCapacity(true)
        .withLicenseeAcknowledgeOperatorRequirements(true)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(portalOrgId, NomineeDetailSummaryService.NOMINEE_ORGANISATION_PURPOSE))
        .thenReturn(Optional.of(portalOrgDto));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var uploadedFile = UploadedFileTestUtil.builder()
        .withFilename("file_a")
        .build();
    var uploadedFileView = UploadedFileViewTestUtil.fromUploadedFile(uploadedFile);

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);

    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    )).thenReturn(
        Map.of(
            NomineeDetailAppendixFileController.PURPOSE,
            List.of(uploadedFileView)
        ));

    assertThrowsExactly(IllegalStateException.class,
        () -> nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR));
  }

  @Test
  void getNomineeDetailSummaryView_whenNomineeDetail_andAllFieldsEmpty_thenAssert() {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .withNominatedOrganisationId(null)
        .withPlannedStartDate(null)
        .withReasonForNomination(null)
        .withOperatorHasAuthority(true)
        .withOperatorHasCapacity(true)
        .withLicenseeAcknowledgeOperatorRequirements(true)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::nominatedOrganisationUnitView)
        .extracting(
            NominatedOrganisationUnitView::id,
            NominatedOrganisationUnitView::name
        ).containsExactly(
            null,
            null
        );

    assertThat(result)
        .extracting(NomineeDetailSummaryView::appointmentPlannedStartDate)
        .isNull();

    assertThat(result)
        .extracting(NomineeDetailSummaryView::nominationReason)
        .isNull();
  }

  @ParameterizedTest
  @MethodSource("calculateAgreementConditionArguments")
  void getNomineeDetailSummaryView_whenNomineeDetail_assertAgreementConditions(boolean hasAuthority,
                                                                               boolean hasCapacity,
                                                                               boolean acknowledgedOperatorRequirements) {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .withOperatorHasAuthority(hasAuthority)
        .withOperatorHasCapacity(hasCapacity)
        .withLicenseeAcknowledgeOperatorRequirements(acknowledgedOperatorRequirements)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    // Ensure that the only time this can be true is when all three values are true.
    assertThat(result)
        .extracting(NomineeDetailSummaryView::nomineeDetailConditionsAccepted)
        .extracting(NomineeDetailConditionsAccepted::accepted)
        .isEqualTo(hasAuthority && hasCapacity && acknowledgedOperatorRequirements);
  }

  /**
   * Create arguments for AgreementConditions with a three column truth table
   * Example: [true, true, false] || [true, false, true] || etc
   *
   * @return Cartesian product of 3 groups of boolean states
   */
  private static Stream<Arguments> calculateAgreementConditionArguments() {
    return Sets.cartesianProduct(Set.of(true, false), Set.of(true, false), Set.of(true, false)).stream()
        .map(booleans -> Arguments.of(booleans.get(0), booleans.get(1), booleans.get(2)));
  }

  @Test
  void getNomineeDetailSummaryView_whenNoNomineeDetail_thenAssertEmpty() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.empty());

    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    var metadataFields = List.of("summarySectionError", "summarySectionDetails");
    var fields = List.of(
        "appointmentPlannedStartDate", "nomineeDetailConditionsAccepted", "nominationReason",
        "nominatedOrganisationUnitView", "appendixDocuments"
    );

    var allFields = new ArrayList<>(metadataFields);
    allFields.addAll(fields);

    assertThat(result)
        .hasOnlyFields(allFields.toArray(String[]::new))
        .extracting(fields.toArray(String[]::new)).containsExactly(
            null,
            null,
            null,
            new NominatedOrganisationUnitView(),
            null
        );
  }

  @Test
  void getNomineeDetailSummaryView_whenIsNotSubmittable_thenHasSummaryErrorMessage() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)).thenReturn(Optional.empty());
    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::summarySectionError)
        .isEqualTo(SummarySectionError.createWithDefaultMessage("nominee details"));
  }

  @Test
  void getNomineeDetailSummaryView_whenEmptyMapFileMap_thenNoAppendixDocuments() {

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var nomineeDetail = NomineeDetailTestingUtil.builder()
        .withNominationDetail(nominationDetail)
        .build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    var fileReferenceCaptor = ArgumentCaptor.forClass(FileAssociationReference.class);
    when(fileAssociationService.getSubmittedUploadedFileViewsForReferenceAndPurposes(
        fileReferenceCaptor.capture(),
        eq(List.of(NomineeDetailAppendixFileController.PURPOSE.purpose()))
    )).thenReturn(Map.of());

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::appendixDocuments)
        .isNull();

    assertThat(fileReferenceCaptor.getValue())
        .extracting(
            FileAssociationReference::getFileReferenceType,
            FileAssociationReference::getReferenceId
        ).containsExactly(
            new NominationDetailFileReference(nominationDetail).getFileReferenceType(),
            new NominationDetailFileReference(nominationDetail).getReferenceId()
        );
  }

  @Test
  void getNomineeDetailSummaryView_whenIsSubmittable_thenNoSummaryErrorMessage() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)).thenReturn(Optional.empty());
    when(nomineeDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(NomineeDetailSummaryView::summarySectionError)
        .isNull();
  }

  @ParameterizedTest
  @EnumSource(SummaryValidationBehaviour.class)
  void getNomineeDetailSummaryView_verifyValidationBehaviourInteractions(
      SummaryValidationBehaviour validationBehaviour
  ) {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nomineeDetailPersistenceService.getNomineeDetail(nominationDetail))
        .thenReturn(Optional.empty());

    nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, validationBehaviour);

    switch (validationBehaviour) {
      case VALIDATED -> verify(nomineeDetailSubmissionService).isSectionSubmittable(nominationDetail);
      case NOT_VALIDATED -> verify(nomineeDetailSubmissionService, never()).isSectionSubmittable(nominationDetail);
    }
  }

}