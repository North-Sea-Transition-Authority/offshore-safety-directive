package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewService;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewTestUtil;

@ExtendWith(MockitoExtension.class)
class InstallationAssetServiceTest {

  @Mock
  NominatedInstallationDetailViewService nominatedInstallationDetailViewService;

  @InjectMocks
  private InstallationAssetService installationAssetService;

  @Test
  void getInstallationAssetDtos_whenNoDetailView_assertEmpty() {
    var nominationDetail = NominationDetailTestUtil.builder().build();

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.empty());

    var result = installationAssetService.getInstallationAssetDtos(nominationDetail);

    assertThat(result).isEmpty();
  }

  @Test
  void getInstallationAssetDtos_whenDetailView_assertValues() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var portalAssetId = 123;
    var installationDto = InstallationDtoTestUtil.builder()
        .withId(portalAssetId)
        .withName("asset name")
        .build();
    var detailView = NominatedInstallationDetailViewTestUtil.builder()
        .withInstallations(List.of(installationDto))
        .build();

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.of(detailView));

    var assetDtos = installationAssetService.getInstallationAssetDtos(nominationDetail);

    assertThat(assetDtos)
        .hasSize(1)
        .first()
        .extracting(
            nominatedAssetDto -> nominatedAssetDto.portalAssetId().id(),
            NominatedAssetDto::portalAssetType,
            nominatedAssetDto -> nominatedAssetDto.portalAssetName().value()
        )
        .containsExactly(
            String.valueOf(portalAssetId),
            PortalAssetType.INSTALLATION,
            "asset name"
        );
  }

  @Test
  void getInstallationAssetDtos_whenForAllInstallationPhases_assertInstallationPhases() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var portalAssetId = 123;
    var installationDto = InstallationDtoTestUtil.builder()
        .withId(portalAssetId)
        .build();
    var detailView = NominatedInstallationDetailViewTestUtil.builder()
        .withInstallations(List.of(installationDto))
        .withForAllInstallationPhases(true)
        .build();

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.of(detailView));

    var assetDto = installationAssetService.getInstallationAssetDtos(nominationDetail);
    var allInstallationPhases = Arrays.stream(InstallationPhase.values())
        .map(Enum::name)
        .toArray();
    assertThat(assetDto)
        .hasSize(1)
        .first()
        .extracting(NominatedAssetDto::phases)
        .asList()
        .containsExactly(allInstallationPhases);
  }

  @Test
  void getInstallationAssetDtos_whenSpecificInstallationPhases_assertInstallationPhases() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    var portalAssetId = 123;
    var installationDto = InstallationDtoTestUtil.builder()
        .withId(portalAssetId)
        .build();
    var detailView = NominatedInstallationDetailViewTestUtil.builder()
        .withInstallations(List.of(installationDto))
        .withForAllInstallationPhases(false)
        .withInstallationPhases(List.of(
            InstallationPhase.DEVELOPMENT_INSTALLATION,
            InstallationPhase.DECOMMISSIONING
        ))
        .build();

    when(nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail))
        .thenReturn(Optional.of(detailView));

    var assetDto = installationAssetService.getInstallationAssetDtos(nominationDetail);
    assertThat(assetDto)
        .hasSize(1)
        .first()
        .extracting(NominatedAssetDto::phases)
        .asList()
        .containsExactly(
            InstallationPhase.DEVELOPMENT_INSTALLATION.name(),
            InstallationPhase.DECOMMISSIONING.name()
        );
  }
}