package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import java.util.Objects;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationUnitQueryService;

@Service
class AppointmentCorrectionValidator implements Validator {

  private final PortalOrganisationUnitQueryService portalOrganisationUnitQueryService;

  @Autowired
  AppointmentCorrectionValidator(PortalOrganisationUnitQueryService portalOrganisationUnitQueryService) {
    this.portalOrganisationUnitQueryService = portalOrganisationUnitQueryService;
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    var form = (AppointmentCorrectionForm) Objects.requireNonNull(target);
    var bindingResult = (BindingResult) Objects.requireNonNull(errors);

    ValidationUtils.rejectIfEmpty(
        bindingResult,
        "appointedOperatorId",
        "appointedOperatorId.required",
        "Select the appointed operator"
    );

    if (form.getAppointedOperatorId() != null) {

      var operator = portalOrganisationUnitQueryService.getOrganisationById(form.getAppointedOperatorId());

      if (operator.isEmpty() || operator.get().isDuplicate()) {
        errors.rejectValue(
            "appointedOperatorId",
            "appointedOperatorId.invalid",
            "Select a valid operator"
        );
      }
    }
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return AppointmentCorrectionForm.class.equals(clazz);
  }
}
