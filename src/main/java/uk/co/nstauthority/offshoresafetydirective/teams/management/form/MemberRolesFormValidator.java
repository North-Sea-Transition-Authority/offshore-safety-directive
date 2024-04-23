package uk.co.nstauthority.offshoresafetydirective.teams.management.form;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.management.TeamManagementService;

@Service
public class MemberRolesFormValidator {
  private final TeamManagementService teamManagementService;

  public MemberRolesFormValidator(TeamManagementService teamManagementService) {
    this.teamManagementService = teamManagementService;
  }

  public boolean isValid(MemberRolesForm form, Long wuaId, Team team, Errors errors) {
    if (form.getRoles() == null || form.getRoles().isEmpty()) {
      errors.rejectValue("roles", "roles.required", "Select at least one role");
      return false;
    }

    var roles = form.getRoles().stream()
        .map(Role::valueOf)
        .toList();

    if (!teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(team, wuaId, roles)) {
      errors.rejectValue(
          "roles",
          "roles.noTeamManager",
          "There must always be at least one user who can add, remove and update members of this team."
      );
    }
    return !errors.hasErrors();
  }
}
