package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TeamScopeRepository extends CrudRepository<TeamScope, UUID> {

  Optional<TeamScope> findByPortalIdAndPortalTeamType(String portalId, PortalTeamType portalTeamType);

  List<TeamScope> findAllByTeamInAndPortalTeamType(List<Team> teamIds, PortalTeamType portalTeamType);

  List<TeamScope> findAllByTeam_UuidInAndPortalTeamType(Collection<UUID> teamIds, PortalTeamType portalTeamType);

  List<TeamScope> findAllByPortalIdInAndPortalTeamType(Collection<String> portalIds, PortalTeamType portalTeamType);
}
