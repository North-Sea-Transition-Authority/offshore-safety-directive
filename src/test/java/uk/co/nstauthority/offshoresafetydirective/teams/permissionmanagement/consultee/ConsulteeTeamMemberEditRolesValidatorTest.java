package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRemovalService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberEditRolesValidatorHint;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;

@ExtendWith(MockitoExtension.class)
class ConsulteeTeamMemberEditRolesValidatorTest {

  @Mock
  private TeamMemberRemovalService teamMemberRemovalService;

  @InjectMocks
  private ConsulteeTeamMemberEditRolesValidator consulteeTeamMemberEditRolesValidator;

  @Test
  void validate_whenValid_thenNoErrors() {
    var form = new TeamMemberRolesForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    form.setRoles(Set.of(ConsulteeTeamRole.CONSULTEE.name()));

    var team = new Team(UUID.randomUUID());
    var teamView = TeamTestUtil.createTeamView(team);
    var teamMember = new TeamMember(new WebUserAccountId(123L), teamView,
        Set.of(ConsulteeTeamRole.ACCESS_MANAGER));

    when(teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(true);

    var dto = new TeamMemberEditRolesValidatorHint(team, teamMember);
    consulteeTeamMemberEditRolesValidator.validate(form, bindingResult, dto);

    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_whenNoRolesSelect_thenHasMatchingError() {
    var form = new TeamMemberRolesForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var team = new Team(UUID.randomUUID());
    var teamView = TeamTestUtil.createTeamView(team);
    var teamMember = new TeamMember(new WebUserAccountId(123L), teamView,
        Set.of(ConsulteeTeamRole.ACCESS_MANAGER));

    var dto = new TeamMemberEditRolesValidatorHint(team, teamMember);
    consulteeTeamMemberEditRolesValidator.validate(form, bindingResult, dto);

    assertTrue(bindingResult.hasErrors());
    assertThat(bindingResult.getFieldErrors()).extracting(
        FieldError::getField,
        DefaultMessageSourceResolvable::getCode,
        DefaultMessageSourceResolvable::getDefaultMessage
    ).containsExactly(
        Tuple.tuple(
            ConsulteeTeamMemberEditRolesValidator.ROLES_FIELD_NAME,
            ConsulteeTeamMemberEditRolesValidator.ROLES_FIELD_REQUIRED_ERROR_CODE,
            ConsulteeTeamMemberEditRolesValidator.ROLES_FIELD_REQUIRED_ERROR_MESSAGE
        )
    );
  }

  @Test
  void validate_whenLastAccessManager_andUpdateHasNoAccessManagerRole_thenHasMatchingError() {
    var form = new TeamMemberRolesForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    form.setRoles(Set.of(ConsulteeTeamRole.CONSULTEE.name()));

    var team = new Team(UUID.randomUUID());
    var teamView = TeamTestUtil.createTeamView(team);

    var lastAccessManager = new TeamMember(new WebUserAccountId(123L), teamView,
        Set.of(ConsulteeTeamRole.ACCESS_MANAGER));

    when(teamMemberRemovalService.canRemoveTeamMember(team, lastAccessManager.wuaId(), ConsulteeTeamRole.ACCESS_MANAGER))
        .thenReturn(false);

    var dto = new TeamMemberEditRolesValidatorHint(team, lastAccessManager);
    consulteeTeamMemberEditRolesValidator.validate(form, bindingResult, dto);

    assertTrue(bindingResult.hasErrors());
    assertThat(bindingResult.getFieldErrors()).extracting(
        FieldError::getField,
        DefaultMessageSourceResolvable::getCode,
        DefaultMessageSourceResolvable::getDefaultMessage
    ).containsExactly(
        Tuple.tuple(
            ConsulteeTeamMemberEditRolesValidator.ROLES_FIELD_NAME,
            ConsulteeTeamMemberEditRolesValidator.ROLES_NO_ACCESS_MANAGER_ERROR_CODE,
            ConsulteeTeamMemberEditRolesValidator.ROLES_NO_ACCESS_MANAGER_ERROR_MESSAGE
        )
    );
  }

  @Test
  void supports_whenCorrectClass_thenSupports() {
    assertTrue(consulteeTeamMemberEditRolesValidator.supports(TeamMemberRolesForm.class));
  }

  @Test
  void supports_whenIncorrectClass_thenDoesNotSupport() {
    assertFalse(consulteeTeamMemberEditRolesValidator.supports(UnsupportedClass.class));
  }

  @Test
  void validate_whenCalledWithTwoArguments_thenThrowsError() {
    var form = new TeamMemberRolesForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    assertThrows(IllegalCallerException.class, () ->
        consulteeTeamMemberEditRolesValidator.validate(form, bindingResult)
    );
  }

  static class UnsupportedClass {
  }
}