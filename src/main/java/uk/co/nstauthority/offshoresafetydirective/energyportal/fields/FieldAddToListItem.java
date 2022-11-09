package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import uk.co.nstauthority.offshoresafetydirective.fds.addtolist.AddToListItem;

public class FieldAddToListItem implements AddToListItem {

  private final String id;
  private final String name;
  private final boolean valid;

  public FieldAddToListItem(String id, String name, boolean valid) {
    this.id = id;
    this.name = name;
    this.valid = valid;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isValid() {
    return valid;
  }
}
