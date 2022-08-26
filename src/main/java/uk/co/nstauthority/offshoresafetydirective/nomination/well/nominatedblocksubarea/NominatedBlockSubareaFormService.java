package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
public class NominatedBlockSubareaFormService {

  private final NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;
  private final NominatedBlockSubareaService nominatedBlockSubareaService;
  private final NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaDetailPersistenceService;

  @Autowired
  public NominatedBlockSubareaFormService(NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator,
                                          NominatedBlockSubareaService nominatedBlockSubareaService,
                                          NominatedBlockSubareaDetailPersistenceService nominatedBlockSubareaPersistenceService) {
    this.nominatedBlockSubareaFormValidator = nominatedBlockSubareaFormValidator;
    this.nominatedBlockSubareaService = nominatedBlockSubareaService;
    this.nominatedBlockSubareaDetailPersistenceService = nominatedBlockSubareaPersistenceService;
  }

  public NominatedBlockSubareaForm getForm(NominationDetail nominationDetail) {
    return nominatedBlockSubareaDetailPersistenceService.findByNominationDetail(nominationDetail)
        .map(this::nominatedSubareaDetailToForm)
        .orElseGet(NominatedBlockSubareaForm::new);
  }

  public BindingResult validate(NominatedBlockSubareaForm form, BindingResult bindingResult) {
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
    List<Integer> blockSubareaIds = nominatedBlockSubareaService.findAllByNominationDetail(entity.getNominationDetail())
        .stream()
        .map(NominatedBlockSubarea::getBlockSubareaId)
        .toList();
    form.setSubareas(blockSubareaIds);
    return form;
  }
}
