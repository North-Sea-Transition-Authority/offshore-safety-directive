package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TeamMemberRoleRepository extends CrudRepository<TeamMemberRole, UUID> {

  List<TeamMemberRole> findAllByTeam(Team team);

  List<TeamMemberRole> findAllByTeam_TeamTypeAndRoleIn(TeamType teamType, Collection<String> roles);

  boolean existsByWuaIdAndTeam_Uuid(long wuaId, UUID teamId);

  boolean existsByWuaIdAndTeam_UuidAndRoleIn(long wuaId, UUID teamId, Set<String> roles);

  List<TeamMemberRole> findAllByTeamAndWuaId(Team team, Long wuaId);

  void deleteAllByTeamAndWuaId(Team team, Long wuaId);

  List<TeamMemberRole> findAllByWuaId(long wuaId);

}
