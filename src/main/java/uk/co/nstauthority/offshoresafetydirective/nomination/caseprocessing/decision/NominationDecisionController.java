package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.decision;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks.NominationQaChecksForm;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationDecisionController {

  public static final String FORM_NAME = "nominationDecisionForm";
  public static final String BINDING_RESULT_NAME = "%sBindingResult".formatted(FORM_NAME);

  private final NominationDecisionValidator nominationDecisionValidator;
  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationDecisionController(NominationDecisionValidator nominationDecisionValidator,
                                      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
                                      ControllerHelperService controllerHelperService,
                                      NominationDetailService nominationDetailService) {
    this.nominationDecisionValidator = nominationDecisionValidator;
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
  }

  @PostMapping(params = CaseProcessingAction.DECISION)
  public ModelAndView submitDecision(@PathVariable("nominationId") NominationId nominationId,
                                     @RequestParam("decision") Boolean slideoutOpen,
                                     // Used for ReverseRouter to call correct route
                                     @Nullable @RequestParam(CaseProcessingAction.DECISION) String postButtonName,
                                     @Nullable @ModelAttribute(FORM_NAME) NominationDecisionForm nominationDecisionForm,
                                     @Nullable BindingResult bindingResult) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    nominationDecisionValidator.validate(
        Objects.requireNonNull(nominationDecisionForm),
        Objects.requireNonNull(bindingResult),
        new NominationDecisionValidatorHint(nominationDetail)
    );

    var modelAndView = nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(nominationDetail,
        new NominationQaChecksForm(), nominationDecisionForm);

    // TODO OSDOP-243 - Add persisted case event on submit for decision
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, nominationDecisionForm,
        () -> ReverseRouter.redirect(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId)));
  }


}
