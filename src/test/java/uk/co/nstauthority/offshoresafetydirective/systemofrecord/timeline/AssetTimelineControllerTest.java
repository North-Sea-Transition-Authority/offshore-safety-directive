package uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;

@ContextConfiguration(classes = AssetTimelineController.class)
class AssetTimelineControllerTest extends AbstractControllerTest {

  private static final PortalAssetId PORTAL_ASSET_ID = new PortalAssetId("portal-asset-id");

  @MockBean
  private AssetTimelineService assetTimelineService;

  @SecurityTest
  void renderInstallationTimeline_verifyUnauthenticated() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.of(AssetAppointmentHistoryTestUtil.builder().build()));

    mockMvc.perform(
        get(ReverseRouter.route(on(AssetTimelineController.class)
            .renderInstallationTimeline(PORTAL_ASSET_ID))
        ))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderWellboreTimeline_verifyUnauthenticated() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.of(AssetAppointmentHistoryTestUtil.builder().build()));

    mockMvc.perform(
        get(ReverseRouter.route(on(AssetTimelineController.class)
            .renderWellboreTimeline(PORTAL_ASSET_ID))
        ))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void renderSubareaTimeline_verifyUnauthenticated() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.of(AssetAppointmentHistoryTestUtil.builder().build()));

    mockMvc.perform(
        get(ReverseRouter.route(on(AssetTimelineController.class)
            .renderSubareaTimeline(PORTAL_ASSET_ID))
        ))
        .andExpect(status().isOk());
  }

  @Test
  void renderInstallationTimeline_verifyModelProperties() throws Exception {

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(
            get(ReverseRouter.route(on(AssetTimelineController.class)
                .renderInstallationTimeline(PORTAL_ASSET_ID))
            ))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/appointmentTimeline"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.INSTALLATION.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeDisplayNameSentenceCase",
            PortalAssetType.INSTALLATION.getSentenceCaseDisplayName())
        )
        .andExpect(model().attribute("timelineItemViews", List.of(appointmentTimelineItemView)));
  }

  @Test
  void renderWellboreTimeline_verifyModelProperties() throws Exception {

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(
            get(ReverseRouter.route(on(AssetTimelineController.class)
                .renderWellboreTimeline(PORTAL_ASSET_ID))
            ))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/appointmentTimeline"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.WELLBORE.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeDisplayNameSentenceCase",
            PortalAssetType.WELLBORE.getSentenceCaseDisplayName())
        )
        .andExpect(model().attribute("timelineItemViews", List.of(appointmentTimelineItemView)));
  }

  @Test
  void renderSubareaTimeline_verifyModelProperties() throws Exception {

    var assetName = "asset name";

    var appointmentTimelineItemView = AssetTimelineItemViewTestUtil.appointmentBuilder().build();

    var assetAppointmentHistory = AssetAppointmentHistoryTestUtil.builder()
        .withAssetName(assetName)
        .withTimelineItemView(appointmentTimelineItemView)
        .build();

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.of(assetAppointmentHistory));

    mockMvc.perform(
            get(ReverseRouter.route(on(AssetTimelineController.class)
                .renderSubareaTimeline(PORTAL_ASSET_ID))
            ))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/systemofrecord/timeline/appointmentTimeline"))
        .andExpect(model().attribute("assetName", assetName))
        .andExpect(model().attribute("assetTypeDisplayName", PortalAssetType.SUBAREA.getDisplayName()))
        .andExpect(model().attribute(
            "assetTypeDisplayNameSentenceCase",
            PortalAssetType.SUBAREA.getSentenceCaseDisplayName())
        )
        .andExpect(model().attribute("timelineItemViews", List.of(appointmentTimelineItemView)));
  }

  @Test
  void renderInstallationTimeline_whenNoAssetFound_thenNotFoundResponse() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.INSTALLATION
    ))
        .willReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(AssetTimelineController.class)
                .renderInstallationTimeline(PORTAL_ASSET_ID))
            ))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderWellboreTimeline_whenNoAssetFound_thenNotFoundResponse() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.WELLBORE
    ))
        .willReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(AssetTimelineController.class)
                .renderWellboreTimeline(PORTAL_ASSET_ID))
            ))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderSubareaTimeline_whenNoAssetFound_thenNotFoundResponse() throws Exception {

    given(assetTimelineService.getAppointmentHistoryForPortalAsset(
        PORTAL_ASSET_ID,
        PortalAssetType.SUBAREA
    ))
        .willReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(AssetTimelineController.class)
                .renderSubareaTimeline(PORTAL_ASSET_ID))
            ))
        .andExpect(status().isNotFound());
  }
}