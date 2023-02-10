package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;

@Service
public class FinalisedNominatedSubareaWellsAccessService {

  private final NominatedSubareaWellRepository nominatedSubareaWellRepository;

  @Autowired
  public FinalisedNominatedSubareaWellsAccessService(NominatedSubareaWellRepository nominatedSubareaWellRepository) {
    this.nominatedSubareaWellRepository = nominatedSubareaWellRepository;
  }

  public Set<NominatedSubareaWellDto> getFinalisedNominatedSubareasWells(NominationDetail nominationDetail) {
    return nominatedSubareaWellRepository.findByNominationDetail(nominationDetail)
        .stream()
        .map(nominatedSubareaWell -> new NominatedSubareaWellDto(new WellboreId(nominatedSubareaWell.getWellboreId())))
        .collect(Collectors.toSet());
  }

}
