package uk.co.nstauthority.offshoresafetydirective.validation;

class NestedTestForm {

  private String firstField;

  private FieldOrderTestForm secondField;

  private String thirdField;

  public String getFirstField() {
    return firstField;
  }

  public void setFirstField(String firstField) {
    this.firstField = firstField;
  }

  public FieldOrderTestForm getSecondField() {
    return secondField;
  }

  public void setSecondField(FieldOrderTestForm secondField) {
    this.secondField = secondField;
  }

  public String getThirdField() {
    return thirdField;
  }

  public void setThirdField(String thirdField) {
    this.thirdField = thirdField;
  }
}
