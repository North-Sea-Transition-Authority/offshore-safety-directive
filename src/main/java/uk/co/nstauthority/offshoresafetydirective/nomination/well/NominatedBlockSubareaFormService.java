package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedBlockSubareaFormService {

  private final NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;
  private final NominatedBlockSubareaPersistenceService nominatedBlockSubareaPersistenceService;
  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Autowired
  NominatedBlockSubareaFormService(NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator,
                                          NominatedBlockSubareaPersistenceService nominatedBlockSubareaService,
                                          NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaPersistenceService) {
    this.nominatedBlockSubareaFormValidator = nominatedBlockSubareaFormValidator;
    this.nominatedBlockSubareaPersistenceService = nominatedBlockSubareaService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaPersistenceService;
  }

  NominatedBlockSubareaForm getForm(NominationDetail nominationDetail) {
    return nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(nominationDetail)
        .map(this::nominatedSubareaDetailToForm)
        .orElseGet(NominatedBlockSubareaForm::new);
  }

  BindingResult validate(NominatedBlockSubareaForm form, BindingResult bindingResult) {
    nominatedBlockSubareaFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NominatedBlockSubareaForm nominatedSubareaDetailToForm(NominatedBlockSubareaDetail entity) {
    var form = new NominatedBlockSubareaForm();
    form.setValidForFutureWellsInSubarea(entity.getValidForFutureWellsInSubarea());
    form.setForAllWellPhases(entity.getForAllWellPhases());
    form.setExplorationAndAppraisalPhase(entity.getExplorationAndAppraisalPhase());
    form.setDevelopmentPhase(entity.getDevelopmentPhase());
    form.setDecommissioningPhase(entity.getDecommissioningPhase());
    List<Integer> blockSubareaIds = nominatedBlockSubareaPersistenceService
        .findAllByNominationDetail(entity.getNominationDetail())
        .stream()
        .map(NominatedBlockSubarea::getBlockSubareaId)
        .toList();
    form.setSubareas(blockSubareaIds);
    return form;
  }
}
