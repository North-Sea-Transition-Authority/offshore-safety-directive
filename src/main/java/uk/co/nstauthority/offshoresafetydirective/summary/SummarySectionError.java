package uk.co.nstauthority.offshoresafetydirective.summary;

public record SummarySectionError(String errorMessage) {

  public static SummarySectionError createWithDefaultMessage(String sectionName) {
    return new SummarySectionError(
        "There are problems with the %s section. Return to the task list to fix the problems."
            .formatted(sectionName)
    );
  }

}
