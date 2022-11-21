package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.ArrayList;
import java.util.List;

class RelatedInformationForm {

  private Boolean relatedToAnyFields;
  private List<Integer> fields = new ArrayList<>();
  private String fieldSelector;

  private Boolean relatedToAnyLicenceApplications;

  private String relatedLicenceApplications;

  private Boolean relatedToAnyWellApplications;

  private String relatedWellApplications;

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

  public Boolean getRelatedToAnyLicenceApplications() {
    return relatedToAnyLicenceApplications;
  }

  public void setRelatedToAnyLicenceApplications(Boolean relatedToAnyLicenceApplications) {
    this.relatedToAnyLicenceApplications = relatedToAnyLicenceApplications;
  }

  public String getRelatedLicenceApplications() {
    return relatedLicenceApplications;
  }

  public void setRelatedLicenceApplications(String relatedLicenceApplications) {
    this.relatedLicenceApplications = relatedLicenceApplications;
  }

  public Boolean getRelatedToAnyWellApplications() {
    return relatedToAnyWellApplications;
  }

  public void setRelatedToAnyWellApplications(Boolean relatedToAnyWellApplications) {
    this.relatedToAnyWellApplications = relatedToAnyWellApplications;
  }

  public String getRelatedWellApplications() {
    return relatedWellApplications;
  }

  public void setRelatedWellApplications(String relatedWellApplications) {
    this.relatedWellApplications = relatedWellApplications;
  }
}
