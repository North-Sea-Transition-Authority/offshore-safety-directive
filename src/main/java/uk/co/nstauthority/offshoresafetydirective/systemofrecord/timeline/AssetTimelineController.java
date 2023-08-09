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
public class AssetTimelineController {

  private final AssetTimelineService assetTimelineService;

  @Autowired
  public AssetTimelineController(AssetTimelineService assetTimelineService) {
    this.assetTimelineService = assetTimelineService;
  }

  @GetMapping("/installation/{portalAssetId}")
  public ModelAndView renderInstallationTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetTimelineModel(portalAssetId, PortalAssetType.INSTALLATION);
  }

  @GetMapping("/wellbore/{portalAssetId}")
  public ModelAndView renderWellboreTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetTimelineModel(portalAssetId, PortalAssetType.WELLBORE);
  }

  @GetMapping("/forward-approval/{portalAssetId}")
  public ModelAndView renderSubareaTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetTimelineModel(portalAssetId, PortalAssetType.SUBAREA);
  }

  private ModelAndView getAssetTimelineModel(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {

    var assetAppointmentHistory = assetTimelineService
        .getAppointmentHistoryForPortalAsset(portalAssetId, portalAssetType)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No portal asset with ID %s and type %s found".formatted(portalAssetId.id(), portalAssetType)
        ));

    return new ModelAndView("osd/systemofrecord/timeline/appointmentTimeline")
        .addObject("assetName", assetAppointmentHistory.assetName().value())
        .addObject("assetTypeDisplayName", portalAssetType.getDisplayName())
        .addObject("assetTypeDisplayNameSentenceCase", portalAssetType.getSentenceCaseDisplayName())
        .addObject("timelineItemViews", assetAppointmentHistory.timelineItemViews());
  }
}
