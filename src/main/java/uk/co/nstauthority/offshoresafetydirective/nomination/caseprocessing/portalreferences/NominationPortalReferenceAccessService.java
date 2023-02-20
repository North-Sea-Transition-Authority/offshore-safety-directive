package uk.co.nstauthority.offshoresafetydirective.nomination.caseprocessing.portalreferences;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  public ActivePortalReferencesView getActivePortalReferenceView(Nomination nomination) {
    var references = getNominationPortalReferenceDtosByNomination(nomination)
        .stream()
        .collect(Collectors.toMap(NominationPortalReferenceDto::portalReferenceType, Function.identity()));

    var pearsReference = Optional.ofNullable(references.getOrDefault(PortalReferenceType.PEARS, null))
        .map(NominationPortalReferenceDto::references)
        .filter(Objects::nonNull)
        .map(PearsReferences::new)
        .orElse(null);

    var wonsReference = Optional.ofNullable(references.getOrDefault(PortalReferenceType.WONS, null))
        .map(NominationPortalReferenceDto::references)
        .filter(Objects::nonNull)
        .map(WonsReferences::new)
        .orElse(null);

    return new ActivePortalReferencesView(pearsReference, wonsReference);
  }

  private NominationPortalReferenceDto mapReferenceToDto(NominationPortalReference nominationPortalReference) {
    return new NominationPortalReferenceDto(
        nominationPortalReference.getPortalReferenceType(),
        nominationPortalReference.getPortalReferences()
    );
  }

}
