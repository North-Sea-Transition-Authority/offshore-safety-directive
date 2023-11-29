package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.CanAccessDraftNomination;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;

@Controller
@RequestMapping("nomination/{nominationId}/installations")
@CanAccessDraftNomination
class InstallationJourneyManagerController {

  private final NominationDetailService nominationDetailService;

  private final InstallationInclusionAccessService installationInclusionAccessService;

  private final NominatedInstallationAccessService nominatedInstallationAccessService;

  @Autowired
  InstallationJourneyManagerController(NominationDetailService nominationDetailService,
                                       InstallationInclusionAccessService installationInclusionAccessService,
                                       NominatedInstallationAccessService nominatedInstallationAccessService) {
    this.nominationDetailService = nominationDetailService;
    this.installationInclusionAccessService = installationInclusionAccessService;
    this.nominatedInstallationAccessService = nominatedInstallationAccessService;
  }

  @GetMapping
  ModelAndView installationJourneyManager(@PathVariable NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() ->
            new OsdEntityNotFoundException(
                "Could not find a NominationDetail for nomination with ID %s".formatted(nominationId.id())
            )
        );

    var isNominationForInstallations = installationInclusionAccessService
        .getInstallationInclusion(nominationDetail)
        .map(installationInclusion -> BooleanUtils.isTrue(installationInclusion.getIncludeInstallationsInNomination()))
        .orElse(false);

    if (isNominationForInstallations && hasAddedInstallations(nominationDetail)) {
      return ReverseRouter.redirect(on(ManageInstallationsController.class).getManageInstallations(nominationId));
    } else {
      return ReverseRouter.redirect(on(InstallationInclusionController.class).getInstallationInclusion(nominationId));
    }
  }

  private boolean hasAddedInstallations(NominationDetail nominationDetail) {
    return !nominatedInstallationAccessService.getNominatedInstallations(nominationDetail).isEmpty();
  }
}
