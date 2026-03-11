package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;

@Service
@Profile("!use-service-access-request")
public class OracleAllowedDomainService implements AllowedDomainService {

  @Override
  public boolean isAllowedDomain(String domain, Team team) {
    return true;
  }

}