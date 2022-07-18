package uk.co.nstauthority.offshoresafetydirective.tasklist;

/**
 * This record is to be sent to the frontend to display the relevant information for each {@link TaskListItem}.
 */
public record TaskListItemView(Integer displayOrder, String name, String actionUrl) {
}
