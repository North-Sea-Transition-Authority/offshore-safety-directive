package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NominationDetailRepository extends CrudRepository<NominationDetail, UUID> {

  Optional<NominationDetail> findFirstByNominationAndVersion(Nomination nomination, int version);

  Optional<NominationDetail> findFirstByNominationOrderByVersionDesc(Nomination nomination);

  Optional<NominationDetail> findFirstByNomination_IdOrderByVersionDesc(UUID nominationId);

  Optional<NominationDetail> findFirstByNomination_IdAndStatusInOrderByVersionDesc(
      UUID nominationId,
      Collection<NominationStatus> nominationStatuses
  );

  Optional<NominationDetail> findFirstByNomination_IdAndVersionAndStatusInOrderByVersionDesc(
      UUID nominationId,
      Integer version,
      Collection<NominationStatus> nominationStatuses
  );

  List<NominationDetail> findAllByNominationAndStatusIn(Nomination nomination, Collection<NominationStatus> statuses);

  List<NominationDetail> getNominationDetailsByStatusInAndNomination_ReferenceContainsIgnoreCase(
      Collection<NominationStatus> statuses,
      String reference
  );

}
