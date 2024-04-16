package uk.co.nstauthority.offshoresafetydirective.nomination.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.Breadcrumbs;
import uk.co.nstauthority.offshoresafetydirective.breadcrumb.BreadcrumbsUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.deletion.DeleteNominationController;
import uk.co.nstauthority.offshoresafetydirective.tasklist.TaskListSectionUtil;

@Controller
@RequestMapping("/nomination/{nominationId}/task-list")
@CanAccessDraftNomination
public class NominationTaskListController {

  public static final String PAGE_NAME = "Task list";

  private final NominationDetailService nominationDetailService;
  private final List<NominationTaskListSection> nominationTaskListSections;
  private final List<NominationTaskListItem> nominationTaskListItems;
  private final CaseEventQueryService caseEventQueryService;
  private final UserDetailService userDetailService;

  @Autowired
  public NominationTaskListController(NominationDetailService nominationDetailService,
                                      List<NominationTaskListSection> nominationTaskListSections,
                                      List<NominationTaskListItem> nominationTaskListItems,
                                      CaseEventQueryService caseEventQueryService,
                                      UserDetailService userDetailService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationTaskListSections = nominationTaskListSections;
    this.nominationTaskListItems = nominationTaskListItems;
    this.caseEventQueryService = caseEventQueryService;
    this.userDetailService = userDetailService;
  }

  @GetMapping
  public ModelAndView getTaskList(@PathVariable("nominationId") NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var nominationDetailDto = NominationDetailDto.fromNominationDetail(nominationDetail);

    var modelAndView = new ModelAndView("osd/nomination/tasklist/taskList")
        .addObject(
            "taskListSections",
            TaskListSectionUtil.createSectionViews(
                nominationTaskListSections,
                nominationTaskListItems,
                new NominationTaskListItemType(nominationDetail)
            )
        );

    var user = userDetailService.getUserDetail();

    var canDeleteNomination = true;
    // TODO OSDOP-811
//    var canDeleteNomination = permissionService.hasPermissionForNomination(
//        nominationDetail,
//        user,
//        Collections.singleton(RolePermission.CREATE_NOMINATION)
//    );

    if (canDeleteNomination) {
      modelAndView
          .addObject("deleteNominationUrl",
              ReverseRouter.route(on(DeleteNominationController.class).renderDeleteNomination(nominationId))
          )
          .addObject(
              "deleteNominationButtonPrompt",
              (nominationDetailDto.version() == 1) ? "Delete nomination" : "Delete draft update"
          );
    }

    // The first version of the nomination would not have been submitted at this point
    // so no need to check if an update request has been made.
    if (nominationDetailDto.version() != null && nominationDetailDto.version().intValue() > 1) {
      var submittedDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
          nominationId,
          EnumSet.of(NominationStatus.SUBMITTED)
      );

      submittedDetail
          .flatMap(caseEventQueryService::getLatestReasonForUpdate)
          .ifPresent(reason -> modelAndView.addObject("reasonForUpdate", reason));
    }

    var breadcrumbs = new Breadcrumbs.BreadcrumbsBuilder(PAGE_NAME)
        .addWorkAreaBreadcrumb()
        .build();

    BreadcrumbsUtil.addBreadcrumbsToModel(modelAndView, breadcrumbs);

    return modelAndView;
  }
}
