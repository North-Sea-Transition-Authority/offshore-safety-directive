package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class PortalAssetNameService {

  private final PortalAssetRetrievalService portalAssetRetrievalService;

  @Autowired
  PortalAssetNameService(PortalAssetRetrievalService portalAssetRetrievalService) {
    this.portalAssetRetrievalService = portalAssetRetrievalService;
  }

  Optional<AssetName> getAssetName(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> portalAssetRetrievalService
          .getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id())))
          .map(installationDto -> new AssetName(installationDto.name()));
      case WELLBORE -> portalAssetRetrievalService
          .getWellbore(new WellboreId(Integer.parseInt(portalAssetId.id())))
          .map(wellDto -> new AssetName(wellDto.name()));
      case SUBAREA -> portalAssetRetrievalService
          .getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id()))
          .map(subareaDto -> new AssetName(subareaDto.displayName()));
    };
  }
}
