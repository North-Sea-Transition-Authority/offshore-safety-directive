package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;

@Service
class NominatedSubareaWellPersistenceService {

  private final NominatedSubareaWellRepository nominatedSubareaWellRepository;

  @Autowired
  NominatedSubareaWellPersistenceService(NominatedSubareaWellRepository nominatedSubareaWellRepository) {
    this.nominatedSubareaWellRepository = nominatedSubareaWellRepository;
  }

  @Transactional
  public void materialiseNominatedSubareaWells(NominationDetail nominationDetail,
                                               Collection<NominatedSubareaWellDto> nominatedSubareaWells) {

    if (CollectionUtils.isEmpty(nominatedSubareaWells)) {
      return;
    }

    var nominatedSubareaWellsToPersist = nominatedSubareaWells
        .stream()
        .map(nominatedSubareaWellDto -> convertToNominatedSubareaWell(nominatedSubareaWellDto, nominationDetail))
        .collect(Collectors.toSet());

    nominatedSubareaWellRepository.saveAll(nominatedSubareaWellsToPersist);
  }

  @Transactional
  public void deleteMaterialisedNominatedWellbores(NominationDetail nominationDetail) {
    nominatedSubareaWellRepository.deleteByNominationDetail(nominationDetail);
  }

  private NominatedSubareaWell convertToNominatedSubareaWell(NominatedSubareaWellDto nominatedSubareaWellDto,
                                                             NominationDetail nominationDetail) {
    return new NominatedSubareaWell(
        nominationDetail,
        nominatedSubareaWellDto.wellboreId().id(),
        nominatedSubareaWellDto.name()
    );
  }

}
