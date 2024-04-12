package uk.co.nstauthority.offshoresafetydirective.nomination.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDisplayType;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationRelatedToNomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationSummaryView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominationHasInstallations;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionType;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation.FinaliseNominatedSubareaWellsService;
import uk.co.nstauthority.offshoresafetydirective.summary.NominationSummaryView;

@Controller
@RequestMapping("nomination/{nominationId}/submit")
public class NominationSubmissionController {

  private final NominationSubmissionService nominationSubmissionService;
  private final NominationDetailService nominationDetailService;
  private final NominationSummaryService nominationSummaryService;
  private final FinaliseNominatedSubareaWellsService finaliseNominatedSubareaWellsService;
  private final CaseEventQueryService caseEventQueryService;
  private final UserDetailService userDetailService;
  private final NominationSubmissionFormValidator nominationSubmissionFormValidator;

  @Autowired
  public NominationSubmissionController(NominationSubmissionService nominationSubmissionService,
                                        NominationDetailService nominationDetailService,
                                        NominationSummaryService nominationSummaryService,
                                        FinaliseNominatedSubareaWellsService finaliseNominatedSubareaWellsService,
                                        CaseEventQueryService caseEventQueryService,
                                        UserDetailService userDetailService,
                                        NominationSubmissionFormValidator nominationSubmissionFormValidator) {
    this.nominationSubmissionService = nominationSubmissionService;
    this.nominationDetailService = nominationDetailService;
    this.nominationSummaryService = nominationSummaryService;
    this.finaliseNominatedSubareaWellsService = finaliseNominatedSubareaWellsService;
    this.caseEventQueryService = caseEventQueryService;
    this.userDetailService = userDetailService;
    this.nominationSubmissionFormValidator = nominationSubmissionFormValidator;
  }

  @GetMapping
  @HasNominationStatus(statuses = {
      NominationStatus.DRAFT,
      NominationStatus.SUBMITTED,
      NominationStatus.APPOINTED,
      NominationStatus.AWAITING_CONFIRMATION,
      NominationStatus.OBJECTED,
      NominationStatus.WITHDRAWN
  })
  public ModelAndView getSubmissionPage(@PathVariable("nominationId") NominationId nominationId,
                                        @ModelAttribute("form") NominationSubmissionForm form) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    if (NominationStatus.DRAFT != nominationDetail.getStatus()) {
      return ReverseRouter.redirect(on(NominationCaseProcessingController.class)
          .renderCaseProcessing(nominationId, null));
    }

    // Materialise the wellbores in any nominated subareas so applicants can check the wells that are included
    // on the nomination prior to submission. If we did this on submission the applicant will not know which
    // wells the service will include on the nomination until after submission.
    finaliseNominatedSubareaWellsService.finaliseNominatedSubareaWells(nominationDetail);

    nominationSubmissionService.populateSubmissionForm(form, nominationDetail);
    return getModelAndView(nominationId, nominationDetail, form);
  }

  @PostMapping
  @HasNominationStatus(statuses = NominationStatus.DRAFT)
  public ModelAndView submitNomination(@PathVariable("nominationId") NominationId nominationId,
                                       @ModelAttribute("form") NominationSubmissionForm form,
                                       BindingResult bindingResult) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    if (nominationSubmissionService.canSubmitNomination(nominationDetail)) {

      nominationSubmissionFormValidator.validate(form, bindingResult, nominationDetail);

      if (bindingResult.hasErrors()) {
        return getModelAndView(nominationId, nominationDetail, form);
      }

      nominationSubmissionService.submitNomination(nominationDetail, form);
      return ReverseRouter.redirect(
          on(NominationSubmitConfirmationController.class).getSubmissionConfirmationPage(nominationId));
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nomination cannot be submitted");
  }

  private ModelAndView getModelAndView(NominationId nominationId, NominationDetail nominationDetail,
                                       NominationSubmissionForm form) {

    var summaryView = nominationSummaryService.getNominationSummaryView(nominationDetail);

    var user = userDetailService.getUserDetail();
    // TODO OSDOP-811
    var userCanSubmitNominations = false;
//    = permissionService.hasPermissionForNomination(
//        nominationDetail,
//        user,
//        Set.of(RolePermission.CREATE_NOMINATION)
//    );
    var isSubmittable = nominationSubmissionService.canSubmitNomination(nominationDetail);
    var isFastTrackNomination = false;

    var modelAndView = new ModelAndView("osd/nomination/submission/submitNomination")
        .addObject("backLinkUrl", ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)))
        .addObject("actionUrl",
            ReverseRouter.route(on(NominationSubmissionController.class).submitNomination(nominationId, null, null)))
        .addObject("isSubmittable", isSubmittable)
        .addObject("summaryView", summaryView)
        .addObject("userCanSubmitNominations", userCanSubmitNominations)
        .addObject("hasLicenceBlockSubareas",
            WellSelectionType.LICENCE_BLOCK_SUBAREA.equals(summaryView.wellSummaryView().getWellSelectionType()));

//    if (isSubmittable && !userCanSubmitNominations) {
//      modelAndView
//          .addObject("organisationUrl",
//              ReverseRouter.route(on(TeamTypeSelectionController.class).renderTeamTypeSelection()));
//    }

    if (isSubmittable && userCanSubmitNominations) {

      String confirmationPrompt = getConfirmAuthorityPrompt(summaryView);

      modelAndView
          .addObject("form", form)
          .addObject("confirmAuthorityPrompt", confirmationPrompt);

      if (nominationSubmissionFormValidator.isNominationWithinFastTrackPeriod(nominationDetail)) {
        isFastTrackNomination = true;
      }
    }

    var submittedDetail = nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        EnumSet.of(NominationStatus.SUBMITTED)
    );

    submittedDetail
        .flatMap(caseEventQueryService::getLatestReasonForUpdate)
        .ifPresent(reason -> modelAndView.addObject("reasonForUpdate", reason));

    return modelAndView
        .addObject("isFastTrackNomination", isFastTrackNomination);
  }

  private String getConfirmAuthorityPrompt(NominationSummaryView summaryView) {

    var nominationHasInstallations = Optional.ofNullable(summaryView.installationSummaryView())
        .map(InstallationSummaryView::installationRelatedToNomination)
        .map(InstallationRelatedToNomination::related)
        .orElse(false);

    var nominationDisplayType = NominationDisplayType.getByWellSelectionTypeAndHasInstallations(
        summaryView.wellSummaryView().getWellSelectionType(),
        NominationHasInstallations.fromBoolean(nominationHasInstallations)
    );

    var confirmationPrompt = switch (nominationDisplayType) {
      case INSTALLATION ->
          "I hereby confirm that %s has the authority for and on behalf of all the relevant licensees" +
              " to nominate the installation operator for the selected installations";
      case WELL ->
          "I hereby confirm that %s has the authority for and on behalf of all the relevant licensees to nominate the " +
              "well operator for the selected wells";
      case WELL_AND_INSTALLATION ->
          "I hereby confirm that %s has the authority for and on behalf of all the relevant licensees to nominate the " +
              "well and installation operator for the selected wells and installations";
      case NOT_PROVIDED -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section is not submittable");
    };

    var applicantDisplayName = summaryView.applicantDetailSummaryView().applicantOrganisationUnitView().displayName();
    return confirmationPrompt.formatted(applicantDisplayName);
  }
}
