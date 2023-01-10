package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationQaChecksController {

  public static final String FORM_NAME = "qaChecksForm";
  public static final String BINDING_RESULT_NAME = "%sBindingResult".formatted(FORM_NAME);

  private final CaseEventService caseEventService;
  private final NominationDetailService nominationDetailService;

  public NominationQaChecksController(
      CaseEventService caseEventService,
      NominationDetailService nominationDetailService) {
    this.caseEventService = caseEventService;
    this.nominationDetailService = nominationDetailService;
  }

  @PostMapping(params = CaseProcessingAction.QA)
  public ModelAndView submitQa(@PathVariable("nominationId") NominationId nominationId,
                               @Nullable @RequestParam(CaseProcessingAction.QA) String postButtonName,
                               @Nullable @ModelAttribute(FORM_NAME) NominationQaChecksForm nominationQaChecksForm,
                               @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    caseEventService.createCompletedQaChecksEvent(
        nominationDetail,
        Objects.requireNonNull(nominationQaChecksForm).getComment()
    );

    if (redirectAttributes != null) {
      var notificationBanner = NotificationBanner.builder()
          .withTitle(CaseEventType.QA_CHECKS.getScreenDisplayText())
          .withHeading("Successfully completed QA checks")
          .withBannerType(NotificationBannerType.SUCCESS)
          .build();
      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(on(NominationCaseProcessingController.class)
        .renderCaseProcessing(nominationId));
  }

}
