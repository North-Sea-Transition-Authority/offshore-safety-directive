package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.util.Set;

public interface TeamRole {

  String getDisplayText();

  String getDescription();

  Integer getDisplayOrder();

  Set<RolePermission> getRolePermissions();

  String name();

}
