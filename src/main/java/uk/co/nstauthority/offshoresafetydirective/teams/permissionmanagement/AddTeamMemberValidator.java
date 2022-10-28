package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;

@Service
public class AddTeamMemberValidator implements SmartValidator {

  static final String USERNAME_FORM_FIELD_NAME = "username";

  static final String NO_USERNAME_ERROR_CODE = "%s.required".formatted(USERNAME_FORM_FIELD_NAME);
  static final String NO_USERNAME_ERROR_MESSAGE = "Enter an Energy Portal username";

  static final String USERNAME_NOT_FOUND_ERROR_CODE = "%s.notFound".formatted(USERNAME_FORM_FIELD_NAME);
  static final String USERNAME_NOT_FOUND_ERROR_MESSAGE = "No Energy Portal user exists with this username";

  static final String TOO_MANY_RESULTS_FOUND_ERROR_CODE = "%s.tooManyResults".formatted(USERNAME_FORM_FIELD_NAME);
  static final String TOO_MANY_RESULTS_FOUND_ERROR_MESSAGE =
      "More than one Energy Portal user exists with this email address. Enter the username of the user instead.";

  static final String SHARED_ACCOUNT_NOT_ALLOWED_ERROR_CODE = "%s.sharedAccountProhibited"
      .formatted(USERNAME_FORM_FIELD_NAME);

  static final String SHARED_ACCOUNT_NOT_ALLOWED_ERROR_MESSAGE = "You cannot add shared accounts to this service";

  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  AddTeamMemberValidator(EnergyPortalUserService energyPortalUserService) {
    this.energyPortalUserService = energyPortalUserService;
  }

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return AddTeamMemberForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors, @NonNull Object... validationHints) {

    var form = (AddTeamMemberForm) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        USERNAME_FORM_FIELD_NAME,
        NO_USERNAME_ERROR_CODE,
        NO_USERNAME_ERROR_MESSAGE
    );

    if (StringUtils.isNotBlank(form.getUsername())) {

      var resultingUsers = energyPortalUserService.findUserByUsername(form.getUsername());

      if (resultingUsers.isEmpty()) {
        errors.rejectValue(
            USERNAME_FORM_FIELD_NAME,
            USERNAME_NOT_FOUND_ERROR_CODE,
            USERNAME_NOT_FOUND_ERROR_MESSAGE
        );
      } else if (resultingUsers.size() > 1) {
        errors.rejectValue(
            USERNAME_FORM_FIELD_NAME,
            TOO_MANY_RESULTS_FOUND_ERROR_CODE,
            TOO_MANY_RESULTS_FOUND_ERROR_MESSAGE
        );
      } else if (resultingUsers.get(0).isSharedAccount()) {
        errors.rejectValue(
            USERNAME_FORM_FIELD_NAME,
            SHARED_ACCOUNT_NOT_ALLOWED_ERROR_CODE,
            SHARED_ACCOUNT_NOT_ALLOWED_ERROR_MESSAGE
        );
      }
    }

  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    validate(target, errors, new Object[0]);
  }
}