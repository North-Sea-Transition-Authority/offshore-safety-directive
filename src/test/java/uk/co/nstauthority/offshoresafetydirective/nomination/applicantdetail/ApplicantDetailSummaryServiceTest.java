package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailSummaryServiceTest {

  private static final SummaryValidationBehaviour VALIDATION_BEHAVIOUR = SummaryValidationBehaviour.VALIDATED;

  @Mock
  private ApplicantDetailSubmissionService applicantDetailSubmissionService;

  @Mock
  private ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Mock
  private PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @InjectMocks
  private ApplicantDetailSummaryService applicantDetailSummaryService;

  @Test
  void getApplicantDetailSummaryView_whenApplicantDetail_andAllFieldsFilled_thenAssert() {

    Integer portalOrgId = 190;
    var portalOrgName = "Portal org";
    var portalOrgDto = new PortalOrganisationDto(portalOrgId.toString(), portalOrgName);

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withApplicantReference("Applicant reference")
        .withPortalOrganisationId(portalOrgId)
        .build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(portalOrgId)).thenReturn(Optional.of(portalOrgDto));

    when(applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(ApplicantDetailSummaryView::applicantOrganisationUnitView)
        .extracting(
            view -> view.id().id(),
            view -> view.name().name()
        ).containsExactly(
            portalOrgId,
            portalOrgName
        );

    assertThat(result)
        .extracting(ApplicantDetailSummaryView::applicantReference)
        .extracting(ApplicantReference::reference)
        .isEqualTo(applicantDetail.getApplicantReference());

    assertThat(result).hasNoNullFieldsOrPropertiesExcept("summarySectionError");
  }

  @Test
  void getApplicantDetailSummaryView_whenApplicantDetail_andNoReference_thenAssert() {
    Integer portalOrgId = 190;
    var portalOrgName = "Portal org";
    var portalOrgDto = new PortalOrganisationDto(portalOrgId.toString(), portalOrgName);

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withNominationDetail(nominationDetail)
        .withApplicantReference(null)
        .withPortalOrganisationId(portalOrgId)
        .build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    when(portalOrganisationUnitQueryService.getOrganisationById(portalOrgId)).thenReturn(Optional.of(portalOrgDto));

    when(applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(ApplicantDetailSummaryView::applicantOrganisationUnitView)
        .extracting(
            view -> view.id().id(),
            view -> view.name().name()
        ).containsExactly(
            portalOrgId,
            portalOrgName
        );

    assertThat(result)
        .extracting(ApplicantDetailSummaryView::applicantReference)
        .isNull();

    assertThat(result.summarySectionError()).isNull();
  }

  @Test
  void getApplicantDetailSummaryView_whenNoApplicantDetail_thenAssertMatchesEmpty() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.empty());
    when(applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(true);

    var result = applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(
            ApplicantDetailSummaryView::applicantOrganisationUnitView,
            ApplicantDetailSummaryView::applicantReference,
            ApplicantDetailSummaryView::summarySectionError
        ).containsExactly(
            new ApplicantOrganisationUnitView(),
            null,
            null
        );
  }

  @Test
  void getApplicantDetailSummaryView_whenIsNotSubmittable_thenHasSummaryErrorMessage() {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.empty());
    when(applicantDetailSubmissionService.isSectionSubmittable(nominationDetail)).thenReturn(false);

    var result = applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    assertThat(result)
        .extracting(
            ApplicantDetailSummaryView::applicantOrganisationUnitView,
            ApplicantDetailSummaryView::applicantReference,
            ApplicantDetailSummaryView::summarySectionError
        ).containsExactly(
            new ApplicantOrganisationUnitView(),
            null,
            SummarySectionError.createWithDefaultMessage("applicant details")
        );
  }

  @ParameterizedTest
  @EnumSource(SummaryValidationBehaviour.class)
  void getApplicantDetailSummaryView_verifyValidationBehaviourInteractions(
      SummaryValidationBehaviour validationBehaviour
  ) {

    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.empty());

    applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, validationBehaviour);

    switch (validationBehaviour) {
      case VALIDATED -> verify(applicantDetailSubmissionService).isSectionSubmittable(nominationDetail);
      case NOT_VALIDATED -> verify(applicantDetailSubmissionService, never()).isSectionSubmittable(nominationDetail);
    }
  }

}