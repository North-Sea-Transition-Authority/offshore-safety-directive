package uk.co.nstauthority.offshoresafetydirective.tasklist;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TaskListSectionUtils {

  /**
   * Create a section view for a given section to be displayed on the frontend. Used from a section level.
   *
   * @param section       The section to generate the {@link TaskListSectionView} for.
   * @param taskListItems A list of {@link TaskListItem}
   * @return A generated {@link TaskListSectionView} object.
   */
  public static TaskListSectionView createSectionView(TaskListSection<?> section,
                                                      Collection<? extends TaskListItem<?>> taskListItems) {
    var itemViews = taskListItems.stream()
        .map(item -> new TaskListItemView(item.getDisplayOrder(), item.getName(), item.getActionUrl()))
        .sorted(Comparator.comparing(TaskListItemView::displayOrder))
        .toList();

    return new TaskListSectionView(section.getDisplayOrder(), section.getSectionName(), itemViews);
  }

  /**
   * Create section views when given a list of sections. Used from a controller level.
   *
   * @param sections The sections to generate a view for.
   * @param target   The object to pass into the task list items.
   * @param <T>      Helps to ensure type safety is kept when passing in the given object.
   * @return A list of generated {@link TaskListSectionView} objects.
   */
  public static <T> List<TaskListSectionView> createSectionViews(Collection<? extends TaskListSection<T>> sections,
                                                                 T target) {
    return sections.stream()
        .map(taskListSection -> taskListSection.getSectionView(target))
        // Hide sections without any items
        .filter(taskListSectionView -> !taskListSectionView.taskListItemViews().isEmpty())
        .sorted(Comparator.comparing(TaskListSectionView::displayOrder))
        .toList();
  }

  /**
   * Used to filter {@link TaskListItem}s for use within the section.
   *
   * @param taskListItems All items pulled in within the section.
   * @param target The object to pass into the isVisible call.
   * @param sectionClass The section class.
   * @param <T> The type of the object to pass into the isVisible call.
   * @return All visible items associated with the section.
   */
  public static <T> List<? extends TaskListItem<T>> getActiveTaskListItems(List<? extends TaskListItem<T>> taskListItems,
                                                                 T target, Class<?> sectionClass) {
    return taskListItems.stream()
        .filter(exampleTaskListItem -> exampleTaskListItem.getTaskListSection().equals(sectionClass))
        .filter(exampleTaskListItem -> exampleTaskListItem.isVisible(target))
        .toList();
  }

}
