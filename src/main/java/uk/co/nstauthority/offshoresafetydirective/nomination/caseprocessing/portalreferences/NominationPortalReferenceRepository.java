package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Repository
interface NominationPortalReferenceRepository extends CrudRepository<NominationPortalReference, UUID> {

  Optional<NominationPortalReference> findByNominationAndPortalReferenceType(Nomination nomination,
                                                                             PortalReferenceType portalReferenceType);

}
