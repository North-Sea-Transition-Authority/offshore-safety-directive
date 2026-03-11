package uk.co.nstauthority.offshoresafetydirective.energyportal.user;

import uk.co.nstauthority.offshoresafetydirective.teams.Team;

public interface AllowedDomainService {

  boolean isAllowedDomain(String domain, Team team);
}