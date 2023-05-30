package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.PortalAssetNameService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/appointment/{appointmentId}")
@HasPermission(permissions = RolePermission.MANAGE_APPOINTMENTS)
public class AppointmentCorrectionController {

  private final AppointmentAccessService appointmentAccessService;
  private final AssetAccessService assetAccessService;
  private final PortalAssetNameService portalAssetNameService;

  @Autowired
  public AppointmentCorrectionController(AppointmentAccessService appointmentAccessService,
                                         AssetAccessService assetAccessService,
                                         PortalAssetNameService portalAssetNameService) {
    this.appointmentAccessService = appointmentAccessService;
    this.assetAccessService = assetAccessService;
    this.portalAssetNameService = portalAssetNameService;
  }

  @GetMapping("/correct")
  public ModelAndView renderCorrection(@PathVariable AppointmentId appointmentId) {
    var appointmentDto = appointmentAccessService.findAppointmentDtoById(appointmentId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Appointment with ID [%s] could not be found".formatted(
                appointmentId.id()
            )
        ));

    var assetDto = assetAccessService.getAsset(appointmentDto.portalAssetId().toPortalAssetId())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Asset with ID [%s] could not be found".formatted(
                appointmentDto.portalAssetId().toPortalAssetId().id()
            )
        ));

    var assetName = portalAssetNameService.getAssetName(assetDto.portalAssetId(), assetDto.portalAssetType())
        .orElse(assetDto.assetName());

    return new ModelAndView("osd/systemofrecord/correction/correctAppointment")
        .addObject("assetName", assetName.value())
        .addObject("assetTypeDisplayName", assetDto.portalAssetType().getDisplayName());
  }


}
