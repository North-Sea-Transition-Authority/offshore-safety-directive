package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedBlockSubareaAccessService {

  private final NominatedBlockSubareaRepository nominatedBlockSubareaRepository;

  @Autowired
  public NominatedBlockSubareaAccessService(NominatedBlockSubareaRepository nominatedBlockSubareaRepository) {
    this.nominatedBlockSubareaRepository = nominatedBlockSubareaRepository;
  }

  public List<NominatedBlockSubareaDto> getNominatedSubareaDtos(NominationDetail nominationDetail) {
    return findAllByNominationDetail(nominationDetail)
        .stream()
        .map(nominatedBlockSubarea -> new NominatedBlockSubareaDto(
            new LicenceBlockSubareaId(nominatedBlockSubarea.getBlockSubareaId()),
            nominatedBlockSubarea.getName()
        ))
        .toList();
  }

  private List<NominatedBlockSubarea> findAllByNominationDetail(NominationDetail nominationDetail) {
    return nominatedBlockSubareaRepository.findAllByNominationDetail(nominationDetail);
  }
}
