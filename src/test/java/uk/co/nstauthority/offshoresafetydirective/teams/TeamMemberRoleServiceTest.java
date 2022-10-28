package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.AddedToTeamEventPublisher;

@ExtendWith(MockitoExtension.class)
class TeamMemberRoleServiceTest {

  @Mock
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @Mock
  private AddedToTeamEventPublisher addedToTeamEventPublisher;

  @Mock
  private UserDetailService userDetailService;

  @InjectMocks
  private TeamMemberRoleService teamMemberRoleService;

  @Captor
  private ArgumentCaptor<List<TeamMemberRole>> teamMemberRoleCaptor;

  @Test
  void addUserTeamRoles_whenMemberWithOneRole_verifySingleRowInsert() {

    var team = TeamTestUtil.Builder().build();
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var role = "ROLE_NAME";
    var instigatingUser = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, Set.of(role));

    verify(teamMemberRoleRepository, times(1)).saveAll(teamMemberRoleCaptor.capture());

    assertThat(teamMemberRoleCaptor.getValue())
        .extracting(TeamMemberRole::getTeam, TeamMemberRole::getWuaId, TeamMemberRole::getRole)
        .containsExactly(tuple(team, (long) userToAdd.webUserAccountId(), role));

    verify(addedToTeamEventPublisher, times(1)).publish(
        new TeamId(team.getUuid()),
        new WebUserAccountId(userToAdd.webUserAccountId()),
        Set.of(role),
        instigatingUser
    );
  }

  @Test
  void addUserTeamRoles_whenMemberWithMultipleRoles_verifyMultipleRowInsert() {

    var team = TeamTestUtil.Builder().build();
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();

    var firstRole = "FIRST_ROLE_NAME";
    var secondRole = "SECOND_ROLE_NAME";

    var rolesToGrant = Set.of(firstRole, secondRole);

    var instigatingUser = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, rolesToGrant);

    verify(teamMemberRoleRepository, times(1)).saveAll(teamMemberRoleCaptor.capture());

    assertThat(teamMemberRoleCaptor.getValue())
        .extracting(TeamMemberRole::getTeam, TeamMemberRole::getWuaId, TeamMemberRole::getRole)
        .containsExactlyInAnyOrder(
            tuple(team, (long) userToAdd.webUserAccountId(), firstRole),
            tuple(team, (long) userToAdd.webUserAccountId(), secondRole)
        );

    verify(addedToTeamEventPublisher, times(1)).publish(
        new TeamId(team.getUuid()),
        new WebUserAccountId(userToAdd.webUserAccountId()),
        rolesToGrant,
        instigatingUser
    );
  }

}