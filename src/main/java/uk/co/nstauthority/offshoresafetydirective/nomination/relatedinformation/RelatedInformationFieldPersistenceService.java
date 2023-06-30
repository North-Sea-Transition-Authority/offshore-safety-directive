package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.FieldId;

@Service
class RelatedInformationFieldPersistenceService {

  private final RelatedInformationFieldRepository relatedInformationFieldRepository;
  private final EnergyPortalFieldQueryService energyPortalFieldQueryService;

  @Autowired
  RelatedInformationFieldPersistenceService(RelatedInformationFieldRepository relatedInformationFieldRepository,
                                            EnergyPortalFieldQueryService energyPortalFieldQueryService) {
    this.relatedInformationFieldRepository = relatedInformationFieldRepository;
    this.energyPortalFieldQueryService = energyPortalFieldQueryService;
  }

  public List<RelatedInformationField> getRelatedInformationFields(RelatedInformation relatedInformation) {
    return relatedInformationFieldRepository.findAllByRelatedInformation(relatedInformation);
  }

  @Transactional
  public void saveAllRelatedInformationFields(Collection<RelatedInformationField> relatedInformationFields) {
    relatedInformationFieldRepository.saveAll(relatedInformationFields);
  }

  @Transactional
  public void updateLinkedFields(RelatedInformation relatedInformation, Collection<FieldId> fieldIds) {

    removeExistingLinkedFields(relatedInformation);

    var uniqueFieldIds = Set.copyOf(fieldIds);

    var fields = energyPortalFieldQueryService.getFieldsByIds(uniqueFieldIds);

    var relatedInformationFields = fields.stream()
        .map(field -> createRelatedInformationField(relatedInformation, field))
        .toList();

    if (!relatedInformationFields.isEmpty()) {
      relatedInformationFieldRepository.saveAll(relatedInformationFields);
    }
  }

  private RelatedInformationField createRelatedInformationField(RelatedInformation relatedInformation, FieldDto field) {
    var newField = new RelatedInformationField();
    newField.setFieldId(field.fieldId().id());
    newField.setFieldName(field.name());
    newField.setRelatedInformation(relatedInformation);
    return newField;
  }

  @Transactional
  public void removeExistingLinkedFields(RelatedInformation relatedInformation) {
    relatedInformationFieldRepository.deleteAllByRelatedInformation(relatedInformation);
  }

}
