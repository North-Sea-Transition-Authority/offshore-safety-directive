package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedWellDetailFormService {

  private final NominatedWellDetailFormValidator nominatedWellDetailFormValidator;
  private final NominatedWellDetailAccessService nominatedWellDetailAccessService;
  private final NominatedWellAccessService nominatedWellAccessService;

  @Autowired
  NominatedWellDetailFormService(NominatedWellDetailFormValidator nominatedWellDetailFormValidator,
                                 NominatedWellDetailAccessService nominatedWellDetailAccessService,
                                 NominatedWellAccessService nominatedWellAccessService) {
    this.nominatedWellDetailFormValidator = nominatedWellDetailFormValidator;
    this.nominatedWellDetailAccessService = nominatedWellDetailAccessService;
    this.nominatedWellAccessService = nominatedWellAccessService;
  }

  NominatedWellDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedWellDetailAccessService.getNominatedWellDetails(nominationDetail)
        .map(this::nominatedWellDetailEntityToForm)
        .orElseGet(NominatedWellDetailForm::new);
  }

  BindingResult validate(NominatedWellDetailForm form, BindingResult bindingResult) {
    nominatedWellDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NominatedWellDetailForm nominatedWellDetailEntityToForm(NominatedWellDetail entity) {
    var form = new NominatedWellDetailForm();
    form.setForAllWellPhases(Objects.toString(entity.getForAllWellPhases(), null));
    form.setExplorationAndAppraisalPhase(Objects.toString(entity.getExplorationAndAppraisalPhase(), null));
    form.setDevelopmentPhase(Objects.toString(entity.getDevelopmentPhase(), null));
    form.setDecommissioningPhase(Objects.toString(entity.getDecommissioningPhase(), null));
    List<Integer> wellIds = nominatedWellAccessService.getNominatedWells(entity.getNominationDetail())
        .stream()
        .map(NominatedWell::getWellId)
        .toList();
    form.setWells(wellIds);
    return form;
  }
}
