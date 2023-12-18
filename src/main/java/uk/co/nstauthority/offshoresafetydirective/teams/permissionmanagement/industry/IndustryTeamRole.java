package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public enum IndustryTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Can add, remove and update members of this team",
      10,
      EnumSet.of(RolePermission.GRANT_ROLES)
  ),
  NOMINATION_SUBMITTER(
      "Nomination submitter",
      "Can create, edit, submit, view and manage nominations linked to this organisation",
      20,
      Set.of(
          RolePermission.CREATE_NOMINATION,
          RolePermission.SUBMIT_NOMINATION,
          RolePermission.EDIT_NOMINATION,
          RolePermission.VIEW_NOMINATION
      )
  ),
  NOMINATION_EDITOR(
      "Nomination editor",
      "Can edit and view nominations linked to this organisation",
      30,
      Set.of(RolePermission.EDIT_NOMINATION, RolePermission.VIEW_NOMINATION)
  ),
  NOMINATION_VIEWER(
      "Nomination viewer",
      "Can view nominations linked to this organisation",
      40,
      Set.of(RolePermission.VIEW_NOMINATION)
  );

  private final String screenDisplayText;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;


  IndustryTeamRole(String screenDisplayText, String description, Integer displayOrder,
                   Set<RolePermission> rolePermissions) {
    this.screenDisplayText = screenDisplayText;
    this.description = description;
    this.displayOrder = displayOrder;
    this.rolePermissions = rolePermissions;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getScreenDisplayText() {
    return this.screenDisplayText;
  }

  @Override
  public String getFormValue() {
    return this.name();
  }

  @Override
  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  static Optional<IndustryTeamRole> getRoleFromString(String role) {
    try {
      return Optional.of(IndustryTeamRole.valueOf(role.toUpperCase()));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}
