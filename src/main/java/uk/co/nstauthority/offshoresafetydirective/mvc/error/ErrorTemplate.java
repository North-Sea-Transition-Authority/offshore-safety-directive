package uk.co.nstauthority.offshoresafetydirective.mvc.error;

enum ErrorTemplate {

  UNEXPECTED_ERROR("osd/error/unexpectedError"),
  PAGE_NOT_FOUND("osd/error/notFound"),
  UNAUTHORISED("osd/error/unauthorised");

  private final String templateName;

  ErrorTemplate(String templateName) {
    this.templateName = templateName;
  }

  String getTemplateName() {
    return templateName;
  }
}
