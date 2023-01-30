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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadForm;
import uk.co.nstauthority.offshoresafetydirective.file.FileUploadService;
import uk.co.nstauthority.offshoresafetydirective.file.UploadedFileId;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.AWAITING_CONFIRMATION)
public class ConfirmNominationAppointmentController {

  public static final String FORM_NAME = "confirmAppointmentForm";

  private final NominationDetailService nominationDetailService;
  private final ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator;
  private final ControllerHelperService controllerHelperService;
  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final FileUploadService fileUploadService;
  private final ConfirmNominationAppointmentSubmissionService confirmNominationAppointmentSubmissionService;

  @Autowired
  public ConfirmNominationAppointmentController(
      NominationDetailService nominationDetailService,
      ConfirmNominationAppointmentValidator confirmNominationAppointmentValidator,
      ControllerHelperService controllerHelperService,
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      FileUploadService fileUploadService,
      ConfirmNominationAppointmentSubmissionService confirmNominationAppointmentSubmissionService) {
    this.nominationDetailService = nominationDetailService;
    this.confirmNominationAppointmentValidator = confirmNominationAppointmentValidator;
    this.controllerHelperService = controllerHelperService;
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.fileUploadService = fileUploadService;
    this.confirmNominationAppointmentSubmissionService = confirmNominationAppointmentSubmissionService;
  }

  @PostMapping(params = CaseProcessingAction.CONFIRM_APPOINTMENT)
  public ModelAndView confirmAppointment(@PathVariable("nominationId") NominationId nominationId,
                                         @RequestParam("confirm-appointment") Boolean slideoutOpen,
                                         // Used for ReverseRouter to call correct route
                                         @Nullable @RequestParam(CaseProcessingAction.CONFIRM_APPOINTMENT) String postButtonName,
                                         @Nullable @ModelAttribute(FORM_NAME)
                                             ConfirmNominationAppointmentForm confirmNominationAppointmentForm,
                                         @Nullable BindingResult bindingResult,
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

    // TODO: OSDOP-266 - Include dto creation in supplier when creating invalid-state ModelAndView
    var modelAndViewDto = CaseProcessingFormDto.builder()
        .withConfirmNominationAppointmentForm(confirmNominationAppointmentForm)
        .build();

    var files = Objects.requireNonNull(confirmNominationAppointmentForm).getFiles()
        .stream()
        .map(FileUploadForm::getUploadedFileId)
        .map(UploadedFileId::new)
        .toList();

    var modelAndView = nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        nominationDetail,
        modelAndViewDto
    ).addObject("confirmNominationFiles", fileUploadService.getUploadedFileViewList(files));

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, confirmNominationAppointmentForm,
        () -> {

          var appointmentDate = confirmNominationAppointmentForm.getAppointmentDate()
              .getAsLocalDate()
              .orElseThrow(() -> new IllegalStateException(
                  "Expected date but got empty optional for form mapping for Nomination [%s]".formatted(
                      nominationId.id()
                  )));

          confirmNominationAppointmentSubmissionService.submitAppointmentConfirmation(nominationDetail,
              confirmNominationAppointmentForm);

          if (redirectAttributes != null) {

            var formattedDate = DateUtil.formatDate(appointmentDate);

            var notificationBanner = NotificationBanner.builder()
                .withBannerType(NotificationBannerType.SUCCESS)
                .withTitle("Appointment confirmed")
                .withHeading(
                    "Appointment confirmed for nomination %s on %s".formatted(
                        nominationDetail.getNomination().getReference(),
                        formattedDate
                    ))
                .build();

            NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
          }

          return ReverseRouter.redirect(
              on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId));

        });
  }

}
