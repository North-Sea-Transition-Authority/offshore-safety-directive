package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetName;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
class PortalAssetNameService {

  private final InstallationQueryService installationQueryService;

  private final WellQueryService wellQueryService;

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  PortalAssetNameService(InstallationQueryService installationQueryService,
                         WellQueryService wellQueryService,
                         LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.installationQueryService = installationQueryService;
    this.wellQueryService = wellQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  Optional<AssetName> getAssetName(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> installationQueryService
          .getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id())))
          .map(installationDto -> new AssetName(installationDto.name()));
      case WELLBORE -> wellQueryService
          .getWell(new WellboreId(Integer.parseInt(portalAssetId.id())))
          .map(wellDto -> new AssetName(wellDto.name()));
      case SUBAREA -> licenceBlockSubareaQueryService
          .getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id()))
          .map(subareaDto -> new AssetName(subareaDto.displayName()));
    };
  }
}
