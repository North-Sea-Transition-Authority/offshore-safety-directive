package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.DatabaseIntegrationTest;
import uk.co.nstauthority.offshoresafetydirective.authentication.SamlAuthenticationUtil;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.metrics.MetricsProvider;
import uk.co.nstauthority.offshoresafetydirective.nomination.Nomination;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSubmissionStage;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.caseevents.CaseEventType;
import uk.co.nstauthority.offshoresafetydirective.nomination.nomineedetail.NomineeDetailTestingUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Transactional
@DatabaseIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class NominationWorkAreaQueryServiceIntegrationTest {

  @Autowired
  DSLContext context;

  @Autowired
  private NominationWorkAreaQueryService nominationWorkAreaQueryService;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private MetricsProvider metricsProvider;

  @MockitoBean
  private PortalOrganisationGroupQueryService organisationGroupQueryService;

  @Test
  @Order(1)
  void getNominationDetailsForWorkArea_verifyTimerForMetricsProvider() {
    assertThat(metricsProvider.getWorkAreaQueryTimer().totalTime(TimeUnit.MILLISECONDS)).isEqualTo(0.0);

    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
    );

    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    nominationWorkAreaQueryService.getWorkAreaItems();

    assertThat(metricsProvider.getWorkAreaQueryTimer().totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0.0);
  }

  @ParameterizedTest
  @MethodSource("getSubmissionStatusArgumentsLinkedToRegulatorTeamRole")
  void getNominationDetailsForWorkArea_whenRegulator_andCanAccessNominations_andSubmittedAndDraftNominationsExist_thenOnlySubmittedResults(
      NominationStatus nominationStatus, Role regulatorTeamRole
  ) {

    // GIVEN a user who has the manage or view nomination role in the regulator team
    var viewNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(regulatorTeamRole)
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
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .contains(tuple(postSubmissionNominationDetail.getNomination().getId(), nominationStatus));

    // AND not the pre submission nomination
    assertThat(workAreaItems)
        .extracting(nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id())
        .doesNotContain(preSubmissionNominationDetail.getNomination().getId());
  }

  @ParameterizedTest
  @MethodSource("getRegulatorRolesWithNoNominationAccess")
  void getNominationDetailsForWorkArea_whenRegulatorHasNotAllowingAccessToNominations_thenEmptyWorkArea(Role role) {

    // GIVEN a regulator user who doesn't have access to nominations in the work area
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(role)
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

  private static Stream<Arguments> getRegulatorRolesWithNoNominationAccess() {
    return TeamType.REGULATOR.getAllowedRoles()
        .stream()
        .filter(role -> !Set.of(Role.NOMINATION_MANAGER, Role.VIEW_ANY_NOMINATION).contains(role))
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("getConsulteeRolesWithNoNominationAccess")
  void getNominationDetailsForWorkArea_whenConsulteeHasNotAllowingAccessToNominations_thenEmptyWorkArea(Role role) {

    // GIVEN a consultee user who doesn't have access to nominations in the work area
    var user = givenUserExistsInStaticTeamWithRoles(
        TeamType.CONSULTEE,
        Collections.singleton(role)
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(user)
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

  private static Stream<Arguments> getConsulteeRolesWithNoNominationAccess() {
    return TeamType.CONSULTEE.getAllowedRoles()
        .stream()
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("getSubmissionStatusArgumentsLinkedToIndustryTeamRole")
  void getNominationDetailsForWorkArea_whenIndustry_andCanEditNominations_andSubmittedAndDraftNominationsExist_thenBothReturned(
      NominationStatus nominationStatus, Role industryTeamRole
  ) {
    var applicantOrgGroupId = 100;
    var applicantOrgId = 10;

    // GIVEN a user who has an edit nomination permission in the industry team
    var viewNominationUser = givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        Collections.singleton(industryTeamRole),
        applicantOrgGroupId
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(viewNominationUser)
        .setSecurityContext();

    // AND their industry organisation group exists on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrgId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        Set.of(applicantOrgGroupId),
        NominationWorkAreaQueryService.ORGANISATION_GROUP_REQUEST_PURPOSE)
    ).thenReturn(List.of(organisationGroup));

    // AND a submitted nomination exists for their industry team

    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetail, applicantOrgId);

    // AND a draft nomination exists for their industry team
    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetail, applicantOrgId);

    // AND a pre submission nomination exists for a different industry team
    var otherApplicantOrgGroupId = 200;

    var preSubmissionNominationForOtherIndustryTeam = givenNominationExists();

    var preSubmissionNominationDetailForOtherIndustryTeam = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNominationForOtherIndustryTeam)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetailForOtherIndustryTeam, otherApplicantOrgGroupId);

    // WHEN we get the work area items for the view nomination user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are shown both pre submission and post submission nominations for their industry team
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .containsExactlyInAnyOrder(
            tuple(preSubmissionNominationDetail.getNomination().getId(), NominationStatus.DRAFT),
            tuple(postSubmissionNominationDetail.getNomination().getId(), nominationStatus)
        );

    // AND not the nomination for the other industry team
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .doesNotContain(
            tuple(preSubmissionNominationDetailForOtherIndustryTeam.getNomination().getId(), NominationStatus.DRAFT));
  }

  @ParameterizedTest
  @MethodSource("getPostSubmissionStatuses")
  void getNominationDetailsForWorkArea_whenIndustry_andCanViewNominations_andSubmittedAndDraftNominationsExist_thenOnlySubmittedReturned(
      NominationStatus nominationStatus
  ) {
    var applicantOrgGroupId = 100;
    var applicantOrgId = 10;

    // GIVEN a user who has the view nomination role in the industry team
    var viewNominationUser = givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        Collections.singleton(Role.NOMINATION_VIEWER),
        applicantOrgGroupId
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(viewNominationUser)
        .setSecurityContext();

    // AND their industry organisation group exists on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrgId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        Set.of(applicantOrgGroupId),
        NominationWorkAreaQueryService.ORGANISATION_GROUP_REQUEST_PURPOSE)
    ).thenReturn(List.of(organisationGroup));

    // AND a submitted nomination exists for their industry team
    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetail, applicantOrgId);

    // AND a draft nomination exists for their industry team

    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetail, applicantOrgId);

    // AND a pre submission nomination exists for a different industry team
    var otherApplicantOrgGroupId = 200;

    var nominationForOtherIndustryTeam = givenNominationExists();

    var nominationDetailForOtherIndustryTeam = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nominationForOtherIndustryTeam)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(nominationDetailForOtherIndustryTeam,
        otherApplicantOrgGroupId);

    // WHEN we get the work area items for the view nomination user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are only shown the post submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .containsExactlyInAnyOrder(tuple(postSubmissionNominationDetail.getNomination().getId(), nominationStatus));

    // AND not the pre submission nomination or the nomination for the other team
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .doesNotContain(
            tuple(preSubmissionNominationDetail.getNomination().getId(), NominationStatus.DRAFT),
            tuple(nominationDetailForOtherIndustryTeam.getNomination().getId(), nominationStatus));
  }

  @ParameterizedTest
  @MethodSource("getOrganisationGroupRolesWithNoNominationAccess")
  void getNominationDetailsForWorkArea_whenIndustryHasNotAllowingAccessToNominations_thenEmptyWorkArea(Role role) {
    var applicantOrgGroupId = 100;

    // GIVEN a industry user who doesn't have access to nominations in the work area
    var noNominationAccessUser = givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        Collections.singleton(role),
        applicantOrgGroupId
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(noNominationAccessUser)
        .setSecurityContext();

    // AND a submitted nomination exists for their industry team

    var postSubmissionNomination = givenNominationExists();

    var postSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNomination)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetail, applicantOrgGroupId);

    // AND a draft nomination exists for their industry team
    var preSubmissionNomination = givenNominationExists();

    var preSubmissionNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetail, applicantOrgGroupId);

    // AND a nomination exists for a different industry team
    var applicantOrgIdForOtherIndustryTeam = 200;

    var nominationForOtherIndustryTeam = givenNominationExists();

    var nominationDetailForOtherIndustryTeam = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nominationForOtherIndustryTeam)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(nominationDetailForOtherIndustryTeam,
        applicantOrgIdForOtherIndustryTeam);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are not shown any results
    assertThat(workAreaItems).isEmpty();
  }

  private static Stream<Arguments> getOrganisationGroupRolesWithNoNominationAccess() {
    return TeamType.ORGANISATION_GROUP.getAllowedRoles()
        .stream()
        .filter(role -> !Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER).contains(role))
        .map(Arguments::of);
  }


  @ParameterizedTest
  @MethodSource("getSubmissionStatusArgumentsLinkedToRegulatorAndIndustryTeamRoles")
  void getNominationDetailsForWorkArea_whenIndustryAndRegulator_andCanViewRegulatorNominations_andCanSubmitIndustryNominations_thenBothReturned(
      NominationStatus nominationStatus, Role regulatorTeamRole, Role industryTeamRole
  ) {
    var applicantOrgGroupId = 100;
    var applicantId = 10;

    // GIVEN a regulator user who has access to nominations in the work area
    var multipleTeamsUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(regulatorTeamRole)
    );

    // AND the user is also in an industry team with edit nomination permission
    givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        multipleTeamsUser,
        Collections.singleton(industryTeamRole),
        applicantOrgGroupId
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(multipleTeamsUser)
        .setSecurityContext();

    // AND their industry organisation group exists on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        Set.of(applicantOrgGroupId),
        NominationWorkAreaQueryService.ORGANISATION_GROUP_REQUEST_PURPOSE)
    ).thenReturn(List.of(organisationGroup));

    // AND a submitted nomination exists for their industry team
    var postSubmissionIndustryNomination = givenNominationExists();

    var postSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionIndustryNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionIndustryNominationDetail, applicantId);

    // AND a draft nomination exists for their industry team
    var preSubmissionIndustryNomination = givenNominationExists();

    var preSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionIndustryNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionIndustryNominationDetail, applicantId);

    // AND a post submission nomination exists for a different industry team
    var otherApplicantOrgId = 200;

    var postSubmissionNominationForOtherIndustryTeam = givenNominationExists();

    var postSubmissionNominationDetailForOtherIndustryTeam = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNominationForOtherIndustryTeam)
        .withStatus(nominationStatus)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetailForOtherIndustryTeam, otherApplicantOrgId);

    // AND a pre submission nomination exists for a different industry team
    var preSubmissionNominationForOtherIndustryTeam = givenNominationExists();

    var preSubmissionNominationDetailForOtherIndustryTeam = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNominationForOtherIndustryTeam)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetailForOtherIndustryTeam, otherApplicantOrgId);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are only shown the post submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .containsExactlyInAnyOrder(
            tuple(preSubmissionIndustryNominationDetail.getNomination().getId(), NominationStatus.DRAFT),
            tuple(postSubmissionIndustryNominationDetail.getNomination().getId(), nominationStatus),
            tuple(postSubmissionNominationDetailForOtherIndustryTeam.getNomination().getId(), nominationStatus)
        );

    // AND not the pre submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .doesNotContain(
            tuple(preSubmissionNominationDetailForOtherIndustryTeam.getNomination().getId(), NominationStatus.DRAFT));
  }

  @ParameterizedTest
  @MethodSource("getSubmissionStatusArgumentsLinkedToConsulteeAndIndustryTeamRoles")
  void getNominationDetailsForWorkArea_whenIndustryAndConsultee_andCanSubmitIndustryNominations_thenOnlyIndustryTeamReturned(
      NominationStatus nominationStatus, Role consulteeTeamRole, Role industryTeamRole
  ) {
    var applicantOrgGroupId = 100;
    var applicantId = 10;

    // GIVEN a consultee user who doesn't have access to nominations in the work area
    var multipleTeamsUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.CONSULTEE,
        Collections.singleton(consulteeTeamRole)
    );

    // AND the user is also in an industry team with edit nomination permission
    givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        multipleTeamsUser,
        Collections.singleton(industryTeamRole),
        applicantOrgGroupId
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(multipleTeamsUser)
        .setSecurityContext();

    // AND their industry organisation group exists on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        Set.of(applicantOrgGroupId),
        NominationWorkAreaQueryService.ORGANISATION_GROUP_REQUEST_PURPOSE)
    ).thenReturn(List.of(organisationGroup));

    // AND a submitted nomination exists for their industry team
    var postSubmissionIndustryNomination = givenNominationExists();

    var postSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionIndustryNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionIndustryNominationDetail, applicantId);

    // AND a submitted nomination exists for their industry team
    var preSubmissionIndustryNomination = givenNominationExists();

    var preSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionIndustryNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionIndustryNominationDetail, applicantId);

    // AND a post submission nomination exists for a different industry team
    var otherApplicantOrganisationId = 200;

    var postSubmissionNominationForOtherOrgGroup = givenNominationExists();

    var postSubmissionNominationDetailForOtherOrgGroup = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNominationForOtherOrgGroup)
        .withStatus(nominationStatus)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetailForOtherOrgGroup, otherApplicantOrganisationId);

    // AND a pre submission nomination exists for a different industry team

    var preSubmissionNominationForOtherOrgGroup = givenNominationExists();

    var preSubmissionNominationDetailForOtherOrgGroup = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNominationForOtherOrgGroup)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetailForOtherOrgGroup, otherApplicantOrganisationId);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are only shown the post submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .containsExactlyInAnyOrder(
            tuple(preSubmissionIndustryNominationDetail.getNomination().getId(), NominationStatus.DRAFT),
            tuple(postSubmissionIndustryNominationDetail.getNomination().getId(), nominationStatus)
        );

    // AND not the pre submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .doesNotContain(
            tuple(preSubmissionNominationDetailForOtherOrgGroup.getNomination().getId(), NominationStatus.DRAFT),
            tuple(postSubmissionNominationDetailForOtherOrgGroup.getNomination().getId(), nominationStatus)
        );
  }

  @ParameterizedTest
  @MethodSource("getSubmissionStatusArgumentsLinkedToIndustryTeamRole")
  void getNominationDetailsForWorkArea_whenMultipleIndustryTeams_andDifferentPermissionsForTeams_thenNominationsFilteredOnTeamPermissions(
      NominationStatus nominationStatus, Role industryTeamRole
  ) {
    var applicantOrgGroupId = 100;
    var otherApplicantOrgGroupId = 200;

    var applicantOrgId = 10;
    var otherApplicantOrgId = 20;

    // GIVEN industry user who can edit nominations
    var multipleTeamsUser = givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        Collections.singleton(industryTeamRole),
        applicantOrgGroupId
    );

    // AND is a nomination viewer in a different industry team
    givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        multipleTeamsUser,
        Collections.singleton(Role.NOMINATION_VIEWER),
        otherApplicantOrgGroupId
    );

    // AND their industry organisation groups exist on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrgId)
        .build();
    var organisationUnitForOtherIndustryTeam = PortalOrganisationDtoTestUtil.builder()
        .withId(otherApplicantOrgId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    var organisationGroupForOtherIndustryTeam = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(otherApplicantOrgGroupId)
        .withOrganisation(organisationUnitForOtherIndustryTeam)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
            argThat(orgGroupIds -> orgGroupIds.containsAll(Set.of(applicantOrgGroupId, otherApplicantOrgGroupId))),
            any(RequestPurpose.class)
        ))
        .thenReturn(List.of(organisationGroup, organisationGroupForOtherIndustryTeam));

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(multipleTeamsUser)
        .setSecurityContext();

    // AND a submitted nomination exists for industry team

    var postSubmissionIndustryNomination = givenNominationExists();

    var postSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionIndustryNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionIndustryNominationDetail, applicantOrgId);

    // AND a draft nomination exists for industry team
    var preSubmissionIndustryNomination = givenNominationExists();

    var preSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionIndustryNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionIndustryNominationDetail, applicantOrgId);

    // AND a post submission nomination exists for a different applicant organisation group
    var postSubmissionNominationForOtherOrgGroup = givenNominationExists();

    var postSubmissionNominationDetailForOtherOrgGroup = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNominationForOtherOrgGroup)
        .withStatus(nominationStatus)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetailForOtherOrgGroup, otherApplicantOrgId);

    // AND a pre submission nomination exists for a different applicant organisation group

    var preSubmissionNominationForOtherOrgGroup = givenNominationExists();

    var preSubmissionNominationDetailForOtherOrgGroup = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNominationForOtherOrgGroup)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetailForOtherOrgGroup, otherApplicantOrgId);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are only shown the post submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .containsExactlyInAnyOrder(
            tuple(preSubmissionIndustryNominationDetail.getNomination().getId(), NominationStatus.DRAFT),
            tuple(postSubmissionIndustryNominationDetail.getNomination().getId(), nominationStatus),
            tuple(postSubmissionNominationDetailForOtherOrgGroup.getNomination().getId(), nominationStatus)
        );

    // AND not the pre submission nomination
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .doesNotContain(
            tuple(preSubmissionNominationDetailForOtherOrgGroup.getNomination().getId(), NominationStatus.DRAFT));
  }

  @ParameterizedTest
  @MethodSource("getSubmissionStatusArgumentsLinkedToIndustryTeamRole")
  void getNominationDetailsForWorkArea_whenMultipleIndustryTeams_andCanSubmitNominationsInBoth_thenAllReturned(
      NominationStatus nominationStatus, Role industryTeamRole
  ) {
    var applicantOrgGroupId = 100;
    var otherApplicantOrgGroupId = 200;

    var applicantOrgId = 10;
    var otherApplicantOrgId = 20;

    // GIVEN an industry user who can edit nominations
    var multipleTeamsUser = givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        Collections.singleton(industryTeamRole),
        applicantOrgGroupId
    );

    // AND the user is also in a different industry team with edit nomination permission
    givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        multipleTeamsUser,
        Collections.singleton(industryTeamRole),
        otherApplicantOrgGroupId
    );

    // AND they are logged into the service
    SamlAuthenticationUtil.Builder()
        .withUser(multipleTeamsUser)
        .setSecurityContext();

    // AND their industry organisation groups exist on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrgId)
        .build();

    var organisationUnitForOtherIndustryTeam = PortalOrganisationDtoTestUtil.builder()
        .withId(otherApplicantOrgId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    var organisationGroupForOtherIndustryTeam = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(otherApplicantOrgGroupId)
        .withOrganisation(organisationUnitForOtherIndustryTeam)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
            argThat(orgGroupIds -> orgGroupIds.containsAll(Set.of(applicantOrgGroupId, otherApplicantOrgGroupId))),
            any(RequestPurpose.class)
        ))
        .thenReturn(List.of(organisationGroup, organisationGroupForOtherIndustryTeam));

    // AND a submitted nomination exists for industry team

    var postSubmissionIndustryNomination = givenNominationExists();

    var postSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionIndustryNomination)
        .withStatus(nominationStatus)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionIndustryNominationDetail, applicantOrgId);

    // AND a draft nomination exists for industry team
    var preSubmissionIndustryNomination = givenNominationExists();

    var preSubmissionIndustryNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionIndustryNomination)
        .withVersion(1)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionIndustryNominationDetail, applicantOrgId);

    // AND a post submission nomination exists for a different industry team
    var postSubmissionNominationForOtherOrgGroup = givenNominationExists();

    var postSubmissionNominationDetailForOtherOrgGroup = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(postSubmissionNominationForOtherOrgGroup)
        .withStatus(nominationStatus)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(postSubmissionNominationDetailForOtherOrgGroup, otherApplicantOrgId);

    // AND a pre submission nomination exists for a different industry team

    var preSubmissionNominationForOtherOrgGroup = givenNominationExists();

    var preSubmissionNominationDetailForOtherOrgGroup = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(preSubmissionNominationForOtherOrgGroup)
        .withStatus(NominationStatus.DRAFT)
        .withVersion(1)
        .build();

    givenNominationDetailExistsWithApplicant(preSubmissionNominationDetailForOtherOrgGroup, otherApplicantOrgId);

    // WHEN we get the work area items for the user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN they are shown all nominations
    assertThat(workAreaItems)
        .extracting(
            nominationWorkAreaQueryResult -> nominationWorkAreaQueryResult.getNominationId().id(),
            NominationWorkAreaQueryResult::getNominationStatus)
        .containsExactlyInAnyOrder(
            tuple(preSubmissionIndustryNominationDetail.getNomination().getId(), NominationStatus.DRAFT),
            tuple(postSubmissionIndustryNominationDetail.getNomination().getId(), nominationStatus),
            tuple(postSubmissionNominationDetailForOtherOrgGroup.getNomination().getId(), nominationStatus),
            tuple(preSubmissionNominationDetailForOtherOrgGroup.getNomination().getId(), NominationStatus.DRAFT)
        );
  }

  @Test
  void getNominationDetailsForWorkArea_whenNominationWithDeletedStatusExist_thenDeletedNominationExcludedFromResults() {

    // GIVEN a user who has a role which allows them to see nominations
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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
  void getNominationDetailsForWorkArea_whenMultipleSubmittedNominationVersion_thenLatestNominationDetailIsReturned(
      NominationStatus nominationStatus) {

    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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
  void getNominationDetailsForWorkArea_whenMultipleNominationVersions_andLatestIsPreSubmission_thenLatestSubmittedNominationDetailIsReturned(
      NominationStatus nominationStatus) {

    // GIVEN a user with the manage nominations permission in the regulator team
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
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

  @Test
  void getNominationDetailsForWorkArea_whenMoreThatOneVersionSubmitted_thenFirstSubmittedIsFromFirstVersion() {

    // GIVEN a user with access to the work area exist
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has multiple submitted version
    var nomination = givenNominationExists();

    var yesterday = LocalDate.now().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withSubmittedInstant(yesterday)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(firstNominationDetail);

    var today = Instant.now();

    var secondNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(2)
        .withStatus(NominationStatus.SUBMITTED)
        .withSubmittedInstant(today)
        .build();

    givenNominationDetailExists(secondNominationDetail);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the first submission date is from the first version
    assertThat(workAreaItems)
        .extracting(NominationWorkAreaQueryResult::getNominationFirstSubmittedOn)
        .containsExactly(Optional.of(yesterday));
  }

  @Test
  void getNominationDetailsForWorkArea_whenNoSubmittedVersion_thenFirstSubmittedIsNotSet() {

    var applicantOrganisationGroupId = 100;
    var applicantOrganisationUnitId = 200;

    // GIVEN a user with access to the work area exist
    var nominationSubmitter = givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        Collections.singleton(Role.NOMINATION_SUBMITTER),
        applicantOrganisationGroupId
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(nominationSubmitter)
        .setSecurityContext();

    // AND their industry organisation group exists on the portal
    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(applicantOrganisationUnitId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantOrganisationGroupId)
        .withOrganisation(organisationUnit)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        Set.of(applicantOrganisationGroupId),
        NominationWorkAreaQueryService.ORGANISATION_GROUP_REQUEST_PURPOSE)
    )
        .thenReturn(List.of(organisationGroup));

    // AND there exists a draft nomination
    var nomination = givenNominationExists();

    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withSubmittedInstant(null)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenNominationDetailExistsWithApplicant(firstNominationDetail, applicantOrganisationUnitId);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the first submission date is an empty optional as the nomination has never been submitted
    assertThat(workAreaItems)
        .extracting(NominationWorkAreaQueryResult::getNominationFirstSubmittedOn)
        .containsExactly(Optional.empty());
  }

  @Test
  void getNominationDetailsForWorkArea_whenMoreThatOneVersionSubmitted_thenPlannedAppointmentDateIsFromLatestVersion() {

    // GIVEN a user with access to the work area exist
    var manageNominationUser = givenUserExistsInStaticTeamWithRoles(
        TeamType.REGULATOR,
        Collections.singleton(Role.NOMINATION_MANAGER)
    );

    // AND the user is logged in
    SamlAuthenticationUtil.Builder()
        .withUser(manageNominationUser)
        .setSecurityContext();

    // AND there exist a nomination which has multiple submitted versions
    var nomination = givenNominationExists();

    var firstNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(1)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(firstNominationDetail);

    var yesterday = LocalDate.now().minusDays(1);

    // AND each version has a different planned start date
    var firstNominationVersionNomineeDetail = NomineeDetailTestingUtil.builder()
        .withId(null)
        .withNominationDetail(firstNominationDetail)
        .withPlannedStartDate(yesterday)
        .build();

    persistAndFlush(firstNominationVersionNomineeDetail);

    var secondNominationDetail = NominationDetailTestUtil.builder()
        .withId(null)
        .withNomination(nomination)
        .withVersion(2)
        .withStatus(NominationStatus.SUBMITTED)
        .build();

    givenNominationDetailExists(secondNominationDetail);

    var today = LocalDate.now();

    var secondNominationVersionNomineeDetail = NomineeDetailTestingUtil.builder()
        .withId(null)
        .withNominationDetail(secondNominationDetail)
        .withPlannedStartDate(today)
        .build();

    persistAndFlush(secondNominationVersionNomineeDetail);

    // WHEN we get the work area items for the logged in user
    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    // THEN the planned start date is that of the second nomination version
    assertThat(workAreaItems)
        .extracting(NominationWorkAreaQueryResult::getPlannedAppointmentDate)
        .containsExactly(today);
  }

  @Test
  void getWorkAreaItems_whenOrganisationGroupScopeTeamHasNoOrganisations() {

    var organisationGroupIdWithNoUnits = 100;

    // GIVEN an organisation group with no units
    var organisationGroupWithoutUnits = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(organisationGroupIdWithNoUnits)
        .withOrganisations(null)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        eq(Set.of(organisationGroupIdWithNoUnits)),
        any(RequestPurpose.class)
    ))
        .thenReturn(List.of(organisationGroupWithoutUnits));

    var user = ServiceUserDetailTestUtil.Builder().build();

    // AND team exists for the organisation with a user who can see nominations
    givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(
        user,
        Set.of(Role.NOMINATION_SUBMITTER),
        organisationGroupIdWithNoUnits
    );

    SamlAuthenticationUtil.Builder()
        .withUser(user)
        .setSecurityContext();

    var workAreaItems = nominationWorkAreaQueryService.getWorkAreaItems();

    assertThat(workAreaItems).isEmpty();
  }

  private ServiceUserDetail givenUserExistsInStaticTeamWithRoles(TeamType teamType, Set<Role> roles) {

    if (teamType.isScoped()) {
      throw new IllegalArgumentException("TeamType %s is scoped, static expected".formatted(teamType));
    }

    var rolesValidForTeamType = teamType.getAllowedRoles().containsAll(roles);

    if (!rolesValidForTeamType) {
      throw new IllegalArgumentException("Not all roles %s are valid for teamType %s".formatted(roles, teamType));
    }

    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    Team team;

    try {
      team = entityManager.createQuery("SELECT t FROM Team t WHERE t.teamType = :teamType", Team.class)
          .setParameter("teamType", teamType)
          .getSingleResult();
    } catch (NoResultException exception) {
      team = null;
    }

    if (team == null) {
      team = new Team();
      team.setTeamType(teamType);
      team.setName("name");
      persistAndFlush(team);
    }

    Team finalTeam = team;

    roles.forEach(role -> {
      var teamRole = new TeamRole();
      teamRole.setTeam(finalTeam);
      teamRole.setRole(role);
      teamRole.setWuaId(user.wuaId());

      persistAndFlush(teamRole);
    });

    return user;
  }

  private ServiceUserDetail givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(Set<Role> roles,
                                                                                         int applicantPortalOrgGroupId) {
    var user = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1000L)
        .build();

    var organisationUnit = PortalOrganisationDtoTestUtil.builder()
        .withId(20)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisationGroupId(applicantPortalOrgGroupId)
        .withOrganisation(organisationUnit)
        .build();

    when(organisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        eq(Set.of(applicantPortalOrgGroupId)),
        any(RequestPurpose.class)
    ))
        .thenReturn(List.of(organisationGroup));

    return givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(user, roles, applicantPortalOrgGroupId);
  }

  private ServiceUserDetail givenUserExistsInIndustryTeamWithRolesAndApplicantOrgGroupId(ServiceUserDetail user,
                                                                                         Set<Role> roles,
                                                                                         int applicantOrgGroupId) {
    Team team;

    try {
      team = entityManager.createQuery("SELECT t FROM Team t WHERE t.teamType = :teamType AND t.scopeId = :scopeId AND t.scopeType = :scopeType", Team.class)
          .setParameter("teamType", TeamType.ORGANISATION_GROUP)
          .setParameter("scopeId", String.valueOf(applicantOrgGroupId))
          .setParameter("scopeType", "ORGANISATION_GROUP")
          .getSingleResult();
    } catch (NoResultException exception) {
      team = null;
    }

    if (team == null) {
      team = new Team();
      team.setTeamType(TeamType.ORGANISATION_GROUP);
      team.setScopeId(String.valueOf(applicantOrgGroupId));
      team.setScopeType("ORGANISATION_GROUP");
      team.setName("name");

      persistAndFlush(team);
    }

    Team finalTeam = team;

    roles.forEach(role -> {
      var teamRole = new TeamRole();
      teamRole.setTeam(finalTeam);
      teamRole.setRole(role);
      teamRole.setWuaId(user.wuaId());

      persistAndFlush(teamRole);
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
    givenNominationDetailExistsWithApplicant(nominationDetail, 200);
  }

  private void givenNominationDetailExistsWithApplicant(NominationDetail nominationDetail, int applicantOrganisationUnitId) {
    persistAndFlush(nominationDetail);

    var applicantDetailForNominationDetail = ApplicantDetailTestUtil.builder()
        .withId(null)
        .withPortalOrganisationId(applicantOrganisationUnitId)
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

  private static Stream<Arguments> getSubmissionStatusArgumentsLinkedToRegulatorTeamRole() {

    var arguments = new ArrayList<Arguments>();

    NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION).forEach(status -> {
      arguments.add(Arguments.of(status, Role.NOMINATION_MANAGER));
      arguments.add(Arguments.of(status, Role.VIEW_ANY_NOMINATION));
    });

    return arguments.stream();
  }

  private static Stream<Arguments> getSubmissionStatusArgumentsLinkedToRegulatorAndIndustryTeamRoles() {

    var arguments = new ArrayList<Arguments>();

    NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION).forEach(status -> {
      arguments.add(Arguments.of(status, Role.NOMINATION_MANAGER, Role.NOMINATION_EDITOR));
      arguments.add(Arguments.of(status, Role.VIEW_ANY_NOMINATION, Role.NOMINATION_SUBMITTER));
    });

    return arguments.stream();
  }

  private static Stream<Arguments> getSubmissionStatusArgumentsLinkedToConsulteeAndIndustryTeamRoles() {

    var arguments = new ArrayList<Arguments>();

    NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION).forEach(status -> {
      arguments.add(Arguments.of(status, Role.CONSULTATION_MANAGER, Role.NOMINATION_EDITOR));
      arguments.add(Arguments.of(status, Role.CONSULTATION_PARTICIPANT, Role.NOMINATION_SUBMITTER));
    });

    return arguments.stream();
  }

  private static Stream<Arguments> getSubmissionStatusArgumentsLinkedToIndustryTeamRole() {

    var arguments = new ArrayList<Arguments>();

    NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION).forEach(status -> {
      arguments.add(Arguments.of(status, Role.NOMINATION_SUBMITTER));
      arguments.add(Arguments.of(status, Role.NOMINATION_EDITOR));
    });

    return arguments.stream();
  }

  private void persistAndFlush(Object entity) {
    entityManager.persist(entity);
    entityManager.flush();
  }

}