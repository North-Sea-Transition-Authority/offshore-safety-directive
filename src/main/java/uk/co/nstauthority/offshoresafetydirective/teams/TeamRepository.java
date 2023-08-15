package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends CrudRepository<Team, UUID> {

  Optional<Team> findByUuidAndTeamType(UUID uuid, TeamType teamType);

  @Query(
      """
      SELECT DISTINCT tmr.team
      FROM TeamMemberRole tmr
      WHERE tmr.wuaId = :wuaId
      AND tmr.team.teamType = :teamType
      """
  )
  List<Team> findAllTeamsOfTypeThatUserIsMemberOf(Long wuaId, TeamType teamType);

  @Query(
      """
      SELECT DISTINCT tmr.team
      FROM TeamMemberRole tmr
      WHERE tmr.wuaId = :wuaId
      """
  )
  List<Team> findAllTeamsThatUserIsMemberOf(Long wuaId);

  List<Team> findAllByTeamTypeIn(Collection<TeamType> teamTypes);

}
