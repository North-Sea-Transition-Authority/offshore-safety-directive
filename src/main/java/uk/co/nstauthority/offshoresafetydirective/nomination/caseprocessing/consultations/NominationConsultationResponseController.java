package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.consultations;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;
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
@RequestMapping("/nomination/{nominationId}/review")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(
    statuses = NominationStatus.SUBMITTED,
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class NominationConsultationResponseController {

  public static final String FORM_NAME = "nominationConsultationResponseForm";

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationConsultationResponseValidator nominationConsultationResponseValidator;
  private final NominationConsultationResponseSubmissionService nominationConsultationResponseSubmissionService;
  private final FileUploadService fileUploadService;
  private final NominationDetailService nominationDetailService;
  private final FormErrorSummaryService formErrorSummaryService;

  @Autowired
  public NominationConsultationResponseController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      NominationConsultationResponseValidator nominationConsultationResponseValidator,
      NominationConsultationResponseSubmissionService nominationConsultationResponseSubmissionService,
      FileUploadService fileUploadService,
      NominationDetailService nominationDetailService,
      FormErrorSummaryService formErrorSummaryService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationConsultationResponseValidator = nominationConsultationResponseValidator;
    this.nominationConsultationResponseSubmissionService = nominationConsultationResponseSubmissionService;
    this.fileUploadService = fileUploadService;
    this.nominationDetailService = nominationDetailService;
    this.formErrorSummaryService = formErrorSummaryService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.CONSULTATION_RESPONSE)
  public ModelAndView addConsultationResponse(@PathVariable("nominationId") NominationId nominationId,
                                              @RequestParam("consultation-response") Boolean slideoutOpen,
                                              // Used for ReverseRouter to call correct route
                                              @Nullable
                                              @RequestParam(CaseProcessingActionIdentifier.CONSULTATION_RESPONSE)
                                              String postButtonName,

                                              @Nullable @ModelAttribute(FORM_NAME)
                                              NominationConsultationResponseForm form,
                                              @Nullable BindingResult bindingResult,
                                              @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.SUBMITTED)
    ).orElseThrow(() ->
        new OsdEntityNotFoundException(String.format(
            "Cannot find latest NominationDetail with nomination ID: %s and status: %s",
            nominationId.id(), NominationStatus.SUBMITTED.name()
        ))
    );

    nominationConsultationResponseValidator.validate(form, bindingResult);

    if (Objects.requireNonNull(bindingResult).hasErrors()) {
      var files = Objects.requireNonNull(form).getConsultationResponseFiles()
          .stream()
          .map(FileUploadForm::getUploadedFileId)
          .map(UploadedFileId::new)
          .toList();

      var modelAndViewDto = CaseProcessingFormDto.builder()
          .withNominationConsultationResponseForm(form)
          .build();
      return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
              nominationDetail,
              modelAndViewDto
          )
          .addObject("existingConsultationResponseFiles", fileUploadService.getUploadedFileViewList(files))
          .addObject("consultationResponseErrorList", formErrorSummaryService.getErrorItems(bindingResult));
    }

    nominationConsultationResponseSubmissionService.submitConsultationResponse(nominationDetail, Objects.requireNonNull(form));

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Added consultation response")
        .build();

    NotificationBannerUtil.applyNotificationBanner(
        Objects.requireNonNull(redirectAttributes),
        notificationBanner
    );

    return ReverseRouter.redirect(
        on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }

}
