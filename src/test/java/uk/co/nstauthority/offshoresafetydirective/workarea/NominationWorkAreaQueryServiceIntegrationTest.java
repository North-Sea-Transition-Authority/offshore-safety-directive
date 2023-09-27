package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberRoleTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.consultee.ConsulteeTeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Transactional
@DatabaseIntegrationTest
class NominationWorkAreaQueryServiceIntegrationTest {

  @Autowired
  DSLContext context;

  @Autowired
  TeamMemberService teamMemberService;

  @Autowired
  private NominationWorkAreaQueryService nominationWorkAreaQueryService;

  @Autowired
  TeamMemberRoleService teamMemberRoleService;

  @Autowired
  private EntityManager entityManager;

  @ParameterizedTest
  @MethodSource("getPostSubmissionStatuses")
  void getNominationDetailsForWorkArea_whenViewNominationUser_andSubmittedAndDraftNominationsExist_thenOnlySubmittedResults(
      NominationStatus nominationStatus
  ) {

    // GIVEN a user who has the VIEW_NOMINATION role in the regulator team
    var viewNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.VIEW_NOMINATION)
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(viewNominationUser)
        .setSecurityContext();

    // AND a submitted nomination exists

    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExists(postSubmissionNominationDetail);

    // AND a draft nomination exists

    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExists(preSubmissionNominationDetail);

    // WHEN we get the work area items for the view nomination user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are only shown the post submission nomination
    assertThat(workAreaItems)
        .extracting(nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id())
        .contains(postSubmissionNominationDetail.getNomination().getId());

