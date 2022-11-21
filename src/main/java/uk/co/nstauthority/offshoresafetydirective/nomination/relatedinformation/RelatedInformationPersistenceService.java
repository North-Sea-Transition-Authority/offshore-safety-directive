package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class RelatedInformationPersistenceService {

  private final RelatedInformationRepository relatedInformationRepository;
  private final RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService;

  @Autowired
  RelatedInformationPersistenceService(
      RelatedInformationRepository relatedInformationRepository,
      RelatedInformationFieldPersistenceService relatedInformationFieldPersistenceService) {
    this.relatedInformationRepository = relatedInformationRepository;
    this.relatedInformationFieldPersistenceService = relatedInformationFieldPersistenceService;
  }

  Optional<RelatedInformation> getRelatedInformation(NominationDetail nominationDetail) {
    return relatedInformationRepository.findByNominationDetail(nominationDetail);
  }

  @Transactional
  public void createOrUpdateRelatedInformation(NominationDetail nominationDetail,
                                               RelatedInformationForm relatedInformationForm) {

    var relatedInformation = getRelatedInformation(nominationDetail)
        .orElseGet(RelatedInformation::new);

    relatedInformation.setNominationDetail(nominationDetail);
    relatedInformation.setRelatedToFields(relatedInformationForm.getRelatedToAnyFields());
    relatedInformation.setRelatedToLicenceApplications(relatedInformationForm.getRelatedToAnyLicenceApplications());
    relatedInformation.setRelatedToWellApplications(relatedInformationForm.getRelatedToAnyWellApplications());

    if (BooleanUtils.isTrue(relatedInformationForm.getRelatedToAnyLicenceApplications())) {
      relatedInformation.setRelatedLicenceApplications(relatedInformationForm.getRelatedLicenceApplications());
    } else {
      relatedInformation.setRelatedLicenceApplications(null);
    }

    if (BooleanUtils.isTrue(relatedInformationForm.getRelatedToAnyWellApplications())) {
      relatedInformation.setRelatedWellApplications(relatedInformationForm.getRelatedWellApplications());
    } else {
      relatedInformation.setRelatedLicenceApplications(null);
    }

    relatedInformationRepository.save(relatedInformation);

    if (BooleanUtils.isTrue(relatedInformationForm.getRelatedToAnyFields())) {
      relatedInformationFieldPersistenceService.updateLinkedFields(relatedInformation,
          relatedInformationForm.getFields());
    } else if (BooleanUtils.isFalse(relatedInformationForm.getRelatedToAnyFields())) {
      relatedInformationFieldPersistenceService.removeExistingLinkedFields(relatedInformation);
    }
  }

}
