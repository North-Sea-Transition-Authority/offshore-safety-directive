package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
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

  @InjectMocks
  private NominationSummaryService nominationSummaryService;

  @Test
  void getNominationSummaryView() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

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

    Map<String, Object> expectedFieldsAndValues = Map.ofEntries(
        Map.entry("applicantDetailSummaryView", applicantDetailSummaryView),
        Map.entry("nomineeDetailSummaryView", nomineeDetailSummaryView),
        Map.entry("relatedInformationSummaryView", relatedInformationSummaryView),
        Map.entry("installationSummaryView", installationSummaryView),
        Map.entry("wellSummaryView", wellSummaryView)
    );

    assertThat(result)
        .hasOnlyFields(expectedFieldsAndValues.keySet().toArray(String[]::new));

    expectedFieldsAndValues.forEach((s, o) -> assertThat(result).hasFieldOrPropertyWithValue(s, o));
  }
}