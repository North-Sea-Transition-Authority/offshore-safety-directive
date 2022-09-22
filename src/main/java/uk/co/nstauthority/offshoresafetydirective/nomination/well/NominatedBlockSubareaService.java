package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedBlockSubareaService {

  private final NominatedBlockSubareaRepository nominatedBlockSubareaRepository;
  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  NominatedBlockSubareaService(NominatedBlockSubareaRepository nominatedBlockSubareaRepository,
                                      LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.nominatedBlockSubareaRepository = nominatedBlockSubareaRepository;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  @Transactional
  public void saveNominatedLicenceBlockSubareas(NominationDetail nominationDetail,
                                                NominatedBlockSubareaForm form) {
    List<Integer> blockSubareaIds = form.getSubareas().stream()
        .distinct()
        .toList();
    List<NominatedBlockSubarea> nominatedBlockSubareas = licenceBlockSubareaQueryService
        .getLicenceBlockSubareasByIdIn(blockSubareaIds)
        .stream()
        .map(blockSubareaDto -> new NominatedBlockSubarea()
            .setNominationDetail(nominationDetail)
            .setBlockSubareaId(blockSubareaDto.id())
        )
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
