// TODO OSDOP-811
//package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//import static org.mockito.Mockito.mock;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//import java.util.Set;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import uk.co.fivium.energyportalapi.client.RequestPurpose;
//import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
//import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
//import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
//import uk.co.nstauthority.offshoresafetydirective.teams.Team;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
//import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
//import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;
//
//class NominationApplicantTeamServiceTest {
//
//  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.builder().build();
//
//  private static ApplicantDetailAccessService applicantDetailAccessService;
//
//  private static TeamScopeService teamScopeService;
//
//  private static TeamMemberViewService teamMemberViewService;
//
//  private static PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;
//
//  private static NominationApplicantTeamService nominationApplicantTeamService;
//
//  @BeforeAll
//  static void beforeAllSetup() {
//
//    applicantDetailAccessService = mock(ApplicantDetailAccessService.class);
//
//    teamScopeService = mock(TeamScopeService.class);
//
//    teamMemberViewService = mock(TeamMemberViewService.class);
//
//    portalOrganisationGroupQueryService = mock(PortalOrganisationGroupQueryService.class);
//
//    nominationApplicantTeamService = new NominationApplicantTeamService(
//        applicantDetailAccessService,
//        teamScopeService,
//        teamMemberViewService,
//        portalOrganisationGroupQueryService
//    );
//
//  }
//
//  @DisplayName("GIVEN I want to get the teams for a nomination applicant")
//  @Nested
//  class GetApplicantTeams {
//
//    @DisplayName("WHEN no applicant detail found")
//    @Nested
//    class WhenNotApplicantDetail {
//
//      @DisplayName("THEN an exception is thrown")
//      @Test
//      void thenExceptionIsThrown() {
//
//        given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
//            .willReturn(Optional.empty());
//
//        assertThatThrownBy(() -> nominationApplicantTeamService.getApplicantTeams(NOMINATION_DETAIL))
//            .isInstanceOf(IllegalStateException.class);
//      }
//    }
//
//    @DisplayName("WHEN we have an applicant")
//    @Nested
//    class WhenApplicant {
//
//      private static final ApplicantDetail APPLICANT_DETAIL = ApplicantDetailTestUtil.builder()
//          .withPortalOrganisationId(100)
//          .build();
//
//      @DisplayName("AND the applicant is not part of an organisation group on the portal")
//      @Nested
//      class AndApplicantNotPartOfOrganisationGroup {
//
//        @BeforeAll
//        static void setup() {
//
//          teamScopeService = mock(TeamScopeService.class);
//
//          nominationApplicantTeamService = new NominationApplicantTeamService(
//              applicantDetailAccessService,
//              teamScopeService,
//              teamMemberViewService,
//              portalOrganisationGroupQueryService
//          );
//        }
//
//        @DisplayName("THEN no teams are returned")
//        @Test
//        void thenNoTeamsReturned() {
//
//          given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
//              .willReturn(Optional.of(ApplicantDetailDto.fromApplicantDetail(APPLICANT_DETAIL)));
//
//          given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
//              eq(APPLICANT_DETAIL.getPortalOrganisationId()),
//              any(RequestPurpose.class)
//          ))
//              .willReturn(Collections.emptySet());
//
//          nominationApplicantTeamService.getApplicantTeams(NOMINATION_DETAIL);
//
//          then(teamScopeService)
//              .shouldHaveNoInteractions();
//        }
//      }
//
//      @DisplayName("AND no team exists scoped to the applicant organisation group")
//      @Nested
//      class AndNoTeamScopedToApplicantGroup {
//
//        @DisplayName("THEN no teams are returned")
//        @Test
//        void thenNoTeamsReturned() {
//
//          given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
//              .willReturn(Optional.of(ApplicantDetailDto.fromApplicantDetail(APPLICANT_DETAIL)));
//
//          var applicantGroup = PortalOrganisationGroupDtoTestUtil.builder()
//              .withOrganisationGroupId(200)
//              .build();
//
//          given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
//              eq(APPLICANT_DETAIL.getPortalOrganisationId()),
//              any(RequestPurpose.class)
//          ))
//              .willReturn(Set.of(applicantGroup));
//
//          given(teamScopeService.getTeamScope(
//              Set.of(String.valueOf(applicantGroup.organisationGroupId())),
//              PortalTeamType.ORGANISATION_GROUP
//          ))
//              .willReturn(Collections.emptyList());
//
//          Set<Team> resultingApplicantTeams = nominationApplicantTeamService.getApplicantTeams(NOMINATION_DETAIL);
//
//          assertThat(resultingApplicantTeams).isEmpty();
//        }
//      }
//
//      @DisplayName("AND teams exist scoped to the applicant organisation group")
//      @Nested
//      class AndTeamScopedToApplicantGroup {
//
//        @DisplayName("THEN applicant group teams returned")
//        @Test
//        void thenApplicantScopedTeamsReturned() {
//
//          given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
//              .willReturn(Optional.of(ApplicantDetailDto.fromApplicantDetail(APPLICANT_DETAIL)));
//
//          var applicantGroup = PortalOrganisationGroupDtoTestUtil.builder()
//              .withOrganisationGroupId(200)
//              .build();
//
//          given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
//              eq(APPLICANT_DETAIL.getPortalOrganisationId()),
//              any(RequestPurpose.class)
//          ))
//              .willReturn(Set.of(applicantGroup));
//
//          TeamScope applicantScopedTeam = TeamScopeTestUtil.builder()
//              .build();
//
//          given(teamScopeService.getTeamScope(
//              Set.of(String.valueOf(applicantGroup.organisationGroupId())),
//              PortalTeamType.ORGANISATION_GROUP
//          ))
//              .willReturn(List.of(applicantScopedTeam));
//
//          Set<Team> resultingApplicantTeams = nominationApplicantTeamService.getApplicantTeams(NOMINATION_DETAIL);
//
//          assertThat(resultingApplicantTeams).containsExactly(applicantScopedTeam.getTeam());
//        }
//      }
//    }
//  }
//
//  @DisplayName("GIVEN I want to get members of the applicant team with certain roles")
//  @Nested
//  class GetApplicantTeamMembersWithAnyRoleOf {
//
//    private static PortalOrganisationGroupDto applicantGroup;
//
//    @BeforeAll
//    static void setup() {
//
//      ApplicantDetail applicantDetail = ApplicantDetailTestUtil.builder()
//          .withPortalOrganisationId(100)
//          .build();
//
//      given(applicantDetailAccessService.getApplicantDetailDtoByNominationDetail(NOMINATION_DETAIL))
//          .willReturn(Optional.of(ApplicantDetailDto.fromApplicantDetail(applicantDetail)));
//
//      applicantGroup = PortalOrganisationGroupDtoTestUtil.builder()
//          .withOrganisationGroupId(200)
//          .build();
//
//      given(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(
//          eq(applicantDetail.getPortalOrganisationId()),
//          any(RequestPurpose.class)
//      ))
//          .willReturn(Set.of(applicantGroup));
//    }
//
//    @DisplayName("WHEN no applicant team exists")
//    @Nested
//    class WhenNoApplicantTeam {
//
//      @DisplayName("THEN no team members returned")
//      @Test
//      void thenNoTeamMembersReturned() {
//
//        given(teamScopeService.getTeamScope(
//            Set.of(applicantGroup.organisationGroupId()),
//            PortalTeamType.ORGANISATION_GROUP
//        ))
//            .willReturn(Collections.emptyList());
//
//        var resultingTeamMembers = nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//            NOMINATION_DETAIL,
//            Set.of(IndustryTeamRole.NOMINATION_EDITOR)
//        );
//
//        assertThat(resultingTeamMembers).isEmpty();
//      }
//    }
//
//    @DisplayName("WHEN an applicant team exists")
//    @Nested
//    class WhenApplicantTeam {
//
//      @DisplayName("AND a no members exists with any of the required roles")
//      @Nested
//      class AndMemberDoNotHaveRightRoles {
//
//        @DisplayName("THEN no team members returned")
//        @Test
//        void thenNoTeamMembersReturned() {
//
//          var applicantTeamScope = TeamScopeTestUtil.builder().build();
//
//          given(teamScopeService.getTeamScope(
//              Set.of(applicantGroup.organisationGroupId()),
//              PortalTeamType.ORGANISATION_GROUP
//          ))
//              .willReturn(List.of(applicantTeamScope));
//
//          given(teamMemberViewService.getTeamMembersWithRoles(
//              Set.of(applicantTeamScope.getTeam()),
//              Set.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//          ))
//              .willReturn(Collections.emptyList());
//
//          var resultingTeamMembers = nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//              NOMINATION_DETAIL,
//              Set.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//          );
//
//          assertThat(resultingTeamMembers).isEmpty();
//        }
//      }
//
//      @DisplayName("AND a member exists with the one of the required roles")
//      @Nested
//      class AndMemberHasOneOfRequiredRoles {
//
//        @DisplayName("THEN member is returned")
//        @Test
//        void thenMemberIsReturned() {
//
//          var applicantTeamScope = TeamScopeTestUtil.builder().build();
//
//          given(teamScopeService.getTeamScope(
//              Set.of(applicantGroup.organisationGroupId()),
//              PortalTeamType.ORGANISATION_GROUP
//          ))
//              .willReturn(List.of(applicantTeamScope));
//
//          var expectedTeamMemberView = TeamMemberViewTestUtil.Builder()
//              .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
//              .build();
//
//          given(teamMemberViewService.getTeamMembersWithRoles(
//              Set.of(applicantTeamScope.getTeam()),
//              Set.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//          ))
//              .willReturn(List.of(expectedTeamMemberView));
//
//          var resultingTeamMembers = nominationApplicantTeamService.getApplicantTeamMembersWithAnyRoleOf(
//              NOMINATION_DETAIL,
//              Set.of(IndustryTeamRole.NOMINATION_SUBMITTER)
//          );
//
//          assertThat(resultingTeamMembers).containsExactly(expectedTeamMemberView);
//        }
//      }
//    }
//  }
//}