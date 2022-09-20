package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;

@Service
class WellSubmissionService implements NominationSectionSubmissionService {

  private final WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;
  private final NominatedBlockSubareaFormService nominatedBlockSubareaFormService;
  private final NominatedWellDetailFormService nominatedWellDetailFormService;

  private final NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Autowired
  WellSubmissionService(WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService,
                        NominatedBlockSubareaFormService nominatedBlockSubareaFormService,
                        NominatedWellDetailFormService nominatedWellDetailFormService,
                        NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService,
                        NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService) {
    this.wellSelectionSetupPersistenceService = wellSelectionSetupPersistenceService;
    this.nominatedBlockSubareaFormService = nominatedBlockSubareaFormService;
    this.nominatedWellDetailFormService = nominatedWellDetailFormService;
    this.nominatedWellDetailPersistenceService = nominatedWellDetailPersistenceService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaDetailPersistenceService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    return wellSelectionSetupPersistenceService.findByNominationDetail(nominationDetail)
        .map(wellSelectionSetup -> switch (wellSelectionSetup.getSelectionType()) {
          case NO_WELLS -> true;
          case SPECIFIC_WELLS -> isSpecificWellsJourneyComplete(nominationDetail);
          case LICENCE_BLOCK_SUBAREA -> isLicenceBlockSubareaJourneyComplete(nominationDetail);
        })
        .orElse(false);
  }

  @Override
  public void onSubmission(NominationDetail nominationDetail) {
    wellSelectionSetupPersistenceService.findByNominationDetail(nominationDetail).ifPresent(wellSelectionSetup -> {
      switch (wellSelectionSetup.getSelectionType()) {
        case NO_WELLS -> {
          nominatedWellDetailPersistenceService.deleteByNominationDetail(nominationDetail);
          nominatedBlockSubareaDetailPersistenceService.deleteByNominationDetail(nominationDetail);
        }
        case SPECIFIC_WELLS -> nominatedBlockSubareaDetailPersistenceService.deleteByNominationDetail(nominationDetail);
        case LICENCE_BLOCK_SUBAREA -> nominatedWellDetailPersistenceService.deleteByNominationDetail(nominationDetail);
        default -> throw new IllegalArgumentException(
            "An unknown WellSelectionType was provided: %s".formatted(wellSelectionSetup.getSelectionType())
        );
      }
    });
  }

  private boolean isSpecificWellsJourneyComplete(NominationDetail nominationDetail) {
    var nominatedWellDetailForm = nominatedWellDetailFormService.getForm(nominationDetail);
    var bindingResult = new BeanPropertyBindingResult(nominatedWellDetailForm, "form");
    nominatedWellDetailFormService.validate(nominatedWellDetailForm, bindingResult);
    return !bindingResult.hasErrors();
  }

  private boolean isLicenceBlockSubareaJourneyComplete(NominationDetail nominationDetail) {
    var licenceBlockSubareaForm = nominatedBlockSubareaFormService.getForm(nominationDetail);
    var bindingResult = new BeanPropertyBindingResult(licenceBlockSubareaForm, "form");
    nominatedBlockSubareaFormService.validate(licenceBlockSubareaForm, bindingResult);
    return !bindingResult.hasErrors();
  }
}
