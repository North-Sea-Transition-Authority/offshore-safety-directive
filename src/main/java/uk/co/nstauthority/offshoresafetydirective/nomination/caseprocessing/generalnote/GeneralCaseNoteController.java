package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = {NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION})
public class GeneralCaseNoteController {

  public static final String FORM_NAME = "generalCaseNoteForm";
  static final Set<NominationStatus> ALLOWED_STATUSES =
      EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION);

  private final NominationDetailService nominationDetailService;
  private final GeneralCaseNoteValidator generalCaseNoteValidator;
  private final ControllerHelperService controllerHelperService;
  private final GeneralCaseNoteSubmissionService generalCaseNoteSubmissionService;
  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final FileUploadService fileUploadService;

  public GeneralCaseNoteController(
      NominationDetailService nominationDetailService,
      GeneralCaseNoteValidator generalCaseNoteValidator,
      ControllerHelperService controllerHelperService,
      GeneralCaseNoteSubmissionService generalCaseNoteSubmissionService,
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      FileUploadService fileUploadService) {
    this.nominationDetailService = nominationDetailService;
    this.generalCaseNoteValidator = generalCaseNoteValidator;
    this.controllerHelperService = controllerHelperService;
    this.generalCaseNoteSubmissionService = generalCaseNoteSubmissionService;
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.fileUploadService = fileUploadService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.GENERAL_NOTE)
  public ModelAndView submitGeneralCaseNote(@PathVariable("nominationId") NominationId nominationId,
                                            @RequestParam("case-note") Boolean slideoutOpen,
                                            // Used for ReverseRouter to call correct route
                                            @Nullable
                                            @RequestParam(CaseProcessingActionIdentifier.GENERAL_NOTE) String postButtonName,

                                            @Nullable @ModelAttribute(FORM_NAME) GeneralCaseNoteForm generalCaseNoteForm,
                                            @Nullable BindingResult bindingResult,
                                            @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
    ).orElseThrow(() -> {

      var statusNames = ALLOWED_STATUSES.stream()
          .map(Enum::name)
          .collect(Collectors.joining(","));

      return new OsdEntityNotFoundException(String.format(
          "Cannot find latest NominationDetail with ID: %s in a status of: %s",
          nominationId.id(), statusNames
      ));
    });

    generalCaseNoteValidator.validate(generalCaseNoteForm, bindingResult);

    var formDto = CaseProcessingFormDto.builder()
        .withGeneralCaseNoteForm(generalCaseNoteForm)
        .build();

    var files = Objects.requireNonNull(generalCaseNoteForm).getCaseNoteFiles()
        .stream()
        .map(FileUploadForm::getUploadedFileId)
        .map(UploadedFileId::new)
        .toList();

    var modelAndView = nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        nominationDetail,
        formDto
    ).addObject("existingCaseNoteFiles", fileUploadService.getUploadedFileViewList(files));

    return controllerHelperService.checkErrorsAndRedirect(
        Objects.requireNonNull(bindingResult),
        modelAndView,
        generalCaseNoteForm,
        () -> {

          generalCaseNoteSubmissionService.submitCaseNote(nominationDetail,
              Objects.requireNonNull(generalCaseNoteForm));

          if (redirectAttributes != null) {

            var notificationBanner = NotificationBanner.builder()
                .withBannerType(NotificationBannerType.SUCCESS)
                .withHeading("A case note has been added to nomination %s".formatted(
                    nominationDetail.getNomination().getReference()
                ))
                .build();

            NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);

          }

          return ReverseRouter.redirect(
              on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId));
        }
    );
  }

}
