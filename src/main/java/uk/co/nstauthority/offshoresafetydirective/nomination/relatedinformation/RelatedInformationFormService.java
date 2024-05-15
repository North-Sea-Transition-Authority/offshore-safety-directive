package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;

@Service
class RelatedInformationFormService {

  private final RelatedInformationPersistenceService relatedInformationPersistenceService;
  private final RelatedInformationFieldRepository relatedInformationFieldRepository;

  @Autowired
  RelatedInformationFormService(
      RelatedInformationPersistenceService relatedInformationPersistenceService,
      RelatedInformationFieldRepository relatedInformationFieldRepository) {
    this.relatedInformationPersistenceService = relatedInformationPersistenceService;
    this.relatedInformationFieldRepository = relatedInformationFieldRepository;
  }

  RelatedInformationForm getForm(NominationDetail nominationDetail) {
    return relatedInformationPersistenceService.getRelatedInformation(nominationDetail)
        .map(this::mapRelatedInformationToForm)
        .orElseGet(RelatedInformationForm::new);
  }

  private RelatedInformationForm mapRelatedInformationToForm(RelatedInformation relatedInformation) {
    var form = new RelatedInformationForm();

    var fields = relatedInformationFieldRepository.findAllByRelatedInformation(relatedInformation)
        .stream()
        .map(RelatedInformationField::getFieldId)
        .map(Objects::toString)
        .toList();

    form.setRelatedToAnyFields(String.valueOf(relatedInformation.getRelatedToFields()));
    form.setFields(fields);

    form.setRelatedToAnyLicenceApplications(String.valueOf(relatedInformation.getRelatedToLicenceApplications()));

    if (BooleanUtils.isTrue(relatedInformation.getRelatedToLicenceApplications())) {
      form.setRelatedLicenceApplications(relatedInformation.getRelatedLicenceApplications());
    } else {
      form.setRelatedLicenceApplications(null);
    }

    form.setRelatedToAnyWellApplications(String.valueOf(relatedInformation.getRelatedToWellApplications()));

    if (BooleanUtils.isTrue(relatedInformation.getRelatedToWellApplications())) {
      form.setRelatedWellApplications(relatedInformation.getRelatedWellApplications());
    } else {
      form.setRelatedWellApplications(null);
    }

    return form;
  }
}
