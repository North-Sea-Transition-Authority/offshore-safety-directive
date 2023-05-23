package uk.co.nstauthority.offshoresafetydirective.nomination.relatedinformation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.offshoresafetydirective.energyportal.fields.EnergyPortalFieldQueryService;

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
  public void updateLinkedFields(RelatedInformation relatedInformation, Collection<Integer> fieldIds) {

    removeExistingLinkedFields(relatedInformation);

    var uniqueFieldIds = Set.copyOf(fieldIds);

    var fields = energyPortalFieldQueryService.getPortalFieldsByIds(uniqueFieldIds);

    var relatedInformationFields = fields.stream()
        .map(field -> createRelatedInformationField(relatedInformation, field))
        .toList();

    if (!relatedInformationFields.isEmpty()) {
      relatedInformationFieldRepository.saveAll(relatedInformationFields);
    }
  }

  private RelatedInformationField createRelatedInformationField(RelatedInformation relatedInformation, Field field) {
    var newField = new RelatedInformationField();
    newField.setFieldId(field.getFieldId());
    newField.setFieldName(field.getFieldName());
    newField.setRelatedInformation(relatedInformation);
    return newField;
  }

  @Transactional
  public void removeExistingLinkedFields(RelatedInformation relatedInformation) {
    relatedInformationFieldRepository.deleteAllByRelatedInformation(relatedInformation);
  }

}
