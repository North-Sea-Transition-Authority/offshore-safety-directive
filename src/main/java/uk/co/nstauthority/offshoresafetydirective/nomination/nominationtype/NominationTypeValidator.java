package uk.co.nstauthority.offshoresafetydirective.nomination.nominationtype;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionFormService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupFormService;

@Service
public class NominationTypeValidator {

  private final WellSelectionSetupFormService wellSelectionSetupFormService;
  private final InstallationInclusionFormService installationInclusionFormService;

  @Autowired
  public NominationTypeValidator(WellSelectionSetupFormService wellSelectionSetupFormService,
                                 InstallationInclusionFormService installationInclusionFormService) {
    this.wellSelectionSetupFormService = wellSelectionSetupFormService;
    this.installationInclusionFormService = installationInclusionFormService;
  }

  /**
   * Add a validation error if cannot exclude a nomination type because other types have already
   * been excluded from the current nomination.
   *
   * @param errors            A reject error will be added in case the nomination type cannot be excluded
   * @param nominationDetail The detail of the nomination
   * @param nominationAssetType   The type of nomination trying to exclude
   * @param fieldName        Name of the field that will have the FieldError attached to it
   */
  public void validateNominationExclusionAssetTypes(Errors errors,
                                                    NominationDetail nominationDetail,
                                                    NominationAssetType nominationAssetType,
                                                    String fieldName) {
    switch (nominationAssetType) {
      case WELL -> {
        if (installationInclusionFormService.isNotRelatedToInstallationOperatorship(nominationDetail)) {
          addFieldError(errors, fieldName);
        }
      }
      case INSTALLATION -> {
        if (wellSelectionSetupFormService.isNotRelatedToWellOperatorship(nominationDetail)) {
          addFieldError(errors, fieldName);
        }
      }
      default -> throw new IllegalStateException("Unexpected value: " + nominationAssetType);
    }
  }

  private void addFieldError(Errors errors, String fieldName) {
    errors.rejectValue(
        fieldName,
        "%s.invalid".formatted(fieldName),
        "You must add at least one well or installation to your nomination"
    );
  }
}
