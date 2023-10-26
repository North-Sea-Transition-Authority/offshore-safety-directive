package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.ArrayList;
import java.util.List;

class RelatedInformationForm {

  private String relatedToAnyFields;

  private List<Integer> fields = new ArrayList<>();

  private String fieldSelector;

  private String relatedToAnyLicenceApplications;

  private String relatedLicenceApplications;

  private String relatedToAnyWellApplications;

  private String relatedWellApplications;

  public String getRelatedToAnyFields() {
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

  public void setRelatedToAnyFields(String relatedToAnyFields) {
    this.relatedToAnyFields = relatedToAnyFields;
  }

  public String getRelatedToAnyLicenceApplications() {
    return relatedToAnyLicenceApplications;
  }

  public void setRelatedToAnyLicenceApplications(String relatedToAnyLicenceApplications) {
    this.relatedToAnyLicenceApplications = relatedToAnyLicenceApplications;
  }

  public String getRelatedLicenceApplications() {
    return relatedLicenceApplications;
  }

  public void setRelatedLicenceApplications(String relatedLicenceApplications) {
    this.relatedLicenceApplications = relatedLicenceApplications;
  }

  public String getRelatedToAnyWellApplications() {
    return relatedToAnyWellApplications;
  }

  public void setRelatedToAnyWellApplications(String relatedToAnyWellApplications) {
    this.relatedToAnyWellApplications = relatedToAnyWellApplications;
  }

  public String getRelatedWellApplications() {
    return relatedWellApplications;
  }

  public void setRelatedWellApplications(String relatedWellApplications) {
    this.relatedWellApplications = relatedWellApplications;
  }
}
