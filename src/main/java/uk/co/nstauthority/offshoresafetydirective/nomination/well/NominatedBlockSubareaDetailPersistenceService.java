package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedBlockSubareaDetailPersistenceService {

  private final NominatedBlockSubareaDetailRepository nominatedBlockSubareaDetailRepository;

  @Autowired
  NominatedBlockSubareaDetailPersistenceService(NominatedBlockSubareaDetailRepository nominatedBlockSubareaDetailRepository) {
    this.nominatedBlockSubareaDetailRepository = nominatedBlockSubareaDetailRepository;
  }

  @Transactional
  public void createOrUpdateNominatedBlockSubareaDetail(NominationDetail nominationDetail, NominatedBlockSubareaForm form) {
    NominatedBlockSubareaDetail blockSubareaDetail =
        nominatedBlockSubareaDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateNominatedBlockSubareaDetailFromForm(nominationDetail, entity, form))
        .orElseGet(() -> createNominatedBlockSubareaDetailFromForm(nominationDetail, form));
    nominatedBlockSubareaDetailRepository.save(blockSubareaDetail);
  }

  @Transactional
  public void deleteByNominationDetail(NominationDetail nominationDetail) {
    nominatedBlockSubareaDetailRepository.deleteAllByNominationDetail(nominationDetail);
  }

  Optional<NominatedBlockSubareaDetail> findByNominationDetail(NominationDetail nominationDetail) {
    return nominatedBlockSubareaDetailRepository.findByNominationDetail(nominationDetail);
  }

  private NominatedBlockSubareaDetail updateNominatedBlockSubareaDetailFromForm(NominationDetail nominationDetail,
                                                                                NominatedBlockSubareaDetail blockSubareaDetail,
                                                                                NominatedBlockSubareaForm form) {
    blockSubareaDetail.setNominationDetail(nominationDetail);
    blockSubareaDetail.setValidForFutureWellsInSubarea(form.getValidForFutureWellsInSubarea());
    blockSubareaDetail.setForAllWellPhases(form.getForAllWellPhases());
    if (BooleanUtils.isTrue(form.getForAllWellPhases())) {
      blockSubareaDetail.setExplorationAndAppraisalPhase(null);
      blockSubareaDetail.setDevelopmentPhase(null);
      blockSubareaDetail.setDecommissioningPhase(null);
    } else {
      blockSubareaDetail.setExplorationAndAppraisalPhase(form.getExplorationAndAppraisalPhase());
      blockSubareaDetail.setDevelopmentPhase(form.getDevelopmentPhase());
      blockSubareaDetail.setDecommissioningPhase(form.getDecommissioningPhase());
    }
    return blockSubareaDetail;
  }

  private NominatedBlockSubareaDetail createNominatedBlockSubareaDetailFromForm(NominationDetail nominationDetail,
                                                                                NominatedBlockSubareaForm form) {
    return updateNominatedBlockSubareaDetailFromForm(nominationDetail, new NominatedBlockSubareaDetail(), form);
  }
}
