package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(
    statuses = NominationStatus.SUBMITTED,
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class NominationQaChecksController {

  public static final String FORM_NAME = "qaChecksForm";

  private final CaseEventService caseEventService;
  private final NominationDetailService nominationDetailService;
  private final NominationQaChecksValidator nominationQaChecksValidator;
  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;

  public NominationQaChecksController(
      CaseEventService caseEventService,
      NominationDetailService nominationDetailService,
      NominationQaChecksValidator nominationQaChecksValidator,
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator) {
    this.caseEventService = caseEventService;
    this.nominationDetailService = nominationDetailService;
    this.nominationQaChecksValidator = nominationQaChecksValidator;
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.QA)
  public ModelAndView submitQa(@PathVariable("nominationId") NominationId nominationId,
                               @RequestParam("qa-checks") Boolean slideoutOpen,
                               @Nullable @RequestParam(CaseProcessingActionIdentifier.QA) String postButtonName,
                               @Nullable @ModelAttribute(FORM_NAME) NominationQaChecksForm nominationQaChecksForm,
                               @Nullable BindingResult bindingResult,
                               @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.SUBMITTED)
    ).orElseThrow(() ->
        new OsdEntityNotFoundException(String.format(
            "Cannot find latest NominationDetail with ID: %s and status: %s",
            nominationId.id(), NominationStatus.SUBMITTED.name()
        ))
    );

    nominationQaChecksValidator.validate(
        Objects.requireNonNull(nominationQaChecksForm),
        Objects.requireNonNull(bindingResult)
    );

    if (bindingResult.hasErrors()) {
      var formDto = CaseProcessingFormDto.builder()
          .withNominationQaChecksForm(nominationQaChecksForm)
          .build();
      return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
          nominationDetail,
          formDto
      );
    }

    caseEventService.createCompletedQaChecksEvent(
        nominationDetail,
        Objects.requireNonNull(nominationQaChecksForm).getComment().getInputValue()
    );

    if (redirectAttributes != null) {
      var notificationBanner = NotificationBanner.builder()
          .withHeading("Successfully completed QA checks")
          .withBannerType(NotificationBannerType.SUCCESS)
          .build();
      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(
        on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));

  }

}
