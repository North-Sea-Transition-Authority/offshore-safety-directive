package uk.co.nstauthority.offshoresafetydirective.exampletasklist;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionUtils;

@Controller
@RequestMapping("/example")
public class ExampleTaskListController {

  private final List<ExampleTaskListSection> taskListSections;

  @Autowired
  public ExampleTaskListController(
      List<ExampleTaskListSection> taskListSections) {
    this.taskListSections = taskListSections;
  }

  @GetMapping("/task-list")
  public ModelAndView renderTaskList() {
    return new ModelAndView("osd/tasklist/taskList")
        .addObject("taskListSections", TaskListSectionUtils.createSectionViews(taskListSections, null));
  }

}
