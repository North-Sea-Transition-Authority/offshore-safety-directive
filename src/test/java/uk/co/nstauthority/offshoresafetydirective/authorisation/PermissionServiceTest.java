package uk.co.nstauthority.offshoresafetydirective.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private ApplicantDetailPersistenceService applicantDetailPersistenceService;

  @Mock
  private PortalOrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private TeamScopeService teamScopeService;

  @InjectMocks
  private PermissionService permissionService;

  @Test
  void hasPermission_whenTeamMemberNull_thenFalse() {
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(null);
    assertFalse(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasPermission_whenTeamMemberEmpty_thenFalse() {
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.emptyList());
    assertFalse(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasPermission_whenTeamMemberWithNoMatchingPermission_thenFalse() {

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.NON_CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertFalse(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasPermission_whenTeamMemberWithMatchingPermission_thenTrue() {

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertTrue(permissionService.hasPermission(USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberNull_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(null);
    assertFalse(
        permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberEmpty_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(Collections.emptyList());
    assertFalse(
        permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberWithNoMatchingPermission_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withRole(TestTeamRole.NON_CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertFalse(
        permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberWithMatchingPermission_thenTrue() {
    var team = TeamTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withTeamId(team.toTeamId())
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertTrue(permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void hasTeamPermission_whenTeamMemberWithMatchingPermission_butInDifferentTeam_thenFalse() {
    var team = TeamTestUtil.Builder().build();
    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.CREATE_NOMINATION_ROLE)
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMember));

    assertFalse(
        permissionService.hasPermissionForTeam(team.toTeamId(), USER, Set.of(RolePermission.CREATE_NOMINATION)));
  }

  @Test
  void getTeamTypePermissionMap_whenInMultipleTeams() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var regulatorTeamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.VIEW_NOMINATION)
        .withRole(RegulatorTeamRole.MANAGE_NOMINATION)
        .build();

    var industryTeamMember = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(regulatorTeamMember, industryTeamMember));

    var result = permissionService.getTeamTypePermissionMap(user);

    assertThat(result)
        .containsOnlyKeys(TeamType.REGULATOR, TeamType.INDUSTRY)
        .containsEntry(TeamType.REGULATOR, Set.of(
            RolePermission.VIEW_ALL_NOMINATIONS,
            RolePermission.MANAGE_NOMINATIONS
        ))
        .containsEntry(TeamType.INDUSTRY, Set.of(RolePermission.GRANT_ROLES));
  }

  @Test
  void getTeamTypePermissionMap_whenNotInAnyTeams() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of());

    var result = permissionService.getTeamTypePermissionMap(user);

    assertThat(result).isEmpty();
  }

  @Test
  void hasPermissionForNomination_whenNoApplicantDetail_thenNotFoundException() {
    var nominationDetail = NominationDetailTestUtil.builder().build();
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.empty());

    var permission = Collections.singleton(RolePermission.CREATE_NOMINATION);

    assertThatThrownBy(() -> permissionService.hasPermissionForNomination(nominationDetail, USER, permission))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("No applicant detail found for nomination detail with id %s".formatted(nominationDetail.getId().toString()));
  }

  @Test
  void hasPermissionForNomination_whenNoMatchingPermission_thenFalse() {
    var portalId = 123;

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withPortalOrganisationId(portalId)
        .build();
    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    var team = TeamTestUtil.Builder().build();

    var teamMemberWithEditPermission = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_EDITOR)
        .withTeamId(team.toTeamId())
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMemberWithEditPermission));

    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();
    var organisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(portalId)
        .withOrganisation(organisationDto)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationId(
        eq(applicantDetail.getPortalOrganisationId()),
        any()
    )).thenReturn(Set.of(organisationGroupDto));

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .build();

    when(teamScopeService.getTeamScope(
        List.of(String.valueOf(portalId)),
        PortalTeamType.ORGANISATION_GROUP)
    ).thenReturn(List.of(teamScope));

    var hasPermissionForNomination = permissionService.hasPermissionForNomination(
        nominationDetail,
        USER,
        Collections.singleton(RolePermission.CREATE_NOMINATION));

    assertFalse(hasPermissionForNomination);
  }

  @Test
  void hasPermissionForNomination_whenMatchingPermissionForDifferentOrganisationGroup_thenFalse() {
    var portalId = 123;

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withPortalOrganisationId(portalId)
        .build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    var team = TeamTestUtil.Builder().build();

    var teamMemberWithEditPermission = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(new TeamId(UUID.randomUUID()))
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMemberWithEditPermission));

    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();
    var organisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(portalId)
        .withOrganisation(organisationDto)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationId(
        eq(portalId),
        any()
    )).thenReturn(Set.of(organisationGroupDto));

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .build();

    when(teamScopeService.getTeamScope(
        List.of(String.valueOf(portalId)),
        PortalTeamType.ORGANISATION_GROUP)
    ).thenReturn(List.of(teamScope));

    var hasPermissionForNomination = permissionService.hasPermissionForNomination(
        nominationDetail,
        USER,
        Collections.singleton(RolePermission.CREATE_NOMINATION));

    assertFalse(hasPermissionForNomination);
  }

  @Test
  void hasPermissionForNomination_whenMatchingPermission_thenTrue() {
    var portalId = 123;

    var nominationDetail = NominationDetailTestUtil.builder().build();
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withPortalOrganisationId(portalId)
        .build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail))
        .thenReturn(Optional.of(applicantDetail));

    var team = TeamTestUtil.Builder().build();

    var teamMemberWithEditPermission = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(team.toTeamId())
        .build();

    when(teamMemberService.getUserAsTeamMembers(USER)).thenReturn(List.of(teamMemberWithEditPermission));

    var organisationDto = PortalOrganisationDtoTestUtil.builder().build();
    var portalOrganisationGroupDto = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(portalId)
        .withOrganisation(organisationDto)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationId(
        eq(applicantDetail.getPortalOrganisationId()),
        any()
    )).thenReturn(Set.of(portalOrganisationGroupDto));

    var teamScope = TeamScopeTestUtil.builder()
        .withTeam(team)
        .build();

    when(teamScopeService.getTeamScope(
        List.of(String.valueOf(portalId)),
        PortalTeamType.ORGANISATION_GROUP)
    ).thenReturn(List.of(teamScope));

    var hasPermissionForNomination = permissionService.hasPermissionForNomination(
        nominationDetail,
        USER,
        Collections.singleton(RolePermission.CREATE_NOMINATION));

    assertTrue(hasPermissionForNomination);
  }

  enum TestTeamRole implements TeamRole {

    CREATE_NOMINATION_ROLE(RolePermission.CREATE_NOMINATION),
    NON_CREATE_NOMINATION_ROLE(RolePermission.VIEW_ALL_NOMINATIONS);

    private final RolePermission rolePermission;

    TestTeamRole(RolePermission rolePermission) {
      this.rolePermission = rolePermission;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public int getDisplayOrder() {
      return 0;
    }

    @Override
    public String getScreenDisplayText() {
      return null;
    }

    @Override
    public Set<RolePermission> getRolePermissions() {
      return Set.of(rolePermission);
    }
  }

}