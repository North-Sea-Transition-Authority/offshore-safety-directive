package uk.co.nstauthority.offshoresafetydirective.tasklist;

public class TaskListTestUtil {

  private TaskListTestUtil() {
    throw new IllegalStateException("TaskListTestUtil is a static utility class and should not be instantiated");
  }

  public static TaskListItemView.TaskListItemViewBuilder getItemViewBuilder() {
    return getItemViewBuilder(10, "displayName", "actionUrl");
  }

  public static TaskListItemView.TaskListItemViewBuilder getItemViewBuilder(int displayOrder,
                                                                            String displayName,
                                                                            String actionUrl) {
    return new TaskListItemView.TaskListItemViewBuilder(displayOrder, displayName, actionUrl);
  }
}
