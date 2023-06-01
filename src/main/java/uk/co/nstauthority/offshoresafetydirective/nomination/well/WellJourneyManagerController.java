package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasNominationStatus;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells.ManageWellsController;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@HasNominationStatus(statuses = NominationStatus.DRAFT)
@HasPermission(permissions = RolePermission.CREATE_NOMINATION)
@RequestMapping("/nomination/{nominationId}/wells")
class WellJourneyManagerController {

  private final NominationDetailService nominationDetailService;

  private final WellSelectionSetupAccessService wellSelectionSetupAccessService;

  private final NominatedWellAccessService nominatedWellAccessService;

  private final NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  @Autowired
  WellJourneyManagerController(NominationDetailService nominationDetailService,
                               WellSelectionSetupAccessService wellSelectionSetupAccessService,
                               NominatedWellAccessService nominatedWellAccessService,
                               NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService) {
    this.nominationDetailService = nominationDetailService;
    this.wellSelectionSetupAccessService = wellSelectionSetupAccessService;
    this.nominatedWellAccessService = nominatedWellAccessService;
    this.nominatedBlockSubareaAccessService = nominatedBlockSubareaAccessService;
  }

  @GetMapping
  ModelAndView wellJourneyManager(@PathVariable NominationId nominationId) {

    var nominationDetail = nominationDetailService.getLatestNominationDetailOptional(nominationId)
        .orElseThrow(() -> new OsdEntityNotFoundException(
            "Could not find NominationDetail for nomination with ID %s".formatted(nominationId.id())
        ));

    Optional<WellSelectionType> wellSelectionType = wellSelectionSetupAccessService
        .getWellSelectionType(nominationDetail);

    if (wellSelectionType.isEmpty()) {
      return getWellSetupRedirection(nominationId);
    }

    return switch (wellSelectionType.get()) {
      case NO_WELLS -> getWellSetupRedirection(nominationId);
      case SPECIFIC_WELLS -> determineSpecificWellRedirection(nominationId, nominationDetail);
      case LICENCE_BLOCK_SUBAREA -> determineLicenceBlockSubareaRedirection(nominationId, nominationDetail);
    };
  }

  private ModelAndView determineSpecificWellRedirection(NominationId nominationId,
                                                        NominationDetail nominationDetail) {
    if (nominatedWellAccessService.getNominatedWells(nominationDetail).isEmpty()) {
      return getWellSetupRedirection(nominationId);
    } else {
      return getWellManagementRedirection(nominationId);
    }
  }

  private ModelAndView determineLicenceBlockSubareaRedirection(NominationId nominationId,
                                                               NominationDetail nominationDetail) {
    if (nominatedBlockSubareaAccessService.getNominatedSubareaDtos(nominationDetail).isEmpty()) {
      return getWellSetupRedirection(nominationId);
    } else {
      return getWellManagementRedirection(nominationId);
    }
  }

  private ModelAndView getWellSetupRedirection(NominationId nominationId) {
    return ReverseRouter.redirect(on(WellSelectionSetupController.class).getWellSetup(nominationId));
  }

  private ModelAndView getWellManagementRedirection(NominationId nominationId) {
    return ReverseRouter.redirect(on(ManageWellsController.class).getWellManagementPage(nominationId));
  }
}
