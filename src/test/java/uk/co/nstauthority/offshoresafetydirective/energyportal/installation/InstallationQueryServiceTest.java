package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

class InstallationQueryServiceTest {

  private static FacilityApi facilityApi;

  private static final ServiceConfigurationProperties serviceConfigurationProperties = new ServiceConfigurationProperties(
      "name",
      "mnemonic"
  );

  private static InstallationQueryService installationQueryService;

  @BeforeAll
  static void setup() {

    facilityApi = mock(FacilityApi.class);

    installationQueryService = new InstallationQueryService(
        facilityApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );
  }

  @Test
  void queryInstallationsByName_whenNoResults_thenEmptyList() {

    var searchTerm = "installation";

    when(facilityApi.searchFacilitiesByNameAndTypeIn(
        eq(searchTerm),
        eq(InstallationQueryService.ALLOWED_INSTALLATION_TYPES),
        eq(InstallationQueryService.FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    assertThat(installationQueryService.queryInstallationsByName(searchTerm)).isEmpty();
  }

  @Test
  void queryInstallationsByName_whenResults_thenMappedCorrectly() {

    var searchTerm = "installation";

    var expectedFacility = EpaFacilityTestUtil.builder().build();

    when(facilityApi.searchFacilitiesByNameAndTypeIn(
        eq(searchTerm),
        eq(InstallationQueryService.ALLOWED_INSTALLATION_TYPES),
        eq(InstallationQueryService.FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedFacility));

    var resultingFacilities = installationQueryService.queryInstallationsByName(searchTerm);

    assertThat(resultingFacilities)
        .extracting(
            InstallationDto::id,
            InstallationDto::name
        )
        .containsExactly(
            tuple(
                expectedFacility.getId(),
                expectedFacility.getName()
            )
        );
  }

  @Test
  void getInstallationsByIdIn_whenNoResults_thenEmptyList() {

    var facilityIdList = List.of(100);

    when(facilityApi.searchFacilitiesByIds(
        eq(facilityIdList),
        eq(InstallationQueryService.FACILITIES_BY_IDS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    assertThat(installationQueryService.getInstallationsByIdIn(facilityIdList)).isEmpty();
  }

  @Test
  void getInstallationsByIdIn_whenResults_thenMappedCorrectly() {

    var facilityIdList = List.of(100);

    var expectedFacility = EpaFacilityTestUtil.builder().build();

    when(facilityApi.searchFacilitiesByIds(
        eq(facilityIdList),
        eq(InstallationQueryService.FACILITIES_BY_IDS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedFacility));

    var resultingFacilities = installationQueryService.getInstallationsByIdIn(facilityIdList);

    assertThat(resultingFacilities)
        .extracting(
            InstallationDto::id,
            InstallationDto::name
        )
        .containsExactly(
            tuple(
                expectedFacility.getId(),
                expectedFacility.getName()
            )
        );
  }
}