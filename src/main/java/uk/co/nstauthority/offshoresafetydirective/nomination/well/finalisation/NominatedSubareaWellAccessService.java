package uk.co.nstauthority.offshoresafetydirective.nomination.well.finalisation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellDto;

@Service
public class NominatedSubareaWellAccessService {

  static final RequestPurpose NOMINATED_SUBAREA_WELLS_PURPOSE =
      new RequestPurpose("Get wells related to nominated subarea for the summary view");

  private final NominatedSubareaWellRepository nominatedSubareaWellRepository;
  private final WellQueryService wellQueryService;


  @Autowired
  public NominatedSubareaWellAccessService(NominatedSubareaWellRepository nominatedSubareaWellRepository,
                                           WellQueryService wellQueryService) {
    this.nominatedSubareaWellRepository = nominatedSubareaWellRepository;
    this.wellQueryService = wellQueryService;
  }

  public List<WellDto> getNominatedSubareaWellDetailView(NominationDetail nominationDetail) {

    var nominatedSubareaWells = nominatedSubareaWellRepository.findByNominationDetail(nominationDetail)
        .stream()
        .map(nominatedSubareaWell -> new NominatedSubareaWellDto(new WellboreId(nominatedSubareaWell.getWellboreId())))
        .toList();

    List<WellboreId> nominatedSubareaIds = nominatedSubareaWells
        .stream()
        .map(NominatedSubareaWellDto::wellboreId)
        .toList();

    return wellQueryService.getWellsByIds(nominatedSubareaIds, NOMINATED_SUBAREA_WELLS_PURPOSE);
  }
}
