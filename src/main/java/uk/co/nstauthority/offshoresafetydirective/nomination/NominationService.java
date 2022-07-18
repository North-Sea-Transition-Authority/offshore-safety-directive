package uk.co.nstauthority.offshoresafetydirective.nomination;

import java.time.Clock;
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
    var nomination = new Nomination(clock.instant());
    var detail = new NominationDetail(
        nomination,
        clock.instant(),
        FIRST_VERSION,
        NominationStatus.DRAFT
    );
    nominationRepository.save(nomination);
    nominationDetailRepository.save(detail);
    return detail;
  }

  Nomination getNominationByIdOrError(Integer nominationId) {
    return nominationRepository.findById(nominationId)
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format("Cannot find Nomination with ID: %s", nominationId));
        });
  }
}
