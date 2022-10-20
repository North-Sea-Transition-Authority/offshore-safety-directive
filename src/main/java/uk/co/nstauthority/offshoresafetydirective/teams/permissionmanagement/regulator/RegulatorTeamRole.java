package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.util.EnumSet;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public enum RegulatorTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Manage regulator access to the team",
      10,
      EnumSet.of(RolePermission.GRANT_ROLES)
  ),
  ORGANISATION_ACCESS_MANAGER(
      "Organisation access manager",
      "Manage organisation access to the service",
      20,
      EnumSet.of(RolePermission.MANAGE_ORGANISATIONS)
  );

  private final String displayText;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;

  RegulatorTeamRole(String displayText, String description, Integer displayOrder,
                    Set<RolePermission> rolePermissions) {
    this.displayText = displayText;
    this.description = description;
    this.displayOrder = displayOrder;
    this.rolePermissions = rolePermissions;
  }

  @Override
  public String getDisplayText() {
    return displayText;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Integer getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }
}
