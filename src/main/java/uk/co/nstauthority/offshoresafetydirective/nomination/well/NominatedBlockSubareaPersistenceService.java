package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedBlockSubareaPersistenceService {

  private final NominatedBlockSubareaRepository nominatedBlockSubareaRepository;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  NominatedBlockSubareaPersistenceService(NominatedBlockSubareaRepository nominatedBlockSubareaRepository,
                                          LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.nominatedBlockSubareaRepository = nominatedBlockSubareaRepository;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @Transactional
  public void saveNominatedLicenceBlockSubareas(NominationDetail nominationDetail,
                                                NominatedBlockSubareaForm form) {
    List<String> blockSubareaIds = form.getSubareas().stream()
        .distinct()
        .toList();

    List<NominatedBlockSubarea> nominatedBlockSubareas = licenceBlockSubareaQueryService
        .getLicenceBlockSubareasByIdIn(blockSubareaIds)
        .stream()
        .map(blockSubareaDto -> {
          var nominatedBlockSubarea = new NominatedBlockSubarea();
          nominatedBlockSubarea.setNominationDetail(nominationDetail);
          nominatedBlockSubarea.setBlockSubareaId(blockSubareaDto.subareaId().id());
          return nominatedBlockSubarea;
        })
        .toList();
    deleteByNominationDetail(nominationDetail);
    nominatedBlockSubareaRepository.saveAll(nominatedBlockSubareas);
  }

  List<NominatedBlockSubarea> findAllByNominationDetail(NominationDetail nominationDetail) {
    return nominatedBlockSubareaRepository.findAllByNominationDetail(nominationDetail);
  }

  @Transactional
  public void deleteByNominationDetail(NominationDetail nominationDetail) {
    nominatedBlockSubareaRepository.deleteAllByNominationDetail(nominationDetail);
  }
}
