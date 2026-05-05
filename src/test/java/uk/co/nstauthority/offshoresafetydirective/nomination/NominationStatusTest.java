package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
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

  @Test
  void getDisplayNameByEnumName_includeDrafts(){

    var resultingStatusMap = NominationStatus.getDisplayNameByEnumName(true);

    assertThat(resultingStatusMap).containsExactly(
        Map.entry(NominationStatus.DRAFT.name(), NominationStatus.DRAFT.getScreenDisplayText()),
        Map.entry(NominationStatus.SUBMITTED.name(), NominationStatus.SUBMITTED.getScreenDisplayText()),
        Map.entry(NominationStatus.AWAITING_CONFIRMATION.name(), NominationStatus.AWAITING_CONFIRMATION.getScreenDisplayText()),
        Map.entry(NominationStatus.APPOINTED.name(), NominationStatus.APPOINTED.getScreenDisplayText()),
        Map.entry(NominationStatus.OBJECTED.name(), NominationStatus.OBJECTED.getScreenDisplayText()),
        Map.entry(NominationStatus.WITHDRAWN.name(), NominationStatus.WITHDRAWN.getScreenDisplayText())
    );
  }

  @Test
  void getDisplayNameByEnumName_doNotIncludeDrafts(){

    var resultingStatusMap = NominationStatus.getDisplayNameByEnumName(false);

    assertThat(resultingStatusMap).containsExactly(
        Map.entry(NominationStatus.SUBMITTED.name(), NominationStatus.SUBMITTED.getScreenDisplayText()),
        Map.entry(NominationStatus.AWAITING_CONFIRMATION.name(), NominationStatus.AWAITING_CONFIRMATION.getScreenDisplayText()),
        Map.entry(NominationStatus.APPOINTED.name(), NominationStatus.APPOINTED.getScreenDisplayText()),
        Map.entry(NominationStatus.OBJECTED.name(), NominationStatus.OBJECTED.getScreenDisplayText()),
        Map.entry(NominationStatus.WITHDRAWN.name(), NominationStatus.WITHDRAWN.getScreenDisplayText())
    );
  }
}
