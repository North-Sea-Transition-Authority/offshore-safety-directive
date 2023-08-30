package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;
import uk.co.nstauthority.offshoresafetydirective.util.ValidatorTestingUtil;

class ConsulteeTeamMemberRolesValidatorTest {

  private static ConsulteeTeamMemberRolesValidator consulteeTeamMemberRolesValidator;

  @BeforeAll
  static void setup() {
    consulteeTeamMemberRolesValidator = new ConsulteeTeamMemberRolesValidator();
  }

  @Test
  void supports_whenSupported_thenTrue() {
    assertTrue(consulteeTeamMemberRolesValidator.supports(TeamMemberRolesForm.class));
  }

  @Test
  void supports_whenNotSupported_thenFalse() {
    assertFalse(consulteeTeamMemberRolesValidator.supports(NonSupportedClass.class));
  }

  @Test
  void validate_whenNoRolesSelected_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of());

    var bindingResult = validateTeamMemberRolesForm(form);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLES_REQUIRED_CODE)
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLES_REQUIRED_ERROR_MESSAGE)
        )
    );
  }

  @Test
  void validate_whenNullRoles_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(null);

    var bindingResult = validateTeamMemberRolesForm(form);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLES_REQUIRED_CODE)
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLES_REQUIRED_ERROR_MESSAGE)
        )
    );
  }

  @Test
  void validate_whenRolesNotInEnum_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of("NOT_FROM_ENUM"));

    var bindingResult = validateTeamMemberRolesForm(form);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLE_INVALID_CODE)
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLES_INVALID_ERROR_MESSAGE)
        )
    );
  }

  @Test
  void validate_whenSomeRolesNotInEnum_thenError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of("NOT_FROM_ENUM", ConsulteeTeamRole.ACCESS_MANAGER.name()));

    var bindingResult = validateTeamMemberRolesForm(form);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLE_INVALID_CODE)
        )
    );

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).containsExactly(
        entry(
            ConsulteeTeamMemberRolesValidator.ROLES_FIELD_NAME,
            Set.of(ConsulteeTeamMemberRolesValidator.ROLES_INVALID_ERROR_MESSAGE)
        )
    );
  }

  @Test
  void validate_whenValidRolesSelected_thenNoError() {

    var form = new TeamMemberRolesForm();
    form.setRoles(Set.of(ConsulteeTeamRole.ACCESS_MANAGER.name()));

    var bindingResult = validateTeamMemberRolesForm(form);

    var resultingErrorCodes = ValidatorTestingUtil.extractErrors(bindingResult);

    assertThat(resultingErrorCodes).isEmpty();

    var resultingErrorMessages = ValidatorTestingUtil.extractErrorMessages(bindingResult);

    assertThat(resultingErrorMessages).isEmpty();
  }

  private BindingResult validateTeamMemberRolesForm(TeamMemberRolesForm teamMemberRolesForm) {
    var bindingResult = new BeanPropertyBindingResult(teamMemberRolesForm, "form");
    consulteeTeamMemberRolesValidator.validate(teamMemberRolesForm, bindingResult);
    return bindingResult;
  }

  static class NonSupportedClass {}

}
