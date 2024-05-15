package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnum;

public enum NominationStatus implements DisplayableEnum {
  DRAFT("Draft", 10, NominationStatusSubmissionStage.PRE_SUBMISSION),
  SUBMITTED("Submitted", 20, NominationStatusSubmissionStage.POST_SUBMISSION),
  AWAITING_CONFIRMATION("Awaiting confirmation of appointment", 30, NominationStatusSubmissionStage.POST_SUBMISSION),
  APPOINTED("Appointed", 40, NominationStatusSubmissionStage.POST_SUBMISSION),
  OBJECTED("Objected", 45, NominationStatusSubmissionStage.POST_SUBMISSION),
  WITHDRAWN("Withdrawn", 50, NominationStatusSubmissionStage.POST_SUBMISSION),
  DELETED("Deleted", 60, NominationStatusSubmissionStage.PRE_SUBMISSION);

  private final String displayText;
  private final Integer displayOrder;
  private final NominationStatusSubmissionStage submissionStage;

  NominationStatus(String displayText, Integer displayOrder,
                   NominationStatusSubmissionStage submissionStage) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
    this.submissionStage = submissionStage;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getScreenDisplayText() {
    return displayText;
  }

  public NominationStatusSubmissionStage getSubmissionStage() {
    return submissionStage;
  }

  public static Set<NominationStatus> getAllStatusesForSubmissionStage(NominationStatusSubmissionStage stage) {
    return Arrays.stream(values())
        .filter(nominationStatus -> nominationStatus.getSubmissionStage().equals(stage))
        .collect(Collectors.toSet());
  }

  public static Set<NominationStatus> getPostSubmissionStatuses() {
    return getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION);
  }
}
