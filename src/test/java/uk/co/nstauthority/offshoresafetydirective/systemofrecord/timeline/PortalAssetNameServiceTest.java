package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@ExtendWith(MockitoExtension.class)
class PortalAssetNameServiceTest {

  @Mock
  private InstallationQueryService installationQueryService;

  @Mock
  private WellQueryService wellQueryService;

  @Mock
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @InjectMocks
  private PortalAssetNameService portalAssetNameService;

  @Test
  void getAssetName_whenInstallationTypeAndNotInPortal_thenEmptyOptional() {

    var portalAssetId = new PortalAssetId("123");

    given(installationQueryService.getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id()))))
        .willReturn(Optional.empty());

    var resultingAssetName = portalAssetNameService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION);

    assertThat(resultingAssetName).isEmpty();

    then(wellQueryService).shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService).shouldHaveNoInteractions();
  }

  @Test
  void getAssetName_whenInstallationTypeAndFoundInPortal_thenPopulatedOptional() {

    var portalAssetId = new PortalAssetId("123");

    var expectedInstallation = InstallationDtoTestUtil.builder()
        .withName("installation name")
        .build();

    given(installationQueryService.getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id()))))
        .willReturn(Optional.of(expectedInstallation));

    var resultingAssetName = portalAssetNameService.getAssetName(portalAssetId, PortalAssetType.INSTALLATION);

    assertThat(resultingAssetName).contains(new AssetName("installation name"));

    then(wellQueryService).shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService).shouldHaveNoInteractions();
  }

  @Test
  void getAssetName_whenWellboreTypeAndNotInPortal_thenEmptyOptional() {

    var portalAssetId = new PortalAssetId("123");

    given(wellQueryService.getWell(new WellboreId(Integer.parseInt(portalAssetId.id()))))
        .willReturn(Optional.empty());

    var resultingAssetName = portalAssetNameService.getAssetName(portalAssetId, PortalAssetType.WELLBORE);

    assertThat(resultingAssetName).isEmpty();

    then(installationQueryService).shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService).shouldHaveNoInteractions();
  }

  @Test
  void getAssetName_whenWellboreTypeAndFoundInPortal_thenPopulatedOptional() {

    var portalAssetId = new PortalAssetId("123");

    var expectedWellbore = WellDtoTestUtil.builder()
        .withRegistrationNumber("well registration number")
        .build();

    given(wellQueryService.getWell(new WellboreId(Integer.parseInt(portalAssetId.id()))))
        .willReturn(Optional.of(expectedWellbore));

    var resultingAssetName = portalAssetNameService.getAssetName(portalAssetId, PortalAssetType.WELLBORE);

    assertThat(resultingAssetName).contains(new AssetName("well registration number"));

    then(installationQueryService).shouldHaveNoInteractions();

    then(licenceBlockSubareaQueryService).shouldHaveNoInteractions();

  }

  @Test
  void getAssetName_whenSubareaTypeAndNotInPortal_thenEmptyOptional() {

    var portalAssetId = new PortalAssetId("123");

    given(licenceBlockSubareaQueryService.getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id())))
        .willReturn(Optional.empty());

    var resultingAssetName = portalAssetNameService.getAssetName(portalAssetId, PortalAssetType.SUBAREA);

    assertThat(resultingAssetName).isEmpty();

    then(installationQueryService).shouldHaveNoInteractions();

    then(wellQueryService).shouldHaveNoInteractions();
  }

  @Test
  void getAssetName_whenSubareaTypeAndFoundInPortal_thenPopulatedOptional() {

    var portalAssetId = new PortalAssetId("123");

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceReference("licence reference")
        .withBlockReference("block reference")
        .withSubareaName("subarea name")
        .build();

    given(licenceBlockSubareaQueryService.getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id())))
        .willReturn(Optional.of(expectedSubarea));

    var resultingAssetName = portalAssetNameService.getAssetName(portalAssetId, PortalAssetType.SUBAREA);

    assertThat(resultingAssetName).contains(new AssetName("licence reference block reference subarea name"));

    then(installationQueryService).shouldHaveNoInteractions();

    then(wellQueryService).shouldHaveNoInteractions();
  }

}