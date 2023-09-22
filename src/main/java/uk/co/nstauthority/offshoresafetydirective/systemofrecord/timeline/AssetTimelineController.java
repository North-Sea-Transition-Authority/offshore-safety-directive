package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.PermissionService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class AssetTimelineController {

  private final AssetTimelineService assetTimelineService;
  private final PermissionService permissionService;
  private final UserDetailService userDetailService;

  @Autowired
  public AssetTimelineController(AssetTimelineService assetTimelineService, PermissionService permissionService,
                                 UserDetailService userDetailService) {
    this.assetTimelineService = assetTimelineService;
    this.permissionService = permissionService;
    this.userDetailService = userDetailService;
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

    var modelAndView = new ModelAndView("osd/systemofrecord/timeline/appointmentTimeline")
        .addObject("assetName", assetAppointmentHistory.assetName().value())
        .addObject("assetTypeDisplayName", portalAssetType.getDisplayName())
        .addObject("assetTypeDisplayNameSentenceCase", portalAssetType.getSentenceCaseDisplayName())
        .addObject("timelineItemViews", assetAppointmentHistory.timelineItemViews());

    if (userDetailService.isUserLoggedIn()
        && permissionService.hasPermission(
        userDetailService.getUserDetail(),
        Set.of(RolePermission.MANAGE_APPOINTMENTS)
    )) {
      modelAndView.addObject("newAppointmentUrl", getNewAppointmentUrl(portalAssetId, portalAssetType));
    }

    return modelAndView;
  }

  private String getNewAppointmentUrl(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> ReverseRouter.route(on(NewAppointmentController.class)
          .renderNewInstallationAppointment(portalAssetId));
      case WELLBORE -> ReverseRouter.route(on(NewAppointmentController.class)
          .renderNewWellboreAppointment(portalAssetId));
      case SUBAREA -> ReverseRouter.route(on(NewAppointmentController.class)
          .renderNewSubareaAppointment(portalAssetId));
    };
  }
}
