package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellboreId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.Appointment;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Service
public class PortalAssetNameService {

  private final PortalAssetRetrievalService portalAssetRetrievalService;

  @Autowired
  PortalAssetNameService(PortalAssetRetrievalService portalAssetRetrievalService) {
    this.portalAssetRetrievalService = portalAssetRetrievalService;
  }

  public Optional<String> getAssetName(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> portalAssetRetrievalService
          .getInstallation(new InstallationId(Integer.parseInt(portalAssetId.id())))
          .map(InstallationDto::name);
      case WELLBORE -> portalAssetRetrievalService
          .getWellbore(new WellboreId(Integer.parseInt(portalAssetId.id())))
          .map(WellDto::name);
      case SUBAREA -> portalAssetRetrievalService
          .getLicenceBlockSubarea(new LicenceBlockSubareaId(portalAssetId.id()))
          .map(LicenceBlockSubareaDto::displayName);
    };
  }

  public Optional<String> getAssetName(Appointment appointment) {
    return getAssetName(
        new PortalAssetId(appointment.getAsset().getPortalAssetId()),
        appointment.getAsset().getPortalAssetType()
    );
  }
}
