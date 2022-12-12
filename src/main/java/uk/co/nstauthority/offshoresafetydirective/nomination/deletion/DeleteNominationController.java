package uk.co.nstauthority.offshoresafetydirective.nomination.deletion;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSummaryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Controller
@RequestMapping("nomination/{nominationId}/delete")
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
public class DeleteNominationController {

  private final NominationDetailService nominationDetailService;
  private final NominationSummaryService nominationSummaryService;

  @Autowired
  public DeleteNominationController(NominationDetailService nominationDetailService,
                                    NominationSummaryService nominationSummaryService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationSummaryService = nominationSummaryService;
  }

  @GetMapping
  public ModelAndView renderDeleteNomination(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var nominationSummaryView = nominationSummaryService.getNominationSummaryView(nominationDetail);
    return new ModelAndView("osd/nomination/deletion/deleteNomination")
        .addObject("cancelUrl", ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)))
        .addObject("deleteUrl", ReverseRouter.route(on(this.getClass()).deleteNomination(nominationId, null)))
        .addObject("nominationSummaryView", nominationSummaryView);
  }

  @PostMapping
  public ModelAndView deleteNomination(@PathVariable("nominationId") NominationId nominationId,
                                       @Nullable RedirectAttributes redirectAttributes) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    nominationDetailService.deleteNominationDetail(nominationDetail);

    if (redirectAttributes != null) {
      var notificationBanner = NotificationBanner.builder()
          .withBannerType(NotificationBannerType.SUCCESS)
          .withTitle("Successfully deleted draft nomination")
          .withHeading("Deleted draft nomination created on %s"
              .formatted(DateUtil.formatDateTime(nominationDetail.getCreatedInstant()))
          )
          .build();
      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea());
  }

}
