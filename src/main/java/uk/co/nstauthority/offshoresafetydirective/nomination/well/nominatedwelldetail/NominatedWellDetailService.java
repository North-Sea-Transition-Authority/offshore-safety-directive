package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import javax.transaction.Transactional;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellDetailService {

  private final NominatedWellDetailRepository nominatedWellDetailRepository;
  private final NominatedWellDetailFormValidator nominatedWellDetailFormValidator;

  @Autowired
  NominatedWellDetailService(NominatedWellDetailRepository nominatedWellDetailRepository,
                             NominatedWellDetailFormValidator nominatedWellDetailFormValidator) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
    this.nominatedWellDetailFormValidator = nominatedWellDetailFormValidator;
  }

  @Transactional
  public void createOrUpdateSpecificWellsNomination(NominationDetail nominationDetail, NominatedWellDetailForm form) {
    NominatedWellDetail wellNomination = nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateSpecificWellNominationFromForm(nominationDetail, entity, form))
        .orElseGet(() -> createNewSpecificWellNominationFromForm(nominationDetail, form));
    nominatedWellDetailRepository.save(wellNomination);
  }

  BindingResult validate(NominatedWellDetailForm form, BindingResult bindingResult) {
    nominatedWellDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  NominatedWellDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(this::specificWellSetupEntityToForm)
        .orElseGet(NominatedWellDetailForm::new);
  }

  private NominatedWellDetailForm specificWellSetupEntityToForm(NominatedWellDetail entity) {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(entity.getForAllWellPhases());
    form.setExplorationAndAppraisalPhase(entity.getExplorationAndAppraisalPhase());
    form.setDevelopmentPhase(entity.getDevelopmentPhase());
    form.setDecommissioningPhase(entity.getDecommissioningPhase());
    return form;
  }

  private NominatedWellDetail createNewSpecificWellNominationFromForm(NominationDetail nominationDetail,
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

  private NominatedWellDetail updateSpecificWellNominationFromForm(NominationDetail nominationDetail,
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
