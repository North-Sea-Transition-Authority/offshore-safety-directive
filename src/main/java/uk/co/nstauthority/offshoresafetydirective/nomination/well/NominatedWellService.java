package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellService {

  private final NominatedWellRepository nominatedWellRepository;
  private final WellQueryService wellQueryService;

  @Autowired
  NominatedWellService(NominatedWellRepository nominatedWellRepository, WellQueryService wellQueryService) {
    this.nominatedWellRepository = nominatedWellRepository;
    this.wellQueryService = wellQueryService;
  }

  @Transactional
  public void saveNominatedWells(NominationDetail nominationDetail, NominatedWellDetailForm form) {
    List<Integer> wellIds = form.getWells().stream()
        .distinct()
        .toList();
    List<NominatedWell> nominatedWells = wellQueryService.getWellsByIdIn(wellIds)
        .stream()
        .map(wellDto -> new NominatedWell(nominationDetail, wellDto.id()))
        .toList();
    deleteByNominationDetail(nominationDetail);
    nominatedWellRepository.saveAll(nominatedWells);
  }

  List<NominatedWell> findAllByNominationDetail(NominationDetail nominationDetail) {
    return nominatedWellRepository.findAllByNominationDetail(nominationDetail);
  }

  @Transactional
  public void deleteByNominationDetail(NominationDetail nominationDetail) {
    nominatedWellRepository.deleteAllByNominationDetail(nominationDetail);
  }
}
