package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licence.LicenceQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@Service
public class PortalAssetRetrievalService {

  static final RequestPurpose INSTALLATION_PURPOSE = new RequestPurpose("Retrieve installation asset");

  private final WellQueryService wellQueryService;

  private final InstallationQueryService installationQueryService;

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  private final LicenceQueryService licenceQueryService;

  @Autowired
  public PortalAssetRetrievalService(WellQueryService wellQueryService,
                                     InstallationQueryService installationQueryService,
                                     LicenceBlockSubareaQueryService licenceBlockSubareaQueryService,
                                     LicenceQueryService licenceQueryService) {
    this.wellQueryService = wellQueryService;
    this.installationQueryService = installationQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
    this.licenceQueryService = licenceQueryService;
  }

  public Optional<WellDto> getWellbore(WellboreId wellboreId) {
    return wellQueryService.getWell(wellboreId);
  }

  public Optional<InstallationDto> getInstallation(InstallationId installationId) {
    return installationQueryService.getInstallation(installationId, INSTALLATION_PURPOSE);
  }

  public Optional<LicenceBlockSubareaDto> getLicenceBlockSubarea(LicenceBlockSubareaId licenceBlockSubareaId) {
    return licenceBlockSubareaQueryService.getLicenceBlockSubarea(licenceBlockSubareaId);
  }

  public Optional<LicenceDto> getLicence(LicenceId licenceId, RequestPurpose requestPurpose) {
    return licenceQueryService.getLicenceById(licenceId, requestPurpose);
  }

  public Optional<String> getAssetName(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id())))
          .map(InstallationDto::name);
      case WELLBORE -> getWellbore(new WellboreId(Integer.parseInt(portalAssetId.id())))
          .map(WellDto::name);
      case SUBAREA -> getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id()))
          .map(LicenceBlockSubareaDto::displayName);
    };
  }

  public boolean isExtantInPortal(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case WELLBORE -> getWellbore(new WellboreId(Integer.parseInt(portalAssetId.id()))).isPresent();
      case INSTALLATION -> getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id()))).isPresent();
      case SUBAREA ->
          getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id()))
          .map(LicenceBlockSubareaDto::isExtant)
          .orElse(false);
    };
  }
}
