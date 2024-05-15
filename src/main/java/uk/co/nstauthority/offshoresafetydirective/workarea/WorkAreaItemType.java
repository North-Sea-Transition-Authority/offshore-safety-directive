package uk.co.nstauthority.offshoresafetydirective.workarea;

enum WorkAreaItemType {

  NOMINATION(10);

  private final Integer displayOrder;

  WorkAreaItemType(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }
}
