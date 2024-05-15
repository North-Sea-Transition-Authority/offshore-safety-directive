package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public record WellAddToListView(int id, String name, boolean isValid) implements AddToListItem {

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
