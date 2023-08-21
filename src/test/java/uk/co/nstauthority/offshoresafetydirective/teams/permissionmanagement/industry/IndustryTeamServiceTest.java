package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class IndustryTeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamScopeService teamScopeService;

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
}