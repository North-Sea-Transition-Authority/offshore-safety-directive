package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

public class WorkAreaFilterForm {

  private Set<NominationStatus> nominationStatuses;

  public Set<NominationStatus> getNominationStatuses() {
    return nominationStatuses;
  }

  public void setNominationStatuses(Set<NominationStatus> nominationStatuses) {
    this.nominationStatuses = nominationStatuses;
  }

  static WorkAreaFilterForm fromFilter(WorkAreaFilter filter) {
    WorkAreaFilterForm workAreaFilterForm = new WorkAreaFilterForm();
    workAreaFilterForm.setNominationStatuses(filter.nominationStatuses());
    return workAreaFilterForm;
  }
}
