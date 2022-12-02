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
  ORGANISATION_ACCESS_MANAGER(
      "Organisation access manager",
      "Can manage organisation access to this service",
      20,
      EnumSet.of(RolePermission.MANAGE_ORGANISATIONS)
  ),
  MANAGE_NOMINATION(
      "Manage nominations",
      "Can create and process phase one nomination applications",
      30,
      EnumSet.of(RolePermission.CREATE_NOMINATION)
  ),
  VIEW_NOMINATION(
      "View nominations",
      "Can view nominations",
      40,
      Set.of()
  ),
  MANAGE_ASSET_OPERATORS(
      "Manage well and installation operators",
      "Can carry out updates to the system of record including data checks and terminations",
      50,
      Set.of()
  ),
  VIEW_ASSET_OPERATORS(
      "View well and installation operators",
      "Can view system of record data",
      60,
      Set.of()
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
