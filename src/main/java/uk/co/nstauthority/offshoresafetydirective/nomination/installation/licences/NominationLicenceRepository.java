package uk.co.nstauthority.offshoresafetydirective.nomination.installation.licences;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Repository
interface NominationLicenceRepository extends CrudRepository<NominationLicence, UUID> {
  void deleteAllByNominationDetail(NominationDetail nominationDetail);

  List<NominationLicence> findAllByNominationDetail(NominationDetail nominationDetail);
}
