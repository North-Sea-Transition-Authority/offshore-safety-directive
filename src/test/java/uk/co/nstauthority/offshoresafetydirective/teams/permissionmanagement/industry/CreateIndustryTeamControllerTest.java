package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermissionSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ContextConfiguration(classes = CreateIndustryTeamController.class)
class CreateIndustryTeamControllerTest extends AbstractControllerTest {

  public static ServiceUserDetail THIRD_PARTY_ACCESS_MANAGER = ServiceUserDetailTestUtil.Builder().build();
  public static TeamMember TEAM_MEMBER = TeamMemberTestUtil.Builder()
      .withRole(RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
      .build();

  @MockBean
  private CreateIndustryTeamValidator createIndustryTeamValidator;

  @MockBean
  private PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @SecurityTest
  void renderCreateIndustryTeam_whenNotLoggedIn_thenRedirectionToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CreateIndustryTeamController.class)
            .renderCreateIndustryTeam())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void smokeTestEndpointPermissions() {

    var orgGroupId = 123;
    var orgGroupDto = PortalOrganisationGroupDtoTestUtil.builder().build();
    when(portalOrganisationGroupQueryService.findOrganisationById(orgGroupId, CreateIndustryTeamController.INDUSTRY_TEAM_PURPOSE))
        .thenReturn(Optional.of(orgGroupDto));

    var team = TeamTestUtil.Builder().build();
    when(industryTeamService.findIndustryTeamForOrganisationGroup(orgGroupDto))
        .thenReturn(Optional.of(team));

    HasPermissionSecurityTestUtil.smokeTester(mockMvc, teamMemberService)
        .withRequiredPermissions(EnumSet.of(
            RolePermission.MANAGE_INDUSTRY_TEAMS
        ))
        .withUser(THIRD_PARTY_ACCESS_MANAGER)
        .withGetEndpoint(
            ReverseRouter.route(on(CreateIndustryTeamController.class)
                .renderCreateIndustryTeam()),
            status().isOk(),
            status().isForbidden()
        )
        .withPostEndpoint(
            ReverseRouter.route(on(CreateIndustryTeamController.class)
                .createIndustryTeam(null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("orgGroupId", String.valueOf(orgGroupId))
        .test();
  }

  @Test
  void renderCreateIndustryTeam() throws Exception {
    when(teamMemberService.getUserAsTeamMembers(THIRD_PARTY_ACCESS_MANAGER))
        .thenReturn(Collections.singletonList(TEAM_MEMBER));

    mockMvc.perform(get(ReverseRouter.route(on(CreateIndustryTeamController.class)
            .renderCreateIndustryTeam()))
        .with(user(THIRD_PARTY_ACCESS_MANAGER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/permissionmanagement/createIndustryTeam"))
        .andExpect(model().attribute("pageTitle", "Select an organisation"))
        .andExpect(model().attribute(
            "orgGroupRestUrl",
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class)
                .searchPortalOrganisationGroups(null))
        ));
  }

  @Test
  void createIndustryTeam_whenInvalid_thenOk() throws Exception {
    when(teamMemberService.getUserAsTeamMembers(THIRD_PARTY_ACCESS_MANAGER))
        .thenReturn(Collections.singletonList(TEAM_MEMBER));

    doAnswer(invocation -> {
      var bindingResult = (BindingResult) invocation.getArgument(1);
      bindingResult.addError(new FieldError("error", "error.code", "error.message"));
      return invocation;
    }).when(createIndustryTeamValidator).validate(any(), any());

    mockMvc.perform(post(ReverseRouter.route(on(CreateIndustryTeamController.class)
            .createIndustryTeam(null, null)))
            .with(user(THIRD_PARTY_ACCESS_MANAGER))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/permissionmanagement/createIndustryTeam"));
  }

  @Test
  void createIndustryTeam_whenValid_thenVerifyTeamCreatedAndRedirected() throws Exception {
    when(teamMemberService.getUserAsTeamMembers(THIRD_PARTY_ACCESS_MANAGER))
        .thenReturn(Collections.singletonList(TEAM_MEMBER));

    var orgGroupId = 123;
    var form = CreateIndustryTeamFormTestUtil.builder()
        .withOrgGroupId(orgGroupId)
        .build();

    var portalOrganisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(String.valueOf(orgGroupId))
        .build();

    var createdTeam = TeamTestUtil.Builder().build();
    when(portalOrganisationGroupQueryService.findOrganisationById(
        orgGroupId,
        CreateIndustryTeamController.INDUSTRY_TEAM_PURPOSE
    ))
        .thenReturn(Optional.of(portalOrganisationGroupDto));

    when(industryTeamService.createIndustryTeam(portalOrganisationGroupDto))
        .thenReturn(createdTeam);

    mockMvc.perform(post(ReverseRouter.route(on(CreateIndustryTeamController.class)
            .createIndustryTeam(null, null)))
            .with(user(THIRD_PARTY_ACCESS_MANAGER))
            .with(csrf())
            .flashAttr("form", form))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
            ReverseRouter.route(on(IndustryTeamManagementController.class)
                .renderMemberList(createdTeam.toTeamId()))
        ));
  }

  @Test
  void createIndustryTeam_whenValid_andOrganisationNotFound_thenNotFound() throws Exception {
    when(teamMemberService.getUserAsTeamMembers(THIRD_PARTY_ACCESS_MANAGER))
        .thenReturn(Collections.singletonList(TEAM_MEMBER));

    var orgGroupId = 123;
    var form = CreateIndustryTeamFormTestUtil.builder()
        .withOrgGroupId(orgGroupId)
        .build();

    when(portalOrganisationGroupQueryService.findOrganisationById(
        orgGroupId,
        CreateIndustryTeamController.INDUSTRY_TEAM_PURPOSE
    ))
        .thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(CreateIndustryTeamController.class)
            .createIndustryTeam(null, null)))
            .with(user(THIRD_PARTY_ACCESS_MANAGER))
            .with(csrf())
            .flashAttr("form", form))
        .andExpect(status().isInternalServerError());
  }
}