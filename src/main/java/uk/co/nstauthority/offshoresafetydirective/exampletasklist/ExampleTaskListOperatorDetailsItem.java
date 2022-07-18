package uk.co.nstauthority.offshoresafetydirective.exampletasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Component;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSection;

@Component
class ExampleTaskListOperatorDetailsItem implements ExampleTaskListItem {

  @Override
  public String getName() {
    return "Operator details";
  }

  @Override
  public String getActionUrl() {
    return ReverseRouter.route(on(ExampleTaskListController.class).renderTaskList());
  }

  @Override
  public int getDisplayOrder() {
    return 10;
  }

  @Override
  public boolean isVisible(Void target) {
    return true;
  }

  @Override
  public boolean isValid(Void target) {
    return true;
  }

  @Override
  public boolean showLabel(Void target) {
    return true;
  }

  @Override
  public Class<? extends TaskListSection<Void>> getTaskListSection() {
    return ExampleTaskListApplicantDetailsSection.class;
  }

}
