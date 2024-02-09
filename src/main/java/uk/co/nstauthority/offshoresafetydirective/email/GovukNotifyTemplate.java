package uk.co.nstauthority.offshoresafetydirective.email;

public enum GovukNotifyTemplate {
  CONSULTATION_REQUESTED("de8371e9-f59c-4c00-b685-87f8ddde774d"),
  FORWARD_AREA_APPROVAL_ENDED("7929731e-cd5d-42d4-b790-796967aae70c"),
  NOMINATION_DECISION_REACHED("6fd8abbe-0aff-432f-8d2d-9e49df01e4a4"),
  APPOINTMENT_CONFIRMED("a9d90f69-b02b-4aaa-a72d-eaaf7252aabe"),
  FEEDBACK_FAILED_TO_SEND("b572e7a2-2bf2-4780-9dc4-810152813cf5")
  ;

  private final String templateId;

  GovukNotifyTemplate(String templateId) {
    this.templateId = templateId;
  }

  String getTemplateId() {
    return templateId;
  }
}
