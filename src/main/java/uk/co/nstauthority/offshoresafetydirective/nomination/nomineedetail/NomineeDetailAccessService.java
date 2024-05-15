package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NomineeDetailAccessService {

  private final NomineeDetailRepository nomineeDetailRepository;

  @Autowired
  public NomineeDetailAccessService(NomineeDetailRepository nomineeDetailRepository) {
    this.nomineeDetailRepository = nomineeDetailRepository;
  }

  public Optional<NomineeDetailDto> getNomineeDetailDtoByNominationDetail(NominationDetail nominationDetail) {
    return nomineeDetailRepository.findByNominationDetail(nominationDetail)
        .map(NomineeDetailDto::fromNomineeDetail);
  }

}
