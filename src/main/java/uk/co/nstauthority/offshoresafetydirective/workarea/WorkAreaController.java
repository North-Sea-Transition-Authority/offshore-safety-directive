package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

@Controller
@RequestMapping("/work-area")
@AccessibleByServiceUsers
public class WorkAreaController {

  public static final String WORK_AREA_TITLE = "Work area";

  private final WorkAreaItemService workAreaItemService;

  private final UserDetailService userDetailService;

  @Autowired
  public WorkAreaController(WorkAreaItemService workAreaItemService,
                            UserDetailService userDetailService) {
    this.workAreaItemService = workAreaItemService;
    this.userDetailService = userDetailService;
  }

  @GetMapping
  public ModelAndView getWorkArea() {

    var modelAndView = new ModelAndView("osd/workarea/workArea")
        .addObject("workAreaItems", workAreaItemService.getWorkAreaItems());

    // TODO OSDOP-811
    var user = userDetailService.getUserDetail();

//    if (permissionService.hasPermission(user, Set.of(RolePermission.CREATE_NOMINATION))) {
      modelAndView.addObject(
          "startNominationUrl",
          ReverseRouter.route(on(StartNominationController.class).startNomination())
      );
//    }

    return modelAndView;
  }
}
