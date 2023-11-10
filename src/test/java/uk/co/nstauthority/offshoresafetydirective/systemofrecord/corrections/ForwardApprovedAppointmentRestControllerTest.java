package uk.co.nstauthority.offshoresafetydirective.systemofrecord.corrections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
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
    when(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestController.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .thenReturn(Set.of());

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
  void searchSubareaAppointments_whenMultipleResultForSameBlockAndSubareaName_thenSortedByLicence() throws Exception {
    var searchTerm = "matching subareas";

    // given multiple different licences
    // then the results are sorted first by type and then number

    var firstSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(1)
        .withSubareaId("1")
        .build();

    var secondSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(2)
        .withSubareaId("2")
        .build();

    var thirdSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("A")
        .withLicenceNumber(10)
        .withSubareaId("3")
        .build();

    var fourthSubareaByLicence = LicenceBlockSubareaDtoTestUtil.builder()
        .withLicenceType("B")
        .withLicenceNumber(1)
        .withSubareaId("4")
        .build();

    var unsortedSubareaList = Set.of(
        fourthSubareaByLicence,
        thirdSubareaByLicence,
        secondSubareaByLicence,
        firstSubareaByLicence
    );

    given(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestController.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .willReturn(unsortedSubareaList);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> portalAssetIdCaptor = ArgumentCaptor.forClass(List.class);

    when(appointmentAccessService.getAppointmentsForAssets(
        eq(ForwardApprovedAppointmentRestController.STATUSES),
        portalAssetIdCaptor.capture(),
        eq(PortalAssetType.SUBAREA))
    )
        .thenReturn(List.of(AppointmentTestUtil.builder().build()));

    mockMvc.perform(get(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm)))
            .with(user(APPOINTMENT_MANAGER))
        )
        .andExpect(status().isOk());

    assertThat(portalAssetIdCaptor.getAllValues())
        .containsExactly(
            List.of(
                firstSubareaByLicence.subareaId().id(),
                secondSubareaByLicence.subareaId().id(),
                thirdSubareaByLicence.subareaId().id(),
                fourthSubareaByLicence.subareaId().id()
            ));
  }

  @Test
  void searchSubareaAppointments_whenMultipleResultForSameLicenceAndSubareaName_thenSortedByBlockComponents() throws Exception {
    var searchTerm = "matching subareas";

    // given multiple different blocks
    // then the results are sorted first by quadrant number, then block number then block suffix

    var firstSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withSubareaId("1")
        .withBlockNumber(1)
        .withBlockSuffix(null)
        .build();

    var secondSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("a")
        .withSubareaId("2")
        .build();

    var thirdSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(1)
        .withBlockSuffix("B")
        .withSubareaId("3")
        .build();

    var fourthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("1")
        .withBlockNumber(2)
        .withSubareaId("4")
        .build();

    var fifthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("10")
        .withSubareaId("5")
        .build();

    var sixthSubareaByBlock = LicenceBlockSubareaDtoTestUtil.builder()
        .withQuadrantNumber("2")
        .withSubareaId("6")
        .build();

    var unsortedSubareaList = Set.of(
        sixthSubareaByBlock,
        firstSubareaByBlock,
        thirdSubareaByBlock,
        secondSubareaByBlock,
        fifthSubareaByBlock,
        fourthSubareaByBlock
    );

    given(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestController.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .willReturn(unsortedSubareaList);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> portalAssetIdCaptor = ArgumentCaptor.forClass(List.class);

    when(appointmentAccessService.getAppointmentsForAssets(
        eq(ForwardApprovedAppointmentRestController.STATUSES),
        portalAssetIdCaptor.capture(),
        eq(PortalAssetType.SUBAREA))
    )
        .thenReturn(List.of(AppointmentTestUtil.builder().build()));

    mockMvc.perform(get(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm)))
            .with(user(APPOINTMENT_MANAGER))
        )
        .andExpect(status().isOk());

    assertThat(portalAssetIdCaptor.getAllValues())
        .containsExactly(
            List.of(
                firstSubareaByBlock.subareaId().id(),
                secondSubareaByBlock.subareaId().id(),
                thirdSubareaByBlock.subareaId().id(),
                fourthSubareaByBlock.subareaId().id(),
                fifthSubareaByBlock.subareaId().id(),
                sixthSubareaByBlock.subareaId().id()
            ));
  }

  @Test
  void searchSubareaAppointments_whenMultipleResultForSameLicenceAndBlock_thenSortedBySubareaName() throws Exception {
    var searchTerm = "matching subareas";

    // given multiple different subarea names
    // then the results are sorted by subarea name

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("1")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .withSubareaId("2")
        .build();

    var thirdSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("c name")
        .withSubareaId("3")
        .build();

    var unsortedSubareaList = Set.of(
        thirdSubareaByName,
        secondSubareaByName,
        firstSubareaByName
    );

    given(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestController.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .willReturn(unsortedSubareaList);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<String>> portalAssetIdCaptor = ArgumentCaptor.forClass(List.class);

    when(appointmentAccessService.getAppointmentsForAssets(
        eq(ForwardApprovedAppointmentRestController.STATUSES),
        portalAssetIdCaptor.capture(),
        eq(PortalAssetType.SUBAREA))
    )
        .thenReturn(List.of(AppointmentTestUtil.builder().build()));

    mockMvc.perform(get(
            ReverseRouter.route(on(ForwardApprovedAppointmentRestController.class).searchSubareaAppointments(searchTerm)))
            .with(user(APPOINTMENT_MANAGER))
        )
        .andExpect(status().isOk());

    assertThat(portalAssetIdCaptor.getAllValues())
        .containsExactly(
            List.of(
                firstSubareaByName.subareaId().id(),
                secondSubareaByName.subareaId().id(),
                thirdSubareaByName.subareaId().id()
            ));
  }

  @Test
  void searchSubareaAppointments_whenSubareasFoundInPortal_thenReturnList_andAssertSortedOrderForAppointments() throws Exception {
    var searchTerm = "some subarea name";

    var firstSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("a name")
        .withSubareaId("1")
        .build();

    var secondSubareaByName = LicenceBlockSubareaDtoTestUtil.builder()
        .withSubareaName("B name")
        .withSubareaId("2")
        .build();

    var unsortedSubareaList = Set.of(
        secondSubareaByName,
        firstSubareaByName
    );

    when(licenceBlockSubareaQueryService.searchSubareasByDisplayName(
        searchTerm,
        ForwardApprovedAppointmentRestController.FORWARD_APPROVED_SEARCH_PURPOSE
    ))
        .thenReturn(unsortedSubareaList);

    var firstSubareaByNameAsset = AssetTestUtil.builder()
        .withPortalAssetId(firstSubareaByName.subareaId().id())
        .withPortalAssetType(PortalAssetType.SUBAREA)
        .build();

    var secondSubareaByNameAsset = AssetTestUtil.builder()
        .withPortalAssetId(secondSubareaByName.subareaId().id())
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
                .formatted(firstSubareaByName.displayName(),
                    DateUtil.formatLongDate(appointmentForSecondAsset.getResponsibleFromDate())),
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                .formatted(secondSubareaByName.displayName(),
                    DateUtil.formatLongDate(earliestAppointmentForFirstAsset.getResponsibleFromDate())),
            ForwardApprovedAppointmentRestController.SEARCH_DISPLAY_STRING
                .formatted(secondSubareaByName.displayName(),
                    DateUtil.formatLongDate(latestAppointmentForFirstAsset.getResponsibleFromDate()))
        );
  }
}