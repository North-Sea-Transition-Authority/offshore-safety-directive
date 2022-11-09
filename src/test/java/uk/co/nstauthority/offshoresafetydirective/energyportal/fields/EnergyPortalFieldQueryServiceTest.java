package uk.co.nstauthority.offshoresafetydirective.energyportal.fields;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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

@ExtendWith(SpringExtension.class)
@IntegrationTest
class EnergyPortalFieldQueryServiceTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Mock
  private FieldApi fieldApi;

  private EnergyPortalFieldQueryService energyPortalFieldQueryService;

  @BeforeEach
  void setUp() {
    var serviceConfigurationProperties = applicationContext.getBean(ServiceConfigurationProperties.class);
    var portalWrapper = new EnergyPortalApiWrapper(serviceConfigurationProperties);
    energyPortalFieldQueryService = new EnergyPortalFieldQueryService(portalWrapper, fieldApi);
  }

  @Test
  void getPortalFieldsByIds_whenIdIsValid_thenFieldReturned() {

    var field = FieldTestUtil.builder().build();

    when(fieldApi.findFieldById(eq(field.getFieldId()), any(), anyString(), anyString()))
        .thenReturn(Optional.of(field));

    var result = energyPortalFieldQueryService.getPortalFieldsByIds(List.of(field.getFieldId()));

    assertThat(result).containsExactly(field);
  }

  @Test
  void getPortalFieldsByIds_whenIdIsInvalid_thenErrorThrown() {

    var field = FieldTestUtil.builder().build();

    when(fieldApi.findFieldById(eq(field.getFieldId()), any(), anyString(), anyString()))
        .thenReturn(Optional.ofNullable(null));

    assertThrows(IllegalArgumentException.class,
        () -> energyPortalFieldQueryService.getPortalFieldsByIds(List.of(field.getFieldId())));
  }

}