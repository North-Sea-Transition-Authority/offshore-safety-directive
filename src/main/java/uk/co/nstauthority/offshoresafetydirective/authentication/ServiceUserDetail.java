package uk.co.nstauthority.offshoresafetydirective.authentication;

import java.io.Serializable;
import org.springframework.security.core.AuthenticatedPrincipal;

record ServiceUserDetail(Long wuaId,
                         Long personId,
                         String forename,
                         String surname,
                         String emailAddress)
    implements AuthenticatedPrincipal, Serializable {

  @Override
  public String getName() {
    return wuaId.toString();
  }
}
