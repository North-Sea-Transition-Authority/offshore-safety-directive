package uk.co.nstauthority.offshoresafetydirective.energyportal.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.co.fivium.energyportalapi.client.facility.FacilityApi;
import uk.co.fivium.energyportalapi.generated.types.FacilityType;
import uk.co.nstauthority.offshoresafetydirective.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.api.EnergyPortalApiWrapper;

class InstallationQueryServiceTest {

  private static FacilityApi facilityApi;

  private static final ServiceConfigurationProperties serviceConfigurationProperties = new ServiceConfigurationProperties(
      "name",
      "mnemonic"
  );

  private static InstallationQueryService installationQueryService;

  @BeforeEach
  void setup() {

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
  void queryInstallationsByName_whenResults_thenOnlyThoseInUkcsReturned() {

    var searchTerm = "installation";

    var facilityInUkcs = EpaFacilityTestUtil.builder()
        .withInUkcs(true)
        .withId(100)
        .build();

    var facilityNotInUkcs = EpaFacilityTestUtil.builder()
        .withInUkcs(false)
        .withId(200)
        .build();

    var facilityWithInUkcsNull = EpaFacilityTestUtil.builder()
        .withInUkcs(null)
        .withId(300)
        .build();

    when(facilityApi.searchFacilitiesByNameAndTypeIn(
        eq(searchTerm),
        eq(InstallationQueryService.ALLOWED_INSTALLATION_TYPES),
        eq(InstallationQueryService.FACILITIES_BY_NAME_AND_TYPES_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(facilityInUkcs, facilityNotInUkcs, facilityWithInUkcsNull));

    var resultingFacilities = installationQueryService.queryInstallationsByName(searchTerm);

    assertThat(resultingFacilities)
        .extracting(InstallationDto::id)
        .containsExactlyInAnyOrder(
            facilityInUkcs.getId(),
            facilityWithInUkcsNull.getId()
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

  @Test
  void isValidInstallations_whenValidTypeAndInUkcs_thenTrue() {

    var validInstallation = InstallationDtoTestUtil.builder()
        .withType(InstallationQueryService.ALLOWED_INSTALLATION_TYPES.get(0))
        .isInUkcs(true)
        .build();

    assertTrue(InstallationQueryService.isValidInstallation(validInstallation));
  }

  @Test
  void isValidInstallations_whenInvalidTypeAndInUkcs_thenFalse() {

    var invalidInstallationType = Arrays.stream(FacilityType.values())
        .filter(type -> !InstallationQueryService.ALLOWED_INSTALLATION_TYPES.contains(type))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Could not find installation type to use"));

    var invalidInstallation = InstallationDtoTestUtil.builder()
        .withType(invalidInstallationType)
        .isInUkcs(true)
        .build();

    assertFalse(InstallationQueryService.isValidInstallation(invalidInstallation));
  }

  @Test
  void isValidInstallations_whenInvalidTypeAndNotInUkcs_thenFalse() {

    var invalidInstallationType = Arrays.stream(FacilityType.values())
        .filter(type -> !InstallationQueryService.ALLOWED_INSTALLATION_TYPES.contains(type))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Could not find installation type to use"));

    var invalidInstallation = InstallationDtoTestUtil.builder()
        .withType(invalidInstallationType)
        .isInUkcs(false)
        .build();

    assertFalse(InstallationQueryService.isValidInstallation(invalidInstallation));
  }

  @Test
  void isValidInstallations_whenValidTypeAndNotInUkcs_thenFalse() {

    var invalidInstallation = InstallationDtoTestUtil.builder()
        .withType(InstallationQueryService.ALLOWED_INSTALLATION_TYPES.get(0))
        .isInUkcs(false)
        .build();

    assertFalse(InstallationQueryService.isValidInstallation(invalidInstallation));
  }

  @Test
  void isInUkcs_whenTrue_thenTrue() {

    var invalidInstallation = EpaFacilityTestUtil.builder()
        .withInUkcs(true)
        .build();

    assertTrue(installationQueryService.isInUkcs(invalidInstallation));
  }

  @Test
  void isInUkcs_whenNull_thenTrue() {

    var invalidInstallation = EpaFacilityTestUtil.builder()
        .withInUkcs(null)
        .build();

    assertTrue(installationQueryService.isInUkcs(invalidInstallation));
  }

  @Test
  void isInUkcs_whenFalse_thenFalse() {

    var invalidInstallation = EpaFacilityTestUtil.builder()
        .withInUkcs(false)
        .build();

    assertFalse(installationQueryService.isInUkcs(invalidInstallation));
  }

  @Test
  void getInstallationsByIds_whenNoMatches_thenEmptyListReturned() {

    var unmatchedFacilityId = List.of(-1);

    when(facilityApi.searchFacilitiesByIds(
        eq(unmatchedFacilityId),
        eq(InstallationQueryService.FACILITIES_BY_IDS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    var resultingFacilities = installationQueryService.getInstallationsByIdIn(unmatchedFacilityId);

    assertThat(resultingFacilities).isEmpty();
  }

  @Test
  void getInstallationsByIds_whenMatches_thenPopulatedListReturned() {

    var facilityId = new InstallationId(100);

    var expectedFacility = EpaFacilityTestUtil.builder().build();

    when(facilityApi.searchFacilitiesByIds(
        eq(List.of(facilityId.id())),
        eq(InstallationQueryService.FACILITIES_BY_IDS_PROJECTION_ROOT),
        any(),
        any()
    )).thenReturn(List.of(expectedFacility));

    var resultingFacilities = installationQueryService.getInstallationsByIds(List.of(facilityId));

    assertThat(resultingFacilities)
        .extracting(
            InstallationDto::id,
            InstallationDto::name,
            InstallationDto::type,
            InstallationDto::isInUkcs
        )
        .containsExactly(
            tuple(
                expectedFacility.getId(),
                expectedFacility.getName(),
                expectedFacility.getType(),
                expectedFacility.getIsInUkcs()
            )
        );
  }

  @ParameterizedTest(name = "{index} => wellbore id list=''{0}''")
  @NullAndEmptySource
  void getInstallationsByIds_whenInputIdsNullOrEmpty_thenEmptyListReturned(List<InstallationId> installationIds) {

    var resultingFacilities = installationQueryService.getInstallationsByIds(installationIds);

    assertThat(resultingFacilities).isEmpty();

    then(facilityApi).should(never()).searchFacilitiesByIds(anyList(), any(), any(), any());
  }

  @ParameterizedTest(name = "{index} => wellbore id list=''{0}''")
  @NullAndEmptySource
  void getInstallationsByIdIn_whenInputIdsNullOrEmpty_thenEmptyListReturned(List<Integer> installationIds) {

    var resultingFacilities = installationQueryService.getInstallationsByIdIn(installationIds);

    assertThat(resultingFacilities).isEmpty();

    then(facilityApi)
        .should(never())
        .searchFacilitiesByIds(anyList(), any(), any(), any());
  }
}