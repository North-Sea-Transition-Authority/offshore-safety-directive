package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.generalnote;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingAction;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = {NominationStatus.SUBMITTED, NominationStatus.AWAITING_CONFIRMATION})
public class GeneralCaseNoteController {

  public static final String FORM_NAME = "generalCaseNoteForm";

  @PostMapping(params = CaseProcessingAction.GENERAL_NOTE)
  public ModelAndView submitGeneralCaseNote(@PathVariable("nominationId") NominationId nominationId,
                                            @RequestParam("case-note") Boolean slideoutOpen,
                                            // Used for ReverseRouter to call correct route
                                            @Nullable @RequestParam(CaseProcessingAction.GENERAL_NOTE) String postButtonName,
                                            @Nullable @ModelAttribute(FORM_NAME) GeneralCaseNoteForm generalCaseNoteForm,
                                            @Nullable BindingResult bindingResult,
                                            @Nullable RedirectAttributes redirectAttributes) {

    return ReverseRouter.redirect(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId));
  }

}
