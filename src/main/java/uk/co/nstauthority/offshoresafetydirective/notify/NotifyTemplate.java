package uk.co.nstauthority.offshoresafetydirective.notify;

public enum NotifyTemplate {

  EMAIL_DELIVERY_FAILED("EMAIL_DELIVERY_FAILED_V1"),
  CONSULTATION_REQUESTED("CONSULTATION_REQUESTED_V1")
  ;

  private final String templateName;

  NotifyTemplate(String templateName) {
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return templateName;
  }
}
