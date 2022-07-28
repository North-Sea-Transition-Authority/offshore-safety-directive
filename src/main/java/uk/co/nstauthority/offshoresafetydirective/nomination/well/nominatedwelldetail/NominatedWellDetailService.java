package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWell;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellService;

@Service
class NominatedWellDetailService {

  private final NominatedWellDetailRepository nominatedWellDetailRepository;
  private final NominatedWellDetailFormValidator nominatedWellDetailFormValidator;
  private final NominatedWellService nominatedWellService;

  @Autowired
  NominatedWellDetailService(NominatedWellDetailRepository nominatedWellDetailRepository,
                             NominatedWellDetailFormValidator nominatedWellDetailFormValidator,
                             NominatedWellService nominatedWellService) {
    this.nominatedWellDetailRepository = nominatedWellDetailRepository;
    this.nominatedWellDetailFormValidator = nominatedWellDetailFormValidator;
    this.nominatedWellService = nominatedWellService;
  }

  @Transactional
  public void createOrUpdateNominatedWellDetail(NominationDetail nominationDetail, NominatedWellDetailForm form) {
    NominatedWellDetail wellNomination = nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateNominatedWellDetailFromForm(nominationDetail, entity, form))
        .orElseGet(() -> createNominatedWellDetailFromForm(nominationDetail, form));
    nominatedWellDetailRepository.save(wellNomination);
  }

  BindingResult validate(NominatedWellDetailForm form, BindingResult bindingResult) {
    nominatedWellDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  NominatedWellDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedWellDetailRepository.findByNominationDetail(nominationDetail)
        .map(this::nominatedWellDetailEntityToForm)
        .orElseGet(NominatedWellDetailForm::new);
  }

  private NominatedWellDetailForm nominatedWellDetailEntityToForm(NominatedWellDetail entity) {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(entity.getForAllWellPhases());
    form.setExplorationAndAppraisalPhase(entity.getExplorationAndAppraisalPhase());
    form.setDevelopmentPhase(entity.getDevelopmentPhase());
    form.setDecommissioningPhase(entity.getDecommissioningPhase());
    List<Integer> wellIds = nominatedWellService.findAllByNominationDetail(entity.getNominationDetail())
        .stream()
        .map(NominatedWell::getWellId)
        .toList();
    form.setWells(wellIds);
    return form;
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
