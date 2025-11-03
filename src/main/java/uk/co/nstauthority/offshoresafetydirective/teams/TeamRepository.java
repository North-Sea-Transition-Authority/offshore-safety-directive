package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends ListCrudRepository<Team, UUID> {
  List<Team> findByTeamType(TeamType teamType);

  Optional<Team> findByTeamTypeAndScopeTypeAndScopeId(TeamType teamType, String scopeType, String scopeId);

  Set<Team> findByTeamTypeAndScopeTypeAndScopeIdIn(TeamType teamType, String scopeType, Set<String> scopeIds);
}
