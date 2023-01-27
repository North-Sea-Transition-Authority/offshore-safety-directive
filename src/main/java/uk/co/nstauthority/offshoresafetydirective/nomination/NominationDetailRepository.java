package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NominationDetailRepository extends CrudRepository<NominationDetail, Integer> {

  Optional<NominationDetail> findFirstByNominationAndVersion(Nomination nomination, int version);

  Optional<NominationDetail> findFirstByNominationOrderByVersionDesc(Nomination nomination);

  Optional<NominationDetail> findFirstByNomination_IdAndStatusInOrderByVersionDesc(
      Integer nominationId,
      Collection<NominationStatus> nominationStatuses
  );

}
