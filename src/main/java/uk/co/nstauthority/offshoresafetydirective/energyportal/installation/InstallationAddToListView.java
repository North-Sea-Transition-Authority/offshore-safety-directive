package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public record InstallationAddToListView(int id, String name, boolean isValid) implements AddToListItem {

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
