package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberViewServiceTest {

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private TeamMemberViewService teamMemberViewService;

  @Test
  void getTeamMemberViewsForTeam_verifyTeamMemberViewMapping() {

    var team = TeamTestUtil.Builder().build();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMember));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMember.wuaId().toInt())
        .build();

    when(energyPortalUserService.findByWuaIds(List.of(teamMember.wuaId()), TeamMemberViewService.USER_ACCOUNTS_PURPOSE))
        .thenReturn(List.of(energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews).extracting(
        TeamMemberView::wuaId,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::lastName,

        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        Tuple.tuple(
            teamMember.wuaId(),
            energyPortalUser.title(),
            energyPortalUser.forename(),
            energyPortalUser.surname(),
            energyPortalUser.emailAddress(),
            energyPortalUser.telephoneNumber(),
            teamMember.roles()
        )
    );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleTeamMembers_verifyOrderedByName() {

    var team = TeamTestUtil.Builder().build();

    var firstTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(1)
        .build();

    var secondTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(2)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(secondTeamMember, firstTeamMember));

    var firstAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(firstTeamMember.wuaId().toInt())
        .withForename("A forename")
        .withSurname("A surname")
        .build();

    var secondAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(secondTeamMember.wuaId().toInt())
        .withForename("B forename")
        .withSurname("B surname")
        .build();

    when(energyPortalUserService.findByWuaIds(
        argThat(wuaIds -> wuaIds.containsAll(List.of(firstTeamMember.wuaId(), secondTeamMember.wuaId()))),
        eq(TeamMemberViewService.USER_ACCOUNTS_PURPOSE)
    ))
        .thenReturn(List.of(secondAlphabeticallyEnergyPortalUser, firstAlphabeticallyEnergyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(firstAlphabeticallyEnergyPortalUser.forename(), firstAlphabeticallyEnergyPortalUser.surname()),
            tuple(secondAlphabeticallyEnergyPortalUser.forename(), secondAlphabeticallyEnergyPortalUser.surname())
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleTeamMembersWithSameForename_verifyOrderedBySurname() {

    var team = TeamTestUtil.Builder().build();

    var firstTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(1)
        .build();

    var secondTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(2)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(secondTeamMember, firstTeamMember));

    var firstAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(firstTeamMember.wuaId().toInt())
        .withForename("A forename")
        .withSurname("A surname")
        .build();

    var secondAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(secondTeamMember.wuaId().toInt())
        .withForename("A forename")
        .withSurname("B surname")
        .build();

    when(energyPortalUserService.findByWuaIds(
        argThat(wuaIds -> wuaIds.containsAll(List.of(firstTeamMember.wuaId(), secondTeamMember.wuaId()))),
        eq(TeamMemberViewService.USER_ACCOUNTS_PURPOSE)
    ))
        .thenReturn(List.of(secondAlphabeticallyEnergyPortalUser, firstAlphabeticallyEnergyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(firstAlphabeticallyEnergyPortalUser.forename(), firstAlphabeticallyEnergyPortalUser.surname()),
            tuple(secondAlphabeticallyEnergyPortalUser.forename(), secondAlphabeticallyEnergyPortalUser.surname())
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleRoles_verifyOrderedByRoleDisplayOrder() {

    var team = TeamTestUtil.Builder().build();

    var teamMemberWithMultipleRoles = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER)
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMemberWithMultipleRoles));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMemberWithMultipleRoles.wuaId().toInt())
        .build();

    when(energyPortalUserService.findByWuaIds(List.of(teamMemberWithMultipleRoles.wuaId()), TeamMemberViewService.USER_ACCOUNTS_PURPOSE))
        .thenReturn(List.of(energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews).hasSize(1);
    assertThat(resultingTeamMemberViews.get(0).teamRoles())
        .containsExactly(
            TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER,
            TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenNoEnergyPortalUserFound_thenException() {

    var team = TeamTestUtil.Builder().build();

    var teamMember = TeamMemberTestUtil.Builder()
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMember));

    when(energyPortalUserService.findByWuaIds(List.of(teamMember.wuaId()), TeamMemberViewService.USER_ACCOUNTS_PURPOSE))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(
        () -> teamMemberViewService.getTeamMemberViewsForTeam(team)
    )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "Did not find an Energy Portal User with WUA ID %s when converting team members"
            .formatted(teamMember.wuaId())
        );
  }

  @Test
  void getTeamMembersWithRoles_whenNoResults_thenEmptyList() {

    var roles = Set.of("ROLE_NAME");
    var teamType = TeamType.REGULATOR;

    when(teamMemberService.getTeamMembersInRoles(roles, teamType))
        .thenReturn(Collections.emptyList());

    var resultingTeamMemberViews = teamMemberViewService.getTeamMembersWithRoles(roles, teamType);

    assertThat(resultingTeamMemberViews).isEmpty();
  }

  @Test
  void getTeamMembersWithRoles_whenResults_thenPopulatedList() {

    var role = RegulatorTeamRole.ACCESS_MANAGER;
    var teamType = TeamType.REGULATOR;

    var expectedTeamMember = TeamMemberTestUtil.Builder().build();

    when(teamMemberService.getTeamMembersInRoles(List.of(role.name()), teamType))
        .thenReturn(List.of(expectedTeamMember));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(expectedTeamMember.wuaId().id())
        .build();

    when(energyPortalUserService.findByWuaIds(List.of(expectedTeamMember.wuaId()), TeamMemberViewService.USER_ACCOUNTS_PURPOSE))
        .thenReturn(List.of(energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMembersWithRoles(List.of(role.name()), teamType);

    assertThat(resultingTeamMemberViews).extracting(
        TeamMemberView::wuaId,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::lastName,
        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        Tuple.tuple(
            expectedTeamMember.wuaId(),
            energyPortalUser.title(),
            energyPortalUser.forename(),
            energyPortalUser.surname(),
            energyPortalUser.emailAddress(),
            energyPortalUser.telephoneNumber(),
            Set.of(role)
        )
    );
  }

  @Test
  void getUserViewsForTeam() {
    var team = TeamTestUtil.Builder().build();
    var wuaId = 100L;

    var teamView = TeamTestUtil.createTeamView(team);

    var teamMember = new TeamMember(new WebUserAccountId(wuaId), teamView,
        Set.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER));

    when(teamMemberService.getTeamMembers(team))
        .thenReturn(List.of(teamMember));

    var portalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .withTitle("Dr")
        .withForename("John")
        .withSurname("Smith")
        .withEmailAddress("john.smith@test.org")
        .withPhoneNumber("telephone")
        .build();

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(wuaId)), TeamMemberViewService.USER_ACCOUNTS_PURPOSE))
        .thenReturn(List.of(portalUser));

    var result = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(result).extracting(
        TeamMemberView::wuaId,
        TeamMemberView::teamView,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::lastName,
        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        Tuple.tuple(
            new WebUserAccountId(wuaId),
            teamView,
            "Dr",
            "John",
            "Smith",
            "john.smith@test.org",
            "telephone",
            Set.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
        )
    );

  }

  @Test
  void getUserViewForTeamMember() {
    var team = TeamTestUtil.Builder().build();
    var wuaId = 100L;

    var teamView = TeamTestUtil.createTeamView(team);

    var teamMember = new TeamMember(new WebUserAccountId(wuaId), teamView,
        Set.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER));

    var portalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(wuaId)
        .withTitle("Dr")
        .withForename("John")
        .withSurname("Smith")
        .withEmailAddress("john.smith@test.org")
        .withPhoneNumber("telephone")
        .build();

    when(energyPortalUserService.findByWuaIds(List.of(new WebUserAccountId(wuaId)), TeamMemberViewService.USER_ACCOUNTS_PURPOSE))
        .thenReturn(List.of(portalUser));

    var result = teamMemberViewService.getTeamMemberView(teamMember);

    assertTrue(result.isPresent());
    assertThat(result.get()).extracting(
        TeamMemberView::wuaId,
        TeamMemberView::teamView,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::lastName,
        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        new WebUserAccountId(wuaId),
        teamView,
        "Dr",
        "John",
        "Smith",
        "john.smith@test.org",
        "telephone",
        Set.of(RegulatorTeamRole.ACCESS_MANAGER, RegulatorTeamRole.THIRD_PARTY_ACCESS_MANAGER)
    );
  }

  enum TestTeamRole implements TeamRole {

    FIRST_ROLE_BY_DISPLAY_ORDER(1),
    SECOND_ROLE_BY_DISPLAY_ORDER(2);

    private final int displayOrder;

    TestTeamRole(int displayOrder) {
      this.displayOrder = displayOrder;
    }

    @Override
    public String getScreenDisplayText() {
      return "";
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public int getDisplayOrder() {
      return displayOrder;
    }

    @Override
    public Set<RolePermission> getRolePermissions() {
      return Set.of();
    }
  }
}