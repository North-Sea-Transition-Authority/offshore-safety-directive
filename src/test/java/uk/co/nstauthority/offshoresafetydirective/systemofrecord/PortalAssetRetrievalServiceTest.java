package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@ExtendWith(MockitoExtension.class)
class PortalAssetRetrievalServiceTest {

  private static final RequestPurpose LICENCE_PURPOSE = new RequestPurpose("get licence purpose");

  @Mock
  private WellQueryService wellQueryService;

  @Mock
  private InstallationQueryService installationQueryService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Mock
  private LicenceQueryService licenceQueryService;

  @InjectMocks
  private PortalAssetRetrievalService portalAssetRetrievalService;

  @Test
  void getWellbore_whenNoMatch_thenEmptyOptional() {

    var wellboreId = new WellboreId(10);

    given(wellQueryService.getWell(wellboreId, PortalAssetRetrievalService.WELL_PURPOSE))
        .willReturn(Optional.empty());

    var resultingWellbore = portalAssetRetrievalService.getWellbore(wellboreId);

    assertThat(resultingWellbore).isEmpty();
  }

  @Test
  void getWellbore_whenMatchingWellbore_thenPopulatedOptional() {

    var wellboreId = new WellboreId(10);

    var expectedWellbore = WellDtoTestUtil.builder().build();

    given(wellQueryService.getWell(wellboreId, PortalAssetRetrievalService.WELL_PURPOSE))
        .willReturn(Optional.of(expectedWellbore));

    var resultingWellbore = portalAssetRetrievalService.getWellbore(wellboreId);

    assertThat(resultingWellbore).contains(expectedWellbore);
  }

  @Test
  void getInstallation_whenNoMatch_thenEmptyOptional() {

    var installationId = new InstallationId(10);

    given(installationQueryService.getInstallation(installationId, PortalAssetRetrievalService.INSTALLATION_PURPOSE))
        .willReturn(Optional.empty());

    var resultingInstallation = portalAssetRetrievalService.getInstallation(installationId);

    assertThat(resultingInstallation).isEmpty();
  }

  @Test
  void getInstallation_whenMatchingInstallation_thenPopulatedOptional() {

    var installationId = new InstallationId(10);

    var expectedInstallation = InstallationDtoTestUtil.builder().build();

    given(installationQueryService.getInstallation(installationId, PortalAssetRetrievalService.INSTALLATION_PURPOSE))
        .willReturn(Optional.of(expectedInstallation));

    var resultingInstallation = portalAssetRetrievalService.getInstallation(installationId);

    assertThat(resultingInstallation).contains(expectedInstallation);
  }

  @Test
  void getLicenceBlockSubarea_whenNoMatch_thenEmptyOptional() {

    var licenceBlockSubareaId = new LicenceBlockSubareaId("subarea-id");

    given(licenceBlockSubareaQueryService.getLicenceBlockSubarea(licenceBlockSubareaId, PortalAssetRetrievalService.SUBAREA_PURPOSE))
        .willReturn(Optional.empty());

    var resultingSubarea = portalAssetRetrievalService.getLicenceBlockSubarea(licenceBlockSubareaId);

    assertThat(resultingSubarea).isEmpty();
  }

  @Test
  void getLicenceBlockSubarea_whenMatchingSubarea_thenPopulatedOptional() {

    var licenceBlockSubareaId = new LicenceBlockSubareaId("subarea-id");

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    given(licenceBlockSubareaQueryService.getLicenceBlockSubarea(licenceBlockSubareaId, PortalAssetRetrievalService.SUBAREA_PURPOSE))
        .willReturn(Optional.of(expectedSubarea));

    var resultingSubarea = portalAssetRetrievalService.getLicenceBlockSubarea(licenceBlockSubareaId);

    assertThat(resultingSubarea).contains(expectedSubarea);
  }

  @Test
  void getLicence_whenMatchingLicence_thenPopulatedOptional() {

    var licenceId = new LicenceId(123);

    var expectedLicence = LicenceDtoTestUtil.builder().build();

    given(licenceQueryService.getLicenceById(licenceId, LICENCE_PURPOSE))
        .willReturn(Optional.of(expectedLicence));

    var resultingLicence = portalAssetRetrievalService.getLicence(licenceId, LICENCE_PURPOSE);

    assertThat(resultingLicence).contains(expectedLicence);
  }

  @Test
  void getLicence_whenNoMatchingLicence_thenEmptyOptional() {

    var licenceId = new LicenceId(123);

    given(licenceQueryService.getLicenceById(licenceId, LICENCE_PURPOSE))
        .willReturn(Optional.empty());

    var resultingLicence = portalAssetRetrievalService.getLicence(licenceId, LICENCE_PURPOSE);

    assertThat(resultingLicence).isEmpty();
  }

