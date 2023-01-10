package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.Collection;
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

}
