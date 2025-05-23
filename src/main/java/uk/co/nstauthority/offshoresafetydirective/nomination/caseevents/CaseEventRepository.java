package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Repository
interface CaseEventRepository extends CrudRepository<CaseEvent, UUID> {

  Optional<CaseEvent> findFirstByCaseEventTypeInAndNominationAndNominationVersion(
      Collection<CaseEventType> caseEventTypes,
      Nomination nomination,
      int nominationVersion
  );

  Optional<CaseEvent> findFirstByCaseEventTypeInAndNomination_IdAndNominationVersion(
      Collection<CaseEventType> caseEventTypes,
      UUID nominationId,
      int nominationVersion
  );

  List<CaseEvent> findAllByNomination(Nomination nomination);

  Optional<CaseEvent> findByUuidAndNomination(UUID uuid, Nomination nomination);

}
