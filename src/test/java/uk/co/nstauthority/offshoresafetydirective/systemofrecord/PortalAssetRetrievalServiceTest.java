package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@ExtendWith(MockitoExtension.class)
class PortalAssetRetrievalServiceTest {

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

    given(wellQueryService.getWell(wellboreId))
        .willReturn(Optional.empty());

    var resultingWellbore = portalAssetRetrievalService.getWellbore(wellboreId);

    assertThat(resultingWellbore).isEmpty();
  }

  @Test
  void getWellbore_whenMatchingWellbore_thenPopulatedOptional() {

    var wellboreId = new WellboreId(10);

    var expectedWellbore = WellDtoTestUtil.builder().build();

    given(wellQueryService.getWell(wellboreId))
        .willReturn(Optional.of(expectedWellbore));

    var resultingWellbore = portalAssetRetrievalService.getWellbore(wellboreId);

    assertThat(resultingWellbore).contains(expectedWellbore);
  }

  @Test
  void getInstallation_whenNoMatch_thenEmptyOptional() {

    var installationId = new InstallationId(10);

    given(installationQueryService.getInstallation(installationId))
        .willReturn(Optional.empty());

    var resultingInstallation = portalAssetRetrievalService.getInstallation(installationId);

    assertThat(resultingInstallation).isEmpty();
  }

  @Test
  void getInstallation_whenMatchingInstallation_thenPopulatedOptional() {

    var installationId = new InstallationId(10);

    var expectedInstallation = InstallationDtoTestUtil.builder().build();

    given(installationQueryService.getInstallation(installationId))
        .willReturn(Optional.of(expectedInstallation));

    var resultingInstallation = portalAssetRetrievalService.getInstallation(installationId);

    assertThat(resultingInstallation).contains(expectedInstallation);
  }

  @Test
  void getLicenceBlockSubarea_whenNoMatch_thenEmptyOptional() {

    var licenceBlockSubareaId = new LicenceBlockSubareaId("subarea-id");

    given(licenceBlockSubareaQueryService.getLicenceBlockSubarea(licenceBlockSubareaId))
        .willReturn(Optional.empty());

    var resultingSubarea = portalAssetRetrievalService.getLicenceBlockSubarea(licenceBlockSubareaId);

    assertThat(resultingSubarea).isEmpty();
  }

  @Test
  void getLicenceBlockSubarea_whenMatchingSubarea_thenPopulatedOptional() {

    var licenceBlockSubareaId = new LicenceBlockSubareaId("subarea-id");

    var expectedSubarea = LicenceBlockSubareaDtoTestUtil.builder().build();

    given(licenceBlockSubareaQueryService.getLicenceBlockSubarea(licenceBlockSubareaId))
        .willReturn(Optional.of(expectedSubarea));

    var resultingSubarea = portalAssetRetrievalService.getLicenceBlockSubarea(licenceBlockSubareaId);

    assertThat(resultingSubarea).contains(expectedSubarea);
  }

  @Test
  void getLicence_whenMatchingLicence_thenPopulatedOptional() {

    var licenceId = new LicenceId(123);

    var expectedLicence = LicenceDtoTestUtil.builder().build();

    given(licenceQueryService.getLicenceById(licenceId))
        .willReturn(Optional.of(expectedLicence));

    var resultingLicence = portalAssetRetrievalService.getLicence(licenceId);

    assertThat(resultingLicence).contains(expectedLicence);
  }

  @Test
  void getLicence_whenNoMatchingLicence_thenEmptyOptional() {

    var licenceId = new LicenceId(123);

    given(licenceQueryService.getLicenceById(licenceId))
        .willReturn(Optional.empty());

    var resultingLicence = portalAssetRetrievalService.getLicence(licenceId);

    assertThat(resultingLicence).isEmpty();
  }
}