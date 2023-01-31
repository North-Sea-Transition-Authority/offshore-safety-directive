package uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea;

import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public record LicenceBlockSubareaAddToListView(String id, String name, boolean isValid, String sortKey) implements AddToListItem {

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

  public String getSortKey() {
    return sortKey;
  }
}
