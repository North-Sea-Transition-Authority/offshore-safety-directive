package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.ArrayList;
import java.util.List;

class RelatedInformationForm {

  private Boolean relatedToAnyFields;
  private List<Integer> fields = new ArrayList<>();
  private String fieldSelector;

  public Boolean getRelatedToAnyFields() {
    return relatedToAnyFields;
  }

  public List<Integer> getFields() {
    return fields;
  }

  public void setFields(List<Integer> fields) {
    this.fields = fields;
  }

  public String getFieldSelector() {
    return fieldSelector;
  }

  public void setFieldSelector(String fieldSelector) {
    this.fieldSelector = fieldSelector;
  }

  public void setRelatedToAnyFields(Boolean relatedToAnyFields) {
    this.relatedToAnyFields = relatedToAnyFields;
  }

}
