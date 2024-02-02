package uk.co.nstauthority.offshoresafetydirective.email;

public enum GovukNotifyTemplate {
  CONSULTATION_REQUESTED("de8371e9-f59c-4c00-b685-87f8ddde774d"),
  FORWARD_AREA_APPROVAL_ENDED("7929731e-cd5d-42d4-b790-796967aae70c"),
  ;

  private final String templateId;

  GovukNotifyTemplate(String templateId) {
    this.templateId = templateId;
  }

  String getTemplateId() {
    return templateId;
  }
}
