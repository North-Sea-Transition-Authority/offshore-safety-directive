package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation.RelatedInformationSummaryView;

@ExtendWith(MockitoExtension.class)
class NominationSummaryServiceTest {

  @Mock
  private ApplicantDetailSummaryService applicantDetailSummaryService;

  @Mock
  private NomineeDetailSummaryService nomineeDetailSummaryService;

  @Mock
  private RelatedInformationSummaryService relatedInformationSummaryService;

  @InjectMocks
  private NominationSummaryService nominationSummaryService;

  @Test
  void getNominationSummaryView() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var applicantDetailSummaryView = new ApplicantDetailSummaryView(null);
    when(applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail))
        .thenReturn(applicantDetailSummaryView);

    var nomineeDetailSummaryView = new NomineeDetailSummaryView(null);
    when(nomineeDetailSummaryService.getNomineeDetailSummaryView(nominationDetail))
        .thenReturn(nomineeDetailSummaryView);

    var relatedInformationSummaryView = new RelatedInformationSummaryView(null);
    when(relatedInformationSummaryService.getRelatedInformationSummaryView(nominationDetail))
        .thenReturn(relatedInformationSummaryView);

    var result = nominationSummaryService.getNominationSummaryView(nominationDetail);

    assertThat(result)
        .hasOnlyFields("applicantDetailSummaryView", "nomineeDetailSummaryView", "relatedInformationSummaryView")
        .hasFieldOrPropertyWithValue("applicantDetailSummaryView", applicantDetailSummaryView)
        .hasFieldOrPropertyWithValue("nomineeDetailSummaryView", nomineeDetailSummaryView)
        .hasFieldOrPropertyWithValue("relatedInformationSummaryView", relatedInformationSummaryView);
  }
}