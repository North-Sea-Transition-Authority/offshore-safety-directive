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
    form.setValidForFutureWellsInSubarea(String.valueOf(entity.getValidForFutureWellsInSubarea()));
    form.setForAllWellPhases(String.valueOf(entity.getForAllWellPhases()));
    form.setExplorationAndAppraisalPhase(String.valueOf(entity.getExplorationAndAppraisalPhase()));
    form.setDevelopmentPhase(String.valueOf(entity.getDevelopmentPhase()));
    form.setDecommissioningPhase(String.valueOf(entity.getDecommissioningPhase()));

    List<String> blockSubareaIds = nominatedBlockSubareaPersistenceService
        .findAllByNominationDetail(entity.getNominationDetail())
        .stream()
        .map(NominatedBlockSubarea::getBlockSubareaId)
        .toList();

    form.setSubareas(blockSubareaIds);

    return form;
  }
}
