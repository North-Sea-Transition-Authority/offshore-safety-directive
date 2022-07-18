package uk.co.nstauthority.offshoresafetydirective.tasklist;

/**
 * <p>
 * This is a generic base class intended to be extended against other package-specific interfaces.
 * The TaskListItem determines each entry under a section of a task list.
 * This should have an implementation alongside {@link TaskListSection}.
 * </p>
 * <p>
 * Extending this class under a package-specific interface allows the application to inject the package-specific
 * extension inside a section.
 * </p>
 * <p>
 * This provides the application with the ability to have multiple task lists without having to reimplement large
 * blocks of logic.
 * </p>
 *
 * @param <T> The type of object received to pass to a validator and to determine visibility.
 */
public interface TaskListItem<T> {

  /**
   * The text to be displayed in the task list.
   *
   * @return The name displayed in the task list.
   */
  String getName();

  /**
   * The GET mapping to be redirected to on click.
   *
   * @return The URL associated with the entry.
   */
  String getActionUrl();

  /**
   * The order the item appears in the list.
   *
   * @return The position relative to other items in the section.
   */
  int getDisplayOrder();

  /**
   * Used to determine if the entry should appear in the list based on {@param target}.
   * Useful if the item depends on a previous question being answered.
   *
   * @param target The object used to confirm visibility.
   * @return True if visible, false if the item should be hidden.
   */
  default boolean isVisible(T target) {
    return false;
  }

  /**
   * Used to run a validator against {@param target}.
   *
   * @param target The object to be passed into a validator.
   * @return True if valid, false if invalid.
   */
  default boolean isValid(T target) {
    return false;
  }

  /**
   * Determines if a label should be shown on the right of the entry in the task list.
   *
   * @param target The object to determine label visibility.
   * @return True if a label should be shown. False if no label is associated.
   */
  default boolean showLabel(T target) {
    return true;
  }

  /**
   * Attaches the TaskListItem to an appropriate TaskListSection implementation.
   *
   * @return Class of the section related to the item.
   */
  Class<? extends TaskListSection<T>> getTaskListSection();

}
