package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TeamScopeRepository extends CrudRepository<TeamScope, UUID> {

  Optional<TeamScope> findByPortalIdAndPortalTeamType(String portalId, PortalTeamType portalTeamType);

  List<TeamScope> findAllByTeamInAndPortalTeamType(List<Team> teamId, PortalTeamType portalTeamType);

}
