package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NominationRepository extends CrudRepository<Nomination, Integer> {

  @Query("""
       SELECT COUNT(DISTINCT n)
       FROM Nomination n
       JOIN NominationDetail nd on nd.nomination = n
       WHERE nd.submittedInstant IS NOT NULL
       AND YEAR(nd.submittedInstant) = :year
      """)
  Integer getTotalSubmissionsForYear(int year);

}
