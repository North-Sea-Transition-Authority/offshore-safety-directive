package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;

@Service
class WellSelectionSetupService {

  private final WellSelectionSetupRepository wellSelectionSetupRepository;
  private final NominationDetailService nominationDetailService;
  private final WellSelectionSetupFormValidator wellSelectionSetupFormValidator;

  @Autowired
  WellSelectionSetupService(WellSelectionSetupRepository wellSelectionSetupRepository,
                            NominationDetailService nominationDetailService,
                            WellSelectionSetupFormValidator wellSelectionSetupFormValidator) {
    this.wellSelectionSetupRepository = wellSelectionSetupRepository;
    this.nominationDetailService = nominationDetailService;
    this.wellSelectionSetupFormValidator = wellSelectionSetupFormValidator;
  }

  @Transactional
  public void createOrUpdateWellSelectionSetup(WellSelectionSetupForm form, int nominationId) {
    var nominationDetail = nominationDetailService.getLatestNominationDetail(nominationId);
    var wellSetup = wellSelectionSetupRepository.findByNominationDetail(nominationDetail)
        .map(entity -> updateWellSelectionSetupWithForm(nominationDetail, entity, form))
        .orElseGet(() -> newWellSelectionSetupFromForm(nominationDetail, form));
    wellSelectionSetupRepository.save(wellSetup);
  }

  WellSelectionSetupForm getForm(NominationDetail nominationDetail) {
    return wellSelectionSetupRepository.findByNominationDetail(nominationDetail)
        .map(this::wellSelectionSetupEntityToForm)
        .orElse(new WellSelectionSetupForm());
  }

  BindingResult validate(WellSelectionSetupForm form, BindingResult bindingResult) {
    wellSelectionSetupFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private WellSelectionSetupForm wellSelectionSetupEntityToForm(WellSelectionSetup wellSelectionSetup) {
    var form = new WellSelectionSetupForm();
    form.setWellSelectionType(wellSelectionSetup.getSelectionType().name());
    return form;
  }

  private WellSelectionSetup newWellSelectionSetupFromForm(NominationDetail nominationDetail,
                                                           WellSelectionSetupForm form) {
    return updateWellSelectionSetupWithForm(nominationDetail, new WellSelectionSetup(), form);
  }

  private WellSelectionSetup updateWellSelectionSetupWithForm(NominationDetail nominationDetail,
                                                              WellSelectionSetup wellSelectionSetup,
                                                              WellSelectionSetupForm form) {
    wellSelectionSetup.setNominationDetail(nominationDetail);
    wellSelectionSetup.setSelectionType(WellSelectionType.valueOf(form.getWellSelectionType()));
    return wellSelectionSetup;
  }
}
