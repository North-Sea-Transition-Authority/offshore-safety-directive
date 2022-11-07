package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement;

import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumWithDescription;

public interface TeamRole extends DisplayableEnumWithDescription {

  Set<RolePermission> getRolePermissions();

}
