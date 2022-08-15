package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;

class WellSubmissionServiceTest {

  private static WellSubmissionService wellSubmissionService;

  @BeforeAll
  static void setup() {
    wellSubmissionService = new WellSubmissionService();
  }

  @Test
  void isSectionSubmittable_assertFalse() {

    var nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .build();

    assertFalse(
        wellSubmissionService.isSectionSubmittable(nominationDetail)
    );
  }

}