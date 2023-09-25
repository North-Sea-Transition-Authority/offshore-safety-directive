package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NominationAccessService {

  private final NominationRepository nominationRepository;

  @Autowired
  public NominationAccessService(NominationRepository nominationRepository) {
    this.nominationRepository = nominationRepository;
  }

  public Optional<NominationDto> getNomination(NominationId nominationId) {
    return nominationRepository.findById(nominationId.id())
        .map(NominationDto::fromNomination);
  }

  public List<Nomination> getNominations(Collection<UUID> nominationIds) {
    return nominationRepository.findByIdIn(nominationIds);
  }
}
