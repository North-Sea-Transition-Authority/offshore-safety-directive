package uk.co.nstauthority.offshoresafetydirective.tasklist;

/**
 * <p>
 * This is a generic base class intended to be extended against other package-specific interfaces.
 * The TaskListSection determines each section of a task list. This should have an implementation
 * alongside {@link TaskListItem}.
 * </p>
 * <p>
 * Extending this class under a package-specific interface allows the application to inject the package-specific
 * extension inside a controller.
 * </p>
 * <p>
 * This provides the application with the ability to have multiple task lists without having to reimplement
 * large blocks of logic.
 * </p>
 *
 * @param <T> The type of object received to pass to each {@link TaskListItem} for validation and visibility.
 */
public interface TaskListSection<T> {

  /**
   * Determines the section name displayed in the task list.
   *
   * @return The section name.
   */
  String getSectionName();

  /**
   * Determines the position that the section is displayed within the task list.
   * This should be a unique number per task list.
   *
   * @return Int specifying the order within the task list.
   */
  int getDisplayOrder();

  /**
   * Determines the warning text to show for the section on the task list.
   * @return the warning text for the section
   */
  default String getSectionWarningText() {
    return "";
  }

}
