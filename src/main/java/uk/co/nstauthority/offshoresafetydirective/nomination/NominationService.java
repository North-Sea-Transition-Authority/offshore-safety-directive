package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Clock;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;

@Service
public class NominationService {

  private static final Integer FIRST_VERSION = 1;

  private final NominationRepository nominationRepository;
  private final NominationDetailRepository nominationDetailRepository;
  private final Clock clock;

  @Autowired
  NominationService(
      NominationRepository nominationRepository,
      NominationDetailRepository nominationDetailRepository,
      Clock clock) {
    this.nominationRepository = nominationRepository;
    this.nominationDetailRepository = nominationDetailRepository;
    this.clock = clock;
  }

  @Transactional
  public NominationDetail startNomination() {
    var nomination = new Nomination()
        .setCreatedInstant(clock.instant());

    var nominationDetail = new NominationDetail()
        .setNomination(nomination)
        .setCreatedInstant(clock.instant())
        .setVersion(FIRST_VERSION)
        .setStatus(NominationStatus.DRAFT);

    nominationRepository.save(nomination);
    return nominationDetailRepository.save(nominationDetail);
  }

  public Nomination getNominationByIdOrError(NominationId nominationId) {
    return nominationRepository.findById(nominationId.id())
        .orElseThrow(() -> new OsdEntityNotFoundException(
            String.format("Cannot find Nomination with ID: %s", nominationId)
        ));
  }

  public Optional<NominationDto> getNomination(NominationId nominationId) {
    return nominationRepository.findById(nominationId.id())
        .map(NominationDto::fromNomination);
  }
}
