package uk.co.nstauthority.offshoresafetydirective.file;

public enum DeleteOutcome {
  SUCCESS("File has successfully been deleted"),
  INTERNAL_SERVER_ERROR("Unexpected error");

  private final String errorMessage;

  DeleteOutcome(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
