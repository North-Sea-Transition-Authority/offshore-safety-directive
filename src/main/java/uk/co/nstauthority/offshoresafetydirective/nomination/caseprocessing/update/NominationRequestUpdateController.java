package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.update;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNoUpdateRequest;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
// TODO OSDOP-811 @HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(
    statuses = NominationStatus.SUBMITTED,
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
@HasNoUpdateRequest
public class NominationRequestUpdateController {

  public static final String FORM_NAME = "nominationRequestUpdateForm";

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationDetailService nominationDetailService;
  private final NominationRequestUpdateValidator nominationRequestUpdateValidator;
  private final NominationRequestUpdateSubmissionService nominationRequestUpdateSubmissionService;
  private final FormErrorSummaryService formErrorSummaryService;

  @Autowired
  public NominationRequestUpdateController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      NominationDetailService nominationDetailService,
      NominationRequestUpdateValidator nominationRequestUpdateValidator,
      NominationRequestUpdateSubmissionService nominationRequestUpdateSubmissionService,
      FormErrorSummaryService formErrorSummaryService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationDetailService = nominationDetailService;
    this.nominationRequestUpdateValidator = nominationRequestUpdateValidator;
    this.nominationRequestUpdateSubmissionService = nominationRequestUpdateSubmissionService;
    this.formErrorSummaryService = formErrorSummaryService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.REQUEST_UPDATE)
  public ModelAndView requestUpdate(@PathVariable("nominationId") NominationId nominationId,
                                    @RequestParam("request-update") Boolean slideoutOpen,
                                    // Used for ReverseRouter to call correct route
                                    @Nullable
                                    @RequestParam(CaseProcessingActionIdentifier.REQUEST_UPDATE) String postButtonName,
                                    @Nullable @ModelAttribute(FORM_NAME) NominationRequestUpdateForm form,
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

    nominationRequestUpdateValidator.validate(form, bindingResult);

    if (Objects.requireNonNull(bindingResult).hasErrors()) {
      var modelAndViewDto = CaseProcessingFormDto.builder()
          .withNominationRequestUpdateForm(form)
          .build();
      return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
          nominationDetail,
          modelAndViewDto
      ).addObject("requestUpdateErrorList", formErrorSummaryService.getErrorItems(bindingResult));
    }

    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("An update has been requested for the nomination %s".formatted(
            nominationDetail.getNomination().getReference()
        ))
        .build();

    NotificationBannerUtil.applyNotificationBanner(
        Objects.requireNonNull(redirectAttributes),
        notificationBanner
    );

    nominationRequestUpdateSubmissionService.submit(nominationDetail, Objects.requireNonNull(form));

    return ReverseRouter.redirect(
        on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }
}
