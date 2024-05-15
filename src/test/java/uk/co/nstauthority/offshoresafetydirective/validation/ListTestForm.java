package uk.co.nstauthority.offshoresafetydirective.validation;

import java.util.List;

class ListTestForm {

  private List<NestedTestForm> listField;

  public List<NestedTestForm> getListField() {
    return listField;
  }

  public void setListField(List<NestedTestForm> listField) {
    this.listField = listField;
  }
}
