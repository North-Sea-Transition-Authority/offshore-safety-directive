package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NomineeDetailFormService {

  private final NomineeDetailPersistenceService nomineeDetailPersistenceService;
  private final NomineeDetailFormValidator nomineeDetailFormValidator;

  @Autowired
  NomineeDetailFormService(NomineeDetailPersistenceService nomineeDetailPersistenceService,
                           NomineeDetailFormValidator nomineeDetailFormValidator) {
    this.nomineeDetailPersistenceService = nomineeDetailPersistenceService;
    this.nomineeDetailFormValidator = nomineeDetailFormValidator;
  }

  NomineeDetailForm getForm(NominationDetail nominationDetail) {
    return nomineeDetailPersistenceService.getNomineeDetail(nominationDetail)
        .map(this::nomineeDetailEntityToForm)
        .orElseGet(NomineeDetailForm::new);
  }

  BindingResult validate(NomineeDetailForm form, BindingResult bindingResult) {
    nomineeDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NomineeDetailForm nomineeDetailEntityToForm(NomineeDetail entity) {
    var form = new NomineeDetailForm();
    form.setNominatedOrganisationId(entity.getNominatedOrganisationId());
    form.setReasonForNomination(entity.getReasonForNomination());
    form.setPlannedStartDay(String.valueOf(entity.getPlannedStartDate().getDayOfMonth()));
    form.setPlannedStartMonth(String.valueOf(entity.getPlannedStartDate().getMonthValue()));
    form.setPlannedStartYear(String.valueOf(entity.getPlannedStartDate().getYear()));
    form.setOperatorHasAuthority(entity.getOperatorHasAuthority());
    form.setOperatorHasCapacity(entity.getOperatorHasCapacity());
    form.setLicenseeAcknowledgeOperatorRequirements(entity.getLicenseeAcknowledgeOperatorRequirements());
    return form;
  }
}
