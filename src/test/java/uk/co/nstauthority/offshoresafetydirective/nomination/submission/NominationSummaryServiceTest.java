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

@ExtendWith(MockitoExtension.class)
class NominationSummaryServiceTest {

  @Mock
  private ApplicantDetailSummaryService applicantDetailSummaryService;

  @InjectMocks
  private NominationSummaryService nominationSummaryService;

  @Test
  void getNominationSummaryView() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    var applicantDetailSummaryView = new ApplicantDetailSummaryView(null);
    when(applicantDetailSummaryService.getApplicantDetailSummaryView(nominationDetail))
        .thenReturn(applicantDetailSummaryView);

    var result = nominationSummaryService.getNominationSummaryView(nominationDetail);

    assertThat(result)
        .hasOnlyFields("applicantDetailSummaryView")
        .hasFieldOrPropertyWithValue("applicantDetailSummaryView", applicantDetailSummaryView);
  }
}