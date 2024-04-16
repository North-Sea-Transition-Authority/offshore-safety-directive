package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

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
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.fds.FormErrorSummaryService;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNoUpdateRequest;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationDetailFetchType;
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
public class NominationDecisionController {

  public static final String FORM_NAME = "form";

  private final NominationDecisionValidator nominationDecisionValidator;
  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationDetailService nominationDetailService;
  private final NominationDecisionSubmissionService nominationDecisionSubmissionService;
  private final FormErrorSummaryService formErrorSummaryService;

  @Autowired
  public NominationDecisionController(NominationDecisionValidator nominationDecisionValidator,
                                      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
                                      NominationDetailService nominationDetailService,
                                      NominationDecisionSubmissionService nominationDecisionSubmissionService,
                                      FormErrorSummaryService formErrorSummaryService) {
    this.nominationDecisionValidator = nominationDecisionValidator;
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationDetailService = nominationDetailService;
    this.nominationDecisionSubmissionService = nominationDecisionSubmissionService;
    this.formErrorSummaryService = formErrorSummaryService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.DECISION)
  public ModelAndView submitDecision(@PathVariable("nominationId") NominationId nominationId,
                                     @RequestParam("decision") Boolean slideoutOpen,
                                     // Used for ReverseRouter to call correct route
                                     @Nullable @RequestParam(CaseProcessingActionIdentifier.DECISION) String postButtonName,
                                     @Nullable @ModelAttribute(FORM_NAME) NominationDecisionForm nominationDecisionForm,
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

    nominationDecisionValidator.validate(
        Objects.requireNonNull(nominationDecisionForm),
        Objects.requireNonNull(bindingResult),
        new NominationDecisionValidatorHint(nominationDetail)
    );

    if (bindingResult.hasErrors()) {
      var files = Objects.requireNonNull(nominationDecisionForm).getDecisionFiles();

      var modelAndViewDto = CaseProcessingFormDto.builder()
          .withNominationDecisionForm(nominationDecisionForm)
          .build();
      return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
              nominationDetail,
              modelAndViewDto
          )
          .addObject("decisionFiles", files)
          .addObject("decisionErrorList", formErrorSummaryService.getErrorItems(bindingResult));
    }

    nominationDecisionSubmissionService.submitNominationDecision(nominationDetail, nominationDecisionForm);

    if (redirectAttributes != null) {
      var notificationBanner = NotificationBanner.builder()
          .withHeading("Decision submitted for %s".formatted(
              nominationDetail.getNomination().getReference()))
          .withBannerType(NotificationBannerType.SUCCESS)
          .build();

      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(
        on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }
}
