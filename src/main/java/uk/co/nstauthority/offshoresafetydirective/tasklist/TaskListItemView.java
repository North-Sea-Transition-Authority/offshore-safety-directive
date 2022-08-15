package uk.co.nstauthority.offshoresafetydirective.tasklist;

/**
 * This class is to be sent to the frontend to display the relevant information for each {@link TaskListItem}.
 */
public class TaskListItemView {

  private final int displayOrder;

  private final String displayName;

  private final String actionUrl;

  private final boolean showTaskListLabels;

  private final boolean isItemValid;

  private final boolean showNotCompletedLabel;

  private final TaskListLabel customTaskListLabel;

  private TaskListItemView(int displayOrder,
                           String displayName,
                           String actionUrl,
                           boolean showTaskListLabels,
                           boolean isItemValid,
                           boolean showNotCompletedLabel,
                           TaskListLabel customTaskListLabel) {
    this.displayOrder = displayOrder;
    this.displayName = displayName;
    this.actionUrl = actionUrl;
    this.showTaskListLabels = showTaskListLabels;
    this.isItemValid = isItemValid;
    this.showNotCompletedLabel = showNotCompletedLabel;
    this.customTaskListLabel = customTaskListLabel;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getActionUrl() {
    return actionUrl;
  }

  public boolean showTaskListLabels() {
    return showTaskListLabels;
  }

  public boolean isItemValid() {
    return isItemValid;
  }

  public boolean showNotCompletedLabel() {
    return showNotCompletedLabel;
  }

  public TaskListLabel getCustomTaskListLabel() {
    return customTaskListLabel;
  }

  public static class TaskListItemViewBuilder {

    private final int displayOrder;

    private final String displayName;

    private final String actionUrl;

    private boolean showTaskListLabels = false;

    private boolean isItemValid = false;

    private boolean showNotCompletedLabel = false;

    private TaskListLabel customTaskListLabel = null;

    public TaskListItemViewBuilder(int displayOrder, String displayName, String actionUrl) {
      this.displayOrder = displayOrder;
      this.displayName = displayName;
      this.actionUrl = actionUrl;
    }

    public TaskListItemViewBuilder withTaskListLabels(boolean showTaskListLabels) {
      this.showTaskListLabels = showTaskListLabels;
      return this;
    }

    public TaskListItemViewBuilder withItemValid(boolean isItemValid) {
      this.isItemValid = isItemValid;
      return this;
    }

    public TaskListItemViewBuilder withNotCompletedLabel(boolean showNotCompletedLabel) {
      this.showNotCompletedLabel = showNotCompletedLabel;
      return this;
    }

    public TaskListItemViewBuilder withCustomTaskListLabel(TaskListLabel taskListLabel) {
      this.customTaskListLabel = taskListLabel;
      return this;
    }

    public TaskListItemView build() {
      return new TaskListItemView(
          displayOrder,
          displayName,
          actionUrl,
          showTaskListLabels,
          isItemValid,
          showNotCompletedLabel,
          customTaskListLabel
      );
    }
  }
}
