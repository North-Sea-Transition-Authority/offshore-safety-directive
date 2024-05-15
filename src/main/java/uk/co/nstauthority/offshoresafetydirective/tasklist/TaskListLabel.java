package uk.co.nstauthority.offshoresafetydirective.tasklist;

/**
 * Record use to represent a label shown on a task list implementation.
 * @param labelText the text of the label
 * @param labelType the type of task list label (used to determine colour)
 */
public record TaskListLabel(String labelText, TaskListLabelType labelType) {
}
