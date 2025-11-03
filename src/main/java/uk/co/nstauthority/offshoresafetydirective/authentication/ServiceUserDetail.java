package uk.co.nstauthority.offshoresafetydirective.authentication;

import java.io.Serializable;
import java.util.Objects;
import org.springframework.security.core.AuthenticatedPrincipal;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;

public record ServiceUserDetail(
    Long wuaId,
    Long personId,
    String forename,
    String surname,
    String emailAddress,
    Long proxyWuaId,
    String proxyUsername
) implements AuthenticatedPrincipal, Serializable {

  @Override
  public String getName() {
    return Objects.nonNull(proxyWuaId) ? proxyWuaId.toString() : wuaId.toString();
  }

  public String displayName() {
    var userName = String.format("%s %s", forename, surname);
    return Objects.nonNull(proxyUsername) ? String.format("%s as %s", proxyUsername, userName) : userName;
  }

  public static ServiceUserDetail from(EnergyPortalUserDto energyPortalUser) {
    return new ServiceUserDetail(
        energyPortalUser.webUserAccountId(),
        null,
        energyPortalUser.forename(),
        energyPortalUser.surname(),
        energyPortalUser.emailAddress(),
        null,
        null
    );
  }
}
