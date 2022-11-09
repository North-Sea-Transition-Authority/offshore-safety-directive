package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

@Service
public class EnergyPortalFieldQueryService {

  private final EnergyPortalApiWrapper energyPortalApiWrapper;
  private final FieldApi fieldApi;

  @Autowired
  public EnergyPortalFieldQueryService(
      EnergyPortalApiWrapper energyPortalApiWrapper, FieldApi fieldApi) {
    this.energyPortalApiWrapper = energyPortalApiWrapper;
    this.fieldApi = fieldApi;
  }

  public List<Field> getPortalFieldsByIds(Collection<Integer> fieldIds) {
    var projection = new FieldProjectionRoot()
        .fieldId()
        .fieldName();

    var fields = fieldIds.stream()
        .map(id ->
            energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
                fieldApi.findFieldById(id, projection, requestPurpose.purpose(), logCorrelationId.id())))
        .toList();

    if (fields.stream().anyMatch(Optional::isEmpty)) {
      var displayableFieldIds = fieldIds.stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));
      throw new IllegalArgumentException(
          "Not all fields ids [%s] could be resolved to a field".formatted(displayableFieldIds));
    }
    return fields.stream().flatMap(Optional::stream).toList();
  }

}
