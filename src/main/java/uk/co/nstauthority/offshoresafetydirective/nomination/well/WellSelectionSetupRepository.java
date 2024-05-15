package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface WellSelectionSetupRepository extends CrudRepository<WellSelectionSetup, UUID> {

  Optional<WellSelectionSetup> findByNominationDetail(NominationDetail nominationDetail);
}
