package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellDetailFormService {

  private final NominatedWellDetailFormValidator nominatedWellDetailFormValidator;
  private final NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;
  private final NominatedWellPersistenceService nominatedWellPersistenceService;

  @Autowired
  NominatedWellDetailFormService(NominatedWellDetailFormValidator nominatedWellDetailFormValidator,
                                        NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService,
                                        NominatedWellPersistenceService nominatedWellPersistenceService) {
    this.nominatedWellDetailFormValidator = nominatedWellDetailFormValidator;
    this.nominatedWellDetailPersistenceService = nominatedWellDetailPersistenceService;
    this.nominatedWellPersistenceService = nominatedWellPersistenceService;
  }

  NominatedWellDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedWellDetailPersistenceService.findByNominationDetail(nominationDetail)
        .map(this::nominatedWellDetailEntityToForm)
        .orElseGet(NominatedWellDetailForm::new);
  }

  BindingResult validate(NominatedWellDetailForm form, BindingResult bindingResult) {
    nominatedWellDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NominatedWellDetailForm nominatedWellDetailEntityToForm(NominatedWellDetail entity) {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(entity.getForAllWellPhases());
    form.setExplorationAndAppraisalPhase(entity.getExplorationAndAppraisalPhase());
    form.setDevelopmentPhase(entity.getDevelopmentPhase());
    form.setDecommissioningPhase(entity.getDecommissioningPhase());
    List<Integer> wellIds = nominatedWellPersistenceService.findAllByNominationDetail(entity.getNominationDetail())
        .stream()
        .map(NominatedWell::getWellId)
        .toList();
    form.setWells(wellIds);
    return form;
  }
}
