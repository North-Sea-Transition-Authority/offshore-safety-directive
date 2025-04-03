package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = AssetTimelineController.class)
class AssetTimelineControllerTest extends AbstractControllerTest {

  private static final PortalAssetId PORTAL_ASSET_ID = new PortalAssetId("portal-asset-id");
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @MockitoBean
  private AssetTimelineService assetTimelineService;

  @SecurityTest
  void renderInstallationTimeline_verifyUnauthenticated() throws Exception {
    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.of(AssetAppointmentHistoryTestUtil.builder().build()));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderWellboreTimeline_verifyUnauthenticated() throws Exception {

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.of(AssetAppointmentHistoryTestUtil.builder().build()));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderWellboreTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderSubareaTimeline_verifyUnauthenticated() throws Exception {

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.of(AssetAppointmentHistoryTestUtil.builder().build()));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderSubareaTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isOk());
  }

  @Test
  void renderInstallationTimeline_verifyModelProperties() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/appointmentTimeline"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.INSTALLATION.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeDisplayNameSentenceCase",
            PortalAssetType.INSTALLATION.getSentenceCaseDisplayName())
        )
        .andExpect(model().attribute("timelineItemViews", List.of(appointmentTimelineItemView)))
        .andExpect(model().attributeDoesNotExist("newAppointmentUrl"));
  }

  @Test
  void renderInstallationTimeline_whenNotExtantInPortalOrDatabase_verifyNotFound() throws Exception {

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(false);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(false);


    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderInstallationTimeline_verifyModelPropertiesWhenHasPermission() throws Exception {

    when(userDetailService.isUserLoggedIn()).thenReturn(true);

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.INSTALLATION))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "newAppointmentUrl",
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewInstallationAppointment(PORTAL_ASSET_ID))
        ));
  }

  @Test
  void renderWellboreTimeline_verifyModelProperties() throws Exception {

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderWellboreTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/appointmentTimeline"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.WELLBORE.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeDisplayNameSentenceCase",
            PortalAssetType.WELLBORE.getSentenceCaseDisplayName())
        )
        .andExpect(model().attribute("timelineItemViews", List.of(appointmentTimelineItemView)))
        .andExpect(model().attributeDoesNotExist("newAppointmentUrl"));
  }

  @Test
  void renderWellboreTimeline_verifyModelPropertiesWhenHasPermission() throws Exception {

    when(userDetailService.isUserLoggedIn()).thenReturn(true);

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderWellboreTimeline(PORTAL_ASSET_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "newAppointmentUrl",
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewWellboreAppointment(PORTAL_ASSET_ID))
        ));
  }

  @Test
  void renderWellboreTimeline_whenNotExtantInPortalOrDatabase_verifyNotFound() throws Exception {

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(false);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.WELLBORE))
        .thenReturn(false);


    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderSubareaTimeline_verifyModelProperties() throws Exception {

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(false);

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderSubareaTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/appointmentTimeline"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.SUBAREA.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeDisplayNameSentenceCase",
            PortalAssetType.SUBAREA.getSentenceCaseDisplayName())
        )
        .andExpect(model().attribute("timelineItemViews", List.of(appointmentTimelineItemView)))
        .andExpect(model().attributeDoesNotExist("newAppointmentUrl"));
  }

  @Test
  void renderSubareaTimeline_verifyModelPropertiesWhenHasPermission() throws Exception {

    when(userDetailService.isUserLoggedIn()).thenReturn(true);

    when(teamQueryService.userHasStaticRole(USER.wuaId(), TeamType.REGULATOR, Role.APPOINTMENT_MANAGER))
        .thenReturn(true);

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(true);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(true);

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderSubareaTimeline(PORTAL_ASSET_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "newAppointmentUrl",
            ReverseRouter.route(on(NewAppointmentController.class)
                .renderNewSubareaAppointment(PORTAL_ASSET_ID))
        ));
  }

  @Test
  void renderSubareaTimeline_whenNotExtantInPortalOrDatabase_verifyNotFound() throws Exception {

    when(portalAssetRetrievalService.isExtantInPortal(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(false);

    when(assetAccessService.isAssetExtant(PORTAL_ASSET_ID, PortalAssetType.SUBAREA))
        .thenReturn(false);


    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderInstallationTimeline_whenNoAssetFound_thenNotFoundResponse() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderInstallationTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderWellboreTimeline_whenNoAssetFound_thenNotFoundResponse() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderWellboreTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderSubareaTimeline_whenNoAssetFound_thenNotFoundResponse() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(AssetTimelineController.class)
        .renderSubareaTimeline(PORTAL_ASSET_ID))))
        .andExpect(status().isNotFound());
  }

  @Test
  void determineRouteByPortalAssetType_whenInstallationType_thenVerifyRoute() {
    var portalAssetType = PortalAssetType.INSTALLATION;
    var result = AssetTimelineController.determineRouteByPortalAssetType(PORTAL_ASSET_ID, portalAssetType);
    assertThat(result)
        .isEqualTo(
            ReverseRouter.route(on(AssetTimelineController.class).renderInstallationTimeline(PORTAL_ASSET_ID))
        );
  }

  @Test
  void determineRouteByPortalAssetType_whenWellboreType_thenVerifyRoute() {
    var portalAssetType = PortalAssetType.WELLBORE;
    var result = AssetTimelineController.determineRouteByPortalAssetType(PORTAL_ASSET_ID, portalAssetType);
    assertThat(result)
        .isEqualTo(
            ReverseRouter.route(on(AssetTimelineController.class).renderWellboreTimeline(PORTAL_ASSET_ID))
        );
  }

  @Test
  void determineRouteByPortalAssetType_whenSubareaType_thenVerifyRoute() {
    var portalAssetType = PortalAssetType.SUBAREA;
    var result = AssetTimelineController.determineRouteByPortalAssetType(PORTAL_ASSET_ID, portalAssetType);
    assertThat(result)
        .isEqualTo(
            ReverseRouter.route(on(AssetTimelineController.class).renderSubareaTimeline(PORTAL_ASSET_ID))
        );
  }

  @Test
  void determineRedirectByPortalAssetType_whenInstallationType_thenVerifyRoute() {
    var portalAssetType = PortalAssetType.INSTALLATION;
    var result = AssetTimelineController.determineRedirectByPortalAssetType(PORTAL_ASSET_ID, portalAssetType);
    assertThat(result.getViewName())
        .isEqualTo(
            "redirect:" +
                ReverseRouter.route(on(AssetTimelineController.class).renderInstallationTimeline(PORTAL_ASSET_ID))
        );
  }

  @Test
  void determineRedirectByPortalAssetType_whenWellboreType_thenVerifyRoute() {
    var portalAssetType = PortalAssetType.WELLBORE;
    var result = AssetTimelineController.determineRedirectByPortalAssetType(PORTAL_ASSET_ID, portalAssetType);
    assertThat(result.getViewName())
        .isEqualTo(
            "redirect:" +
                ReverseRouter.route(on(AssetTimelineController.class).renderWellboreTimeline(PORTAL_ASSET_ID))
        );
  }

  @Test
  void determineRedirectByPortalAssetType_whenSubareaType_thenVerifyRoute() {
    var portalAssetType = PortalAssetType.SUBAREA;
    var result = AssetTimelineController.determineRedirectByPortalAssetType(PORTAL_ASSET_ID, portalAssetType);
    assertThat(result.getViewName())
        .isEqualTo(
            "redirect:" +
                ReverseRouter.route(on(AssetTimelineController.class).renderSubareaTimeline(PORTAL_ASSET_ID))
        );
  }
}