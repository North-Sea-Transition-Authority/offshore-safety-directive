package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

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
        .toList();

    form.setRelatedToAnyFields(relatedInformation.getRelatedToFields());
    form.setFields(fields);
    return form;
  }
}
