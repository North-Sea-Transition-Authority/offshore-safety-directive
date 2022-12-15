package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/manage")
@HasPermission(permissions = {RolePermission.CREATE_NOMINATION, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(statuses = NominationStatus.SUBMITTED)
public class NominationCaseProcessingController {

  private final NominationDetailService nominationDetailService;
  private final NominationCaseProcessingService nominationCaseProcessingService;

  @Autowired
  public NominationCaseProcessingController(
      NominationDetailService nominationDetailService,
      NominationCaseProcessingService nominationCaseProcessingService) {
    this.nominationDetailService = nominationDetailService;
    this.nominationCaseProcessingService = nominationCaseProcessingService;
  }

  @GetMapping
  public ModelAndView renderCaseProcessing(@PathVariable("nominationId") NominationId nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var headerInformation = nominationCaseProcessingService.getNominationCaseProcessingHeader(nominationDetail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Unable to find %s for nomination with ID: [%d]".formatted(
                NominationCaseProcessingHeader.class.getSimpleName(),
                nominationId.id()
            )
        ));
    return new ModelAndView("osd/nomination/caseProcessing/caseProcessing")
        .addObject("headerInformation", headerInformation);
  }

}
