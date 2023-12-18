package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public enum RegulatorTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Can add, remove and update members of this team",
      10,
      EnumSet.of(RolePermission.GRANT_ROLES)
  ),
  THIRD_PARTY_ACCESS_MANAGER(
      "Third party access manager",
      "Can manage organisation and consultee access to this service",
      20,
      EnumSet.of(RolePermission.MANAGE_CONSULTEE_TEAMS, RolePermission.MANAGE_INDUSTRY_TEAMS)
  ),
  MANAGE_NOMINATION(
      "Manage nominations",
      "Can create, process and view nomination applications",
      30,
      EnumSet.of(RolePermission.MANAGE_NOMINATIONS)
  ),
  VIEW_NOMINATION(
      "View nominations",
      "Can view nominations",
      40,
      Set.of(RolePermission.VIEW_ALL_NOMINATIONS)
  ),
  MANAGE_ASSET_APPOINTMENTS(
      "Manage well and installation appointments",
      "Can view and carry out updates to the system of record including corrections and terminations",
      50,
      Set.of(RolePermission.MANAGE_APPOINTMENTS)
  );

  private final String screenDisplayText;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;

  RegulatorTeamRole(String screenDisplayText, String description, Integer displayOrder,
                    Set<RolePermission> rolePermissions) {
    this.screenDisplayText = screenDisplayText;
    this.description = description;
    this.displayOrder = displayOrder;
    this.rolePermissions = rolePermissions;
  }

  @Override
  public String getScreenDisplayText() {
    return screenDisplayText;
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
  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  static Optional<RegulatorTeamRole> getRoleFromString(String role) {
    try {
      return Optional.of(RegulatorTeamRole.valueOf(role.toUpperCase()));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }


}
