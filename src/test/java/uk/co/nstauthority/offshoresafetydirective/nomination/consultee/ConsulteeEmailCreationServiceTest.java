package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NominatedOrganisationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.notify.EmailUrlGenerationService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmail;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailBuilderService;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyEmailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.notify.NotifyTemplate;

@ExtendWith(MockitoExtension.class)
class ConsulteeEmailCreationServiceTest {

  private static final ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties
      = NotifyEmailTestUtil.serviceBrandingConfigurationProperties;

  private static final String CONSULTATION_VIEW_URL = "consultation-view-url";

  private static final String RECIPIENT_NAME = "recipientName";

  private static final NominationId NOMINATION_ID = new NominationId(100);

  private static String consultationViewUrl;

  private static NominationDetail nominationDetail;

  private NotifyEmailBuilderService notifyEmailBuilderService;

  private EmailUrlGenerationService emailUrlGenerationService;

  private ApplicantDetailAccessService applicantDetailAccessService;

  private NomineeDetailAccessService nomineeDetailAccessService;

  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  private WellSelectionSetupAccessService wellSelectionSetupAccessService;

  private InstallationInclusionAccessService installationInclusionAccessService;

  private NominationDetailService nominationDetailService;

  private ConsulteeEmailCreationService consulteeEmailCreationService;

  @BeforeAll
  static void beforeAll() {

    consultationViewUrl = ReverseRouter.route(on(NominationConsulteeViewController.class)
        .renderNominationView(NOMINATION_ID));

    nominationDetail = NominationDetailTestUtil.builder()
        .withNominationId(NOMINATION_ID)
        .build();
  }

  @BeforeEach
  void setup() {

    emailUrlGenerationService = mock(EmailUrlGenerationService.class);
    applicantDetailAccessService = mock(ApplicantDetailAccessService.class);
    nomineeDetailAccessService = mock(NomineeDetailAccessService.class);
    organisationUnitQueryService = mock(PortalOrganisationUnitQueryService.class);
    wellSelectionSetupAccessService = mock(WellSelectionSetupAccessService.class);
    installationInclusionAccessService = mock(InstallationInclusionAccessService.class);
    nominationDetailService = mock(NominationDetailService.class);

    NotifyEmailBuilderService notifyEmailBuilderService = new NotifyEmailBuilderService(
        serviceBrandingConfigurationProperties
    );

    consulteeEmailCreationService = new ConsulteeEmailCreationService(
        notifyEmailBuilderService, emailUrlGenerationService, applicantDetailAccessService, nomineeDetailAccessService,
        organisationUnitQueryService, wellSelectionSetupAccessService, installationInclusionAccessService,
        nominationDetailService
    );
  }

