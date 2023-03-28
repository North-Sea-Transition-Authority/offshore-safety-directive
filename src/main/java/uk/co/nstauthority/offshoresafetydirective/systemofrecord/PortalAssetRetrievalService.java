package uk.co.nstauthority.offshoresafetydirective.systemofrecord;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;

@Service
public class PortalAssetRetrievalService {

  private final WellQueryService wellQueryService;

  private final InstallationQueryService installationQueryService;

  private final LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Autowired
  public PortalAssetRetrievalService(WellQueryService wellQueryService,
                                     InstallationQueryService installationQueryService,
                                     LicenceBlockSubareaQueryService licenceBlockSubareaQueryService) {
    this.wellQueryService = wellQueryService;
    this.installationQueryService = installationQueryService;
    this.licenceBlockSubareaQueryService = licenceBlockSubareaQueryService;
  }

  public Optional<WellDto> getWellbore(WellboreId wellboreId) {
    return wellQueryService.getWell(wellboreId);
  }

  public Optional<InstallationDto> getInstallation(InstallationId installationId) {
    return installationQueryService.getInstallation(installationId);
  }

  public Optional<LicenceBlockSubareaDto> getLicenceBlockSubarea(LicenceBlockSubareaId licenceBlockSubareaId) {
    return licenceBlockSubareaQueryService.getLicenceBlockSubarea(licenceBlockSubareaId);
  }
}
