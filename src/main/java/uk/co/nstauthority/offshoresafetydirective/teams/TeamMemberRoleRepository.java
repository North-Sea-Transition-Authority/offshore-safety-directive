package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TeamMemberRoleRepository extends CrudRepository<TeamMemberRole, UUID> {

  List<TeamMemberRole> findAllByTeam(Team team);

}