package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.authorisation.NominationDetailFetchType;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/nomination/{nominationId}/review")
@HasPermission(permissions = {RolePermission.MANAGE_NOMINATIONS, RolePermission.VIEW_NOMINATIONS})
@HasNominationStatus(
    fetchType = NominationDetailFetchType.LATEST_POST_SUBMISSION,
    statuses = {
        NominationStatus.SUBMITTED, NominationStatus.CLOSED, NominationStatus.AWAITING_CONFIRMATION,
        NominationStatus.WITHDRAWN
    }
)
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
  public ModelAndView renderCaseProcessing(@PathVariable("nominationId") NominationId nominationId,
                                           @RequestParam(value = "version", required = false) Integer version) {

    var nominationDetailVersion = Optional.ofNullable(version);

    var nominationDetail = nominationDetailVersion
        .map(versionNumber -> getVersionedPostSubmissionNominationDetail(nominationId, versionNumber))
        .orElseGet(() -> getLatestPostSubmissionNominationDetail(nominationId));

    var modelAndViewDto = CaseProcessingFormDto.builder().build();
    return nominationCaseProcessingModelAndViewGenerator.getCaseProcessingModelAndView(
        nominationDetail,
        modelAndViewDto
    );

  }

  private NominationDetail getVersionedPostSubmissionNominationDetail(NominationId nominationId, Integer version) {
    return nominationDetailService.getVersionedNominationDetailWithStatuses(
        nominationId,
        version,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "No NominationDetail in a post submission status found for Nomination [%s] with version [%d]".formatted(
            nominationId,
            version
        )
    ));
  }

  private NominationDetail getLatestPostSubmissionNominationDetail(NominationId nominationId) {
    return nominationDetailService.getLatestNominationDetailWithStatuses(
        nominationId,
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ).orElseThrow(() -> new OsdEntityNotFoundException(
        "No NominationDetail found with Nomination ID [%d] in a post submission status".formatted(
            nominationId.id()
        )
    ));
  }

}
