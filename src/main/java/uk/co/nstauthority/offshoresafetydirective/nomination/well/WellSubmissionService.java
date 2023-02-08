package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellSubmissionService;

@Service
class WellSubmissionService implements NominationSectionSubmissionService {

  private final WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService;
  private final NominatedBlockSubareaFormService nominatedBlockSubareaFormService;
  private final NominatedWellDetailFormService nominatedWellDetailFormService;

  private final NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;

  private final NominatedWellPersistenceService nominatedWellPersistenceService;

  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  private final NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;

  private final ExcludedWellSubmissionService excludedWellSubmissionService;

  @Autowired
  WellSubmissionService(WellSelectionSetupPersistenceService wellSelectionSetupPersistenceService,
                        NominatedBlockSubareaFormService nominatedBlockSubareaFormService,
                        NominatedWellDetailFormService nominatedWellDetailFormService,
                        NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService,
                        NominatedWellPersistenceService nominatedWellPersistenceService,
                        NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService,
                        NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService,
                        ExcludedWellSubmissionService excludedWellSubmissionService) {
    this.wellSelectionSetupPersistenceService = wellSelectionSetupPersistenceService;
    this.nominatedBlockSubareaFormService = nominatedBlockSubareaFormService;
    this.nominatedWellDetailFormService = nominatedWellDetailFormService;
    this.nominatedWellDetailPersistenceService = nominatedWellDetailPersistenceService;
    this.nominatedWellPersistenceService = nominatedWellPersistenceService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaDetailPersistenceService;
    this.nominatedBlockSubareaPersistenceService = nominatedBlockSubareaPersistenceService;
    this.excludedWellSubmissionService = excludedWellSubmissionService;
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
          cleanUpSpecificWellData(nominationDetail);
          cleanUpLicenceBlockSubareaData(nominationDetail);
        }
        case SPECIFIC_WELLS -> cleanUpLicenceBlockSubareaData(nominationDetail);
        case LICENCE_BLOCK_SUBAREA -> cleanUpSpecificWellData(nominationDetail);
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
    return isNominatedBlockSubareaFormComplete(nominationDetail) && isExcludedWellsFormComplete(nominationDetail);
  }

  private boolean isNominatedBlockSubareaFormComplete(NominationDetail nominationDetail) {
    var licenceBlockSubareaForm = nominatedBlockSubareaFormService.getForm(nominationDetail);
    var bindingResult = new BeanPropertyBindingResult(licenceBlockSubareaForm, "form");
    nominatedBlockSubareaFormService.validate(licenceBlockSubareaForm, bindingResult);
    return !bindingResult.hasErrors();
  }

  private boolean isExcludedWellsFormComplete(NominationDetail nominationDetail) {
    return excludedWellSubmissionService.isExcludedWellJourneyComplete(nominationDetail);
  }

  private void cleanUpSpecificWellData(NominationDetail nominationDetail) {
    nominatedWellDetailPersistenceService.deleteByNominationDetail(nominationDetail);
    nominatedWellPersistenceService.deleteByNominationDetail(nominationDetail);
  }

  private void cleanUpLicenceBlockSubareaData(NominationDetail nominationDetail) {
    nominatedBlockSubareaDetailPersistenceService.deleteByNominationDetail(nominationDetail);
    nominatedBlockSubareaPersistenceService.deleteByNominationDetail(nominationDetail);
    excludedWellSubmissionService.cleanUpExcludedWellData(nominationDetail);
  }
}
