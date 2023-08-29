package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class IndustryTeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamScopeService teamScopeService;

  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  private IndustryTeamService industryTeamService;

  @Test
  void findIndustryTeamForOrganisationGroup_whenFound_thenPresent() {
    var organisationId = UUID.randomUUID().toString();
    var portalOrganisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(organisationId)
        .build();

    var teamId = new TeamId(UUID.randomUUID());
    var team = TeamTestUtil.Builder()
        .withId(teamId.uuid())
        .build();

    var teamScope = TeamScopeTestUtil.builder()
        .withPortalId(organisationId)
        .withTeam(team)
        .build();
    when(teamScopeService.getTeamScope(organisationId, PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(Optional.of(teamScope));

    var result = industryTeamService.findIndustryTeamForOrganisationGroup(portalOrganisationGroupDto);

    assertThat(result).contains(team);
  }

  @Test
  void findIndustryTeamForOrganisationGroup_whenNoTeamScopeFound_thenEmpty() {
    var organisationId = UUID.randomUUID().toString();
    var portalOrganisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(organisationId)
        .build();

    when(teamScopeService.getTeamScope(organisationId, PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(Optional.empty());

    var result = industryTeamService.findIndustryTeamForOrganisationGroup(portalOrganisationGroupDto);

    assertThat(result).isEmpty();
  }

  @Test
  void createIndustryTeam() {
    var organisationId = UUID.randomUUID().toString();
    var orgName = "Org name";
    var portalOrganisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(organisationId)
        .withName(orgName)
        .build();

    var teamCaptor = ArgumentCaptor.forClass(Team.class);
    doAnswer(invocation -> invocation.getArgument(0)).when(teamRepository).save(teamCaptor.capture());

    var result = industryTeamService.createIndustryTeam(portalOrganisationGroupDto);
    verify(teamScopeService).addTeamScope(
        teamCaptor.getValue(),
        PortalTeamType.ORGANISATION_GROUP,
        organisationId
    );
    assertThat(result).isEqualTo(teamCaptor.getValue())
        .extracting(
            Team::getTeamType,
            Team::getDisplayName
        )
        .containsExactly(
            TeamType.INDUSTRY,
            orgName
        );
  }
  
  @Test
  void addUserTeamRoles_verifyRepositoryInteractions() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.CONSULTEE)
        .build();
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var industryRoles = Set.of(
        IndustryTeamRole.ACCESS_MANAGER
    );

    industryTeamService.addUserTeamRoles(team, userToAdd, industryRoles);

    var rolesAsStrings = industryRoles
        .stream()
        .map(IndustryTeamRole::name)
        .collect(Collectors.toSet());

    verify(teamMemberRoleService, times(1)).addUserTeamRoles(team, userToAdd, rolesAsStrings);
 }

  @Test
  void getTeamsForUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();

    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY))
        .thenReturn(List.of(team));

    var result = industryTeamService.getTeamsForUser(user);

    AssertionsForInterfaceTypes.assertThat(result)
        .containsExactly(team);
  }

  @Test
  void isMemberOfIndustryTeam_isPresent() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();

    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY))
        .thenReturn(List.of(team));

    var memberOfIndustryTeam = industryTeamService.isMemberOfIndustryTeam(user);
    assertTrue(memberOfIndustryTeam);
  }

  @Test
  void isMemberOfConsulteeTeam_isEmpty() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY))
        .thenReturn(List.of());

    var memberOfIndustryTeam = industryTeamService.isMemberOfIndustryTeam(user);
    assertFalse(memberOfIndustryTeam);
  }
}