package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.authorisation.Unauthenticated;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetAccessService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetRetrievalService;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Controller
@RequestMapping("/system-of-record")
@Unauthenticated
public class AssetTimelineController {

  private final AssetTimelineService assetTimelineService;
  private final UserDetailService userDetailService;
  private final AssetAccessService assetAccessService;
  private final PortalAssetRetrievalService portalAssetRetrievalService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public AssetTimelineController(AssetTimelineService assetTimelineService,
                                 UserDetailService userDetailService,
                                 AssetAccessService assetAccessService,
                                 PortalAssetRetrievalService portalAssetRetrievalService,
                                 TeamQueryService teamQueryService) {
    this.assetTimelineService = assetTimelineService;
    this.userDetailService = userDetailService;
    this.assetAccessService = assetAccessService;
    this.portalAssetRetrievalService = portalAssetRetrievalService;
    this.teamQueryService = teamQueryService;
  }

  public static String determineRouteByPortalAssetType(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    return ReverseRouter.route(getRouteMethodCallByPortalAssetType(portalAssetId, portalAssetType));
  }

  public static ModelAndView determineRedirectByPortalAssetType(PortalAssetId portalAssetId,
                                                          PortalAssetType portalAssetType) {
    return ReverseRouter.redirect(getRouteMethodCallByPortalAssetType(portalAssetId, portalAssetType));
  }

  @GetMapping("/installation/{portalAssetId}")
  public ModelAndView renderInstallationTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetTimelineModelIfInPortal(portalAssetId, PortalAssetType.INSTALLATION);
  }

  @GetMapping("/wellbore/{portalAssetId}")
  public ModelAndView renderWellboreTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetTimelineModelIfInPortal(portalAssetId, PortalAssetType.WELLBORE);
  }

  @GetMapping("/forward-approval/{portalAssetId}")
  public ModelAndView renderSubareaTimeline(@PathVariable PortalAssetId portalAssetId) {
    return getAssetTimelineModelIfInPortal(portalAssetId, PortalAssetType.SUBAREA);
  }

  private static Object getRouteMethodCallByPortalAssetType(PortalAssetId portalAssetId,
                                                            PortalAssetType portalAssetType) {
    return switch (portalAssetType) {
      case INSTALLATION -> on(AssetTimelineController.class).renderInstallationTimeline(portalAssetId);
      case WELLBORE -> on(AssetTimelineController.class).renderWellboreTimeline(portalAssetId);
      case SUBAREA -> on(AssetTimelineController.class).renderSubareaTimeline(portalAssetId);
    };
  }

  private ModelAndView getAssetTimelineModelIfInPortal(PortalAssetId portalAssetId, PortalAssetType portalAssetType) {
    if (portalAssetRetrievalService.isExtantInPortal(portalAssetId, portalAssetType)
        || assetAccessService.isAssetExtant(portalAssetId, portalAssetType)) {
      return getAssetTimelineModel(portalAssetId, portalAssetType);
    }

    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "No extant asset found for PortalAssetId [%s] and PortalAssetType [%s]"
            .formatted(portalAssetId, portalAssetType));
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

    if (userDetailService.isUserLoggedIn() && userIsAppointmentManager()) {
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

  private boolean userIsAppointmentManager() {
    return teamQueryService.userHasStaticRole(
        userDetailService.getUserDetail().wuaId(),
        TeamType.REGULATOR,
        Role.APPOINTMENT_MANAGER
    );
  }
}
