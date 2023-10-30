package uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class NomineeDetailPersistenceService {

  private final NomineeDetailRepository nomineeDetailRepository;

  @Autowired
  NomineeDetailPersistenceService(NomineeDetailRepository nomineeDetailRepository) {
    this.nomineeDetailRepository = nomineeDetailRepository;
  }

  Optional<NomineeDetail> getNomineeDetail(NominationDetail nominationDetail) {
    return nomineeDetailRepository.findByNominationDetail(nominationDetail);
  }

  @Transactional
  public NomineeDetail saveNomineeDetail(NomineeDetail nomineeDetail) {
    return nomineeDetailRepository.save(nomineeDetail);
  }

  @Transactional
  public void createOrUpdateNomineeDetail(NominationDetail nominationDetail, NomineeDetailForm form) {
    var nomineeDetailOptional = nomineeDetailRepository.findByNominationDetail(nominationDetail);
    NomineeDetail nomineeDetail;
    nomineeDetail = nomineeDetailOptional
        .map(value -> updateNomineeDetailEntityFromForm(nominationDetail, value, form))
        .orElseGet(() -> newNomineeDetailEntityFromForm(nominationDetail, form));
    nomineeDetailRepository.save(nomineeDetail);
  }

  private NomineeDetail newNomineeDetailEntityFromForm(NominationDetail detail, NomineeDetailForm form) {
    return new NomineeDetail(
        detail,
        form.getNominatedOrganisationId(),
        form.getReasonForNomination(),
        createProposedStartDate(form),
        BooleanUtils.toBooleanObject(form.getOperatorHasAuthority()),
        BooleanUtils.toBooleanObject(form.getOperatorHasCapacity()),
        BooleanUtils.toBooleanObject(form.getLicenseeAcknowledgeOperatorRequirements())
    );
  }

  private NomineeDetail updateNomineeDetailEntityFromForm(NominationDetail nominationDetail,
                                                          NomineeDetail nomineeDetail,
                                                          NomineeDetailForm form) {
    nomineeDetail.setNominationDetail(nominationDetail);
    nomineeDetail.setNominatedOrganisationId(form.getNominatedOrganisationId());
    nomineeDetail.setReasonForNomination(form.getReasonForNomination());
    nomineeDetail.setPlannedStartDate(createProposedStartDate(form));
    nomineeDetail.setOperatorHasAuthority(BooleanUtils.toBooleanObject(form.getOperatorHasAuthority()));
    nomineeDetail.setOperatorHasCapacity(BooleanUtils.toBooleanObject(form.getOperatorHasCapacity()));
    nomineeDetail.setLicenseeAcknowledgeOperatorRequirements(
        BooleanUtils.toBooleanObject(form.getLicenseeAcknowledgeOperatorRequirements()));
    return nomineeDetail;
  }

  private LocalDate createProposedStartDate(NomineeDetailForm form) {
    return LocalDate.of(
        Integer.parseInt(form.getPlannedStartYear()),
        Integer.parseInt(form.getPlannedStartMonth()),
        Integer.parseInt(form.getPlannedStartDay())
    );
  }
}
