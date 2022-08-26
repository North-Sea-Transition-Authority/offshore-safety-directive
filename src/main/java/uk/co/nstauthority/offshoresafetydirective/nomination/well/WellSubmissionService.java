package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.submission.NominationSectionSubmissionService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea.NominatedBlockSubareaFormService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailFormService;

@Service
class WellSubmissionService implements NominationSectionSubmissionService {

  private final WellSelectionSetupService wellSelectionSetupService;
  private final NominatedBlockSubareaFormService nominatedBlockSubareaFormService;
  private final NominatedWellDetailFormService nominatedWellDetailFormService;

  @Autowired
  WellSubmissionService(WellSelectionSetupService wellSelectionSetupService,
                        NominatedBlockSubareaFormService nominatedBlockSubareaFormService,
                        NominatedWellDetailFormService nominatedWellDetailFormService) {
    this.wellSelectionSetupService = wellSelectionSetupService;
    this.nominatedBlockSubareaFormService = nominatedBlockSubareaFormService;
    this.nominatedWellDetailFormService = nominatedWellDetailFormService;
  }

  @Override
  public boolean isSectionSubmittable(NominationDetail nominationDetail) {
    var wellSelectionForm = wellSelectionSetupService.getForm(nominationDetail);
    if (StringUtils.isNotBlank(wellSelectionForm.getWellSelectionType())) {
      return switch (WellSelectionType.valueOf(wellSelectionForm.getWellSelectionType())) {
        case NO_WELLS -> true;
        case SPECIFIC_WELLS -> isSpecificWellsJourneyComplete(nominationDetail);
        case LICENCE_BLOCK_SUBAREA -> isLicenceBlockSubareaJourneyComplete(nominationDetail);
      };
    }
    return false;
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
