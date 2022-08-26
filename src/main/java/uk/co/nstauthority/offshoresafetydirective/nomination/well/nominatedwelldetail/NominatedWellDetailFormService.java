package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWell;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellService;

@Service
public class NominatedWellDetailFormService {

  private final NominatedWellDetailFormValidator nominatedWellDetailFormValidator;
  private final NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService;
  private final NominatedWellService nominatedWellService;

  @Autowired
  public NominatedWellDetailFormService(NominatedWellDetailFormValidator nominatedWellDetailFormValidator,
                                        NominatedWellDetailPersistenceService nominatedWellDetailPersistenceService,
                                        NominatedWellService nominatedWellService) {
    this.nominatedWellDetailFormValidator = nominatedWellDetailFormValidator;
    this.nominatedWellDetailPersistenceService = nominatedWellDetailPersistenceService;
    this.nominatedWellService = nominatedWellService;
  }

  public NominatedWellDetailForm getForm(NominationDetail nominationDetail) {
    return nominatedWellDetailPersistenceService.findByNominationDetail(nominationDetail)
        .map(this::nominatedWellDetailEntityToForm)
        .orElseGet(NominatedWellDetailForm::new);
  }

  public BindingResult validate(NominatedWellDetailForm form, BindingResult bindingResult) {
    nominatedWellDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
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
}
