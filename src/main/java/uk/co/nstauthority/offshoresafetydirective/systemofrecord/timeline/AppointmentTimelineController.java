package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class AppointmentTimelineController {

  private final AppointmentTimelineService appointmentTimelineService;

  @Autowired
  public AppointmentTimelineController(AppointmentTimelineService appointmentTimelineService) {
    this.appointmentTimelineService = appointmentTimelineService;
  }

  @GetMapping("/installation/{portalAssetId}")
  public ModelAndView renderInstallationAppointmentTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetAppointmentTimelineModel(portalAssetId, PortalAssetType.INSTALLATION);
  }

  @GetMapping("/wellbore/{portalAssetId}")
  public ModelAndView renderWellboreAppointmentTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetAppointmentTimelineModel(portalAssetId, PortalAssetType.WELLBORE);
  }

  @GetMapping("/forward-approval/{portalAssetId}")
  public ModelAndView renderSubareaAppointmentTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetAppointmentTimelineModel(portalAssetId, PortalAssetType.SUBAREA);
  }

  private ModelAndView getAssetAppointmentTimelineModel(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {

    var assetAppointmentHistory = appointmentTimelineService
        .getAppointmentHistoryForPortalAsset(portalAssetId, portalAssetType)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No portal asset with ID %s and type %s found".formatted(portalAssetId.id(), portalAssetType)
        ));

    return new ModelAndView("osd/systemofrecord/timeline/appointmentTimeline")
        .addObject("assetName", assetAppointmentHistory.assetName().value())
        .addObject("assetTypeDisplayName", portalAssetType.getDisplayName());
  }
}
