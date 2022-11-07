package uk.co.nstauthority.offshoresafetydirective.displayableutil;

import uk.co.nstauthority.offshoresafetydirective.fds.DisplayableEnumOption;

/**
 * Wrapper around DisplayableEnumOption to prevent the need to implement getFormValue.
 */
public interface DisplayableEnum extends DisplayableEnumOption {

  String name();

  @Override
  default String getFormValue() {
    return name();
  }
}
