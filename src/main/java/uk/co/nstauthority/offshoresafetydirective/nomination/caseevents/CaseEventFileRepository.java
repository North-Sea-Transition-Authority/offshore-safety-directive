package uk.co.nstauthority.offshoresafetydirective.nomination.caseevents;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CaseEventFileRepository extends CrudRepository<CaseEventFile, UUID> {

  List<CaseEventFile> findAllByCaseEventIn(Collection<CaseEvent> caseEvent);

}
