package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.email.EmailService;
import uk.co.nstauthority.offshoresafetydirective.email.GovukNotifyTemplate;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype.NominationTypeService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;

@ExtendWith(MockitoExtension.class)
class ConsulteeEmailBuilderServiceTest {

  @Mock
  private NominationDetailService nominationDetailService;

  @Mock
  private ApplicantDetailAccessService applicantDetailAccessService;

  @Mock
  private NomineeDetailAccessService nomineeDetailAccessService;

  @Mock
  private PortalOrganisationUnitQueryService organisationUnitQueryService;

  @Mock
  private NominationTypeService nominationTypeService;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private ConsulteeEmailBuilderService consulteeEmailBuilderService;

  @Nested
  class BuildNominationDecisionTemplate {

    private final Nomination nomination = NominationTestUtil.builder()
        .withReference("nomination-reference")
        .build();

    private final NominationDetail nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .build();

    private final NominationId nominationId = new NominationId(nominationDetail);

    @Test
    void whenCannotFindNominationDetail() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildNominationDecisionTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id()));
    }

    @Test
    void whenCannotFindApplicant() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildNominationDecisionTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to retrieve ApplicantDetail for NominationDetail with ID %s".formatted(nominationDetail.getId()));
    }

    @Test
    void whenCannotFindNominee() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(ApplicantDetailDtoTestUtil.builder().build()));

      given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildNominationDecisionTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to retrieve NomineeDetail for NominationDetail with ID %s".formatted(nominationDetail.getId()));

    }

    @Test
    void whenSuccess_thenVerifyMailMergeFields() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      var applicationDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(10)
          .build();

      var applicantOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .withName("applicant")
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(applicationDetail));

      var nomineeDetail = NomineeDetailTestingUtil.builder()
          .withNominatedOrganisationId(20)
          .build();

      var nomineeOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .withName("nominee")
          .build();

      var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

      given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(nomineeDetailDto));

      given(organisationUnitQueryService.getOrganisationByIds(
          eq(Set.of(
              new PortalOrganisationUnitId(nomineeDetailDto.nominatedOrganisationId().id()),
              new PortalOrganisationUnitId(applicationDetail.applicantOrganisationId().id())
          )),
          any(RequestPurpose.class)
      ))
          .willReturn(List.of(applicantOrganisation, nomineeOrganisation));

      given(emailService.withUrl(ReverseRouter.route(on(NominationConsulteeViewController.class)
              .renderNominationView(nominationId))
      ))
          .willReturn("/url");

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.INSTALLATION);

      given(emailService.getTemplate(GovukNotifyTemplate.NOMINATION_DECISION_REACHED))
          .willReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

      var resultingMergeTemplate = consulteeEmailBuilderService
          .buildNominationDecisionTemplate(nominationId)
          .merge();

      assertThat(resultingMergeTemplate.getMailMergeFields())
          .extracting(MailMergeField::name, MailMergeField::value)
          .contains(
              tuple("APPLICANT_ORGANISATION", applicantOrganisation.name()),
              tuple("NOMINATED_ORGANISATION", nomineeOrganisation.name()),
              tuple("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference()),
              tuple("NOMINATION_LINK", "/url"),
              tuple("OPERATORSHIP_DISPLAY_TYPE", "an installation operator")
          );
    }
  }

  @Nested
  class BuildConsultationRequestedTemplate {

    private final Nomination nomination = NominationTestUtil.builder()
        .withReference("nomination-reference")
        .build();

    private final NominationDetail nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .build();

    private final NominationId nominationId = new NominationId(nominationDetail);

    @Test
    void whenCannotFindNominationDetail() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildConsultationRequestedTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id()));
    }

    @Test
    void whenCannotFindApplicant() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildConsultationRequestedTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to retrieve ApplicantDetail for NominationDetail with ID %s".formatted(nominationDetail.getId()));
    }

    @Test
    void whenCannotFindNominee() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(ApplicantDetailDtoTestUtil.builder().build()));

      given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildConsultationRequestedTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to retrieve NomineeDetail for NominationDetail with ID %s".formatted(nominationDetail.getId()));

    }

    @Test
    void whenSuccess_thenVerifyMailMergeFields() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      var applicationDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(10)
          .build();

      var applicantOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .withName("applicant")
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(applicationDetail));

      var nomineeDetail = NomineeDetailTestingUtil.builder()
          .withNominatedOrganisationId(20)
          .build();

      var nomineeOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .withName("nominee")
          .build();

      var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

      given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(nomineeDetailDto));

      given(organisationUnitQueryService.getOrganisationByIds(
          eq(Set.of(
              new PortalOrganisationUnitId(nomineeDetailDto.nominatedOrganisationId().id()),
              new PortalOrganisationUnitId(applicationDetail.applicantOrganisationId().id())
          )),
          any(RequestPurpose.class)
      ))
          .willReturn(List.of(applicantOrganisation, nomineeOrganisation));

      given(emailService.withUrl(ReverseRouter.route(on(NominationConsulteeViewController.class)
          .renderNominationView(nominationId))
      ))
          .willReturn("/url");

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.INSTALLATION);

      given(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUESTED))
          .willReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

      var resultingMergeTemplate = consulteeEmailBuilderService
          .buildConsultationRequestedTemplate(nominationId)
          .merge();

      assertThat(resultingMergeTemplate.getMailMergeFields())
          .extracting(MailMergeField::name, MailMergeField::value)
          .contains(
              tuple("APPLICANT_ORGANISATION", applicantOrganisation.name()),
              tuple("NOMINATED_ORGANISATION", nomineeOrganisation.name()),
              tuple("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference()),
              tuple("NOMINATION_LINK", "/url"),
              tuple("OPERATORSHIP_DISPLAY_TYPE", "an installation operator")
          );
    }
  }

  @Nested
  class BuildAppointmentConfirmedTemplate {

    private final Nomination nomination = NominationTestUtil.builder()
        .withReference("nomination-reference")
        .build();

    private final NominationDetail nominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .build();

    private final NominationId nominationId = new NominationId(nominationDetail);

    @Test
    void whenCannotFindNominationDetail() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildAppointmentConfirmedTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Could not find latest submitted NominationDetail for nomination with ID %s".formatted(nominationId.id()));
    }

    @Test
    void whenCannotFindApplicant() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildAppointmentConfirmedTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to retrieve ApplicantDetail for NominationDetail with ID %s".formatted(nominationDetail.getId()));
    }

    @Test
    void whenCannotFindNominee() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(ApplicantDetailDtoTestUtil.builder().build()));

      given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> consulteeEmailBuilderService.buildAppointmentConfirmedTemplate(nominationId))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to retrieve NomineeDetail for NominationDetail with ID %s".formatted(nominationDetail.getId()));

    }

    @Test
    void whenSuccess_thenVerifyMailMergeFields() {

      given(nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
      ))
          .willReturn(Optional.of(nominationDetail));

      var applicationDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(10)
          .build();

      var applicantOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(10)
          .withName("applicant")
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(applicationDetail));

      var nomineeDetail = NomineeDetailTestingUtil.builder()
          .withNominatedOrganisationId(20)
          .build();

      var nomineeOrganisation = PortalOrganisationDtoTestUtil.builder()
          .withId(20)
          .withName("nominee")
          .build();

      var nomineeDetailDto = NomineeDetailDto.fromNomineeDetail(nomineeDetail);

      given(nomineeDetailAccessService.getNomineeDetailDtoByNominationDetail(nominationDetail))
          .willReturn(Optional.of(nomineeDetailDto));

      given(organisationUnitQueryService.getOrganisationByIds(
          eq(Set.of(
              new PortalOrganisationUnitId(nomineeDetailDto.nominatedOrganisationId().id()),
              new PortalOrganisationUnitId(applicationDetail.applicantOrganisationId().id())
          )),
          any(RequestPurpose.class)
      ))
          .willReturn(List.of(applicantOrganisation, nomineeOrganisation));

      given(emailService.withUrl(ReverseRouter.route(on(NominationConsulteeViewController.class)
          .renderNominationView(nominationId))
      ))
          .willReturn("/url");

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.INSTALLATION);

      given(emailService.getTemplate(GovukNotifyTemplate.APPOINTMENT_CONFIRMED))
          .willReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

      var resultingMergeTemplate = consulteeEmailBuilderService
          .buildAppointmentConfirmedTemplate(nominationId)
          .merge();

      assertThat(resultingMergeTemplate.getMailMergeFields())
          .extracting(MailMergeField::name, MailMergeField::value)
          .contains(
              tuple("APPLICANT_ORGANISATION", applicantOrganisation.name()),
              tuple("NOMINATED_ORGANISATION", nomineeOrganisation.name()),
              tuple("NOMINATION_REFERENCE", nominationDetail.getNomination().getReference()),
              tuple("NOMINATION_LINK", "/url"),
              tuple("OPERATORSHIP_DISPLAY_TYPE", "an installation operator")
          );
    }
  }

  @Nested
  class GetNominationOperatorshipText {

    private final NominationDetail nominationDetail = NominationDetailTestUtil.builder().build();

    @Test
    void whenWellAndInstallation() {

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.WELL_AND_INSTALLATION);

      assertThat(consulteeEmailBuilderService.getNominationOperatorshipText(nominationDetail))
          .isEqualTo("a well and installation operator");
    }

    @Test
    void whenWellOnly() {

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.WELL);

      assertThat(consulteeEmailBuilderService.getNominationOperatorshipText(nominationDetail))
          .isEqualTo("a well operator");
    }

    @Test
    void whenInstallationOnly() {

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.INSTALLATION);

      assertThat(consulteeEmailBuilderService.getNominationOperatorshipText(nominationDetail))
          .isEqualTo("an installation operator");
    }

    @Test
    void whenNotProvided() {

      given(nominationTypeService.getNominationDisplayType(nominationDetail))
          .willReturn(NominationDisplayType.NOT_PROVIDED);

      assertThat(consulteeEmailBuilderService.getNominationOperatorshipText(nominationDetail))
          .isEqualTo("an operator");
    }

  }
}