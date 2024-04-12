package uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;

// TODO OSDOP-811
@Service
public class NominationApplicantTeamService {

  private final ApplicantDetailAccessService applicantDetailAccessService;

  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  NominationApplicantTeamService(ApplicantDetailAccessService applicantDetailAccessService,
                                 PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

//  public Set<Team> getApplicantTeams(NominationDetail nominationDetail) {
//
//    ApplicantDetailDto applicantDetail = applicantDetailAccessService
//        .getApplicantDetailDtoByNominationDetail(nominationDetail)
//        .orElseThrow(() -> new IllegalStateException(
//            "Could not find applicant detail for nomination detail with ID %s".formatted(nominationDetail.getId())
//        ));
//
//    var requestPurpose = new RequestPurpose("Query applicant organisation group");
//
//    Set<String> applicantOrganisationGroupIds = portalOrganisationGroupQueryService
//        .getOrganisationGroupsByOrganisationId(applicantDetail.applicantOrganisationId().id(), requestPurpose)
//        .stream()
//        .map(PortalOrganisationGroupDto::organisationGroupId)
//        .map(String::valueOf)
//        .collect(Collectors.toSet());
//
//    if (CollectionUtils.isEmpty(applicantOrganisationGroupIds)) {
//      return Collections.emptySet();
//    }
//
//    return teamScopeService.getTeamScope(applicantOrganisationGroupIds, PortalTeamType.ORGANISATION_GROUP)
//        .stream()
//        .map(TeamScope::getTeam)
//        .collect(Collectors.toSet());
//  }

//  public Set<TeamMemberView> getApplicantTeamMembersWithAnyRoleOf(NominationDetail nominationDetail,
//                                                                  Collection<IndustryTeamRole> roles) {
//
//    Set<Team> applicantTeams = getApplicantTeams(nominationDetail);
//
//    if (CollectionUtils.isEmpty(applicantTeams)) {
//      return Collections.emptySet();
//    }
//
//    return new HashSet<>(teamMemberViewService.getTeamMembersWithRoles(applicantTeams, roles));
//  }
}
