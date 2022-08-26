package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import java.util.Optional;
import javax.transaction.Transactional;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellService;

@Service
class NominatedWellDetailPersistenceService {

  private final NominatedWellDetailRepository nominatedWellDetailRepository;
  private final NominatedWellService nominatedWellService;

  @Autowired
  NominatedWellDetailPersistenceService(NominatedWellDetailRepository nominatedWellDetailRepository,
                                        NominatedWellService nominatedWellService) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
    this.nominatedWellService = nominatedWellService;
  }

  @Transactional
  public void createOrUpdateNominatedWellDetail(NominationDetail nominationDetail, NominatedWellDetailForm form) {
    NominatedWellDetail wellNomination = nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateNominatedWellDetailFromForm(nominationDetail, entity, form))
        .orElseGet(() -> createNominatedWellDetailFromForm(nominationDetail, form));
    nominatedWellService.saveNominatedWells(nominationDetail, form);
    nominatedWellDetailRepository.save(wellNomination);
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
