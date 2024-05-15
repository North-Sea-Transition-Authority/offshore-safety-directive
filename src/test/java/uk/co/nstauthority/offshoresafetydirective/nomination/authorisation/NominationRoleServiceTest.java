package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class NominationRoleServiceTest {

  private static final long WUA_ID = 1L;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private ApplicantDetailAccessService applicantDetailAccessService;

  @Mock
  private PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @InjectMocks
  private NominationRoleService nominationRoleService;

  @Nested
  class UserCanStartNomination {

    @Test
    void whenCanStartNomination() {

      var organisationGroupTeam = new Team();
      organisationGroupTeam.setTeamType(TeamType.ORGANISATION_GROUP);

      var nominationSubmitterRole = new TeamRole();
      nominationSubmitterRole.setRole(Role.NOMINATION_SUBMITTER);
      nominationSubmitterRole.setTeam(organisationGroupTeam);

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of(nominationSubmitterRole));

      assertThat(nominationRoleService.userCanStartNomination(WUA_ID)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TeamType.class, mode = EnumSource.Mode.EXCLUDE, names = "ORGANISATION_GROUP")
    void whenCannotStartNomination(TeamType nonOrganisationGroupTeamType) {

      var nonOrganisationGroupTeam = new Team();
      nonOrganisationGroupTeam.setTeamType(nonOrganisationGroupTeamType);

      var nonOrganisationGroupRole = nonOrganisationGroupTeamType
          .getAllowedRoles()
          .stream()
          .findFirst()
          .get();

      var nonOrganisationGroupTeamRole = new TeamRole();
      nonOrganisationGroupTeamRole.setRole(nonOrganisationGroupRole);
      nonOrganisationGroupTeamRole.setTeam(nonOrganisationGroupTeam);

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of(nonOrganisationGroupTeamRole));

      assertThat(nominationRoleService.userCanStartNomination(WUA_ID)).isFalse();
    }

    @Test
    void whenRoleInWrongTeamType() {

      var nonOrganisationGroupTeam = new Team();
      nonOrganisationGroupTeam.setTeamType(TeamType.REGULATOR);

      var nominationSubmitterTeamRoleInWrongTeam = new TeamRole();
      nominationSubmitterTeamRoleInWrongTeam.setRole(Role.NOMINATION_SUBMITTER);
      nominationSubmitterTeamRoleInWrongTeam.setTeam(nonOrganisationGroupTeam);

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of(nominationSubmitterTeamRoleInWrongTeam));

      assertThat(nominationRoleService.userCanStartNomination(WUA_ID)).isFalse();
    }

    @Test
    void whenUserHasNoRoles() {

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of());

      assertThat(nominationRoleService.userCanStartNomination(WUA_ID)).isFalse();
    }
  }

  @Nested
  class UserHasAtLeastOneRoleInApplicantOrganisationGroupTeam {

    private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

    @Captor
    private ArgumentCaptor<TeamScopeReference> teamScopeReferenceCaptor;

    @Test
    void whenRolesNotForOrganisationGroupTeam() {

      given(teamQueryService.areRolesValidForTeamType(
          Set.of(Role.CONSULTATION_PARTICIPANT),
          TeamType.ORGANISATION_GROUP
      ))
          .willReturn(false);

      var rolesToCheckFor = Set.of(Role.CONSULTATION_PARTICIPANT);

      assertThatThrownBy(() ->
          nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
              WUA_ID,
              NOMINATION_DETAIL,
              rolesToCheckFor
      ))
          .isInstanceOf(IllegalArgumentException.class);

      verifyNoInteractions(applicantDetailAccessService);
      verifyNoInteractions(portalOrganisationGroupQueryService);
      verify(teamQueryService, never()).userHasAtLeastOneScopedRole(any(), any(), any(), any());
    }

    @Test
    void whenApplicantDetailsNotFound() {

      givenRoleIsValidForTeamType(Role.NOMINATION_EDITOR);

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.empty());

      var rolesToCheckFor = Set.of(Role.NOMINATION_EDITOR);

      assertThatThrownBy(() ->
          nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
              WUA_ID,
              NOMINATION_DETAIL,
              rolesToCheckFor
          ))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenApplicantHasNoOrganisationGroup() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = ApplicantDetailDtoTestUtil.builder().build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of());

      nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Set.of(Role.NOMINATION_SUBMITTER)
      );

      verify(teamQueryService, never()).userHasAtLeastOneScopedRole(any(), any(), any(), any());
    }

    @Test
    void whenUserHasRoleInApplicantOrganisationGroupTeam() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = givenApplicantDetailExists();

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder().build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      given(teamQueryService.userHasAtLeastOneScopedRole(
          eq(WUA_ID),
          eq(TeamType.ORGANISATION_GROUP),
          refEq(TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP")),
          eq(Set.of(Role.NOMINATION_SUBMITTER))
      ))
          .willReturn(true);

      var userHasRole = nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Set.of(Role.NOMINATION_SUBMITTER)
      );

      assertThat(userHasRole).isTrue();
    }

    @Test
    void whenUserDoesNotHaveRoleInApplicantOrganisationGroupTeam() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = givenApplicantDetailExists();

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder().build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      given(teamQueryService.userHasAtLeastOneScopedRole(
          eq(WUA_ID),
          eq(TeamType.ORGANISATION_GROUP),
          refEq(TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP")),
          eq(Set.of(Role.NOMINATION_SUBMITTER))
      ))
          .willReturn(false);

      var rolesToCheckFor = Set.of(Role.NOMINATION_SUBMITTER);

      var userHasRole = nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          rolesToCheckFor
      );

      assertThat(userHasRole).isFalse();
    }

    @Test
    void whenApplicantHasMultipleOrganisationGroups() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = givenApplicantDetailExists();

      var firstOrganisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(10)
          .build();

      var secondOrganisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(20)
          .build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(firstOrganisationGroup, secondOrganisationGroup));

      nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Set.of(Role.NOMINATION_SUBMITTER)
      );

      then(teamQueryService)
          .should(times(2))
          .userHasAtLeastOneScopedRole(
              eq(WUA_ID),
              eq(TeamType.ORGANISATION_GROUP),
              teamScopeReferenceCaptor.capture(),
              eq(Set.of(Role.NOMINATION_SUBMITTER))
          );

      assertThat(teamScopeReferenceCaptor.getAllValues())
          .extracting(TeamScopeReference::getId, TeamScopeReference::getType)
          .containsExactlyInAnyOrder(
              tuple(firstOrganisationGroup.organisationGroupId(), "ORGANISATION_GROUP"),
              tuple(secondOrganisationGroup.organisationGroupId(), "ORGANISATION_GROUP")
          );
    }

    private ApplicantDetailDto givenApplicantDetailExists() {

      var applicantDetail = ApplicantDetailDtoTestUtil.builder().build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      return applicantDetail;
    }
  }

  @Nested
  class UserHasRoleInApplicantOrganisationGroupTeam {

    private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

    @Captor
    private ArgumentCaptor<TeamScopeReference> teamScopeReferenceCaptor;

    @Test
    void whenRolesNotForOrganisationGroupTeam() {

      given(teamQueryService.areRolesValidForTeamType(
          Set.of(Role.CONSULTATION_PARTICIPANT),
          TeamType.ORGANISATION_GROUP
      ))
          .willReturn(false);

      assertThatThrownBy(() ->
          nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(
              WUA_ID,
              NOMINATION_DETAIL,
              Role.CONSULTATION_PARTICIPANT
          ))
          .isInstanceOf(IllegalArgumentException.class);

      verifyNoInteractions(applicantDetailAccessService);
      verifyNoInteractions(portalOrganisationGroupQueryService);
      verify(teamQueryService, never()).userHasAtLeastOneScopedRole(any(), any(), any(), any());
    }

    @Test
    void whenApplicantDetailsNotFound() {

      givenRoleIsValidForTeamType(Role.NOMINATION_EDITOR);

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.empty());

      assertThatThrownBy(() ->
          nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(
              WUA_ID,
              NOMINATION_DETAIL,
              Role.NOMINATION_EDITOR
          ))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenApplicantHasNoOrganisationGroup() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = ApplicantDetailDtoTestUtil.builder().build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of());

      nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Role.NOMINATION_SUBMITTER
      );

      verify(teamQueryService, never()).userHasAtLeastOneScopedRole(any(), any(), any(), any());
    }

    @Test
    void whenUserHasRoleInApplicantOrganisationGroupTeam() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = givenApplicantDetailExists();

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder().build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      given(teamQueryService.userHasAtLeastOneScopedRole(
          eq(WUA_ID),
          eq(TeamType.ORGANISATION_GROUP),
          refEq(TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP")),
          eq(Set.of(Role.NOMINATION_SUBMITTER))
      ))
          .willReturn(true);

      var userHasRole = nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Role.NOMINATION_SUBMITTER
      );

      assertThat(userHasRole).isTrue();
    }

    @Test
    void whenUserDoesNotHaveRoleInApplicantOrganisationGroupTeam() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = givenApplicantDetailExists();

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder().build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      given(teamQueryService.userHasAtLeastOneScopedRole(
          eq(WUA_ID),
          eq(TeamType.ORGANISATION_GROUP),
          refEq(TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP")),
          eq(Set.of(Role.NOMINATION_SUBMITTER))
      ))
          .willReturn(false);

      var userHasRole = nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Role.NOMINATION_SUBMITTER
      );

      assertThat(userHasRole).isFalse();
    }

    @Test
    void whenApplicantHasMultipleOrganisationGroups() {

      givenRoleIsValidForTeamType(Role.NOMINATION_SUBMITTER);

      var applicantDetail = givenApplicantDetailExists();

      var firstOrganisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(10)
          .build();

      var secondOrganisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(20)
          .build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(applicantDetail.applicantOrganisationId().id()),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(firstOrganisationGroup, secondOrganisationGroup));

      nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(
          WUA_ID,
          NOMINATION_DETAIL,
          Role.NOMINATION_SUBMITTER
      );

      then(teamQueryService)
          .should(times(2))
          .userHasAtLeastOneScopedRole(
              eq(WUA_ID),
              eq(TeamType.ORGANISATION_GROUP),
              teamScopeReferenceCaptor.capture(),
              eq(Set.of(Role.NOMINATION_SUBMITTER))
          );

      assertThat(teamScopeReferenceCaptor.getAllValues())
          .extracting(TeamScopeReference::getId, TeamScopeReference::getType)
          .containsExactlyInAnyOrder(
              tuple(firstOrganisationGroup.organisationGroupId(), "ORGANISATION_GROUP"),
              tuple(secondOrganisationGroup.organisationGroupId(), "ORGANISATION_GROUP")
          );
    }

    private ApplicantDetailDto givenApplicantDetailExists() {

      var applicantDetail = ApplicantDetailDtoTestUtil.builder().build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      return applicantDetail;
    }
  }

  @Nested
  class GetUserRolesInApplicantOrganisationGroupTeam {

    private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();

    @Test
    void whenNoApplicantDetail() {

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.empty());

      assertThatThrownBy(
          () -> nominationRoleService.getUserRolesInApplicantOrganisationGroupTeam(WUA_ID, NOMINATION_DETAIL)
      )
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenApplicantHasNoOrganisationGroup() {

      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of());

      var resultingTeamRoles = nominationRoleService.getUserRolesInApplicantOrganisationGroupTeam(WUA_ID, NOMINATION_DETAIL);

      assertThat(resultingTeamRoles).isEmpty();

      then(teamQueryService)
          .shouldHaveNoInteractions();
    }

    @Test
    void whenUserOnlyInNonOrganisationGroupTeams() {

      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder().build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      var nonOrganisationTeam = new Team();
      nonOrganisationTeam.setTeamType(TeamType.REGULATOR);

      var nonOrganisationTeamRole = new TeamRole();
      nonOrganisationTeamRole.setTeam(nonOrganisationTeam);
      nonOrganisationTeamRole.setRole(Role.NOMINATION_MANAGER);

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of(nonOrganisationTeamRole));

      var resultingTeamRoles = nominationRoleService.getUserRolesInApplicantOrganisationGroupTeam(WUA_ID, NOMINATION_DETAIL);

      assertThat(resultingTeamRoles).isEmpty();
    }

    @Test
    void whenUserNotInAnyOrganisationTeamsLinkedToNomination() {

      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(200)
          .build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      var organisationTeamNotLinkedToNomination = new Team();
      organisationTeamNotLinkedToNomination.setTeamType(TeamType.ORGANISATION_GROUP);
      organisationTeamNotLinkedToNomination.setScopeId("50");

      var organisationTeamRoleNotLinkedToNomination = new TeamRole();
      organisationTeamRoleNotLinkedToNomination.setTeam(organisationTeamNotLinkedToNomination);
      organisationTeamRoleNotLinkedToNomination.setRole(Role.NOMINATION_SUBMITTER);

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of(organisationTeamRoleNotLinkedToNomination));

      var resultingTeamRoles = nominationRoleService.getUserRolesInApplicantOrganisationGroupTeam(WUA_ID, NOMINATION_DETAIL);

      assertThat(resultingTeamRoles).isEmpty();
    }

    @Test
    void whenUserInOrganisationTeamsLinkedToNomination() {

      // GIVEN applicant detail exists
      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
          .willReturn(Optional.of(applicantDetail));

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(200)
          .build();

      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .willReturn(Set.of(organisationGroup));

      // AND a team exists for the group associated to the applicant of the nomination
      var firstNominationTeam = new Team();
      firstNominationTeam.setTeamType(TeamType.ORGANISATION_GROUP);
      firstNominationTeam.setScopeId("200");

      // AND the current user has multiple roles in that team
      var firstNominationTeamRoleFirstTeam = new TeamRole(UUID.randomUUID());
      firstNominationTeamRoleFirstTeam.setTeam(firstNominationTeam);
      firstNominationTeamRoleFirstTeam.setRole(Role.NOMINATION_SUBMITTER);

      var secondNominationTeamRoleFirstTeam = new TeamRole(UUID.randomUUID());
      secondNominationTeamRoleFirstTeam.setTeam(firstNominationTeam);
      secondNominationTeamRoleFirstTeam.setRole(Role.NOMINATION_EDITOR);

      // AND an additional team exists for a different group associated to the applicant of the nomination
      var secondNominationTeam = new Team();
      secondNominationTeam.setTeamType(TeamType.ORGANISATION_GROUP);
      secondNominationTeam.setScopeId("200");

      // AND the current user has a role in that team with is the same as the role in the previous team
      var nominationTeamRoleSecondTeam = new TeamRole(UUID.randomUUID());
      nominationTeamRoleSecondTeam.setTeam(secondNominationTeam);
      nominationTeamRoleSecondTeam.setRole(Role.NOMINATION_SUBMITTER);

      // AND an organisation group team exists that isn't related to the applicant of the nomination
      var nonNominationTeam = new Team();
      nonNominationTeam.setTeamType(TeamType.ORGANISATION_GROUP);
      nonNominationTeam.setScopeId("50");

      // AND the current user has a role in that team
      var nonNominationTeamRole = new TeamRole(UUID.randomUUID());
      nonNominationTeamRole.setTeam(nonNominationTeam);
      nonNominationTeamRole.setRole(Role.NOMINATION_VIEWER);

      // AND a regulator team exists
      var regulatorTeam = new Team();
      regulatorTeam.setTeamType(TeamType.REGULATOR);

      // AND the current use has a role in that team
      var regulatorTeamRole = new TeamRole(UUID.randomUUID());
      regulatorTeamRole.setTeam(regulatorTeam);
      regulatorTeamRole.setRole(Role.NOMINATION_MANAGER);

      given(teamQueryService.getTeamRolesForUser(WUA_ID))
          .willReturn(Set.of(
              firstNominationTeamRoleFirstTeam,
              secondNominationTeamRoleFirstTeam,
              nominationTeamRoleSecondTeam,
              nonNominationTeamRole,
              regulatorTeamRole
          ));

      // WHEN we get the roles associated to the applicant group team
      var resultingTeamRoles = nominationRoleService.getUserRolesInApplicantOrganisationGroupTeam(WUA_ID, NOMINATION_DETAIL);

      // THEN we only get roles belonging to teams associated to the nomination
      assertThat(resultingTeamRoles)
          .containsExactlyInAnyOrder(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR);
    }
  }

  @Nested
  class GetUsersInApplicantOrganisationTeamWithAnyRoleOf {

    @Test
    void whenRoleNotValid() {

      var consulteeRoles = Set.of(Role.CONSULTATION_PARTICIPANT);

      when(teamQueryService.areRolesValidForTeamType(consulteeRoles, TeamType.ORGANISATION_GROUP))
          .thenReturn(false);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      assertThatThrownBy(() -> nominationRoleService.getUsersInApplicantOrganisationTeamWithAnyRoleOf(
          nominationDetail,
          consulteeRoles
      ))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenNoApplicantDetail() {

      var roles = Set.of(Role.NOMINATION_SUBMITTER);

      when(teamQueryService.areRolesValidForTeamType(roles, TeamType.ORGANISATION_GROUP))
          .thenReturn(true);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      when(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> nominationRoleService.getUsersInApplicantOrganisationTeamWithAnyRoleOf(
          nominationDetail,
          roles
      ))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenNoOrganisationGroup() {

      var roles = Set.of(Role.NOMINATION_SUBMITTER);

      when(teamQueryService.areRolesValidForTeamType(roles, TeamType.ORGANISATION_GROUP))
          .thenReturn(true);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      when(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .thenReturn(Optional.of(applicantDetail));

      when(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .thenReturn(Set.of());

      var resultingUsers = nominationRoleService.getUsersInApplicantOrganisationTeamWithAnyRoleOf(
          nominationDetail,
          roles
      );

      assertThat(resultingUsers).isEmpty();

      verify(teamQueryService, never()).getUsersInScopedTeam(any(), any());
    }

    @Test
    void whenNoUsersInOrganisationGroup() {

      var roles = Set.of(Role.NOMINATION_SUBMITTER);

      when(teamQueryService.areRolesValidForTeamType(roles, TeamType.ORGANISATION_GROUP))
          .thenReturn(true);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      when(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .thenReturn(Optional.of(applicantDetail));

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(20)
          .build();

      when(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .thenReturn(Set.of(organisationGroup));

      when(teamQueryService.getUsersInScopedTeam(
          eq(TeamType.ORGANISATION_GROUP),
          refEq(TeamScopeReference.from("20", "ORGANISATION_GROUP"))
      ))
          .thenReturn(Map.of());

      var resultingUsers = nominationRoleService.getUsersInApplicantOrganisationTeamWithAnyRoleOf(
          nominationDetail,
          roles
      );

      assertThat(resultingUsers).isEmpty();
    }

    @Test
    void whenUsersInOrganisationGroup() {

      var roles = Set.of(Role.NOMINATION_SUBMITTER, Role.TEAM_MANAGER);

      when(teamQueryService.areRolesValidForTeamType(roles, TeamType.ORGANISATION_GROUP))
          .thenReturn(true);

      var nominationDetail = NominationDetailTestUtil.builder().build();

      var applicantDetail = ApplicantDetailDtoTestUtil.builder()
          .withApplicantOrganisationId(100)
          .build();

      when(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(nominationDetail))
          .thenReturn(Optional.of(applicantDetail));

      var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
          .withOrganisationGroupId(20)
          .build();

      when(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
          eq(100),
          any(RequestPurpose.class)
      ))
          .thenReturn(Set.of(organisationGroup));

      var userWithMultipleMatchingRoles = EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(50)
          .build();

      var userWithOneMatchingRoles = EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(60)
          .build();

      var userWithNoMatchingRoles = EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(70)
          .build();

      when(teamQueryService.getUsersInScopedTeam(
          eq(TeamType.ORGANISATION_GROUP),
          refEq(TeamScopeReference.from("20", "ORGANISATION_GROUP"))
      ))
          .thenReturn(Map.of(
              Role.NOMINATION_SUBMITTER, Set.of(userWithMultipleMatchingRoles),
              Role.TEAM_MANAGER, Set.of(userWithMultipleMatchingRoles, userWithOneMatchingRoles),
              Role.NOMINATION_VIEWER, Set.of(userWithNoMatchingRoles)
          ));

      var resultingUsers = nominationRoleService.getUsersInApplicantOrganisationTeamWithAnyRoleOf(
          nominationDetail,
          roles
      );

      assertThat(resultingUsers)
          .containsExactlyInAnyOrder(userWithMultipleMatchingRoles, userWithOneMatchingRoles);
    }
  }

  private void givenRoleIsValidForTeamType(Role role) {

    given(teamQueryService.areRolesValidForTeamType(
        Set.of(role),
        TeamType.ORGANISATION_GROUP
    ))
        .willReturn(true);
  }
}