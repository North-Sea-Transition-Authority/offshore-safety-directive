package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberView;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberViewService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry.IndustryTeamRole;

@Service
public class NominationApplicantTeamService {

  private final ApplicantDetailAccessService applicantDetailAccessService;

  private final TeamScopeService teamScopeService;

  private final TeamMemberViewService teamMemberViewService;

  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  NominationApplicantTeamService(ApplicantDetailAccessService applicantDetailAccessService,
                                 TeamScopeService teamScopeService,
                                 TeamMemberViewService teamMemberViewService,
                                 PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.teamScopeService = teamScopeService;
    this.teamMemberViewService = teamMemberViewService;
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  public Set<Team> getApplicantTeams(NominationDetail nominationDetail) {

    ApplicantDetailDto applicantDetail = applicantDetailAccessService
        .getApplicantDetailDtoByNominationDetail(nominationDetail)
        .orElseThrow(() -> new IllegalStateException(
            "Could not find applicant detail for nomination detail with ID %s".formatted(nominationDetail.getId())
        ));

    var requestPurpose = new RequestPurpose("Query applicant organisation group");

    Set<String> applicantOrganisationGroupIds = portalOrganisationGroupQueryService
        .getOrganisationGroupsByOrganisationId(applicantDetail.applicantOrganisationId().id(), requestPurpose)
        .stream()
        .map(PortalOrganisationGroupDto::organisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    if (CollectionUtils.isEmpty(applicantOrganisationGroupIds)) {
      return Collections.emptySet();
    }

    return teamScopeService.getTeamScope(applicantOrganisationGroupIds, PortalTeamType.ORGANISATION_GROUP)
        .stream()
        .map(TeamScope::getTeam)
        .collect(Collectors.toSet());
  }

  public Set<TeamMemberView> getApplicantTeamMembersWithAnyRoleOf(NominationDetail nominationDetail,
                                                                  Collection<IndustryTeamRole> roles) {

    Set<Team> applicantTeams = getApplicantTeams(nominationDetail);

    if (CollectionUtils.isEmpty(applicantTeams)) {
      return Collections.emptySet();
    }

    return new HashSet<>(teamMemberViewService.getTeamMembersWithRoles(applicantTeams, roles));
  }
}
