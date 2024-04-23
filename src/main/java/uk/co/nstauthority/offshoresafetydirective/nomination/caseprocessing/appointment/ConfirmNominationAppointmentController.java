package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.appointment;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.CanPerformRegulatorNominationAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
@CanPerformRegulatorNominationAction
@HasNominationStatus(
    statuses = NominationStatus.AWAITING_CONFIRMATION,
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class ConfirmNominationAppointmentController {

  public static final String FORM_NAME = "confirmAppointmentForm";

  private final NominationDetailService nominationDetailService;
  private final ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator;
  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final ConfirmNominationAppointmentSubmissionService confirmNominationAppointmentSubmissionService;
  private final FormErrorSummaryService formErrorSummaryService;

  @Autowired
  public ConfirmNominationAppointmentController(
      NominationDetailService nominationDetailService,
      ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator,
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      ConfirmNominationAppointmentSubmissionService confirmNominationAppointmentSubmissionService,
      FormErrorSummaryService formErrorSummaryService) {
    this.nominationDetailService = nominationDetailService;
    this.confirmNominationAppointmentValidator = confirmNominationAppointmentValidator;
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.confirmNominationAppointmentSubmissionService = confirmNominationAppointmentSubmissionService;
    this.formErrorSummaryService = formErrorSummaryService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT)
  public ModelAndView confirmAppointment(@PathVariable("nominationId") NominationId nominationId,
                                         @RequestParam("confirm-appointment") Boolean slideoutOpen,
                                         // Used for ReverseRouter to call correct route
                                         @Nullable
                                         @RequestParam(CaseProcessingActionIdentifier.CONFIRM_APPOINTMENT) String postButtonName,

                                         @Nullable @ModelAttribute(FORM_NAME)
                                         ConfirmNominationAppointmentForm confirmNominationAppointmentForm,
                                         BindingResult bindingResult,
                                         @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.AWAITING_CONFIRMATION)
    ).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Nomination [%s] has no detail with expected state [%s]".formatted(
            nominationId, NominationStatus.AWAITING_CONFIRMATION.name()
        )));

    var validatorHint = new ConfirmNominationAppointmentValidatorHint(nominationDetail);

    confirmNominationAppointmentValidator.validate(confirmNominationAppointmentForm, bindingResult, validatorHint);

    if (bindingResult.hasErrors()) {
      var modelAndViewDto = CaseProcessingFormDto.builder()
          .withConfirmNominationAppointmentForm(confirmNominationAppointmentForm)
          .build();

      var files = Objects.requireNonNull(confirmNominationAppointmentForm).getFiles();

      return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
          nominationDetail,
          modelAndViewDto
      )
          .addObject("confirmNominationFiles", files)
          .addObject("confirmAppointmentErrorList", formErrorSummaryService.getErrorItems(bindingResult));
    }

    var appointmentDate = Objects.requireNonNull(confirmNominationAppointmentForm).getAppointmentDate()
        .getAsLocalDate()
        .orElseThrow(() -> new IllegalStateException(
            "Expected date but got empty optional for form mapping for Nomination [%s]".formatted(
                nominationId.id()
            )));

    confirmNominationAppointmentSubmissionService.submitAppointmentConfirmation(nominationDetail,
        confirmNominationAppointmentForm);

    if (redirectAttributes != null) {

      var notificationBanner = NotificationBanner.builder()
          .withBannerType(NotificationBannerType.SUCCESS)
          .withHeading(
              "Appointment confirmed for nomination %s with effect from %s".formatted(
                  nominationDetail.getNomination().getReference(),
                  DateUtil.formatLongDate(appointmentDate)
              ))
          .build();

      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(
        on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }
}
