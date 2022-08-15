package uk.co.nstauthority.offshoresafetydirective.nomination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.exception.OsdEntityNotFoundException;

@Service
public class NominationDetailService {

  private final NominationService nominationService;
  private final NominationDetailRepository nominationDetailRepository;

  @Autowired
  public NominationDetailService(
      NominationService nominationService,
      NominationDetailRepository nominationDetailRepository) {
    this.nominationService = nominationService;
    this.nominationDetailRepository = nominationDetailRepository;
  }

  public NominationDetail getLatestNominationDetail(NominationId nominationId) {
    var nomination = nominationService.getNominationByIdOrError(nominationId);
    return nominationDetailRepository.findFirstByNominationOrderByVersionDesc(nomination)
        .orElseThrow(() -> {
          throw new OsdEntityNotFoundException(String.format(
              "Cannot find latest NominationDetail with ID: %s",
              nomination.getId()
          ));
        });
  }
}
