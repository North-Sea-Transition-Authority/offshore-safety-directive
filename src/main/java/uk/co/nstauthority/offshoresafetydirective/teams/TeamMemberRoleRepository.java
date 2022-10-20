package uk.co.nstauthority.offshoresafetydirective.teams;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TeamMemberRoleRepository extends CrudRepository<TeamMemberRole, UUID> {
}
