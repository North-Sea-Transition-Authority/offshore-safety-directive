package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRoleRepository extends ListCrudRepository<TeamRole, UUID> {
  List<TeamRole> findByWuaIdAndRole(Long wuaId, Role role);

  List<TeamRole> findByWuaIdAndTeam(Long wuaId, Team team);

  List<TeamRole> findByTeam(Team team);

  void deleteByWuaIdAndTeam(Long wuaId, Team team);

  boolean existsByTeamAndWuaId(Team team, Long wuaId);

  List<TeamRole> findAllByWuaId(long wuaId);

  Set<TeamRole> findAllByTeamAndRole(Team team, Role role);

  Set<TeamRole> findDistinctByWuaIdAndRoleInAndTeam_teamType(
      Long wuaId,
      Collection<Role> roles,
      TeamType teamType
  );
}
