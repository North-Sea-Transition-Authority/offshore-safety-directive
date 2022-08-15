package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListItem;

/**
 * Interface to represent a task list item on the nomination form task list. Applies sensible defaults
 * specific to the nomination task list implementation.
 */
public interface NominationTaskListItem extends TaskListItem<NominationTaskListItemType> {

  @Override
  default boolean isVisible(NominationTaskListItemType target) {
    return true;
  }

  @Override
  default boolean showTaskListLabels(NominationTaskListItemType target) {
    return true;
  }
}
