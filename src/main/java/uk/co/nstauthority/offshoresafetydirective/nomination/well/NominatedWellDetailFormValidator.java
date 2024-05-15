package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@Service
class NominatedWellDetailFormValidator implements Validator {

  static final RequestPurpose WELL_QUERY_REQUEST_PURPOSE =
      new RequestPurpose("Validating selected wells exist on portal");

  private final WellQueryService wellQueryService;

  @Autowired
  NominatedWellDetailFormValidator(WellQueryService wellQueryService) {
    this.wellQueryService = wellQueryService;
  }

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return NominatedWellDetailForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    var form = (NominatedWellDetailForm) target;

    if (form.getWells() == null) {
      errors.rejectValue(
          "wellsSelect",
          "wellsSelect.notEmpty",
          "You must select at least one well"
      );
    } else {
      var numericWellIds = form.getWells()
          .stream()
          .filter(NumberUtils::isDigits)
          .toList();

      form.setWells(numericWellIds);

      if (form.getWells() == null || form.getWells().isEmpty()) {
        errors.rejectValue(
            "wellsSelect",
            "wellsSelect.notEmpty",
            "You must select at least one well"
        );
      } else {
        var wellboreIds = numericWellIds.stream()
            .map(Integer::parseInt)
            .map(WellboreId::new)
            .collect(Collectors.toSet());
        var wells = wellQueryService.getWellsByIds(wellboreIds, WELL_QUERY_REQUEST_PURPOSE);

        var allWellsFound = wellboreIds.stream()
            .allMatch(wellboreId -> wells.stream().anyMatch(wellDto -> wellDto.wellboreId().equals(wellboreId)));

        if (!allWellsFound) {
          errors.rejectValue(
              "wellsSelect",
              "wellsSelect.notAllSelectable",
              "You can only submit valid wells"
          );
        }
      }

      if (BooleanUtils.toBooleanObject(form.getForAllWellPhases()) == null) {
        errors.rejectValue(
            "forAllWellPhases",
            "forAllWellPhases.required",
            "Select Yes if this nomination is for all well activity phases"
        );
      } else if (BooleanUtils.isFalse(BooleanUtils.toBooleanObject(form.getForAllWellPhases()))
          && !anyNominationPhaseSelected(form)) {
        errors.rejectValue(
            "explorationAndAppraisalPhase",
            "explorationAndAppraisalPhase.required",
            "Select which well activity phases this nomination is for"
        );
      } else if (BooleanUtils.isFalse(BooleanUtils.toBooleanObject(form.getForAllWellPhases()))
          && allNominationPhasesSelected(form)) {
        errors.rejectValue(
            "forAllWellPhases",
            "forAllWellPhases.selectedAll",
            "Select Yes if all phases are applicable"
        );
      }
    }
  }

  private boolean anyNominationPhaseSelected(NominatedWellDetailForm form) {
    return !(BooleanUtils.toBooleanObject(form.getExplorationAndAppraisalPhase()) == null
        && BooleanUtils.toBooleanObject(form.getDevelopmentPhase()) == null
        && BooleanUtils.toBooleanObject(form.getDecommissioningPhase()) == null);
  }

  private boolean allNominationPhasesSelected(NominatedWellDetailForm form) {
    return (BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getExplorationAndAppraisalPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDevelopmentPhase()))
        && BooleanUtils.isTrue(BooleanUtils.toBooleanObject(form.getDecommissioningPhase())));
  }
}
