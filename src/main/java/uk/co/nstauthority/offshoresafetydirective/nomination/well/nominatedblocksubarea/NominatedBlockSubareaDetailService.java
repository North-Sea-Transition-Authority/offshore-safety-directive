package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import java.util.List;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NominatedBlockSubareaDetailService {

  private final NominatedBlockSubareaDetailRepository nominatedBlockSubareaDetailRepository;
  private final NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator;
  private final NominatedBlockSubareaService nominatedBlockSubareaService;

  @Autowired
  NominatedBlockSubareaDetailService(NominatedBlockSubareaDetailRepository nominatedBlockSubareaDetailRepository,
                                     NominatedBlockSubareaFormValidator nominatedBlockSubareaFormValidator,
                                     NominatedBlockSubareaService nominatedBlockSubareaService) {
    this.nominatedBlockSubareaDetailRepository = nominatedBlockSubareaDetailRepository;
    this.nominatedBlockSubareaFormValidator = nominatedBlockSubareaFormValidator;
    this.nominatedBlockSubareaService = nominatedBlockSubareaService;
  }

  @Transactional
  public void createOrUpdateNominatedBlockSubareaDetail(NominationDetail nominationDetail, NominatedBlockSubareaForm form) {
    NominatedBlockSubareaDetail blockSubareaDetail =
        nominatedBlockSubareaDetailRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateNominatedBlockSubareaDetailFromForm(nominationDetail, entity, form))
        .orElseGet(() -> createNominatedBlockSubareaDetailFromForm(nominationDetail, form));
    nominatedBlockSubareaDetailRepository.save(blockSubareaDetail);
  }

  BindingResult validate(NominatedBlockSubareaForm form, BindingResult bindingResult) {
    nominatedBlockSubareaFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  NominatedBlockSubareaForm getForm(NominationDetail nominationDetail) {
    return nominatedBlockSubareaDetailRepository.findByNominationDetail(nominationDetail)
        .map(this::nominatedSubareaDetailToForm)
        .orElseGet(NominatedBlockSubareaForm::new);
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

  private NominatedBlockSubareaDetail updateNominatedBlockSubareaDetailFromForm(NominationDetail nominationDetail,
                                                                                NominatedBlockSubareaDetail blockSubareaDetail,
                                                                                NominatedBlockSubareaForm form) {
    blockSubareaDetail.setNominationDetail(nominationDetail);
    blockSubareaDetail.setValidForFutureWellsInSubarea(form.getValidForFutureWellsInSubarea());
    blockSubareaDetail.setForAllWellPhases(form.getForAllWellPhases());
    if (BooleanUtils.isTrue(form.getForAllWellPhases())) {
      blockSubareaDetail.setExplorationAndAppraisalPhase(null);
      blockSubareaDetail.setDevelopmentPhase(null);
      blockSubareaDetail.setDecommissioningPhase(null);
    } else {
      blockSubareaDetail.setExplorationAndAppraisalPhase(form.getExplorationAndAppraisalPhase());
      blockSubareaDetail.setDevelopmentPhase(form.getDevelopmentPhase());
      blockSubareaDetail.setDecommissioningPhase(form.getDecommissioningPhase());
    }
    return blockSubareaDetail;
  }

  private NominatedBlockSubareaDetail createNominatedBlockSubareaDetailFromForm(NominationDetail nominationDetail,
                                                                                NominatedBlockSubareaForm form) {
    return updateNominatedBlockSubareaDetailFromForm(nominationDetail, new NominatedBlockSubareaDetail(), form);
  }
}
