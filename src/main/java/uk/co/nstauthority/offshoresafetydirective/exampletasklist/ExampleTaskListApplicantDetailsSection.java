package uk.co.nstauthority.offshoresafetydirective.exampletasklist;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionUtils;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionView;

@Component
class ExampleTaskListApplicantDetailsSection implements ExampleTaskListSection {

  private final List<ExampleTaskListItem> taskListItems;

  @Autowired
  public ExampleTaskListApplicantDetailsSection(List<ExampleTaskListItem> taskListItems) {
    this.taskListItems = taskListItems;
  }

  @Override
  public String getSectionName() {
    return "Applicant details";
  }

  @Override
  public int getDisplayOrder() {
    return 10;
  }

  @Override
  public TaskListSectionView getSectionView(Void target) {
    var activeItems = TaskListSectionUtils.getActiveTaskListItems(taskListItems, target, this.getClass());
    return TaskListSectionUtils.createSectionView(this, activeItems);
  }


}
