package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddTeamMemberValidator;

@ContextConfiguration(classes = {
    RegulatorAddMemberController.class,
    AddTeamMemberValidator.class,
    RegulatorTeamMemberRolesValidator.class
})
class RegulatorAddMemberControllerTest extends AbstractControllerTest {

  @MockBean
  private RegulatorTeamService regulatorTeamService;

  @MockBean
  private EnergyPortalUserService energyPortalUserService;

  @Autowired
  AddTeamMemberValidator addTeamMemberValidator;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  RegulatorTeamMemberRolesValidator regulatorTeamMemberRolesValidator;

  @Test
  void renderAddTeamMember_whenNotAccessManager_thenForbidden() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddTeamMember_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void renderAddTeamMember_whenNoTeamFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var teamId = new TeamId(UUID.randomUUID());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderAddTeamMember_whenTeamIdIsNotRegulatorTeam_then404() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderAddTeamMember_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();

    var customerMnemonic = applicationContext.getBean(CustomerConfigurationProperties.class).mnemonic();
    var registrationUrl = applicationContext.getBean(EnergyPortalConfiguration.class).registrationUrl();

    assertThat(modelAndView.getModel())
        .extractingByKeys(
            "htmlTitle",
            "backLinkUrl",
            "registrationUrl",
            "submitUrl"
        )
        .containsExactly(
            "Add user to %s".formatted(customerMnemonic),
            ReverseRouter.route(on(RegulatorTeamManagementController.class).renderMemberList(teamId)),
            registrationUrl,
            ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, null, null))
        );
  }

  @Test
  void addMemberToTeamSubmission_whenTeamIdNotRegulatorType_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.empty());

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, null, null)))
                .with(csrf())
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void addMemberToTeamSubmission_whenNotAccessManager_thenForbidden() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, null, null)))
                .with(csrf())
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addMemberToTeamSubmission_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, null, null)))
                .with(csrf())
                .with(user(user))
        )
        .andExpect(status().isOk());
  }

  @Test
  void addMemberToTeamSubmission_whenInvalidForm_thenUserStaysOnFormPage() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, null, null)))
                .with(csrf())
                .with(user(user))
                .param("username", "")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/permissionmanagement/regulator/regulatorAddTeamMember"));
  }

  @Test
  void addMemberToTeamSubmission_whenValidForm_thenUserTakenToRolesSelection() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    var username = "username";
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();

    when(energyPortalUserService.findUserByUsername(username))
        .thenReturn(List.of(userToAdd));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .addMemberToTeamSubmission(teamId, null, null)))
                .with(csrf())
                .with(user(user))
                .param("username", username)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(RegulatorAddMemberController.class)
            .renderAddTeamMemberRoles(teamId, new WebUserAccountId(userToAdd.webUserAccountId())))));
  }

  @Test
  void renderAddTeamMemberRoles_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd)))
                .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void renderAddTeamMemberRoles_whenNotAccessManager_thenForbidden() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd)))
                .with(user(user)))
        .andExpect(status().isForbidden());
  }

  @Test
  void renderAddTeamMemberRoles_whenNotRegulatorTeam_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void renderAddTeamMemberRoles_whenNoEnergyPortalUser_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd)))
                .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("getEnergyPortalUserThatShouldResultInBadRequest")
  void renderAddTeamMemberRoles_whenEnergyPortalUserNotValid_thenBadRequest(EnergyPortalUserDto energyPortalUser) throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd)))
                .with(user(user)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void renderAddTeamMemberRoles_assertModelProperties() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    var energyPortalUserToAdd = EnergyPortalUserDtoTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(energyPortalUserToAdd));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .renderAddTeamMemberRoles(teamId, webUserAccountIdToAdd)))
                .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModel()).isNotNull();
    assertThat(modelAndView.getModel())
        .extractingByKeys(
            "pageTitle",
            "roles",
            "backLinkUrl"
        )
        .containsExactly(
            "What actions does %s perform?".formatted(energyPortalUserToAdd.displayName()),
            getDisplayableRegulatorRoles(),
            ReverseRouter.route(on(RegulatorAddMemberController.class).renderAddTeamMember(teamId))
        );
  }

  @Test
  void saveAddTeamMemberRoles_whenNotAccessManager_thenForbidden() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void saveAddTeamMemberRoles_whenAccessManager_thenOk() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isOk());
  }

  @Test
  void saveAddTeamMemberRoles_whenNotRegulatorTeam_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.empty());
    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isNotFound());
  }

  @Test
  void saveAddTeamMemberRoles_whenEnergyPortalUserNotFound_thenNotFound() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.empty());

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @MethodSource("getEnergyPortalUserThatShouldResultInBadRequest")
  void saveAddTeamMemberRoles_whenEnergyPortalUserNotValid_thenBadRequest(EnergyPortalUserDto energyPortalUser) throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(energyPortalUser));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void saveAddTeamMemberRoles_whenInvalidTeamMemberRolesForm_thenStayOnFormPage() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
                .param("roles", "")
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/permissionmanagement/regulator/regulatorAddTeamMemberRoles"));
  }

  @Test
  void saveAddTeamMemberRoles_whenValidTeamMemberRolesForm_thenRedirectionToMembersPage() throws Exception {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(userDetailService.getUserDetail()).thenReturn(user);

    var team = TeamTestUtil.Builder()
        .build();

    var teamId = new TeamId(team.getUuid());

    var webUserAccountIdToAdd = new WebUserAccountId(123);

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(RegulatorTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    when(regulatorTeamService.getTeam(teamId)).thenReturn(Optional.of(team));

    when(energyPortalUserService.findByWuaId(webUserAccountIdToAdd))
        .thenReturn(Optional.of(EnergyPortalUserDtoTestUtil.Builder().build()));

    mockMvc.perform(
            post(ReverseRouter.route(on(RegulatorAddMemberController.class)
                .saveAddTeamMemberRoles(teamId, webUserAccountIdToAdd, null, null)))
                .with(user(user))
                .with(csrf())
                .param("roles", RegulatorTeamRole.ACCESS_MANAGER.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(RegulatorTeamManagementController.class)
            .renderMemberList(teamId))));
  }

  private Map<String, String> getDisplayableRegulatorRoles() {
    return Arrays.stream(RegulatorTeamRole.values())
        .sorted(Comparator.comparing(RegulatorTeamRole::getDisplayOrder))
        .collect(Collectors.toMap(
            RegulatorTeamRole::name,
            role -> "%s (%s)".formatted(role.getDescription(), role.getDisplayText()),
            (x, y) -> x,
            LinkedHashMap::new)
        );
  }

  private static Stream<Arguments> getEnergyPortalUserThatShouldResultInBadRequest() {

    var noLoginEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .canLogin(false)
        .build();

    var sharedAccountEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .hasSharedAccount(true)
        .build();

    return Stream.of(
        Arguments.of(noLoginEnergyPortalUser),
        Arguments.of(sharedAccountEnergyPortalUser)
    );
  }

}