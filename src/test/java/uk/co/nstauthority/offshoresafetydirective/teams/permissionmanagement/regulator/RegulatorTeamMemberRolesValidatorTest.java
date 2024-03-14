package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

class RegulatorTeamMemberRolesValidatorTest {

  private static RegulatorTeamMemberRolesValidator regulatorTeamMemberRolesValidator;

  @BeforeAll
  static void setup() {
    regulatorTeamMemberRolesValidator = new RegulatorTeamMemberRolesValidator();
  }

  @Test
  void supports_whenSupported_thenTrue() {
    assertTrue(regulatorTeamMemberRolesValidator.supports(TeamMemberRolesForm.class));
  }

  @Test
  void supports_whenNotSupported_thenFalse() {
    assertFalse(regulatorTeamMemberRolesValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenNoRolesSelected_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of());

    var bindingResult = validateTeamMemberRolesForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RegulatorTeamMemberRolesValidator.ROLES_FIELD_NAME,
                RegulatorTeamMemberRolesValidator.ROLES_REQUIRED_CODE,
                RegulatorTeamMemberRolesValidator.ROLES_REQUIRED_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenNullRoles_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(null);

    var bindingResult = validateTeamMemberRolesForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RegulatorTeamMemberRolesValidator.ROLES_FIELD_NAME,
                RegulatorTeamMemberRolesValidator.ROLES_REQUIRED_CODE,
                RegulatorTeamMemberRolesValidator.ROLES_REQUIRED_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenRolesNotInEnum_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of("NOT_FROM_ENUM"));

    var bindingResult = validateTeamMemberRolesForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RegulatorTeamMemberRolesValidator.ROLES_FIELD_NAME,
                RegulatorTeamMemberRolesValidator.ROLE_INVALID_CODE,
                RegulatorTeamMemberRolesValidator.ROLES_INVALID_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenSomeRolesNotInEnum_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of("NOT_FROM_ENUM", RegulatorTeamRole.ACCESS_MANAGER.name()));

    var bindingResult = validateTeamMemberRolesForm(form);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(
                RegulatorTeamMemberRolesValidator.ROLES_FIELD_NAME,
                RegulatorTeamMemberRolesValidator.ROLE_INVALID_CODE,
                RegulatorTeamMemberRolesValidator.ROLES_INVALID_ERROR_MESSAGE
            )
        );
  }

  @Test
  void validate_whenValidRolesSelected_thenNoError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of(RegulatorTeamRole.ACCESS_MANAGER.name()));

    var bindingResult = validateTeamMemberRolesForm(form);

    assertThat(bindingResult.hasFieldErrors()).isFalse();
  }

  private BindingResult validateTeamMemberRolesForm(TeamMemberRolesForm teamMemberRolesForm) {
    var bindingResult = new BeanPropertyBindingResult(teamMemberRolesForm, "form");
    regulatorTeamMemberRolesValidator.validate(teamMemberRolesForm, bindingResult);
    return bindingResult;
  }

  static class NonSupportedClass {}

}