package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import uk.co.nstauthority.offshoresafetydirective.exception.IllegalUtilClassInstantiationException;

class WellSelectionSetupFormTestUtil {

  private WellSelectionSetupFormTestUtil() {
    throw new IllegalUtilClassInstantiationException(this.getClass());
  }

  static WellSelectionSetupFormBuilder builder() {
    return new WellSelectionSetupFormBuilder();
  }

  static class WellSelectionSetupFormBuilder {
    private String wellSelectionType = WellSelectionType.SPECIFIC_WELLS.getFormValue();

    WellSelectionSetupFormBuilder withWellSelectionType(String wellSelectionType) {
      this.wellSelectionType = wellSelectionType;
      return this;
    }

    WellSelectionSetupFormBuilder withWellSelectionType(WellSelectionType wellSelectionType) {
      return withWellSelectionType(wellSelectionType.getFormValue());
    }

    WellSelectionSetupForm build() {
      var form = new WellSelectionSetupForm();
      form.setWellSelectionType(wellSelectionType);
      return form;
    }
  }
}