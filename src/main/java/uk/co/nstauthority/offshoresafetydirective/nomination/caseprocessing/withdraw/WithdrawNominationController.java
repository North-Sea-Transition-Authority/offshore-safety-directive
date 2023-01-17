package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.withdraw;

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
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventService;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class WithdrawNominationController {

  public static final String FORM_NAME = "withdrawNominationForm";

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final ControllerHelperService controllerHelperService;
  private final NominationDetailService nominationDetailService;
  private final CaseEventService caseEventService;

  @Autowired
  public WithdrawNominationController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      ControllerHelperService controllerHelperService, NominationDetailService nominationDetailService,
      CaseEventService caseEventService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.controllerHelperService = controllerHelperService;
    this.nominationDetailService = nominationDetailService;
    this.caseEventService = caseEventService;
  }

  @PostMapping(params = CaseProcessingAction.WITHDRAW)
  public ModelAndView withdrawNomination(@PathVariable NominationId nominationId,
                                         @RequestParam("withdraw") Boolean slideoutOpen,
                                         @Nullable @RequestParam(CaseProcessingAction.WITHDRAW) String postButtonName,
                                         @Nullable @ModelAttribute("form") WithdrawNominationForm withdrawNominationForm,
                                         @Nullable BindingResult bindingResult,
                                         @Nullable RedirectAttributes redirectAttributes) {
    // TODO OSDOP-245 - Validate and process withdrawal
    return null;
  }
}
