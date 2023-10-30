package uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaWellboreService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.validation.FrontEndErrorMessage;

@Component
class ExcludedWellValidator implements SmartValidator {

  static final FrontEndErrorMessage HAS_WELL_TO_EXCLUDE_REQUIRED = new FrontEndErrorMessage(
      "hasWellsToExclude",
      "%s.required".formatted("hasWellsToExclude"),
      "Select Yes if any wells are to be excluded from this nomination"
  );

  static final FrontEndErrorMessage WELL_TO_EXCLUDE_EMPTY = new FrontEndErrorMessage(
      "excludedWells",
      "%s.empty".formatted("excludedWells"),
      "You must select at least one well to exclude"
  );

  static final FrontEndErrorMessage NO_WELLS_IN_SUBAREAS = new FrontEndErrorMessage(
      "hasWellsToExclude",
      "%s.invalid".formatted("hasWellsToExclude"),
      "Select No to excluding wells as there are no wells in the selected subareas"
  );

  private final NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService;

  private final LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService;

  @Autowired
  ExcludedWellValidator(NominatedBlockSubareaAccessService nominatedBlockSubareaAccessService,
                        LicenceBlockSubareaWellboreService licenceBlockSubareaWellboreService) {
    this.nominatedBlockSubareaAccessService = nominatedBlockSubareaAccessService;
    this.licenceBlockSubareaWellboreService = licenceBlockSubareaWellboreService;
  }

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return WellExclusionForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors, @NonNull Object... validationHints) {

    var form = (WellExclusionForm) target;
    var validationHint = (ExcludedWellValidatorHint) validationHints[0];

    var hasWellsToExclude = BooleanUtils.toBooleanObject(form.hasWellsToExclude());

    if (hasWellsToExclude == null) {
      errors.rejectValue(
          HAS_WELL_TO_EXCLUDE_REQUIRED.field(),
          HAS_WELL_TO_EXCLUDE_REQUIRED.code(),
          HAS_WELL_TO_EXCLUDE_REQUIRED.message()
      );
    } else if (BooleanUtils.isTrue(hasWellsToExclude)) {

      var nominatedSubareaIds = nominatedBlockSubareaAccessService
          .getNominatedSubareaDtos(validationHint.nominationDetail())
          .stream()
          .map(NominatedBlockSubareaDto::subareaId)
          .toList();

      var relatedWellbores = licenceBlockSubareaWellboreService.getSubareaRelatedWellbores(nominatedSubareaIds);

      // if no wellbores are related to any subareas that are selected then the
      // users must select No to the wells to be excluded question
      if (CollectionUtils.isEmpty(relatedWellbores)) {
        errors.rejectValue(
            NO_WELLS_IN_SUBAREAS.field(),
            NO_WELLS_IN_SUBAREAS.code(),
            NO_WELLS_IN_SUBAREAS.message()
        );
      } else if (CollectionUtils.isEmpty(form.getExcludedWells())) {
        // if wellbores in subarea then at least one well must be selected to exclude
        errors.rejectValue(
            WELL_TO_EXCLUDE_EMPTY.field(),
            WELL_TO_EXCLUDE_EMPTY.code(),
            WELL_TO_EXCLUDE_EMPTY.message()
        );
      }
    }
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    throw new UnsupportedOperationException("ExcludedWellValidator requires validation hints to be provided");
  }
}