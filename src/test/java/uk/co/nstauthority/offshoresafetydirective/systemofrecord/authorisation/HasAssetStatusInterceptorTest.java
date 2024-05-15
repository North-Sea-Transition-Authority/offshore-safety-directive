package uk.co.nstauthority.offshoresafetydirective.systemofrecord.authorisation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetId;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetStatus;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.timeline.AssetDtoTestUtil;

@ContextConfiguration(classes = HasAssetStatusInterceptorTest.HasAssetStatusInterceptorTestController.class)
class HasAssetStatusInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  private static final AppointmentId APPOINTMENT_ID = new AppointmentId(UUID.randomUUID());
  private static final AssetId ASSET_ID = new AssetId(UUID.randomUUID());

  @Test
  void endpointWithoutSupportedAnnotation() throws Exception {
    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .endpointWithoutSupportedAnnotation(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void appointment_endpointWithoutAppointmentId() throws Exception {
    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .appointmentEndpointWithoutAppointmentId());

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void appointment_verifyAssetStatusMatching_whenNoAppointmentFound_thenIsNotFound() throws Exception {
    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.empty());

    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .requiresExtantAppointment(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void appointment_verifyAppointmentStatusMatching_whenHasIncorrectStatus_thenForbidden() throws Exception {
    var asset = AssetTestUtil.builder()
        .withAssetStatus(AssetStatus.REMOVED)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .requiresExtantAppointment(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void appointment_verifyAppointmentStatusMatching_whenHasCorrectStatus_thenOk() throws Exception {
    var asset = AssetTestUtil.builder()
        .withAssetStatus(AssetStatus.EXTANT)
        .build();

    var appointment = AppointmentTestUtil.builder()
        .withAsset(asset)
        .build();

    when(appointmentAccessService.getAppointment(APPOINTMENT_ID))
        .thenReturn(Optional.of(appointment));

    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .requiresExtantAppointment(APPOINTMENT_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name(HasAssetStatusInterceptorTestController.VIEW_NAME));
  }

  @Test
  void Asset_endpointWithoutAppointmentId_thenBadRequest() throws Exception {
    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .assetEndpointWithoutAssetId());

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void Asset_verifyAssetStatusMatching_whenNoAppointmentFound_thenIsNotFound() throws Exception {
    when(assetAccessService.getAsset(ASSET_ID))
        .thenReturn(Optional.empty());

    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .requiresExtantAsset(ASSET_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isNotFound());
  }

  @Test
  void Asset_verifyAppointmentStatusMatching_whenHasIncorrectStatus_thenForbidden() throws Exception {
    var asset = AssetDtoTestUtil.builder()
        .withStatus(AssetStatus.REMOVED)
        .build();

    when(assetAccessService.getAsset(ASSET_ID))
        .thenReturn(Optional.of(asset));

    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .requiresExtantAsset(ASSET_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @Test
  void Asset_verifyAppointmentStatusMatching_whenHasCorrectStatus_thenOk() throws Exception {
    var asset = AssetDtoTestUtil.builder()
        .withStatus(AssetStatus.EXTANT)
        .build();

    when(assetAccessService.getAsset(ASSET_ID))
        .thenReturn(Optional.of(asset));

    var route = ReverseRouter.route(on(HasAssetStatusInterceptorTestController.class)
        .requiresExtantAsset(ASSET_ID));

    mockMvc.perform(get(route)
            .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name(HasAssetStatusInterceptorTestController.VIEW_NAME));
  }

  @Controller
  @RequestMapping
  static class HasAssetStatusInterceptorTestController {

    static final String VIEW_NAME = "test_view";

    @GetMapping("/appointment/{appointmentId}/no-annotation")
    public ModelAndView endpointWithoutSupportedAnnotation(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/without-appointment-id")
    @HasAssetStatus(AssetStatus.EXTANT)
    public ModelAndView appointmentEndpointWithoutAppointmentId() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/asset/without-asset-id")
    @HasAssetStatus(AssetStatus.EXTANT)
    public ModelAndView assetEndpointWithoutAssetId() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/appointment/{appointmentId}/requires-extant-status")
    @HasAssetStatus(AssetStatus.EXTANT)
    public ModelAndView requiresExtantAppointment(@PathVariable("appointmentId") AppointmentId appointmentId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/asset/{assetId}/requires-extant-status")
    @HasAssetStatus(AssetStatus.EXTANT)
    public ModelAndView requiresExtantAsset(@PathVariable("assetId") AssetId assetId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}