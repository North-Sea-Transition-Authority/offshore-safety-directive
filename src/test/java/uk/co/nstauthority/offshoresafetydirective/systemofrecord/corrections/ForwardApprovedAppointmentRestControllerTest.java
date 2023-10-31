package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.fivium.energyportalapi.generated.types.SubareaStatus;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.date.DateUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AppointmentTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.AssetTestUtil;
import uk.co.nstauthority.offshoresafetydirective.systemofrecord.PortalAssetType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = {ForwardApprovedAppointmentRestController.class})
class ForwardApprovedAppointmentRestControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail APPOINTMENT_MANAGER = ServiceUserDetailTestUtil.Builder().build();
  private static final TeamMember APPOINTMENT_MANAGER_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.MANAGE_ASSET_APPOINTMENTS)
      .build();
  private static final List<SubareaStatus> SUBAREA_STATUSES = List.of(SubareaStatus.EXTANT, SubareaStatus.NOT_EXTANT);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @BeforeEach
  void setUp() {
    when(teamMemberService.getUserAsTeamMembers(APPOINTMENT_MANAGER))
        .thenReturn(Collections.singletonList(APPOINTMENT_MANAGER_MEMBER));
  }

  @SecurityTest
  void searchSubareaAppointments_whenUnauthenticated_thenRedirectedToLogin() throws Exception {
    var searchTerm = "search";
    mockMvc.perform(get(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm)))
        )
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void searchSubareaAppointments_whenAuthenticated_assertPermissionsPermitted() {

    var searchTerm = "search";

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(Collections.singleton(RolePermission.MANAGE_APPOINTMENTS))
        .withUser(APPOINTMENT_MANAGER)
        .withGetEndpoint(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm))
        )
        .test();
  }

  @Test
  void searchSubareaAppointments_whenNoSubareasFoundInPortal_returnEmptyList() throws Exception {
    var searchTerm = "some subarea name";
    when(licenceBlockSubareaQueryService.searchSubareasByName(searchTerm, SUBAREA_STATUSES))
        .thenReturn(Collections.emptyList());

    var result = mockMvc.perform(get(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm)))
            .with(user(APPOINTMENT_MANAGER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    var mappedResult = OBJECT_MAPPER.readValue(result, RestSearchResult.class);

    assertThat(mappedResult.getResults()).isEmpty();
  }

  @Test
  void searchSubareaAppointments_whenSubareasFoundInPortal_thenReturnList_andAssertSortedOrder() throws Exception {
    var searchTerm = "some subarea name";

    var firstPortalSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("first")
        .withSubareaName("a")
        .build();

    var secondPortalSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaId("second")
        .withSubareaName("B")
        .build();

    when(licenceBlockSubareaQueryService.searchSubareasByName(searchTerm, SUBAREA_STATUSES))
        .thenReturn(List.of(secondPortalSubareaByName, firstPortalSubareaByName));

    var firstSubareaByNameAsset = AssetTestUtil.builder()
        .withPortalAssetId(firstPortalSubareaByName.subareaId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();

    var secondSubareaByNameAsset = AssetTestUtil.builder()
        .withPortalAssetId(secondPortalSubareaByName.subareaId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();

    var portalAssetIds = List.of(firstSubareaByNameAsset.getPortalAssetId(), secondSubareaByNameAsset.getPortalAssetId());

    var latestAppointmentForFirstAsset = AppointmentTestUtil.builder()
        .withAsset(secondSubareaByNameAsset)
        .withResponsibleFromDate(LocalDate.now())
        .build();

    var earliestAppointmentForFirstAsset = AppointmentTestUtil.builder()
        .withAsset(secondSubareaByNameAsset)
        .withResponsibleFromDate(LocalDate.now().minusDays(2L))
        .build();

    var appointmentForSecondAsset = AppointmentTestUtil.builder()
        .withAsset(firstSubareaByNameAsset)
        .withResponsibleFromDate(LocalDate.now())
        .build();

    when(appointmentAccessService.getAppointmentsForAssets(
        ForwardApprovedAppointmentRestController.STATUSES,
        portalAssetIds,
        PortalAssetType.SUBAREA)
    )
        .thenReturn(List.of(latestAppointmentForFirstAsset, earliestAppointmentForFirstAsset, appointmentForSecondAsset));

    var result = mockMvc.perform(get(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm)))
            .with(user(APPOINTMENT_MANAGER))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    var mappedResult = OBJECT_MAPPER.readValue(result, RestSearchResult.class);

    assertThat(mappedResult.getResults())
        .extracting(RestSearchItem::text)
        .containsExactly(
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                .formatted(firstPortalSubareaByName.displayName(),
                    DateUtil.formatLongDate(appointmentForSecondAsset.getResponsibleFromDate())),
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                .formatted(secondPortalSubareaByName.displayName(),
                    DateUtil.formatLongDate(earliestAppointmentForFirstAsset.getResponsibleFromDate())),
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                .formatted(secondPortalSubareaByName.displayName(),
                    DateUtil.formatLongDate(latestAppointmentForFirstAsset.getResponsibleFromDate()))
        );
  }
}