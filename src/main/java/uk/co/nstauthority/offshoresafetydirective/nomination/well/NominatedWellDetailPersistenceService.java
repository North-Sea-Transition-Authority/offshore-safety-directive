package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.Optional;
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

  Optional<NominatedWellDetail> findByNominationDetail(NominationDetail nominationDetail) {
    return nominatedWellDetailRepository.findByNominationDetail(nominationDetail);
  }

  private NominatedWellDetail createNominatedWellDetailFromForm(NominationDetail nominationDetail,
                                                                NominatedWellDetailForm form) {
    NominatedWellDetail nominatedWellDetail = new NominatedWellDetail(nominationDetail, form.getForAllWellPhases());
    if (BooleanUtils.isFalse(form.getForAllWellPhases())) {
      nominatedWellDetail
          .setExplorationAndAppraisalPhase(form.getExplorationAndAppraisalPhase())
          .setDevelopmentPhase(form.getDevelopmentPhase())
          .setDecommissioningPhase(form.getDecommissioningPhase());
    }
    return nominatedWellDetail;
  }

  private NominatedWellDetail updateNominatedWellDetailFromForm(NominationDetail nominationDetail,
                                                                NominatedWellDetail nominatedWellDetail,
                                                                NominatedWellDetailForm form) {
    nominatedWellDetail.setNominationDetail(nominationDetail);
    nominatedWellDetail.setForAllWellPhases(form.getForAllWellPhases());
    if (BooleanUtils.isTrue(form.getForAllWellPhases())) {
      nominatedWellDetail.setExplorationAndAppraisalPhase(null);
      nominatedWellDetail.setDevelopmentPhase(null);
      nominatedWellDetail.setDecommissioningPhase(null);
    } else {
      nominatedWellDetail.setExplorationAndAppraisalPhase(form.getExplorationAndAppraisalPhase());
      nominatedWellDetail.setDevelopmentPhase(form.getDevelopmentPhase());
      nominatedWellDetail.setDecommissioningPhase(form.getDecommissioningPhase());
    }
    return nominatedWellDetail;
  }
}
