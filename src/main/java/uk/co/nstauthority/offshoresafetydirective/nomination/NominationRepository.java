package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NominationRepository extends CrudRepository<Nomination, Integer> {

}
