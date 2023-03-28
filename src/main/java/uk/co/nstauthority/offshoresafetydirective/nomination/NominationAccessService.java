package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.util.Optional;
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
}
