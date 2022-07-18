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
   * Used to retrieve a read-only view of the current section and associated items.
   * The result is to be passed to the frontend.
   * <p>
   * Work is required inside this method when implementing a section as it needs to inject all package-specific
   * extensions of the {@link TaskListItem}.
   * </p>
   * <p>
   * The method should filter by a TaskListItem's {@code TaskListItem::getTaskListSection}
   * </p>
   * @param target The object to pass through to each associated project-scoped {@link TaskListItem}.
   * @return TaskListSectionView related to self.
   */
  TaskListSectionView getSectionView(T target);

}
