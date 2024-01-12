package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public record LicenceBlockSubareaAddToListView(String id, String name, boolean isValid) implements AddToListItem {

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isValid() {
    return isValid;
  }

}
