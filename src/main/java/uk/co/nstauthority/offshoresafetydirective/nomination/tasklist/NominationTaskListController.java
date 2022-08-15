package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionUtil;

@Controller
@RequestMapping("/nomination/{nominationId}/task-list")
public class NominationTaskListController {

  public static final String PAGE_NAME = "Task list";

  private final NominationDetailService nominationDetailService;

  private final List<NominationTaskListSection> nominationTaskListSections;

  private final List<NominationTaskListItem> nominationTaskListItems;

  @Autowired
  public NominationTaskListController(NominationDetailService nominationDetailService,
                                      List<NominationTaskListSection> nominationTaskListSections,
                                      List<NominationTaskListItem> nominationTaskListItems) {
    this.nominationDetailService = nominationDetailService;
    this.nominationTaskListSections = nominationTaskListSections;
    this.nominationTaskListItems = nominationTaskListItems;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable("nominationId") NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    var modelAndView = new ModelAndView("osd/nomination/tasklist/taskList")
        .addObject(
            "taskListSections",
            TaskListSectionUtil.createSectionViews(
                nominationTaskListSections,
                nominationTaskListItems,
                new NominationTaskListItemType(nominationDetail)
            )
        );

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_NAME)
        .addWorkAreaBreadcrumb()
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }
}
