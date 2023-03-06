package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;

@Service
public class NominatedSubareaWellAccessService {

  private final NominatedSubareaWellRepository nominatedSubareaWellRepository;

  @Autowired
  public NominatedSubareaWellAccessService(NominatedSubareaWellRepository nominatedSubareaWellRepository) {
    this.nominatedSubareaWellRepository = nominatedSubareaWellRepository;
  }

  public List<NominatedSubareaWellDto> getNominatedSubareaWellbores(NominationDetail nominationDetail) {
    return nominatedSubareaWellRepository.findByNominationDetail(nominationDetail)
        .stream()
        .map(nominatedSubareaWell -> new NominatedSubareaWellDto(new WellboreId(nominatedSubareaWell.getWellboreId())))
        .toList();
  }
}