  @Test
  void constructConsultationRequestEmail_whenValidNomination_thenConstructEmail() {

    givenAValidNominationDetail();

    var applicant = PortalOrganisationDtoTestUtil.builder()
        .withId(1)
        .withName("applicant organisation name")
        .build();

    var applicantDetail = ApplicantDetailDtoTestUtil.builder()
        .withApplicantOrganisationId(new ApplicantOrganisationId(applicant.id()))
        .build();

    when(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    var nominee = PortalOrganisationDtoTestUtil.builder()
        .withId(2)
        .withName("nominated organisation name")
        .build();

    var nomineeDetail = new NomineeDetailDto(new NominatedOrganisationId(nominee.id()));

    when(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
        .thenReturn(Optional.of(nomineeDetail));

    when(organisationUnitQueryService.getOrganisationByIds(
        List.of(new PortalOrganisationUnitId(nominee.id()), new PortalOrganisationUnitId(applicant.id())))
    )
        .thenReturn(List.of(nominee, applicant));

    var installationsIncluded = InstallationInclusionTestUtil.builder()
        .includeInstallationsInNomination(true)
        .build();

    when(installationInclusionAccessService.getInstallationInclusion(nominationDetail))
        .thenReturn(Optional.of(installationsIncluded));

    when(wellSelectionSetupAccessService.getWellSelectionType(nominationDetail))
        .thenReturn(Optional.of(WellSelectionType.NO_WELLS));

    var resultingNotifyEmail = consulteeEmailCreationService
        .constructDefaultConsultationRequestEmail(NOMINATION_ID)
        .build();

    assertThat(resultingNotifyEmail.getPersonalisations())
        .contains(
            entry("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference()),
            entry("NOMINATION_LINK", CONSULTATION_VIEW_URL),
            entry("APPLICANT_ORGANISATION", applicant.name()),
            entry("NOMINATED_ORGANISATION", nominee.name()),
            entry("OPERATORSHIP_DISPLAY_TYPE", "an installation operator")
        );

    assertThat(resultingNotifyEmail)
        .extracting(NotifyEmail::getTemplate)
        .isEqualTo(NotifyTemplate.CONSULTATION_REQUESTED);
  }

//  @Test
//  void constructConsultationRequestEmail_whenNoNomination_thenThrowException() {
//    given(nominationService.getNomination(NOMINATION_ID))
//        .willReturn(Optional.empty());
//
//    assertThatExceptionOfType(IllegalStateException.class)
//        .isThrownBy(() -> consulteeEmailCreationService
//            .constructDefaultConsultationRequestEmail(NOMINATION_ID, RECIPIENT_NAME, applicantOrganisationName, nominatedOrganisationName, OPERATORSHIP_DISPLAY_TYPE));
//  }
//
//  @Test
//  void constructNominationDecisionEmail_whenValidNomination_thenConstructEmail() {
//    givenAValidNomination();
//
//    var resultingNotifyEmail = consulteeEmailCreationService
//        .constructDefaultNominationDecisionDeterminedEmail(NOMINATION_ID, RECIPIENT_NAME, applicantOrganisationName,
//            nominatedOrganisationName, OPERATORSHIP_DISPLAY_TYPE);
//
//    assertThat(resultingNotifyEmail.getPersonalisations())
//        .contains(
//            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
//            entry("NOMINATION_LINK", CONSULTATION_VIEW_URL),
//            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, RECIPIENT_NAME),
//            entry("APPLICANT_ORGANISATION", applicantOrgDto.name()),
//            entry("NOMINATED_ORGANISATION", nominatedOrgDto.name()),
//            entry("OPERATORSHIP_DISPLAY_TYPE", "an installation operator")
//        );
//
//    assertThat(resultingNotifyEmail)
//        .extracting(NotifyEmail::getTemplate)
//        .isEqualTo(NotifyTemplate.NOMINATION_DECISION_DETERMINED);
//  }
//
//  @Test
//  void constructNominationDecisionEmail_whenNoNomination_thenThrowException() {
//    given(nominationService.getNomination(NOMINATION_ID))
//        .willReturn(Optional.empty());
//
//    assertThatExceptionOfType(IllegalStateException.class)
//        .isThrownBy(() -> consulteeEmailCreationService
//            .constructDefaultNominationDecisionDeterminedEmail(NOMINATION_ID, RECIPIENT_NAME, applicantOrganisationName, nominatedOrganisationName, OPERATORSHIP_DISPLAY_TYPE));
//  }
//
//  @Test
//  void constructAppointmentConfirmedEmail_whenValidNomination_thenConstructEmail() {
//    givenAValidNomination();
//
//    var resultingNotifyEmail = consulteeEmailCreationService
//        .constructDefaultAppointmentConfirmedEmail(NOMINATION_ID, RECIPIENT_NAME, applicantOrganisationName, nominatedOrganisationName, OPERATORSHIP_DISPLAY_TYPE);
//
//    assertThat(resultingNotifyEmail.getPersonalisations())
//        .contains(
//            entry("NOMINATION_REFERENCE", nomination.nominationReference()),
//            entry("NOMINATION_LINK", CONSULTATION_VIEW_URL),
//            entry(NotifyEmail.RECIPIENT_NAME_PERSONALISATION_KEY, RECIPIENT_NAME),
//            entry("APPLICANT_ORGANISATION", applicantOrgDto.name()),
//            entry("NOMINATED_ORGANISATION", nominatedOrgDto.name()),
//            entry("OPERATORSHIP_DISPLAY_TYPE", "an installation operator")
//        );
//
//    assertThat(resultingNotifyEmail)
//        .extracting(NotifyEmail::getTemplate)
//        .isEqualTo(NotifyTemplate.NOMINATION_APPOINTMENT_CONFIRMED);
//  }
//
//  @Test
//  void constructAppointmentConfirmedEmail_whenNoNomination_thenThrowException() {
//    given(nominationService.getNomination(NOMINATION_ID))
//        .willReturn(Optional.empty());
//
//    assertThatExceptionOfType(IllegalStateException.class)
//        .isThrownBy(() -> consulteeEmailCreationService
//            .constructDefaultAppointmentConfirmedEmail(NOMINATION_ID, RECIPIENT_NAME, applicantOrganisationName, nominatedOrganisationName, OPERATORSHIP_DISPLAY_TYPE));
//  }
//
  private void givenAValidNominationDetail() {
    given(nominationDetailService.getLatestNominationDetailWithStatuses(
        NOMINATION_ID,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION))
    )
        .willReturn(Optional.of(nominationDetail));

    given(emailUrlGenerationService.generateEmailUrl(consultationViewUrl))
        .willReturn(CONSULTATION_VIEW_URL);
  }
}