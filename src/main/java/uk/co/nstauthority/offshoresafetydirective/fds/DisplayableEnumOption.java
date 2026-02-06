package uk.co.nstauthority.offshoresafetydirective.fds;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

/**
 * Enums that implement this interface might be passed as an option to a list of answers in a form.
 * For example a list of answers in a radio group.
 */
public interface DisplayableEnumOption extends Displayable {

  @Override
  int getDisplayOrder();

  String getScreenDisplayText();

  String getFormValue();

  @Override
  default String getDisplayName() {
    return getScreenDisplayText();
  }

}
