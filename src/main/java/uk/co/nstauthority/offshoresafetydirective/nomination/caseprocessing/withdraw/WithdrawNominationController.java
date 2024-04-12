package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

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
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.action.CaseProcessingActionIdentifier;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
// TODO OSDOP-811 @HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(
    statuses = {NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION},
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class WithdrawNominationController {

  public static final String FORM_NAME = "withdrawNominationForm";

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationDetailService nominationDetailService;
  private final CaseEventService caseEventService;
  private final WithdrawNominationValidator withdrawNominationValidator;
  private final FormErrorSummaryService formErrorSummaryService;

  @Autowired
  public WithdrawNominationController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      NominationDetailService nominationDetailService,
      CaseEventService caseEventService,
      WithdrawNominationValidator withdrawNominationValidator, FormErrorSummaryService formErrorSummaryService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationDetailService = nominationDetailService;
    this.caseEventService = caseEventService;
    this.withdrawNominationValidator = withdrawNominationValidator;
    this.formErrorSummaryService = formErrorSummaryService;
  }

  @PostMapping(params = CaseProcessingActionIdentifier.WITHDRAW)
  public ModelAndView withdrawNomination(@PathVariable NominationId nominationId,
                                         @RequestParam("withdraw") Boolean slideoutOpen,
                                         @Nullable @RequestParam(CaseProcessingActionIdentifier.WITHDRAW) String postButtonName,
                                         @Nullable @ModelAttribute(FORM_NAME) WithdrawNominationForm withdrawNominationForm,
                                         @Nullable BindingResult bindingResult,
                                         @Nullable RedirectAttributes redirectAttributes) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION)
    ).orElseThrow(() ->
        new OsdEntityNotFoundException(String.format(
            "Cannot find latest NominationDetail with ID: %s and status: %s",
            nominationId.id(), NominationStatus.SUBMITTED.name()
        ))
    );

    withdrawNominationValidator.validate(
        Objects.requireNonNull(withdrawNominationForm),
        Objects.requireNonNull(bindingResult)
    );

    if (bindingResult.hasErrors()) {
      var modelAndViewDto = CaseProcessingFormDto.builder()
          .withWithdrawNominationForm(withdrawNominationForm)
          .build();

      return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
          nominationDetail,
          modelAndViewDto
      ).addObject("withdrawNominationErrorList", formErrorSummaryService.getErrorItems(bindingResult));
    }

    caseEventService.createWithdrawEvent(nominationDetail, withdrawNominationForm.getReason().getInputValue());
    nominationDetailService.withdrawNominationDetail(nominationDetail);

    if (redirectAttributes != null) {
      var notificationBanner = NotificationBanner.builder()
          .withBannerType(NotificationBannerType.SUCCESS)
          .withHeading("Withdrawn nomination %s".formatted(nominationDetail.getNomination().getReference()))
          .build();

      NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
    }

    return ReverseRouter.redirect(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }
}
