package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations.request;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
// TODO OSDOP-811 @HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(
    statuses = NominationStatus.SUBMITTED,
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class NominationConsultationRequestController {

  private final NominationDetailService nominationDetailService;
  private final ConsultationRequestService consultationRequestService;

  @Autowired
  public NominationConsultationRequestController(NominationDetailService nominationDetailService,
                                                 ConsultationRequestService consultationRequestService) {
    this.nominationDetailService = nominationDetailService;
    this.consultationRequestService = consultationRequestService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION)
  public ModelAndView requestConsultation(@PathVariable("nominationId") NominationId nominationId,
                                          @RequestParam("send-for-consultation") Boolean slideoutOpen,
                                          // Used for ReverseRouter to call correct route
                                          @Nullable
                                          @RequestParam(CaseProcessingActionIdentifier.SEND_FOR_CONSULTATION)
                                          String postButtonName,

                                          @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
            nominationId,
            Set.of(NominationStatus.SUBMITTED)
        )
        .orElseThrow(() -> new OsdEntityNotFoundException(
            "No nomination detail found for nomination [%s] with a post submission status".formatted(
                nominationId.id()
            )
        ));

    consultationRequestService.requestConsultation(nominationDetail);

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Consultation request sent")
        .build();

    NotificationBannerUtil.applyNotificationBanner(Objects.requireNonNull(redirectAttributes), notificationBanner);

    return ReverseRouter.redirect(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }

}
