package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SubmissionSummaryViewTest {

  @Test
  void from() {
    var submissionInformation = NominationSubmissionInformationTestUtil.builder().build();
    var result = SubmissionSummaryView.from(submissionInformation);
    assertThat(result)
        .extracting(
            SubmissionSummaryView::confirmedAuthority,
            SubmissionSummaryView::fastTrackReason
        )
        .containsExactly(
            submissionInformation.getAuthorityConfirmed(),
            submissionInformation.getFastTrackReason()
        );
  }
}