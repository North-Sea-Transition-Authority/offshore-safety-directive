package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class EnergyPortalFieldQueryService {

  static final FieldsProjectionRoot MULTI_FIELD_PROJECTION_ROOT =
      new FieldsProjectionRoot()
          .fieldId()
          .fieldName()
          .status().root();

  private final EnergyPortalApiWrapper energyPortalApiWrapper;
  private final FieldApi fieldApi;

  @Autowired
  public EnergyPortalFieldQueryService(EnergyPortalApiWrapper energyPortalApiWrapper,
                                       FieldApi fieldApi) {
    this.energyPortalApiWrapper = energyPortalApiWrapper;
    this.fieldApi = fieldApi;
  }

  public List<FieldDto> getFieldsByIds(Collection<FieldId> fieldIds) {

    if (CollectionUtils.isEmpty(fieldIds)) {
      return Collections.emptyList();
    }

    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
        fieldApi.searchFields(
            null,
            null,
            fieldIds.stream().map(FieldId::id).toList(),
            MULTI_FIELD_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
    )
        .stream()
        .map(FieldDto::fromPortalField)
        .sorted(Comparator.comparing(field -> field.name().toLowerCase()))
        .toList();
  }

  public List<FieldDto> searchFields(String fieldName, Set<FieldStatus> fieldStatuses) {
    return energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
        fieldApi.searchFields(
            fieldName,
            fieldStatuses.stream().toList(),
            MULTI_FIELD_PROJECTION_ROOT,
            requestPurpose,
            logCorrelationId
        )
    )
        .stream()
        .map(FieldDto::fromPortalField)
        .sorted(Comparator.comparing(field -> field.name().toLowerCase()))
        .toList();
  }

}
