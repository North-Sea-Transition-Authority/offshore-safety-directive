package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.summary.WellSummaryView;
import uk.co.nstauthority.offshoresafetydirective.summary.SummaryValidationBehaviour;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

@ExtendWith(MockitoExtension.class)
class NominationSummaryServiceTest {

  private static final SummaryValidationBehaviour VALIDATION_BEHAVIOUR = SummaryValidationBehaviour.NOT_VALIDATED;

  @Mock
  private ApplicantDetailSummaryService applicantDetailSummaryService;

  @Mock
  private NomineeDetailSummaryService nomineeDetailSummaryService;

  @Mock
  private RelatedInformationSummaryService relatedInformationSummaryService;

  @Mock
  private InstallationSummaryService installationSummaryService;

  @Mock
  private WellSummaryService wellSummaryService;

  @Mock
  private SubmissionSummaryService submissionSummaryService;

  @InjectMocks
  private NominationSummaryService nominationSummaryService;

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, names = {
      "SUBMITTED",
      "AWAITING_CONFIRMATION",
      "APPOINTED",
      "OBJECTED",
      "WITHDRAWN"
  })
  void getNominationSummaryView_whenNominationStatusIsPostSubmission(NominationStatus nominationStatus) {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(nominationStatus)
        .build();

    var applicantDetailSummaryView = new ApplicantDetailSummaryView(null);
    when(applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(applicantDetailSummaryView);

    var nomineeDetailSummaryView = new NomineeDetailSummaryView(null);
    when(nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(nomineeDetailSummaryView);

    var relatedInformationSummaryView = new RelatedInformationSummaryView(null);
    when(relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(relatedInformationSummaryView);

    var installationSummaryView = new InstallationSummaryView(null);
    when(installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(installationSummaryView);

    var wellSummaryView = WellSummaryView.builder(WellSelectionType.NO_WELLS).build();
    when(wellSummaryService.getWellSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(wellSummaryView);

    var submissionSummaryView = new SubmissionSummaryView(true, "reason");
    when(submissionSummaryService.getSubmissionSummaryView(nominationDetail)).thenReturn(submissionSummaryView);

    var result = nominationSummaryService.getNominationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("applicantDetailSummaryView", applicantDetailSummaryView)
        .hasFieldOrPropertyWithValue("nomineeDetailSummaryView", nomineeDetailSummaryView)
        .hasFieldOrPropertyWithValue("relatedInformationSummaryView", relatedInformationSummaryView)
        .hasFieldOrPropertyWithValue("installationSummaryView", installationSummaryView)
        .hasFieldOrPropertyWithValue("wellSummaryView", wellSummaryView)
        .hasFieldOrPropertyWithValue("submissionSummaryView", submissionSummaryView)
        .hasAssertedAllProperties();
  }

  @ParameterizedTest
  @EnumSource(value = NominationStatus.class, names = {
      "DRAFT",
      "DELETED"
  })
  void getNominationSummaryView_whenNominationStatusIsPreSubmission(NominationStatus nominationStatus) {
    var nominationDetail = NominationDetailTestUtil.builder()
        .withStatus(nominationStatus)
        .build();

    var applicantDetailSummaryView = new ApplicantDetailSummaryView(null);
    when(applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(applicantDetailSummaryView);

    var nomineeDetailSummaryView = new NomineeDetailSummaryView(null);
    when(nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(nomineeDetailSummaryView);

    var relatedInformationSummaryView = new RelatedInformationSummaryView(null);
    when(relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(relatedInformationSummaryView);

    var installationSummaryView = new InstallationSummaryView(null);
    when(installationSummaryService.getInstallationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(installationSummaryView);

    var wellSummaryView = WellSummaryView.builder(WellSelectionType.NO_WELLS).build();
    when(wellSummaryService.getWellSummaryView(nominationDetail, VALIDATION_BEHAVIOUR))
        .thenReturn(wellSummaryView);

    var result = nominationSummaryService.getNominationSummaryView(nominationDetail, VALIDATION_BEHAVIOUR);

    PropertyObjectAssert.thenAssertThat(result)
        .hasFieldOrPropertyWithValue("applicantDetailSummaryView", applicantDetailSummaryView)
        .hasFieldOrPropertyWithValue("nomineeDetailSummaryView", nomineeDetailSummaryView)
        .hasFieldOrPropertyWithValue("relatedInformationSummaryView", relatedInformationSummaryView)
        .hasFieldOrPropertyWithValue("installationSummaryView", installationSummaryView)
        .hasFieldOrPropertyWithValue("wellSummaryView", wellSummaryView)
        .hasFieldOrPropertyWithValue("submissionSummaryView", null)
        .hasAssertedAllProperties();

    verify(submissionSummaryService, never()).getSubmissionSummaryView(nominationDetail);
  }
}