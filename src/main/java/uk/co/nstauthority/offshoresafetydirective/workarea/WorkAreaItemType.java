package uk.co.nstauthority.offshoresafetydirective.workarea;

import uk.co.nstauthority.offshoresafetydirective.displayableutil.Displayable;

enum WorkAreaItemType implements Displayable {

  NOMINATION(10);

  private final Integer displayOrder;

  WorkAreaItemType(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }
}
