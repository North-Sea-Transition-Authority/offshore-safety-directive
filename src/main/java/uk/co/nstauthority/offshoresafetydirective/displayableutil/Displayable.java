package uk.co.nstauthority.offshoresafetydirective.displayableutil;

import uk.co.fivium.digitalenummaterialisationlibrary.enummaterialisation.MaterialisableEnum;

public interface Displayable extends MaterialisableEnum {
  default String getDisplayName() {
    return "";
  }
}
