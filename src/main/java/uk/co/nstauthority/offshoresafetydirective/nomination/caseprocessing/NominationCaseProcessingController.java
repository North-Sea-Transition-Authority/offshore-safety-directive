package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(statuses = {
    NominationStatus.SUBMITTED, NominationStatus.CLOSED, NominationStatus.AWAITING_CONFIRMATION,
    NominationStatus.WITHDRAWN
})
public class NominationCaseProcessingController {

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationCaseProcessingController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      NominationDetailService nominationDetailService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView renderCaseProcessing(@PathVariable("nominationId") NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    var modelAndViewDto = CaseProcessingFormDto.builder().build();
    return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        nominationDetail,
        modelAndViewDto
    );

  }

}
