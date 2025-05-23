package uk.co.nstauthority.offshoresafetydirective.nomination.consultee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.InvokingUserHasAtLeastOneStaticRole;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.CaseProcessingFormDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.NominationCaseProcessingModelAndViewGenerator;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Controller
@RequestMapping("/nomination/{nominationId}/consultation")
@InvokingUserHasAtLeastOneStaticRole(
    teamType = TeamType.CONSULTEE,
    roles = { Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT }
)
@HasNominationStatus(
    statuses = {
        NominationStatus.SUBMITTED,
        NominationStatus.APPOINTED,
        NominationStatus.AWAITING_CONFIRMATION,
        NominationStatus.WITHDRAWN,
        NominationStatus.OBJECTED
    },
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION
)
public class NominationConsulteeViewController {

  private final NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator;
  private final NominationDetailService nominationDetailService;

  @Autowired
  public NominationConsulteeViewController(
      NominationCaseProcessingModelAndViewGenerator nominationCaseProcessingModelAndViewGenerator,
      NominationDetailService nominationDetailService) {
    this.nominationCaseProcessingModelAndViewGenerator = nominationCaseProcessingModelAndViewGenerator;
    this.nominationDetailService = nominationDetailService;
  }

  @GetMapping
  public ModelAndView renderNominationView(@PathVariable("nominationId") NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);

    var modelAndViewDto = CaseProcessingFormDto.builder().build();
    return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        nominationDetail,
        modelAndViewDto
    );

  }
}
