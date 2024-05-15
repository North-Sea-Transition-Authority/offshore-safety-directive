package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NominationStatusTest {

  @Test
  void getPostSubmissionStatuses() {

    var resultingPostSubmissionStatuses = NominationStatus.getPostSubmissionStatuses();

    assertThat(resultingPostSubmissionStatuses).containsExactlyInAnyOrder(
        NominationStatus.SUBMITTED,
        NominationStatus.AWAITING_CONFIRMATION,
        NominationStatus.APPOINTED,
        NominationStatus.OBJECTED,
        NominationStatus.WITHDRAWN
    );
  }

  @Test
  void getAllStatusesForSubmissionStage_whenPostSubmission() {

    var resultingSubmissionStatuses = NominationStatus.getAllStatusesForSubmissionStage(
        NominationStatusSubmissionStage.POST_SUBMISSION
    );

    assertThat(resultingSubmissionStatuses).containsExactlyInAnyOrder(
        NominationStatus.SUBMITTED,
        NominationStatus.AWAITING_CONFIRMATION,
        NominationStatus.APPOINTED,
        NominationStatus.OBJECTED,
        NominationStatus.WITHDRAWN
    );
  }

  @Test
  void getAllStatusesForSubmissionStage_whenPreSubmission() {

    var resultingSubmissionStatuses = NominationStatus.getAllStatusesForSubmissionStage(
        NominationStatusSubmissionStage.PRE_SUBMISSION
    );

    assertThat(resultingSubmissionStatuses).containsExactlyInAnyOrder(
        NominationStatus.DRAFT,
        NominationStatus.DELETED
    );
  }
}
