package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NomineeDetailService {

  private final NomineeDetailRepository nomineeDetailRepository;
  private final NomineeDetailFormValidator nomineeDetailFormValidator;

  @Autowired
  NomineeDetailService(
      NomineeDetailRepository nomineeDetailRepository,
      NomineeDetailFormValidator nomineeDetailFormValidator) {
    this.nomineeDetailRepository = nomineeDetailRepository;
    this.nomineeDetailFormValidator = nomineeDetailFormValidator;
  }

  @Transactional
  public void createOrUpdateNomineeDetail(NominationDetail detail, NomineeDetailForm form) {
    var nomineeDetailOptional = nomineeDetailRepository.findByNominationDetail(detail);
    NomineeDetail nomineeDetail;
    nomineeDetail = nomineeDetailOptional.map(value -> updateNomineeDetailEntityFromForm(detail, value, form))
        .orElseGet(() -> newNomineeDetailEntityFromForm(detail, form));
    nomineeDetailRepository.save(nomineeDetail);
  }

  NomineeDetailForm getForm(NominationDetail detail) {
    return nomineeDetailRepository.findByNominationDetail(detail)
        .map(this::nomineeDetailEntityToForm)
        .orElseGet(NomineeDetailForm::new);
  }

  BindingResult validate(NomineeDetailForm form, BindingResult bindingResult) {
    nomineeDetailFormValidator.validate(form, bindingResult);
    return bindingResult;
  }

  private NomineeDetail newNomineeDetailEntityFromForm(NominationDetail detail, NomineeDetailForm form) {
    return new NomineeDetail(
        detail,
        form.getNominatedOrganisationId(),
        form.getReasonForNomination(),
        createProposedStartDate(form),
        form.getOperatorHasAuthority(),
        form.getOperatorHasCapacity(),
        form.getLicenseeAcknowledgeOperatorRequirements()
    );
  }

  private NomineeDetail updateNomineeDetailEntityFromForm(NominationDetail nominationDetail,
                                                          NomineeDetail nomineeDetail,
                                                          NomineeDetailForm form) {
    nomineeDetail.setNominationDetail(nominationDetail);
    nomineeDetail.setNominatedOrganisationId(form.getNominatedOrganisationId());
    nomineeDetail.setReasonForNomination(form.getReasonForNomination());
    nomineeDetail.setPlannedStartDate(createProposedStartDate(form));
    nomineeDetail.setOperatorHasAuthority(form.getOperatorHasAuthority());
    nomineeDetail.setOperatorHasCapacity(form.getOperatorHasCapacity());
    nomineeDetail.setLicenseeAcknowledgeOperatorRequirements(form.getLicenseeAcknowledgeOperatorRequirements());
    return nomineeDetail;
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

  private LocalDate createProposedStartDate(NomineeDetailForm form) {
    return LocalDate.of(
        Integer.parseInt(form.getPlannedStartYear()),
        Integer.parseInt(form.getPlannedStartMonth()),
        Integer.parseInt(form.getPlannedStartDay())
    );
  }
}
