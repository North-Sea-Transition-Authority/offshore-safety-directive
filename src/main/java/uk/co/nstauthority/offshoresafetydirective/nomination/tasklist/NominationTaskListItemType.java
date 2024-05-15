package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;

/**
 * Class to be used as the generic type in the nomination form task list implementation. The class wraps
 * properties that methods in the task list implementation require.
 * @param nominationDetail The nomination detail that is being accessed
 */
public record NominationTaskListItemType(NominationDetail nominationDetail) {

  public NominationId nominationId() {
    return new NominationId(nominationDetail.getNomination().getId());
  }
}
