package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRemovalService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberEditRolesValidatorHint;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamMemberRolesForm;


@Service
class IndustryTeamMemberEditRolesValidator implements SmartValidator {

  public static final String ROLES_FIELD_NAME = "roles";

  static final String ROLE_INVALID_CODE = "%s.notValid".formatted(ROLES_FIELD_NAME);
  static final String ROLES_INVALID_ERROR_MESSAGE = "Select a valid action";
  public static final String ROLES_FIELD_REQUIRED_ERROR_CODE = "%s.required".formatted(ROLES_FIELD_NAME);
  public static final String ROLES_FIELD_REQUIRED_ERROR_MESSAGE = "Select at least one action";

  public static final String ROLES_NO_ACCESS_MANAGER_ERROR_CODE =
      "%s.accessManagerRequired".formatted(ROLES_FIELD_NAME);

  public static final String ROLES_NO_ACCESS_MANAGER_ERROR_MESSAGE =
      "There must always be at least one access manager in the team";

  private final TeamMemberRemovalService teamMemberRemovalService;

  @Autowired
  IndustryTeamMemberEditRolesValidator(TeamMemberRemovalService teamMemberRemovalService) {
    this.teamMemberRemovalService = teamMemberRemovalService;
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors, Object... validationHints) {
    var form = (TeamMemberRolesForm) Objects.requireNonNull(target);
    var validatorHints = (TeamMemberEditRolesValidatorHint) Objects.requireNonNull(validationHints[0]);
    var bindingResult = Objects.requireNonNull(errors);

    Set<String> formRoles = form.getRoles() != null ? form.getRoles() : Set.of();

    if (formRoles.isEmpty()) {
      ValidationUtils.rejectIfEmpty(errors,
          ROLES_FIELD_NAME,
          ROLES_FIELD_REQUIRED_ERROR_CODE,
          ROLES_FIELD_REQUIRED_ERROR_MESSAGE);
      return;
    }

    var validRolesFromForm = formRoles
        .stream()
        .map(IndustryTeamRole::getRoleFromString)
        .flatMap(Optional::stream)
        .toList();

    if (formRoles.size() != validRolesFromForm.size()) {
      bindingResult.rejectValue(
          ROLES_FIELD_NAME,
          ROLE_INVALID_CODE,
          ROLES_INVALID_ERROR_MESSAGE
      );
    }

    if (!canUpdateTeamMemberWithNewRoles(
        validatorHints.getTeam(),
        validatorHints.getTeamMember(),
        validRolesFromForm
    )) {
      errors.rejectValue(ROLES_FIELD_NAME, ROLES_NO_ACCESS_MANAGER_ERROR_CODE, ROLES_NO_ACCESS_MANAGER_ERROR_MESSAGE);
    }
  }

  @Override
  public boolean supports(@Nullable Class<?> clazz) {
    return TeamMemberRolesForm.class.equals(clazz);
  }

  @Override
  public void validate(@Nullable Object target, @Nullable Errors errors) {
    throw new IllegalCallerException(
        "This validator [%s] requires a %s validation hint"
            .formatted(
                this.getClass().getSimpleName(),
                TeamMemberEditRolesValidatorHint.class.getSimpleName()
            ));
  }

  private boolean canUpdateTeamMemberWithNewRoles(Team team, TeamMember teamMember,
                                                  Collection<IndustryTeamRole> newRoles) {

    var accessManagerRole = IndustryTeamRole.ACCESS_MANAGER;

    var isRemovingAccessManagerRole = Sets.difference(teamMember.roles(), Set.copyOf(newRoles))
        .contains(accessManagerRole);

    return !isRemovingAccessManagerRole
        || teamMemberRemovalService.canRemoveTeamMember(team, teamMember.wuaId(), accessManagerRole);
  }
}
