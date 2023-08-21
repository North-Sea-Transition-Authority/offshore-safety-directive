package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
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

@ExtendWith(MockitoExtension.class)
class TeamScopeServiceTest {

  @Mock
  private TeamScopeRepository teamScopeRepository;

  @InjectMocks
  private TeamScopeService teamScopeService;

  @Test
  void getTeamScope_whenTeamScopeFound_thenNotEmpty() {
    var orgGroupId = UUID.randomUUID().toString();
    var teamScope = TeamScopeTestUtil.builder().build();

    when(teamScopeRepository.findByPortalIdAndPortalTeamType(orgGroupId, PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(Optional.of(teamScope));

    var result = teamScopeService.getTeamScope(orgGroupId, PortalTeamType.ORGANISATION_GROUP);
    assertThat(result).contains(teamScope);
  }

  @Test
  void getTeamScope_whenNoTeamScopeFound_thenEmpty() {
    var orgGroupId = UUID.randomUUID().toString();

    when(teamScopeRepository.findByPortalIdAndPortalTeamType(orgGroupId, PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(Optional.empty());

    var result = teamScopeService.getTeamScope(orgGroupId, PortalTeamType.ORGANISATION_GROUP);
    assertThat(result).isEmpty();
  }

  @Test
  void addTeamScope() {
    var team = TeamTestUtil.Builder().build();
    var orgGroupId = UUID.randomUUID().toString();
    var portalTeamType = PortalTeamType.ORGANISATION_GROUP;
    teamScopeService.addTeamScope(team, portalTeamType, orgGroupId);

    var captor = ArgumentCaptor.forClass(TeamScope.class);
    verify(teamScopeRepository).save(captor.capture());

    assertThat(captor.getValue())
        .extracting(
            TeamScope::getTeam,
            TeamScope::getPortalId,
            TeamScope::getPortalTeamType
        )
        .containsExactly(
            team,
            orgGroupId,
            portalTeamType
        );
  }
}