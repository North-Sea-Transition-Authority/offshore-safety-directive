package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Service
class NominationPortalReferencePersistenceService {

  private final NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @Autowired
  NominationPortalReferencePersistenceService(NominationPortalReferenceRepository nominationPortalReferenceRepository) {
    this.nominationPortalReferenceRepository = nominationPortalReferenceRepository;
  }

  @Transactional
  public void updatePortalReferences(Nomination nomination, PortalReferenceType portalReferenceType,
                                     @Nullable String newReferences) {
    var reference = nominationPortalReferenceRepository.findByNominationAndPortalReferenceType(nomination, portalReferenceType)
        .orElseGet(() -> createPortalReference(nomination, portalReferenceType));

    reference.setPortalReferences(newReferences);
    nominationPortalReferenceRepository.save(reference);
  }

  NominationPortalReference createPortalReference(Nomination nomination, PortalReferenceType portalReferenceType) {
    var portalReference = new NominationPortalReference();
    portalReference.setNomination(nomination);
    portalReference.setPortalReferenceType(portalReferenceType);
    return portalReference;
  }

}
