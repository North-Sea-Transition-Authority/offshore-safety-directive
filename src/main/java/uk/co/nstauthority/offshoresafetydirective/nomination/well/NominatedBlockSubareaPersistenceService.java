package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedBlockSubareaPersistenceService {

  static final RequestPurpose SAVE_LICENCE_BLOCK_SUBAREAS_PURPOSE =
      new RequestPurpose("Get the licence block subareas to save on nomination");
  private final NominatedBlockSubareaRepository nominatedBlockSubareaRepository;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  NominatedBlockSubareaPersistenceService(NominatedBlockSubareaRepository nominatedBlockSubareaRepository,
                                          LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.nominatedBlockSubareaRepository = nominatedBlockSubareaRepository;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @Transactional
  public void saveAllNominatedLicenceBlockSubareas(Collection<NominatedBlockSubarea> subareas) {
    nominatedBlockSubareaRepository.saveAll(subareas);
  }

  @Transactional
  public void saveNominatedLicenceBlockSubareas(NominationDetail nominationDetail,
                                                NominatedBlockSubareaForm form) {
    List<LicenceBlockSubareaId> blockSubareaIds = form.getSubareas()
        .stream()
        .distinct()
        .map(LicenceBlockSubareaId::new)
        .toList();

    List<NominatedBlockSubarea> nominatedBlockSubareas = licenceBlockSubareaQueryService
        .getLicenceBlockSubareasByIds(blockSubareaIds, SAVE_LICENCE_BLOCK_SUBAREAS_PURPOSE)
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
