package uk.co.nstauthority.offshoresafetydirective.energyportal.well;

import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public class WellAddToListView implements AddToListItem {

  private final int id;

  private final String name;

  private final boolean isValid;

  private final String sortKey;

  public WellAddToListView(int id, String name, boolean isValid, String sortKey) {
    this.id = id;
    this.name = name;
    this.isValid = isValid;
    this.sortKey = sortKey;
  }

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Boolean isValid() {
    return isValid;
  }

  public String getSortKey() {
    return sortKey;
  }
}
