package uk.co.nstauthority.offshoresafetydirective.fds;

public class ErrorItem {

  private final int displayOrder;
  private final String fieldName;
  private final String errorMessage;

  public ErrorItem(int displayOrder, String fieldName, String errorMessage) {
    this.displayOrder = displayOrder;
    this.fieldName = fieldName;
    this.errorMessage = errorMessage;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
