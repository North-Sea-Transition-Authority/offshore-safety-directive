package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.IntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.WebUserAccountId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRoleUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Transactional
@IntegrationTest
class NominationWorkAreaQueryServiceIntegrationTest {

  @Autowired
  private DSLContext context;

  @Autowired
  private TeamMemberService teamMemberService;

  @Autowired
  private NominationWorkAreaQueryService nominationWorkAreaQueryService;

  @Autowired
  private TeamMemberRoleService teamMemberRoleService;

  @Autowired
  private TestEntityManager entityManager;

  @ParameterizedTest
  @EnumSource(NominationStatus.class)
  void getNominationDetailsForWorkArea_whenRoleCanOnlySeeSubmittedResults_thenOnlySubmittedResults(
      NominationStatus nominationStatus
  ) {

    // Roles to test status filter rule
    Set<TeamRole> userRoles = Set.of(RegulatorTeamRole.VIEW_NOMINATION);

    // Setup user
    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    // Ensure user is treated as currently logged in
    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    // Stub regulator team
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withId(null)
        .build();

    entityManager.persistAndFlush(team);

    // Add user to team with the previously defined roles
    teamMemberRoleService.updateUserTeamRoles(team, new WebUserAccountId(user.wuaId()),
        TeamRoleUtil.getRoleNames(userRoles));

    // Create and persist nomination details
    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    entityManager.persistAndFlush(nomination);

    var submittedNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(nominationStatus)
        .build();

    submittedNominationDetail = entityManager.persistAndFlush(submittedNominationDetail);

    var submittedApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(submittedNominationDetail)
        .withId(null)
        .build();

    entityManager.persistAndFlush(submittedApplicantDetails);

    var dtos = nominationWorkAreaQueryService.getWorkAreaItems();

    switch (nominationStatus) {
      case SUBMITTED -> assertThat(dtos)
          .map(result -> result.getNominationId().id())
          .containsExactly(submittedNominationDetail.getNomination().getId());
      default -> assertThat(dtos).isEmpty();
    }

  }

  @Test
  void getNominationDetailsForWorkArea_whenRoleCanSeeAllResults_thenAllResults() {

    // Roles to test status filter rule
    Set<TeamRole> userRoles = Set.of(RegulatorTeamRole.MANAGE_NOMINATION);

    // Setup user
    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    // Ensure user is treated as currently logged in
    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    // Stub regulator team
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withId(null)
        .build();

    entityManager.persistAndFlush(team);

    // Add user to team with the previously defined roles
    teamMemberRoleService.updateUserTeamRoles(team, new WebUserAccountId(user.wuaId()),
        TeamRoleUtil.getRoleNames(userRoles));

    // Create and persist nomination details
    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    entityManager.persistAndFlush(nomination);

    var submittedNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var draftNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(NominationStatus.DRAFT)
        .build();

    submittedNominationDetail = entityManager.persistAndFlush(submittedNominationDetail);
    draftNominationDetail = entityManager.persistAndFlush(draftNominationDetail);

    var draftApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(draftNominationDetail)
        .withId(null)
        .build();

    var submittedApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(submittedNominationDetail)
        .withId(null)
        .build();

    entityManager.persistAndFlush(draftApplicantDetails);
    entityManager.persistAndFlush(submittedApplicantDetails);

    var dtos = nominationWorkAreaQueryService.getWorkAreaItems();
    assertThat(dtos)
        .map(result -> result.getNominationId().id())
        .containsExactly(
            submittedNominationDetail.getNomination().getId(),
            draftNominationDetail.getNomination().getId()
        );

  }

  @ParameterizedTest
  @EnumSource(
      value = RegulatorTeamRole.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"MANAGE_NOMINATION", "VIEW_NOMINATION"}
  )
  void getNominationDetailsForWorkArea_whenRoleShouldNotHaveResults_thenNoResults(RegulatorTeamRole teamRole) {

    // Roles to test status filter rule
    Set<TeamRole> userRoles = Set.of(teamRole);

    // Setup user
    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    // Ensure user is treated as currently logged in
    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    // Stub regulator team
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withId(null)
        .build();

    entityManager.persistAndFlush(team);

    // Add user to team with the previously defined roles
    teamMemberRoleService.updateUserTeamRoles(team, new WebUserAccountId(user.wuaId()),
        TeamRoleUtil.getRoleNames(userRoles));

    // Create and persist nomination details
    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    entityManager.persistAndFlush(nomination);

    var submittedNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    var draftNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(NominationStatus.DRAFT)
        .build();

    submittedNominationDetail = entityManager.persistAndFlush(submittedNominationDetail);
    draftNominationDetail = entityManager.persistAndFlush(draftNominationDetail);

    var draftApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(draftNominationDetail)
        .withId(null)
        .build();

    var submittedApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(submittedNominationDetail)
        .withId(null)
        .build();

    entityManager.persistAndFlush(draftApplicantDetails);
    entityManager.persistAndFlush(submittedApplicantDetails);

    var dtos = nominationWorkAreaQueryService.getWorkAreaItems();
    assertThat(dtos).isEmpty();

  }

  @Test
  void getNominationDetailsForWorkArea_whenStatusIsDeleted_thenExcludedFromResults() {

    // Roles to test status filter rule
    Set<TeamRole> userRoles = Set.of(RegulatorTeamRole.MANAGE_NOMINATION);

    // Setup user
    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    // Ensure user is treated as currently logged in
    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    // Stub regulator team
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withId(null)
        .build();

    entityManager.persistAndFlush(team);

    // Add user to team with the previously defined roles
    teamMemberRoleService.updateUserTeamRoles(team, new WebUserAccountId(user.wuaId()),
        TeamRoleUtil.getRoleNames(userRoles));

    // Create and persist nomination details
    var nomination = NominationTestUtil.builder()
        .withId(null)
        .build();

    entityManager.persistAndFlush(nomination);

    var deletedNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(NominationStatus.DELETED)
        .build();

    var draftNominationDetail = NominationDetailTestUtil.builder()
        .withNomination(nomination)
        .withNominationId(new NominationId(nomination.getId()))
        .withId(null)
        .withStatus(NominationStatus.DRAFT)
        .build();

    deletedNominationDetail = entityManager.persistAndFlush(deletedNominationDetail);
    draftNominationDetail = entityManager.persistAndFlush(draftNominationDetail);

    var draftApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(draftNominationDetail)
        .withId(null)
        .build();

    var deletedApplicantDetails = ApplicantDetailTestUtil.builder()
        .withNominationDetail(deletedNominationDetail)
        .withId(null)
        .build();

    entityManager.persistAndFlush(draftApplicantDetails);
    entityManager.persistAndFlush(deletedApplicantDetails);

    var dtos = nominationWorkAreaQueryService.getWorkAreaItems();
    assertThat(dtos)
        .map(result -> result.getNominationId().id())
        .containsExactly(draftNominationDetail.getNomination().getId());

  }

}