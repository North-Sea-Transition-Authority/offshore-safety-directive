package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;

@ExtendWith(SpringExtension.class)
@IntegrationTest
class FieldRestServiceTest {

  @Mock
  private FieldApi fieldApi;

  @Autowired
  private ApplicationContext applicationContext;

  private FieldRestService fieldRestService;

  @BeforeEach
  void setUp() {
    var serviceConfigurationProperties = applicationContext.getBean(ServiceConfigurationProperties.class);
    var energyPortalApiWrapper = new EnergyPortalApiWrapper(serviceConfigurationProperties);
    fieldRestService = new FieldRestService(fieldApi, energyPortalApiWrapper);
  }

  @Test
  void searchForFields_whenNoFieldsFound_thenEmptyResult() {
    var searchTerm = "search term";

    when(fieldApi.searchFields(eq(searchTerm), eq(FieldRestService.NON_DELETION_FIELD_STATUSES), any(),
        anyString(), anyString()))
        .thenReturn(List.of());

    var result = fieldRestService.searchForFields(searchTerm);

    assertThat(result.getResults()).isEmpty();
    verify(fieldApi).searchFields(eq(searchTerm), eq(FieldRestService.NON_DELETION_FIELD_STATUSES), any(),
        anyString(), anyString());
  }

  @Test
  void searchForFields_whenFieldsFound_thenResult() {
    var searchTerm = "search term";

    var field = FieldTestUtil.builder().build();

    when(fieldApi.searchFields(eq(searchTerm), eq(FieldRestService.NON_DELETION_FIELD_STATUSES), any(),
        anyString(), anyString()))
        .thenReturn(List.of(field));

    var result = fieldRestService.searchForFields(searchTerm);

    assertThat(result.getResults()).extracting(RestSearchItem::id, RestSearchItem::text)
        .containsExactly(Tuple.tuple(field.getFieldId().toString(), field.getFieldName()));
  }

  @Test
  void getAddToListItemsFromFieldIds_whenFieldIsReturned_thenFieldItemMatches() {

    var field = FieldTestUtil.builder().build();

    when(fieldApi.findFieldById(eq(field.getFieldId()), any(), anyString(), anyString())).thenReturn(
        Optional.of(field));

    var result = fieldRestService.getAddToListItemsFromFieldIds(List.of(field.getFieldId()));

    assertThat(result).extracting(FieldAddToListItem::getId, FieldAddToListItem::getName)
        .containsExactly(
            Tuple.tuple(field.getFieldId().toString(), field.getFieldName())
        );
  }

  @Test
  void getAddToListItemsFromFieldIds_whenFieldIsNotReturned_thenNoFieldItem() {

    Integer fieldId = 1;
    when(fieldApi.findFieldById(eq(fieldId), any(), anyString(), anyString())).thenReturn(Optional.empty());

    var result = fieldRestService.getAddToListItemsFromFieldIds(List.of(fieldId));

    assertThat(result).isEmpty();
  }
}