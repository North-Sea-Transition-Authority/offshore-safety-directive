package uk.co.nstauthority.offshoresafetydirective.validation;

class TestFormWithParentForm extends FieldOrderTestForm {

  private String nonParentField;

  public String getNonParentField() {
    return nonParentField;
  }

  public void setNonParentField(String nonParentField) {
    this.nonParentField = nonParentField;
  }
}
