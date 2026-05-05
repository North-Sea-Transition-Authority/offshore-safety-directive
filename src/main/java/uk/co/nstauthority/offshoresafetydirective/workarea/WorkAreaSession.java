package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.io.Serial;
import java.io.Serializable;

public class WorkAreaSession implements Serializable {

  @Serial
  private static final long serialVersionUID = -4448034632615579794L;

  private WorkAreaFilter workAreaFilter;
  private final boolean canSeeDrafts;

  public WorkAreaSession(WorkAreaFilter workAreaFilter, boolean canSeeDrafts) {
    this.workAreaFilter = workAreaFilter;
    this.canSeeDrafts = canSeeDrafts;
  }

  public void updateSession(WorkAreaFilter workAreaFilter) {
    this.workAreaFilter = workAreaFilter;
  }

  public WorkAreaFilter getWorkAreaFilter() {
    return workAreaFilter;
  }

  public boolean canSeeDrafts() {
    return canSeeDrafts;
  }

}
