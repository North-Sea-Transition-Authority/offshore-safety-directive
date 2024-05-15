package uk.co.nstauthority.offshoresafetydirective.fds;

/**
 * Enums that implement this interface might be passed as an option to a list of answers in a form.
 * For example a list of answers in a radio group.
 */
public interface DisplayableEnumOption {

  int getDisplayOrder();

  String getScreenDisplayText();

  String getFormValue();

}
