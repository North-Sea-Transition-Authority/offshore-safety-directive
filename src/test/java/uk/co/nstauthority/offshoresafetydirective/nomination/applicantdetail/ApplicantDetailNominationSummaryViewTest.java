package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.summary.SummarySectionError;

class ApplicantDetailNominationSummaryViewTest {

  @Test
  void whenCompactConstructorCalled_thenAssertValues() {
    var error = new SummarySectionError("error");
    var result = new ApplicantDetailSummaryView(error);
    assertThat(result)
        .extracting(
            ApplicantDetailSummaryView::applicantOrganisationUnitView,
            ApplicantDetailSummaryView::applicantReference,
            ApplicantDetailSummaryView::summarySectionError
        ).containsExactly(
            new ApplicantOrganisationUnitView(),
            null,
            error
        );

    assertThat(result)
        .extracting(ApplicantDetailSummaryView::summarySectionDetails)
        .extracting(
            details -> details.summarySectionId().id(),
            details -> details.summarySectionName().name()
        ).containsExactly(
            "applicant-details-summary",
            "Applicant details"
        );
  }
}