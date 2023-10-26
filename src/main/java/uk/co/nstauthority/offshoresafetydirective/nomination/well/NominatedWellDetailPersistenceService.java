package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellDetailPersistenceService {

  private final NominatedWellDetailRepository nominatedWellDetailRepository;
  private final NominatedWellPersistenceService nominatedWellPersistenceService;

  @Autowired
  NominatedWellDetailPersistenceService(NominatedWellDetailRepository nominatedWellDetailRepository,
                                        NominatedWellPersistenceService nominatedWellPersistenceService) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
    this.nominatedWellPersistenceService = nominatedWellPersistenceService;
  }

  @Transactional
  public void saveNominatedWellDetail(NominatedWellDetail nominatedWellDetail) {
    nominatedWellDetailRepository.save(nominatedWellDetail);
  }

  @Transactional
  public void createOrUpdateNominatedWellDetail(NominationDetail nominationDetail, NominatedWellDetailForm form) {
    NominatedWellDetail wellNomination = nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateNominatedWellDetailFromForm(nominationDetail, entity, form))
        .orElseGet(() -> createNominatedWellDetailFromForm(nominationDetail, form));
    nominatedWellPersistenceService.saveNominatedWells(nominationDetail, form);
    nominatedWellDetailRepository.save(wellNomination);
  }

  @Transactional
  public void deleteByNominationDetail(NominationDetail nominationDetail) {
    nominatedWellDetailRepository.deleteAllByNominationDetail(nominationDetail);
  }

  private NominatedWellDetail createNominatedWellDetailFromForm(NominationDetail nominationDetail,
                                                                NominatedWellDetailForm form) {
    var nominatedWellDetail = new NominatedWellDetail(nominationDetail, Boolean.valueOf(form.getForAllWellPhases()));
    if (BooleanUtils.isFalse(Boolean.valueOf(form.getForAllWellPhases()))) {
      nominatedWellDetail
          .setExplorationAndAppraisalPhase(Boolean.valueOf(form.getExplorationAndAppraisalPhase()))
          .setDevelopmentPhase(Boolean.valueOf(form.getDevelopmentPhase()))
          .setDecommissioningPhase(Boolean.valueOf(form.getDecommissioningPhase()));
    }
    return nominatedWellDetail;
  }

  private NominatedWellDetail updateNominatedWellDetailFromForm(NominationDetail nominationDetail,
                                                                NominatedWellDetail nominatedWellDetail,
                                                                NominatedWellDetailForm form) {
    nominatedWellDetail.setNominationDetail(nominationDetail);
    nominatedWellDetail.setForAllWellPhases(Boolean.valueOf(form.getForAllWellPhases()));
    if (BooleanUtils.isTrue(Boolean.valueOf(form.getForAllWellPhases()))) {
      nominatedWellDetail.setExplorationAndAppraisalPhase(null);
      nominatedWellDetail.setDevelopmentPhase(null);
      nominatedWellDetail.setDecommissioningPhase(null);
    } else {
      nominatedWellDetail.setExplorationAndAppraisalPhase(Boolean.valueOf(form.getExplorationAndAppraisalPhase()));
      nominatedWellDetail.setDevelopmentPhase(Boolean.valueOf(form.getDevelopmentPhase()));
      nominatedWellDetail.setDecommissioningPhase(Boolean.valueOf(form.getDecommissioningPhase()));
    }
    return nominatedWellDetail;
  }
}
