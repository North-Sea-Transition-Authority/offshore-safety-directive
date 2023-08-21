package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@Service
class IndustryTeamMemberRolesValidator implements Validator {

  static final String ROLES_FIELD_NAME = "roles";

  static final String ROLES_REQUIRED_CODE = "%s.required".formatted(ROLES_FIELD_NAME);
  static final String ROLES_REQUIRED_ERROR_MESSAGE = "Select at least one action";
  static final String ROLE_INVALID_CODE = "%s.notValid".formatted(ROLES_FIELD_NAME);
  static final String ROLES_INVALID_ERROR_MESSAGE = "Select a valid action";

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return TeamMemberRolesForm.class.equals(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    var form = (TeamMemberRolesForm) target;

    var selectedRolesFromForm = form.getRoles();

    if (selectedRolesFromForm != null && !selectedRolesFromForm.isEmpty()) {

      // ensure all values posted are valid entries in the enum
      var validRolesFromForm = selectedRolesFromForm
          .stream()
          .filter(role -> IndustryTeamRole.getRoleFromString(role).isPresent())
          .count();

      if (selectedRolesFromForm.size() != validRolesFromForm) {
        errors.rejectValue(
            ROLES_FIELD_NAME,
            ROLE_INVALID_CODE,
            ROLES_INVALID_ERROR_MESSAGE
        );
      }
    } else {
      errors.rejectValue(
          ROLES_FIELD_NAME,
          ROLES_REQUIRED_CODE,
          ROLES_REQUIRED_ERROR_MESSAGE
      );
    }
  }
}
