package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailForm;

@Service
public class WellService {

  private final WellRepository wellRepository;
  private final WellQueryService wellQueryService;

  @Autowired
  WellService(WellRepository wellRepository, WellQueryService wellQueryService) {
    this.wellRepository = wellRepository;
    this.wellQueryService = wellQueryService;
  }

  @Transactional
  public void saveWells(NominationDetail nominationDetail, NominatedWellDetailForm form) {
    List<Integer> wellIds = form.getWells().stream()
        .distinct()
        .toList();
    List<Well> wells = wellQueryService.getWellsByIdIn(wellIds)
        .stream()
        .map(wellDto -> new Well(nominationDetail, wellDto.id()))
        .toList();
    wellRepository.deleteAllByNominationDetail(nominationDetail);
    wellRepository.saveAll(wells);
  }

  public List<Well> findAllByNominationDetail(NominationDetail nominationDetail) {
    return wellRepository.findAllByNominationDetail(nominationDetail);
  }
}
