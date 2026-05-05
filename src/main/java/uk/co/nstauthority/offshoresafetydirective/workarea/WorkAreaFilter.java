package uk.co.nstauthority.offshoresafetydirective.workarea;

import java.io.Serial;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;

public record WorkAreaFilter(
    Set<NominationStatus> nominationStatuses
) implements Serializable {

  @Serial
  private static final long serialVersionUID = -162950725732865861L;

  static WorkAreaFilter defaultFilter(boolean canSeeDrafts) {

    var nominationStatuses = EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION);

    if (canSeeDrafts) {
      nominationStatuses.add(NominationStatus.DRAFT);
    }


    return new WorkAreaFilter(nominationStatuses);
  }

  static WorkAreaFilter fromForm(WorkAreaFilterForm form) {
    return  new WorkAreaFilter(form.getNominationStatuses());
  }
}
