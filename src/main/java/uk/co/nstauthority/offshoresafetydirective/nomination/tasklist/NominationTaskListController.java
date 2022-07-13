package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/nomination/task-list")
public class NominationTaskListController {

  @GetMapping
  public ModelAndView getTaskList() {
    return new ModelAndView("osd/nomination/tasklist/taskList");
  }
}
