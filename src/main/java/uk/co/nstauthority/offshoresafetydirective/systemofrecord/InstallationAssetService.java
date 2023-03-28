package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.enumutil.EnumUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationPhase;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.NominatedInstallationDetailViewService;

@Service
class InstallationAssetService {

  private final NominatedInstallationDetailViewService nominatedInstallationDetailViewService;

  @Autowired
  InstallationAssetService(NominatedInstallationDetailViewService nominatedInstallationDetailViewService) {
    this.nominatedInstallationDetailViewService = nominatedInstallationDetailViewService;
  }

  public List<NominatedAssetDto> getInstallationAssetDtos(NominationDetail nominationDetail) {
    var optionalInstallationDetailView =
        nominatedInstallationDetailViewService.getNominatedInstallationDetailView(nominationDetail);

    if (optionalInstallationDetailView.isEmpty()) {
      return List.of();
    }

    var installationDetailView = optionalInstallationDetailView.get();
    var phases = getInstallationPhases(installationDetailView);
    return installationDetailView.getInstallations()
        .stream()
        .map(dto -> new PortalAssetId(String.valueOf(dto.id())))
        .map(portalAssetId -> new NominatedAssetDto(portalAssetId, PortalAssetType.INSTALLATION, phases))
        .toList();

  }

  private List<String> getInstallationPhases(NominatedInstallationDetailView installationDetailView) {
    List<InstallationPhase> installationPhases;
    if (BooleanUtils.isTrue(installationDetailView.getForAllInstallationPhases())) {
      installationPhases = Arrays.stream(InstallationPhase.values()).toList();
    } else {
      installationPhases = installationDetailView.getInstallationPhases();
    }
    return EnumUtil.getEnumNames(installationPhases);
  }

}
