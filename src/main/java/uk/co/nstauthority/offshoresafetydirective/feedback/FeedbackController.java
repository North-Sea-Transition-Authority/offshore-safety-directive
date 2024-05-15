package uk.co.nstauthority.offshoresafetydirective.feedback;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBanner;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerType;
import uk.co.nstauthority.offshoresafetydirective.fds.notificationbanner.NotificationBannerUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasRoleInApplicantOrganisationGroupTeam;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@Controller
@RequestMapping
public class FeedbackController {

  public static final String PAGE_NAME = "Feedback";
  public static final int MAX_FEEDBACK_CHARACTER_LENGTH = 2000;
  private final FeedbackService feedbackService;
  private final FeedbackFormValidator feedbackFormValidator;
  private final UserDetailService userDetailService;
  private final NominationService nominationService;

  @Autowired
  public FeedbackController(FeedbackService feedbackService,
                            FeedbackFormValidator feedbackFormValidator,
                            UserDetailService userDetailService, NominationService nominationService) {
    this.feedbackService = feedbackService;
    this.feedbackFormValidator = feedbackFormValidator;
    this.userDetailService = userDetailService;
    this.nominationService = nominationService;
  }

  @GetMapping("/feedback")
  @AccessibleByServiceUsers
  public ModelAndView getFeedback(@ModelAttribute("form") FeedbackForm form) {
    return getFeedbackModelAndView(form);
  }

  @PostMapping("/feedback")
  @AccessibleByServiceUsers
  public ModelAndView submitFeedback(@ModelAttribute("form") FeedbackForm form,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {

    feedbackFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getFeedbackModelAndView(form);
    }

    feedbackService.saveFeedback(form, userDetailService.getUserDetail());

    applyNotificationBanner(redirectAttributes);
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea());
  }

  @GetMapping("/nomination/{nominationId}/feedback")
  @HasNominationStatus(statuses = NominationStatus.SUBMITTED)
  @HasRoleInApplicantOrganisationGroupTeam(roles = Role.NOMINATION_SUBMITTER)
  public ModelAndView getNominationFeedback(@PathVariable("nominationId") NominationId nominationId,
                                            @ModelAttribute("form") FeedbackForm form) {
    var nomination = nominationService.getNominationByIdOrError(nominationId);
    return getNominationFeedbackModelAndView(form, nomination);
  }

  @PostMapping("/nomination/{nominationId}/feedback")
  @HasNominationStatus(statuses = NominationStatus.SUBMITTED)
  @HasRoleInApplicantOrganisationGroupTeam(roles = Role.NOMINATION_SUBMITTER)
  public ModelAndView submitNominationFeedback(@PathVariable("nominationId") NominationId nominationId,
                                               @ModelAttribute("form") FeedbackForm form,
                                               BindingResult bindingResult,
                                               RedirectAttributes redirectAttributes) {
    var nomination = nominationService.getNominationByIdOrError(nominationId);
    feedbackFormValidator.validate(form, bindingResult);

    if (bindingResult.hasErrors()) {
      return getNominationFeedbackModelAndView(form, nomination);
    }

    feedbackService.saveFeedback(nomination, form, userDetailService.getUserDetail());

    applyNotificationBanner(redirectAttributes);
    return ReverseRouter.redirect(on(WorkAreaController.class).getWorkArea());
  }

  private ModelAndView getBaseModelAndView(FeedbackForm feedbackForm) {
    return new ModelAndView("osd/feedback/feedback")
        .addObject("form", feedbackForm)
        .addObject("pageName", PAGE_NAME)
        .addObject("maxCharacterLength", String.valueOf(MAX_FEEDBACK_CHARACTER_LENGTH))
        .addObject("serviceRatings",
            DisplayableEnumOptionUtil.getDisplayableOptions(ServiceFeedbackRating.class));
  }

  private ModelAndView getNominationFeedbackModelAndView(FeedbackForm feedbackForm, Nomination nomination) {
    return getBaseModelAndView(feedbackForm)
        .addObject("actionUrl", ReverseRouter.route(on(FeedbackController.class)
            .submitNominationFeedback(new NominationId(nomination.getId()), null, null, null)));
  }

  private ModelAndView getFeedbackModelAndView(FeedbackForm feedbackForm) {
    return getBaseModelAndView(feedbackForm)
        .addObject("actionUrl", ReverseRouter.route(on(FeedbackController.class)
            .submitFeedback(null, null, null)));
  }

  private void applyNotificationBanner(RedirectAttributes redirectAttributes) {
    var notificationBanner = NotificationBanner.builder()
        .withBannerType(NotificationBannerType.SUCCESS)
        .withHeading("Your feedback has been submitted")
        .build();

    NotificationBannerUtil.applyNotificationBanner(redirectAttributes, notificationBanner);
  }
}
