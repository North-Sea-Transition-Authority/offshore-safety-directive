package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

public enum ConsulteeTeamRole implements TeamRole {

  ACCESS_MANAGER(
      "Access manager",
      "Can add, remove and update members of this team",
      10,
      EnumSet.of(RolePermission.GRANT_ROLES)
  ),
  CONSULTATION_COORDINATOR(
      "Consultation coordinator",
      "Receives consultation requests and can view nomination forms",
      20,
      EnumSet.of(RolePermission.VIEW_ALL_NOMINATIONS)
  ),
  CONSULTEE(
      "Consultee",
      "Can view nomination forms",
      30,
      EnumSet.of(RolePermission.VIEW_ALL_NOMINATIONS)
  )
  ;

  private final String screenDisplayText;
  private final String description;
  private final Integer displayOrder;
  private final Set<RolePermission> rolePermissions;

  ConsulteeTeamRole(String screenDisplayText, String description, Integer displayOrder,
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

  static Optional<ConsulteeTeamRole> getRoleFromString(String role) {
    try {
      return Optional.of(ConsulteeTeamRole.valueOf(role.toUpperCase()));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

}