    // AND not the pre submission nomination
    assertThat(workAreaItems)
        .extracting(nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id())
        .doesNotContain(preSubmissionNominationDetail.getNomination().getId());
  }

  @Test
  void getNominationDetailsForWorkArea_whenManageNominationUser_andSubmittedAndDraftNominationsExist_thenBothReturned() {

    // GIVEN a user who has the MANAGE_NOMINATION role in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND a submitted nomination exists

    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(postSubmissionNominationDetail);

    // AND a draft nomination exists

    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExists(preSubmissionNominationDetail);

    // WHEN we get the work area items for the manage nomination user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are shows both submitted and draft
    assertThat(workAreaItems)
        .extracting(nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id())
        .contains(
            postSubmissionNominationDetail.getNomination().getId(),
            preSubmissionNominationDetail.getNomination().getId()
        );
  }

  @ParameterizedTest
  @EnumSource(
      value = RegulatorTeamRole.class,
      mode = EnumSource.Mode.EXCLUDE,
      names = {"MANAGE_NOMINATION", "VIEW_NOMINATION"}
  )
  void getNominationDetailsForWorkArea_whenUserHasNotAllowingAccessToNominations_thenEmptyWorkArea(RegulatorTeamRole teamRole) {

    // GIVEN a user who has a role which doesn't allow access to nominations in the work area
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(teamRole)
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND a submitted nomination exists

    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(postSubmissionNominationDetail);

    // AND a draft nomination exists

    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExists(preSubmissionNominationDetail);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are not shown any results
    assertThat(workAreaItems).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(ConsulteeTeamRole.class)
  void getNominationDetailsForWorkArea_whenUserHasNotAllowingAccessToNominations_thenEmptyWorkArea(ConsulteeTeamRole teamRole) {

    // GIVEN a user who has a role which doesn't allow access to nominations in the work area
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.CONSULTEE,
        Collections.singleton(teamRole)
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND a submitted nomination exists

    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(postSubmissionNominationDetail);

    // AND a draft nomination exists

    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExists(preSubmissionNominationDetail);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are not shown any results
    assertThat(workAreaItems).isEmpty();
  }

  @Test
  void getNominationDetailsForWorkArea_whenNominationWithDeletedStatusExist_thenDeletedNominationExcludedFromResults() {

    // GIVEN a user who has a role which allows them to see nominations
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND a deleted nomination exists

    var deletedNomination = givenNominationExists();

    var deletedNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(deletedNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DELETED)
        .build();

    givenNominationDetailExists(deletedNominationDetail);

    // AND a submitted nomination exists

    var submittedNomination = givenNominationExists();

    var submittedNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(submittedNomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(submittedNominationDetail);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the submitted nomination is not shown in the work area
    assertThat(workAreaItems)
        .extracting(nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id())
        .contains(submittedNominationDetail.getNomination().getId());

    // AND the deleted nomination is not shown in the work area
    assertThat(workAreaItems)
        .extracting(nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id())
        .doesNotContain(deletedNominationDetail.getNomination().getId());
  }

  @ParameterizedTest
  @MethodSource("getPostSubmissionStatuses")
  void getNominationDetailsForWorkArea_whenMultipleSubmittedNominationVersion_thenLatestNominationDetailIsReturned(NominationStatus nominationStatus) {

    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has two submitted versions

    var nomination = givenNominationExists();

    var firstNominationDetailVersion = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(firstNominationDetailVersion);

    var secondNominationDetailVersion = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(2)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExists(secondNominationDetailVersion);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN only the latest version of the nomination is returned
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationId().id(),
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationVersion().version()
        )
        .contains(
            tuple(nomination.getId(), 2)
        );

    // AND the previous version of the nomination is not returned
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationId().id(),
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationVersion().version()
        )
        .doesNotContain(
            tuple(nomination.getId(), 1)
        );
  }

  @ParameterizedTest
  @MethodSource("getPreSubmissionStatuses")
  void getNominationDetailsForWorkArea_whenMultipleNominationVersions_andLatestIsPreSubmission_thenLatestSubmittedNominationDetailIsReturned(NominationStatus nominationStatus) {

    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has a submitted version

    var nomination = givenNominationExists();

    var firstSubmittedNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(firstSubmittedNominationDetail);

    // AND for the same nomination there exists a pre submission version
    var secondPreSubmissionNominationDetailVersion = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(2)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExists(secondPreSubmissionNominationDetailVersion);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the latest post submitted version of the nomination is returned
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationId().id(),
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationVersion().version()
        )
        .contains(
            tuple(nomination.getId(), 1)
        );

    // AND the latest version of the nomination is not returned
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationId().id(),
            nominationWorkAreaItem -> nominationWorkAreaItem.getNominationVersion().version()
        )
        .doesNotContain(
            tuple(nomination.getId(), 2)
        );
  }

  @Test
  void getNominationDetailsForWorkArea_hasUpdateRequest() {
    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has a submitted version

    var nomination = givenNominationExists();

    var nominationDetailVersion = 1;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(nominationDetailVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(nominationDetail);

    // AND there is a case event for the nomination and version of type UPDATE_REQUESTED
    var updateRequestCaseEvent = CaseEventTestUtil.builder()
        .withUuid(null)
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .withNomination(nomination)
        .withNominationVersion(nominationDetailVersion)
        .build();

    persistAndFlush(updateRequestCaseEvent);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the item should have an update request
    assertThat(workAreaItems).hasSize(1);
    assertTrue(workAreaItems.get(0).getNominationHasUpdateRequest().value());
  }

  @Test
  void getNominationDetailsForWorkArea_hasNoUpdateRequest() {
    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has a submitted version
    var nomination = givenNominationExists();

    var nominationDetailVersion = 1;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(nominationDetailVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(nominationDetail);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the item should not have an update request
    assertThat(workAreaItems).hasSize(1);
    assertFalse(workAreaItems.get(0).getNominationHasUpdateRequest().value());
  }

  @Test
  void getNominationDetailsForWorkArea_whenOutstandingUpdateRequestAndNominationStatusIsWithdrawn_thenNoUpdateRequestFlag() {
    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has been withdrawn
    var nomination = givenNominationExists();

    var nominationDetailVersion = 1;
    var nominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(nominationDetailVersion)
        .withStatus(NominationStatus.WITHDRAWN)
        .build();

    givenNominationDetailExists(nominationDetail);

    // AND there is a case event for the nomination and version of type UPDATE_REQUESTED
    var updateRequestCaseEvent = CaseEventTestUtil.builder()
        .withUuid(null)
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .withNomination(nomination)
        .withNominationVersion(nominationDetailVersion)
        .build();

    persistAndFlush(updateRequestCaseEvent);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the item should not have an update request
    assertThat(workAreaItems).hasSize(1);
    assertFalse(workAreaItems.get(0).getNominationHasUpdateRequest().value());
  }

  @Test
  void getNominationDetailsForWorkArea_hasUpdateRequestOnOlderSubmittedVersion_thenShouldNotHaveUpdateRequest() {

    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(RegulatorTeamRole.MANAGE_NOMINATION)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has a submitted version

    var nomination = givenNominationExists();

    var firstNominationDetailVersion = 1;
    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(firstNominationDetailVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(firstNominationDetail);

    var secondNominationDetailVersion = 2;
    var secondNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(secondNominationDetailVersion)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(secondNominationDetail);

    // AND there exists an update request for an older version of the nomination
    var updateRequestCaseEvent = CaseEventTestUtil.builder()
        .withUuid(null)
        .withCaseEventType(CaseEventType.UPDATE_REQUESTED)
        .withNomination(nomination)
        .withNominationVersion(firstNominationDetailVersion)
        .build();

    persistAndFlush(updateRequestCaseEvent);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the item should not have an update request
    assertThat(workAreaItems).hasSize(1);
    assertFalse(workAreaItems.get(0).getNominationHasUpdateRequest().value());
  }

  private ServiceUserDetail givenUserExistsInTeamWithRoles(TeamType teamType, Set<TeamRole> teamRoles) {

    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    var regulatorTeam = TeamTestUtil.Builder()
        .withId(null)
        .withTeamType(teamType)
        .build();

    persistAndFlush(regulatorTeam);

    teamRoles.forEach(teamRole -> {
      var teamMemberRole = TeamMemberRoleTestUtil.Builder()
          .withUuid(null)
          .withTeam(regulatorTeam)
          .withRole(teamRole.name())
          .withWebUserAccountId(user.wuaId())
          .build();

      persistAndFlush(teamMemberRole);
    });

    return user;
  }

  private Nomination givenNominationExists() {

    var nomination = NominationTestUtil.builder()
        .withId(null)
        .withReference("reference %s".formatted(UUID.randomUUID()))
        .build();

    persistAndFlush(nomination);

    return nomination;
  }

  private void givenNominationDetailExists(NominationDetail nominationDetail) {

    persistAndFlush(nominationDetail);

    var applicantDetailForNominationDetail = ApplicantDetailTestUtil.builder()
        .withId(null)
        .withNominationDetail(nominationDetail)
        .build();

    persistAndFlush(applicantDetailForNominationDetail);
  }

  private static Stream<Arguments> getPreSubmissionStatuses() {
    return getSubmissionStatusArguments(NominationStatusSubmissionStage.PRE_SUBMISSION);
  }

  private static Stream<Arguments> getPostSubmissionStatuses() {
    return getSubmissionStatusArguments(NominationStatusSubmissionStage.POST_SUBMISSION);
  }

  private static Stream<Arguments> getSubmissionStatusArguments(NominationStatusSubmissionStage submissionStage) {
    return NominationStatus.getAllStatusesForSubmissionStage(submissionStage)
        .stream()
        .map(Arguments::of);
  }

  private void persistAndFlush(Object entity) {
    entityManager.persist(entity);
    entityManager.flush();
  }

}