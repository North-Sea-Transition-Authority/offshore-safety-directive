package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.types.FieldStatus;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.util.assertion.PropertyObjectAssert;

class EnergyPortalFieldQueryServiceTest {

  static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("a request purpose");
  private FieldApi fieldApi;

  private EnergyPortalFieldQueryService energyPortalFieldQueryService;

  @BeforeEach
  void setup() {

    fieldApi = mock(FieldApi.class);

    energyPortalFieldQueryService = new EnergyPortalFieldQueryService(
        new EnergyPortalApiWrapper(),
        fieldApi
    );
  }

  @Test
  void getFieldsByIds_whenMatchingField_thenFieldReturned() {

    var matchedFieldId = new FieldId(200);

    var field = FieldTestUtil.builder()
        .withId(matchedFieldId.id())
        .withName("field-name")
        .withStatus(FieldStatus.STATUS100)
        .build();

    when(fieldApi.searchFields(
        eq(null),
        eq(null),
        eq(List.of(matchedFieldId.id())),
        eq(EnergyPortalFieldQueryService.MULTI_FIELD_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .thenReturn(List.of(field));

    var resultingFields = energyPortalFieldQueryService.getFieldsByIds(List.of(matchedFieldId), REQUEST_PURPOSE);

    assertThat(resultingFields).hasSize(1);
    PropertyObjectAssert.thenAssertThat(resultingFields.get(0))
        .hasFieldOrPropertyWithValue("fieldId", matchedFieldId)
        .hasFieldOrPropertyWithValue("name", field.getFieldName())
        .hasFieldOrPropertyWithValue("status", field.getStatus())
        .hasAssertedAllProperties();
  }

  @Test
  void getFieldsByIds_whenNoMatchingField_thenEmptyList() {

    var unmatchedFieldId = new FieldId(-100);

    when(fieldApi.searchFields(
        eq(null),
        eq(null),
        eq(List.of(unmatchedFieldId.id())),
        eq(EnergyPortalFieldQueryService.MULTI_FIELD_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .thenReturn(Collections.emptyList());

    var resultingFields = energyPortalFieldQueryService.getFieldsByIds(List.of(unmatchedFieldId), REQUEST_PURPOSE);

    assertThat(resultingFields).isEmpty();
  }

  @Test
  void getFieldsByIds_whenMultipleFieldsReturned_thenOrderedByCaseInsensitiveFieldName() {

    var firstFieldByName = FieldTestUtil.builder()
        .withName("a field")
        .withId(10)
        .build();

    var secondFieldByName = FieldTestUtil.builder()
        .withName("B field")
        .withId(20)
        .build();

    when(fieldApi.searchFields(
        eq(null),
        eq(null),
        eq(List.of(firstFieldByName.getFieldId(), secondFieldByName.getFieldId())),
        eq(EnergyPortalFieldQueryService.MULTI_FIELD_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        // return the fields out of order
        .thenReturn(List.of(secondFieldByName, firstFieldByName));

    var resultingFields = energyPortalFieldQueryService.getFieldsByIds(
        List.of(
            new FieldId(firstFieldByName.getFieldId()),
            new FieldId(secondFieldByName.getFieldId())
        ),
        REQUEST_PURPOSE
    );

    assertThat(resultingFields)
        .extracting(FieldDto::name)
        .containsExactly(firstFieldByName.getFieldName(), secondFieldByName.getFieldName());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void getFieldsByIds_whenNoIdsProvided_thenEmptyListReturned(List<FieldId> nullOrEmptyFieldIds) {
    var resultingFields = energyPortalFieldQueryService.getFieldsByIds(nullOrEmptyFieldIds, REQUEST_PURPOSE);
    assertThat(resultingFields).isEmpty();
    verify(fieldApi, never()).getFieldsByIds(any(), any(), any(), any());
  }

  @Test
  void searchFields_whenMatchingField_thenFieldReturned() {

    var fieldName = "field-name";
    var fieldStatus = FieldStatus.STATUS500;

    var expectedField = FieldTestUtil.builder().build();

    when(fieldApi.searchFields(
        eq(fieldName),
        eq(List.of(fieldStatus)),
        eq(EnergyPortalFieldQueryService.MULTI_FIELD_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .thenReturn(List.of(expectedField));

    var resultingFields = energyPortalFieldQueryService.searchFields(
        fieldName,
        Set.of(fieldStatus),
        new RequestPurpose("a request purpose"));

    assertThat(resultingFields).hasSize(1);
    PropertyObjectAssert.thenAssertThat(resultingFields.get(0))
        .hasFieldOrPropertyWithValue("fieldId", new FieldId(expectedField.getFieldId()))
        .hasFieldOrPropertyWithValue("name", expectedField.getFieldName())
        .hasFieldOrPropertyWithValue("status", expectedField.getStatus())
        .hasAssertedAllProperties();
  }

  @Test
  void searchFields_whenNoMatchingField_thenEmptyListReturned() {

    var fieldName = "field-name";
    var fieldStatus = FieldStatus.STATUS500;

    when(fieldApi.searchFields(
        eq(fieldName),
        eq(List.of(fieldStatus)),
        eq(EnergyPortalFieldQueryService.MULTI_FIELD_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        .thenReturn(Collections.emptyList());

    var resultingFields = energyPortalFieldQueryService.searchFields(
        fieldName,
        Set.of(fieldStatus),
        new RequestPurpose("a request purpose")
    );

    assertThat(resultingFields).isEmpty();
  }

  @Test
  void searchFields_whenMultipleFieldsReturned_thenOrderedByCaseInsensitiveFieldName() {

    var fieldName = "field-name";
    var fieldStatus = FieldStatus.STATUS500;

    var firstFieldByName = FieldTestUtil.builder()
        .withName("a field")
        .withId(10)
        .build();

    var secondFieldByName = FieldTestUtil.builder()
        .withName("B field")
        .withId(20)
        .build();

    when(fieldApi.searchFields(
        eq(fieldName),
        eq(List.of(fieldStatus)),
        eq(EnergyPortalFieldQueryService.MULTI_FIELD_PROJECTION_ROOT),
        any(RequestPurpose.class),
        any(LogCorrelationId.class)
    ))
        // return the fields out of order
        .thenReturn(List.of(secondFieldByName, firstFieldByName));

    var resultingFields = energyPortalFieldQueryService.searchFields(
        fieldName,
        Set.of(fieldStatus),
        new RequestPurpose("a request purpose"));

    assertThat(resultingFields)
        .extracting(FieldDto::name)
        .containsExactly(firstFieldByName.getFieldName(), secondFieldByName.getFieldName());
  }
}