  @Test
  void getAssetName_whenInstallation_thenInstallationNameReturned() {
    var portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(String.valueOf(portalAssetIdAsInt));
    var portalAssetType = PortalAssetType.INSTALLATION;

    var expectedName = "asset name";
    var installationDto = InstallationDtoTestUtil.builder()
        .withName(expectedName)
        .build();
    when(installationQueryService.getInstallation(new InstallationId(portalAssetIdAsInt), PortalAssetRetrievalService.INSTALLATION_PURPOSE))
        .thenReturn(Optional.of(installationDto));

    var result = portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType);
    assertThat(result).contains(expectedName);
  }

  @Test
  void getAssetName_whenWellbore_thenWellboreNameReturned() {
    var portalAssetIdAsInt = 123;
    var portalAssetId = new PortalAssetId(String.valueOf(portalAssetIdAsInt));
    var portalAssetType = PortalAssetType.WELLBORE;

    var registrationNumber = "asset name";
    var wellboreDto = WellDtoTestUtil.builder()
        .withRegistrationNumber(registrationNumber)
        .build();
    when(wellQueryService.getWell(new WellboreId(portalAssetIdAsInt), PortalAssetRetrievalService.WELL_PURPOSE))
        .thenReturn(Optional.of(wellboreDto));

    var result = portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType);
    assertThat(result).contains(registrationNumber);
  }

  @Test
  void getAssetName_whenSubarea_thenSubareaNameReturned() {
    var portalAssetId = new PortalAssetId("123");
    var portalAssetType = PortalAssetType.SUBAREA;

    var expectedName = "asset name";
    var subareaDto = mock(LicenceBlockSubareaDto.class);
    when(subareaDto.displayName()).thenReturn(expectedName);

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(
        new LicenceBlockSubareaId(portalAssetId.id()),
        PortalAssetRetrievalService.SUBAREA_PURPOSE
    ))
        .thenReturn(Optional.of(subareaDto));

    var result = portalAssetRetrievalService.getAssetName(portalAssetId, portalAssetType);
    assertThat(result).contains(expectedName);
  }

  @Test
  void isExtantInPortal_whenWellboreIsExtantInPortal_thenTrue() {
    var wellboreId = new WellboreId(123);
    var portalAssetId = new PortalAssetId(String.valueOf(wellboreId.id()));

    when(wellQueryService.getWell(wellboreId, PortalAssetRetrievalService.WELL_PURPOSE))
        .thenReturn(Optional.of(WellDtoTestUtil.builder().build()));

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.WELLBORE);
    assertTrue(resultingIsExtantInPortal);
  }

  @Test
  void isExtantInPortal_whenWellboreIsNotExtantInPortal_thenFalse() {
    var wellboreId = new WellboreId(123);
    var portalAssetId = new PortalAssetId(String.valueOf(wellboreId.id()));

    when(wellQueryService.getWell(wellboreId, PortalAssetRetrievalService.WELL_PURPOSE))
        .thenReturn(Optional.empty());

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.WELLBORE);
    assertFalse(resultingIsExtantInPortal);
  }

  @Test
  void isExtantInPortal_whenInstallationIsExtantInPortal_thenTrue() {
    var installationId = new InstallationId(123);
    var portalAssetId = new PortalAssetId(String.valueOf(installationId.id()));

    when(installationQueryService.getInstallation(installationId, PortalAssetRetrievalService.INSTALLATION_PURPOSE))
        .thenReturn(Optional.of(InstallationDtoTestUtil.builder().build()));

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.INSTALLATION);
    assertTrue(resultingIsExtantInPortal);
  }

  @Test
  void isExtantInPortal_whenInstallationIsNotExtantInPortal_thenFalse() {
    var installationId = new InstallationId(123);
    var portalAssetId = new PortalAssetId(String.valueOf(installationId.id()));

    when( installationQueryService.getInstallation(installationId, PortalAssetRetrievalService.INSTALLATION_PURPOSE))
        .thenReturn(Optional.empty());

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.INSTALLATION);
    assertFalse(resultingIsExtantInPortal);
  }

  @Test
  void isExtantInPortal_whenLicenceBlockSubareaIsExtantInPortal_thenTrue() {
    var subareaId = new LicenceBlockSubareaId("123");
    var portalAssetId = new PortalAssetId(subareaId.id());

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(subareaId, PortalAssetRetrievalService.SUBAREA_PURPOSE))
        .thenReturn(Optional.of(LicenceBlockSubareaDtoTestUtil.builder().build()));

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.SUBAREA);
    assertTrue(resultingIsExtantInPortal);
  }

  @Test
  void isExtantInPortal_whenLicenceBlockSubareaDoesNotExist_thenFalse() {
    var subareaId = new LicenceBlockSubareaId("123");
    var portalAssetId = new PortalAssetId(subareaId.id());

    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(subareaId, PortalAssetRetrievalService.SUBAREA_PURPOSE))
        .thenReturn(Optional.empty());

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.SUBAREA);
    assertFalse(resultingIsExtantInPortal);
  }

  @Test
  void isExtantInPortal_whenLicenceBlockSubareaIsNotExtantInPortal_thenFalse() {
    var subareaId = new LicenceBlockSubareaId("123");
    var portalAssetId = new PortalAssetId(subareaId.id());

    var nonExtantSubarea = LicenceBlockSubareaDtoTestUtil.builder().isExtant(false).build();
    when(licenceBlockSubareaQueryService.getLicenceBlockSubarea(subareaId, PortalAssetRetrievalService.SUBAREA_PURPOSE))
        .thenReturn(Optional.of(nonExtantSubarea));

    var resultingIsExtantInPortal = portalAssetRetrievalService.isExtantInPortal(portalAssetId, PortalAssetType.SUBAREA);
    assertFalse(resultingIsExtantInPortal);
  }
}