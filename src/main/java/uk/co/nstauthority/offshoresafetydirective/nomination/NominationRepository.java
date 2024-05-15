package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NominationRepository
    extends CrudRepository<Nomination, UUID>, RevisionRepository<Nomination, UUID, Long> {

  @Query("""
       SELECT COUNT(DISTINCT n)
       FROM Nomination n
       JOIN NominationDetail nd on nd.nomination = n
       WHERE nd.submittedInstant IS NOT NULL
       AND n.reference IS NOT NULL
       AND YEAR(nd.submittedInstant) = :year
      """)
  Integer getTotalSubmissionsForYear(int year);

  List<Nomination> findByIdIn(Collection<UUID> nominationIds);

}
