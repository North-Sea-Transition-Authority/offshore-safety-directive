package uk.co.nstauthority.offshoresafetydirective.teams.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.UserDetailService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRoleRepository;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class TeamManagementServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private EnergyPortalAccessService energyPortalAccessService;

  @Mock
  private UserDetailService userDetailService;

  @InjectMocks
  private TeamManagementService teamManagementService;

  @Captor
  private ArgumentCaptor<Team> teamArgumentCaptor;
  @Captor
  private ArgumentCaptor<List<TeamRole>> teamRoleListCaptor;

  private static Team regTeam;
  private static Team orgTeam1;
  private static Team orgTeam2;

  private static TeamRole regTeamUser1RoleManage;
  private static TeamRole regTeamUser1RoleOrgAdmin;
  private static TeamRole regTeamUser2RoleOrgAdmin;

  private static TeamRole orgTeam1User1RoleManage;
  private static TeamRole orgTeam2User1RoleManage;

  private static final Long user1WuaId = 1L;
  private static EnergyPortalUserDto user1;
  private static final Long user2WuaId = 2L;
  private static EnergyPortalUserDto user2;

  @BeforeAll
  static void setUp() {
    regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);
    regTeamUser1RoleManage = new TeamRole();
    regTeamUser1RoleManage.setTeam(regTeam);
    regTeamUser1RoleManage.setWuaId(user1WuaId);
    regTeamUser1RoleManage.setRole(Role.TEAM_MANAGER);

    regTeamUser1RoleOrgAdmin = new TeamRole();
    regTeamUser1RoleOrgAdmin.setTeam(regTeam);
    regTeamUser1RoleOrgAdmin.setWuaId(user1WuaId);
    regTeamUser1RoleOrgAdmin.setRole(Role.THIRD_PARTY_TEAM_MANAGER);

    regTeamUser2RoleOrgAdmin = new TeamRole();
    regTeamUser2RoleOrgAdmin.setTeam(regTeam);
    regTeamUser2RoleOrgAdmin.setWuaId(user2WuaId);
    regTeamUser2RoleOrgAdmin.setRole(Role.THIRD_PARTY_TEAM_MANAGER);

    orgTeam1 = new Team(UUID.randomUUID());
    orgTeam1.setTeamType(TeamType.ORGANISATION_GROUP);
    orgTeam1User1RoleManage = new TeamRole();
    orgTeam1User1RoleManage.setTeam(orgTeam1);
    orgTeam1User1RoleManage.setWuaId(user1WuaId);
    orgTeam1User1RoleManage.setRole(Role.TEAM_MANAGER);

    orgTeam2 = new Team(UUID.randomUUID());
    orgTeam2.setTeamType(TeamType.ORGANISATION_GROUP);
    orgTeam2User1RoleManage = new TeamRole();
    orgTeam2User1RoleManage.setTeam(orgTeam2);
    orgTeam2User1RoleManage.setWuaId(user1WuaId);
    orgTeam2User1RoleManage.setRole(Role.TEAM_MANAGER);

    user1 = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user1WuaId)
        .withTitle("Dr")
        .withForename("User")
        .withSurname("One")
        .withEmailAddress("user-one@example.com")
        .withPhoneNumber("1")
        .canLogin(true)
        .hasSharedAccount(false)
        .build();

    user2 = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(user2WuaId)
        .withTitle("Dr")
        .withForename("User")
        .withSurname("Two")
        .withEmailAddress("user-two@example.com")
        .withPhoneNumber("2")
        .canLogin(true)
        .hasSharedAccount(false)
        .build();

  }

  @Test
  void createScopedTeam() {
    var scopeRef = TeamScopeReference.from("1", "ORGANISATION_GROUP");

    teamManagementService.createScopedTeam("foo", TeamType.ORGANISATION_GROUP, scopeRef);

    verify(teamRepository).save(teamArgumentCaptor.capture());
    var newTeam = teamArgumentCaptor.getValue();

    assertThat(newTeam.getName()).isEqualTo("foo");
    assertThat(newTeam.getTeamType()).isEqualTo(TeamType.ORGANISATION_GROUP);
    assertThat(newTeam.getScopeType()).isEqualTo(scopeRef.getType());
    assertThat(newTeam.getScopeId()).isEqualTo(scopeRef.getId());
  }

  @Test
  void createScopedTeam_wrongType() {
    var scopeRef = TeamScopeReference.from("1", "ORGANISATION_GROUP");

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.REGULATOR, scopeRef));
    verify(teamRepository, never()).save(any());
  }

  @Test
  void createScopedTeam_alreadyExists() {
    var scopeRef = TeamScopeReference.from("1", "ORGANISATION_GROUP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.createScopedTeam("foo", TeamType.ORGANISATION_GROUP, scopeRef));

    verify(teamRepository, never()).save(any());
  }

  @Test
  void getTeamTypesUserIsMemberOf() {

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getTeamTypesUserIsMemberOf(user1WuaId))
        .containsExactlyInAnyOrder(TeamType.REGULATOR, TeamType.ORGANISATION_GROUP);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage, orgTeam2User1RoleManage));

    assertThat(teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.REGULATOR, user1WuaId))
        .hasValue(regTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage_notStatic() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserCanManage(TeamType.ORGANISATION_GROUP, user1WuaId));
  }

  @Test
  void getStaticTeamOfTypeUserCanManage_whenConsulteeTeamType_andThirdPartyAccessManagerRole_thenTeamReturned() {

    var consulteeTeamType = TeamType.CONSULTEE;

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of());

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    var expectedTeam = new Team();

    when(teamRepository.findByTeamType(consulteeTeamType))
        .thenReturn(List.of(expectedTeam));

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserCanManage(consulteeTeamType, user1WuaId);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getStaticTeamOfTypeUserCanManage_whenConsulteeTeamType_andNotThirdPartyAccessManagerRole_thenNoTeamReturned() {

    var consulteeTeamType = TeamType.CONSULTEE;

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of());

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserCanManage(consulteeTeamType, user1WuaId);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getScopedTeamOfTypeUserCanManage() {
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION_GROUP, user1WuaId))
        .containsExactlyInAnyOrder(orgTeam1);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_regulatorWithRoleCanManageAllOrgs() {
    // User has direct manage team role in reg team and org team 1
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of(regTeamUser1RoleManage, orgTeam1User1RoleManage));

    // User has the special create/manage any org team priv
    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    // There are 2 org teams
    when(teamRepository.findByTeamType(TeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(orgTeam1, orgTeam2));

    // Verify they can manage both org team 1 and 2
    assertThat(teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.ORGANISATION_GROUP, user1WuaId))
        .containsExactlyInAnyOrder(orgTeam1, orgTeam2);
  }

  @Test
  void getScopedTeamOfTypeUserCanManage_notScoped() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserCanManage(TeamType.REGULATOR, user1WuaId));
  }

  @Test
  void getTeam() {
    var uuid = UUID.randomUUID();
    when(teamRepository.findById(uuid))
        .thenReturn(Optional.of(regTeam));

    assertThat(teamManagementService.getTeam(uuid))
        .isEqualTo(Optional.of(regTeam));
  }

  @Test
  void getEnergyPortalUser() {
    teamManagementService.getEnergyPortalUser("foo");
    verify(energyPortalUserService).findUserByEmail(eq("foo"), any(RequestPurpose.class));
  }

  @Test
  void getTeamMemberView() {
    when(teamRoleRepository.findByWuaIdAndTeam(user1WuaId, regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage, regTeamUser1RoleOrgAdmin));

    var user = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(1)
        .withTitle("Dr")
        .withForename("Foo")
        .withSurname("Bar")
        .withEmailAddress("someone@example.com")
        .withPhoneNumber("012345678")
        .canLogin(true)
        .hasSharedAccount(false)
        .build();

    when(energyPortalUserService.findByWuaId(refEq(new WebUserAccountId(1)),any(RequestPurpose.class)))
        .thenReturn(Optional.of(user));

    var teamMemberView = teamManagementService.getTeamMemberView(regTeam, user1WuaId);

    assertThat(teamMemberView.wuaId()).isEqualTo(Long.valueOf(user.webUserAccountId()));
    assertThat(teamMemberView.title()).isEqualTo(user.title());
    assertThat(teamMemberView.forename()).isEqualTo(user.forename());
    assertThat(teamMemberView.surname()).isEqualTo(user.surname());
    assertThat(teamMemberView.email()).isEqualTo(user.emailAddress());
    assertThat(teamMemberView.telNo()).isEqualTo(user.telephoneNumber());
    assertThat(teamMemberView.teamId()).isEqualTo(regTeam.getId());
    assertThat(teamMemberView.roles()).containsExactlyInAnyOrder(regTeamUser1RoleManage.getRole(), regTeamUser1RoleOrgAdmin.getRole());
  }

  @Test
  void getTeamMemberViewsForTeam() {

    // the list returns roles not in the order declared in the TeamType enum
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleOrgAdmin, regTeamUser1RoleManage, regTeamUser2RoleOrgAdmin));

    when(energyPortalUserService.findByWuaIds(eq(Set.of(new WebUserAccountId(1), new WebUserAccountId(2))), any(RequestPurpose.class)))
        .thenReturn(List.of(user1, user2));

    var teamMemberViews = teamManagementService.getTeamMemberViewsForTeam(regTeam);

    assertThat(teamMemberViews)
        .extracting(
            TeamMemberView::wuaId,
            TeamMemberView::title,
            TeamMemberView::forename,
            TeamMemberView::surname,
            TeamMemberView::email,
            TeamMemberView::telNo,
            TeamMemberView::teamId,
            TeamMemberView::roles
        )
        .containsExactly(
            tuple(
                user1.webUserAccountId(),
                user1.title(),
                user1.forename(),
                user1.surname(),
                user1.emailAddress(),
                user1.telephoneNumber(),
                regTeam.getId(),
                List.of(regTeamUser1RoleManage.getRole(), regTeamUser1RoleOrgAdmin.getRole())
            ),
            tuple(
                user2.webUserAccountId(),
                user2.title(),
                user2.forename(),
                user2.surname(),
                user2.emailAddress(),
                user2.telephoneNumber(),
                regTeam.getId(),
                List.of(regTeamUser2RoleOrgAdmin.getRole())
            )
        );
  }

  @Test
  void getTeamMemberViewsForTeam_verifyCaseInsensitiveNameOrderOfMembers() {

    var firstUserByForename = EnergyPortalUserDtoTestUtil.Builder()
        .withForename("a")
        .withSurname("surname")
        .withWebUserAccountId(100)
        .build();

    var secondUserByForename = EnergyPortalUserDtoTestUtil.Builder()
        .withForename("B forename")
        .withSurname("a surname")
        .withWebUserAccountId(200)
        .build();

    var thirdUserBySurname = EnergyPortalUserDtoTestUtil.Builder()
        .withForename("B forename")
        .withSurname("B surname")
        .withWebUserAccountId(300)
        .build();

    when(energyPortalUserService.findByWuaIds(
        eq(Set.of(new WebUserAccountId(100), new WebUserAccountId(200), new WebUserAccountId(300))),
        any(RequestPurpose.class)
    ))
        .thenReturn(List.of(thirdUserBySurname, secondUserByForename, firstUserByForename));

    var teamRoleForFirstUser = new TeamRole();
    teamRoleForFirstUser.setWuaId(firstUserByForename.webUserAccountId());

    var teamRoleForSecondUser = new TeamRole();
    teamRoleForSecondUser.setWuaId(secondUserByForename.webUserAccountId());

    var teamRoleForThirdUser = new TeamRole();
    teamRoleForThirdUser.setWuaId(thirdUserBySurname.webUserAccountId());

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(teamRoleForFirstUser, teamRoleForSecondUser, teamRoleForThirdUser));

    var teamMemberViews = teamManagementService.getTeamMemberViewsForTeam(regTeam);

    assertThat(teamMemberViews)
        .extracting(TeamMemberView::wuaId)
        .containsExactly(
            firstUserByForename.webUserAccountId(),
            secondUserByForename.webUserAccountId(),
            thirdUserBySurname.webUserAccountId()
        );
  }

  @Test
  void setUserTeamRoles() {
    
    when(energyPortalUserService.findByWuaId(refEq(new WebUserAccountId(1)), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));
    
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage)); // Make doesTeamHaveTeamManager() check return true

    var instigatingUser = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail())
        .thenReturn(instigatingUser);

    teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_MANAGER, Role.THIRD_PARTY_TEAM_MANAGER));

    verify(teamRoleRepository).deleteByWuaIdAndTeam(user1WuaId, regTeam);
    verify(teamRoleRepository).saveAll(teamRoleListCaptor.capture());

    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getTeam)
        .contains(regTeam, regTeam);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getWuaId)
        .contains(user1WuaId, user1WuaId);
    assertThat(teamRoleListCaptor.getValue()).extracting(TeamRole::getRole)
        .contains(Role.TEAM_MANAGER, Role.THIRD_PARTY_TEAM_MANAGER);
  }

  @Test
  void setUserTeamRoles_noTeamManagerLeft() {
    
    when(energyPortalUserService.findByWuaId(refEq(new WebUserAccountId(1)), any(RequestPurpose.class)))
        .thenReturn(Optional.of(user1));

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of()); // Make doesTeamHaveTeamManager() check return false

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.THIRD_PARTY_TEAM_MANAGER)));
  }

  @Test
  void setUserTeamRoles_invalidRoles() {
    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.NOMINATION_EDITOR)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_noEpaUser() {
    when(energyPortalUserService.findByWuaId(refEq(new WebUserAccountId(1)), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_MANAGER, Role.THIRD_PARTY_TEAM_MANAGER)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_sharedAccount() {
    var epaUser = new User();
    epaUser.setIsAccountShared(true);

    when(energyPortalUserService.findByWuaId(refEq(new WebUserAccountId(1)), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_MANAGER, Role.THIRD_PARTY_TEAM_MANAGER)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void setUserTeamRoles_canNotLogin() {
    var epaUser = new User();
    epaUser.setCanLogin(false);

    when(energyPortalUserService.findByWuaId(refEq(new WebUserAccountId(1)), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.setUserTeamRoles(user1WuaId, regTeam, List.of(Role.TEAM_MANAGER, Role.THIRD_PARTY_TEAM_MANAGER)));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(any(), any());
    verify(teamRoleRepository, never()).saveAll(any());
  }

  @Test
  void removeUserFromTeam() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    when(userDetailService.getUserDetail()).thenReturn(ServiceUserDetailTestUtil.Builder().build());

    teamManagementService.removeUserFromTeam(user2WuaId, regTeam);
    verify(teamRoleRepository).deleteByWuaIdAndTeam(user2WuaId, regTeam);
  }

  @Test
  void removeUserFromTeam_lastTeamManager() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThatExceptionOfType(TeamManagementException.class)
        .isThrownBy(() -> teamManagementService.removeUserFromTeam(user1WuaId, regTeam));

    verify(teamRoleRepository, never()).deleteByWuaIdAndTeam(user1WuaId, regTeam);
  }

  @Test
  void removeUserFromTeam_whenUserInOtherTeams() {

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleOrgAdmin));

    // AND they have another role in another team
    when(teamRoleRepository.findAllByWuaId(user2WuaId))
        .thenReturn(List.of(regTeamUser2RoleOrgAdmin));

    var accessManagerRole = new TeamRole();
    accessManagerRole.setRole(Role.TEAM_MANAGER);
    accessManagerRole.setWuaId(20L);

    // AND an access manager still exists
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(accessManagerRole));

    teamManagementService.removeUserFromTeam(user2WuaId, regTeam);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(user2WuaId, regTeam);
    verify(energyPortalAccessService, never()).removeUserFromAccessTeam(any(), any(), any());
  }

  @Test
  void removeUserFromTeam_whenUserNotInOtherTeams() {

    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleOrgAdmin));

    // AND they do not have another role in another team
    when(teamRoleRepository.findAllByWuaId(user2WuaId))
        .thenReturn(List.of());

    var accessManagerRole = new TeamRole();
    accessManagerRole.setWuaId(10L);
    accessManagerRole.setRole(Role.TEAM_MANAGER);

    // AND an access manager still exists
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(accessManagerRole));

    var instigatingUser = ServiceUserDetailTestUtil.Builder().build();

    when(userDetailService.getUserDetail())
        .thenReturn(instigatingUser);

    teamManagementService.removeUserFromTeam(user2WuaId, regTeam);

    verify(teamRoleRepository).deleteByWuaIdAndTeam(user2WuaId, regTeam);
    verify(energyPortalAccessService).removeUserFromAccessTeam(
        refEq(new ResourceType("WIOS_ACCESS_TEAM")),
        refEq(new TargetWebUserAccountId(user2WuaId)),
        refEq(new InstigatingWebUserAccountId(instigatingUser.wuaId()))
    );
  }
  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, user2WuaId, List.of(Role.THIRD_PARTY_TEAM_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_newRolesIncludeManage() {
    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, user1WuaId, List.of(Role.TEAM_MANAGER, Role.THIRD_PARTY_TEAM_MANAGER)))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRoleUpdate_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRoleUpdate(regTeam, user1WuaId, List.of(Role.THIRD_PARTY_TEAM_MANAGER)))
        .isFalse();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, user2WuaId))
        .isTrue();
  }

  @Test
  void willManageTeamRoleBePresentAfterMemberRemoval_noManageRoleLeft() {
    when(teamRoleRepository.findByTeam(regTeam))
        .thenReturn(List.of(regTeamUser1RoleManage));

    assertThat(teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(regTeam, user1WuaId))
        .isFalse();
  }

  @Test
  void doesScopedTeamWithReferenceExist_existingTeam() {
    var scopeRef = TeamScopeReference.from("1", "ORGANISATION_GROUP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", "1"))
        .thenReturn(Optional.of(orgTeam1));

    assertThat(teamManagementService.doesScopedTeamWithReferenceExist(TeamType.ORGANISATION_GROUP, scopeRef))
        .isTrue();
  }

  @Test
  void doesScopedTeamWithReferenceExist_noExistingTeam() {
    var scopeRef = TeamScopeReference.from("1", "ORGANISATION_GROUP");

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.ORGANISATION_GROUP, "ORGANISATION_GROUP", "1"))
        .thenReturn(Optional.empty());

    assertThat(teamManagementService.doesScopedTeamWithReferenceExist(TeamType.ORGANISATION_GROUP, scopeRef))
        .isFalse();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(user1WuaId)).isTrue();
  }

  @Test
  void userCanManageAnyOrganisationTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyOrganisationTeam(user1WuaId)).isFalse();
  }

  @Test
  void userCanManageAnyConsulteeTeam_whenHasRole_thenTrue() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    assertThat(teamManagementService.userCanManageAnyConsulteeTeam(user1WuaId)).isTrue();
  }

  @Test
  void userCanManageAnyConsulteeTeam_whenNoRole_thenFalse() {

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    assertThat(teamManagementService.userCanManageAnyConsulteeTeam(user1WuaId)).isFalse();
  }

  @Test
  void isMemberOfTeam_whenMemberOfTeam_thenTrue() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, user1WuaId))
        .thenReturn(true);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, user1WuaId)).isTrue();
  }

  @Test
  void isMemberOfTeam_whenNotMemberOfTeam_thenFalse() {

    when(teamRoleRepository.existsByTeamAndWuaId(regTeam, user1WuaId))
        .thenReturn(false);

    assertThat(teamManagementService.isMemberOfTeam(regTeam, user1WuaId)).isFalse();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCanManageTeam_thenTrue() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    var teamRole = new TeamRole();
    teamRole.setTeam(scopedTeam);
    teamRole.setRole(Role.TEAM_MANAGER);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(scopedTeam, user1WuaId)).isTrue();
  }

  @Test
  void canManageTeam_whenScopedTeam_andCannotManageTeam_thenFalse() {

    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(scopedTeam, user1WuaId)).isFalse();
  }

  @Test
  void canManageTeam_whenOrganisationScopedTeam_andCannotManageTeam_andHasManageAnyOrganisationTeamRole_thenTrue() {

    // GIVEN a scoped organisation team
    var scopedTeam = new Team(UUID.randomUUID());
    scopedTeam.setTeamType(TeamType.ORGANISATION_GROUP);

    // AND the user doesn't have the manage team permission in that team
    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of());

    // WHEN the user has the CREATE_MANAGE_ANY_ORGANISATION_TEAM role in the regulator team
    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(TeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(scopedTeam));

    // THEN the user can manage the team
    assertThat(teamManagementService.canManageTeam(scopedTeam, user1WuaId)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCanManageTeam_thenTrue() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    var teamRole = new TeamRole();
    teamRole.setTeam(staticTeam);
    teamRole.setRole(Role.TEAM_MANAGER);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of(teamRole));

    assertThat(teamManagementService.canManageTeam(staticTeam, user1WuaId)).isTrue();
  }

  @Test
  void canManageTeam_whenStaticTeam_andCannotManageTeam_thenFalse() {

    var staticTeam = new Team((UUID.randomUUID()));
    staticTeam.setTeamType(TeamType.REGULATOR);

    when(teamRoleRepository.findByWuaIdAndRole(user1WuaId, Role.TEAM_MANAGER))
        .thenReturn(List.of());

    assertThat(teamManagementService.canManageTeam(staticTeam, user1WuaId)).isFalse();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenScopedTeamType_thenException() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;

    assertThatThrownBy(() -> teamManagementService.getStaticTeamOfTypeUserIsMemberOf(scopedTeamType, user1WuaId))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenNotMemberOfTeamOfType_thenEmptyOptional() {

    var staticTeamType = TeamType.REGULATOR;

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of());

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, user1WuaId);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenMemberOfTeamOfType_thenTeamReturned() {

    var staticTeamType = TeamType.REGULATOR;

    var expectedTeam = new Team(UUID.randomUUID());
    expectedTeam.setTeamType(staticTeamType);

    var teamRole = new TeamRole();
    teamRole.setTeam(expectedTeam);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(teamRole));

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, user1WuaId);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenConsulteeTeamType_andNotMemberOfTeam_andCanManageAnyConsulteeTeam_thenTeamReturned() {

    var staticTeamType = TeamType.CONSULTEE;

    var expectedTeam = new Team(UUID.randomUUID());
    expectedTeam.setTeamType(staticTeamType);

    // user is not in any team of this type
    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of());

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(staticTeamType))
        .thenReturn(List.of(expectedTeam));

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, user1WuaId);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getStaticTeamOfTypeUserIsMemberOf_whenConsulteeTeamType_andNotMemberOfTeam_andCannotManageAnyConsulteeTeam_thenNoTeamReturned() {

    var staticTeamType = TeamType.CONSULTEE;

    // user is not in any team of this type
    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of());

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(false);

    var resultingTeam = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(staticTeamType, user1WuaId);

    assertThat(resultingTeam).isEmpty();
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenStaticTeamType_thenException() {

    var staticTeamType = TeamType.REGULATOR;

    assertThatThrownBy(() -> teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(staticTeamType, user1WuaId))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserNotMemberOfAnyTeamOfType_thenEmptySetReturned() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of());

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams).isEmpty();
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserMemberOfTeamOfType_thenScopedTeamsReturned() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;

    var firstTeamOfType = new Team(UUID.randomUUID());
    firstTeamOfType.setTeamType(scopedTeamType);

    var firstRoleForFirstTeam = new TeamRole();
    firstRoleForFirstTeam.setTeam(firstTeamOfType);
    firstRoleForFirstTeam.setRole(Role.TEAM_MANAGER);

    var secondRoleForFirstTeam = new TeamRole();
    secondRoleForFirstTeam.setTeam(firstTeamOfType);
    secondRoleForFirstTeam.setRole(Role.THIRD_PARTY_TEAM_MANAGER);

    var secondTeamOfType = new Team(UUID.randomUUID());
    secondTeamOfType.setTeamType(scopedTeamType);

    var firstRoleForSecondTeam = new TeamRole();
    firstRoleForSecondTeam.setTeam(secondTeamOfType);
    firstRoleForSecondTeam.setRole(Role.TEAM_MANAGER);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(firstRoleForSecondTeam, firstRoleForFirstTeam, secondRoleForFirstTeam));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(firstTeamOfType, secondTeamOfType);
  }

  @Test
  void getScopedTeamsOfTypeUserIsMemberOf_whenUserHasManageAnyOrganisationTeamRole_thenAllOrganisationTeamsReturned() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;

    var teamUserIsMemberOf = new Team(UUID.randomUUID());
    teamUserIsMemberOf.setTeamType(scopedTeamType);

    var roleForTeamUserIsMemberOf = new TeamRole();
    roleForTeamUserIsMemberOf.setTeam(teamUserIsMemberOf);
    roleForTeamUserIsMemberOf.setRole(Role.TEAM_MANAGER);

    var teamUserIsNotMemberOf = new Team(UUID.randomUUID());
    teamUserIsNotMemberOf.setTeamType(scopedTeamType);

    when(teamRoleRepository.findAllByWuaId(user1WuaId))
        .thenReturn(List.of(roleForTeamUserIsMemberOf));

    when(teamQueryService.userHasStaticRole(user1WuaId, TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER))
        .thenReturn(true);

    when(teamRepository.findByTeamType(scopedTeamType))
        .thenReturn(List.of(teamUserIsNotMemberOf, teamUserIsMemberOf));

    var resultingScopedTeams = teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(scopedTeamType, user1WuaId);

    assertThat(resultingScopedTeams)
        .containsExactlyInAnyOrder(teamUserIsNotMemberOf, teamUserIsMemberOf);
  }

  @Test
  void getStaticTeamOfType_whenScopedType_thenException() {

    var scopedTeamType = TeamType.ORGANISATION_GROUP;

    assertThatThrownBy(() -> teamManagementService.getStaticTeamOfType(scopedTeamType))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getStaticTeamOfType_whenTeamExists() {

    var staticTeamType = TeamType.REGULATOR;

    var expectedTeam = new Team();

    when(teamRepository.findByTeamType(staticTeamType))
        .thenReturn(List.of(expectedTeam));

    var resultingTeam = teamManagementService.getStaticTeamOfType(staticTeamType);

    assertThat(resultingTeam).contains(expectedTeam);
  }

  @Test
  void getStaticTeamOfType_whenNoTeamExists_thenEmptyOptionalReturned() {

    var staticTeamType = TeamType.REGULATOR;

    when(teamRepository.findByTeamType(staticTeamType))
        .thenReturn(List.of());

    var resultingTeam = teamManagementService.getStaticTeamOfType(staticTeamType);

    assertThat(resultingTeam).isEmpty();
  }
}