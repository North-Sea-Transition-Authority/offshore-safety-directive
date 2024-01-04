package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationunit.PortalOrganisationDtoTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;

public abstract class AbstractNominationControllerTest extends AbstractControllerTest {

  private static final TeamScope TEAM_SCOPE = TeamScopeTestUtil.builder().build();

  public void givenUserHasNominationPermission(NominationDetail nominationDetail, ServiceUserDetail user) {
    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withPortalOrganisationId(1)
        .build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.ofNullable(applicantDetail));

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder().build();

    when(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationId(eq(applicantDetail.getPortalOrganisationId()), any()))
        .thenReturn(Set.of(organisationGroup));

    when(teamScopeService.getTeamScope(List.of(organisationGroup.organisationGroupId()),
        PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(TEAM_SCOPE));

    var nominationCreatorTeamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(TEAM_SCOPE.getTeam().toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(Collections.singletonList(nominationCreatorTeamMember));
  }

  public void givenUserHasViewPermissionForPostSubmissionNominations(NominationDetail nominationDetail, ServiceUserDetail user) {
    var portalOrgId = 1;
    var organisation = PortalOrganisationDtoTestUtil.builder()
        .withId(portalOrgId)
        .build();

    var organisationGroup = PortalOrganisationGroupDtoTestUtil.builder()
        .withOrganisation(organisation)
        .build();

    var applicantDetail = ApplicantDetailTestUtil.builder()
        .withPortalOrganisationId(portalOrgId)
        .build();

    when(applicantDetailPersistenceService.getApplicantDetail(nominationDetail)).thenReturn(Optional.ofNullable(applicantDetail));

    when(portalOrganisationGroupQueryService.getOrganisationGroupsByOrganisationIds(
        eq(List.of(Integer.valueOf(TEAM_SCOPE.getPortalId()))), any()))
        .thenReturn(List.of(organisationGroup));

    when(teamScopeService.getTeamScopesFromTeamIds(List.of(TEAM_SCOPE.getTeam().getUuid()),
        PortalTeamType.ORGANISATION_GROUP))
        .thenReturn(List.of(TEAM_SCOPE));

    when(nominationDetailService.getLatestNominationDetailWithStatuses(
        new NominationId(nominationDetail.getNomination().getId()),
        NominationStatus.getAllStatusesForSubmissionStage(NominationStatusSubmissionStage.POST_SUBMISSION)
    ))
        .thenReturn(Optional.of(nominationDetail));

    var nominationCreatorTeamMember = TeamMemberTestUtil.Builder()
        .withRole(IndustryTeamRole.NOMINATION_SUBMITTER)
        .withTeamId(TEAM_SCOPE.getTeam().toTeamId())
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(Collections.singletonList(nominationCreatorTeamMember));
  }

  public Team getTeam() {
    return TEAM_SCOPE.getTeam();
  }
}
