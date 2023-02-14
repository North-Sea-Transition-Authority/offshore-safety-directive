package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;

@Service
public class NominationPortalReferenceAccessService {

  private final NominationPortalReferenceRepository nominationPortalReferenceRepository;

  @Autowired
  NominationPortalReferenceAccessService(NominationPortalReferenceRepository nominationPortalReferenceRepository) {
    this.nominationPortalReferenceRepository = nominationPortalReferenceRepository;
  }

  public List<NominationPortalReferenceDto> getNominationPortalReferenceDtosByNomination(Nomination nomination) {
    return nominationPortalReferenceRepository.findAllByNomination(nomination)
        .stream()
        .map(this::mapReferenceToDto)
        .toList();
  }

  private NominationPortalReferenceDto mapReferenceToDto(NominationPortalReference nominationPortalReference) {
    return new NominationPortalReferenceDto(
        nominationPortalReference.getPortalReferenceType(),
        nominationPortalReference.getPortalReferences()
    );
  }

}
