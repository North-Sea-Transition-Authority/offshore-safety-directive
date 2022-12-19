package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.qachecks;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage/submit-qa")
@HasPermission(permissions = RolePermission.MANAGE_NOMINATIONS)
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationQaChecksController {

  @PostMapping
  public ModelAndView submitQa(@PathVariable("nominationId") NominationId nominationId,
                               @ModelAttribute("form") NominationQaChecksForm nominationQaChecksForm) {
    // TODO - Persist QA checks answer
    return ReverseRouter.redirect(on(NominationCaseProcessingController.class).renderCaseProcessing(nominationId, null));
  }

}
