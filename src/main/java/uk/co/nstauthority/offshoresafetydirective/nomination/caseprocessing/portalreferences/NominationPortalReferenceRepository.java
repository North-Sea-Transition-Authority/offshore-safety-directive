package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NominationPortalReferenceRepository extends CrudRepository<NominationPortalReference, UUID> {
}
