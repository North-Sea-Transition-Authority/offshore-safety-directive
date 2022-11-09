package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;

@Service
public class FieldRestService {

  private final FieldApi fieldApi;
  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  static final List<FieldStatus> NON_DELETION_FIELD_STATUSES = EnumSet.allOf(FieldStatus.class)
      .stream()
      .filter(fieldStatus -> !EnumSet.of(FieldStatus.STATUS9999).contains(fieldStatus))
      .toList();

  @Autowired
  FieldRestService(FieldApi fieldApi,
                   EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.fieldApi = fieldApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public RestSearchResult searchForFields(String searchTerm) {
    var projectionRoot = new FieldsProjectionRoot()
        .fieldId()
        .fieldName();

    var fields = energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) ->
        fieldApi.searchFields(searchTerm, NON_DELETION_FIELD_STATUSES, projectionRoot,
            requestPurpose.purpose(), logCorrelationId.id()));

    var searchItems = fields.stream()
        .map(field -> new RestSearchItem(field.getFieldId().toString(), field.getFieldName()))
        .toList();

    return new RestSearchResult(searchItems);
  }

  public List<FieldAddToListItem> getAddToListItemsFromFieldIds(Collection<Integer> fieldIds) {
    var projection = new FieldProjectionRoot()
        .fieldId()
        .fieldName();

    // TODO OSDOP-293 - Backfit EPA Fields multi-id search
    var results = energyPortalApiWrapper.makeRequest((logCorrelationId, requestPurpose) -> fieldIds.stream()
        .map(fieldId -> fieldApi.findFieldById(fieldId, projection, requestPurpose.purpose(), logCorrelationId.id()))
        .toList());

    return results.stream()
        .flatMap(Optional::stream)
        .filter(Objects::nonNull)
        .map(this::mapFieldToAddToListItem)
        .toList();
  }

  @NotNull
  private FieldAddToListItem mapFieldToAddToListItem(Field field) {
    return new FieldAddToListItem(field.getFieldId().toString(), field.getFieldName(), true);
  }

}
