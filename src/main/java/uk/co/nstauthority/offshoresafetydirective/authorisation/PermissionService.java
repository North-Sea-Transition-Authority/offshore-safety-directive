package uk.co.nstauthority.offshoresafetydirective.authorisation;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailPersistenceService;
import uk.co.nstauthority.offshoresafetydirective.teams.PortalTeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScope;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@Service
public class PermissionService {

  private static final RequestPurpose REQUEST_PURPOSE = new RequestPurpose("Get organisations that applicant is a part of");
  private final TeamMemberService teamMemberService;
  private final ApplicantDetailPersistenceService applicantDetailPersistenceService;
  private final PortalOrganisationGroupQueryService organisationGroupQueryService;
  private final TeamScopeService teamScopeService;

  @Autowired
  public PermissionService(TeamMemberService teamMemberService,
                           ApplicantDetailPersistenceService applicantDetailPersistenceService,
                           PortalOrganisationGroupQueryService organisationGroupQueryService,
                           TeamScopeService teamScopeService) {
    this.teamMemberService = teamMemberService;
    this.applicantDetailPersistenceService = applicantDetailPersistenceService;
    this.organisationGroupQueryService = organisationGroupQueryService;
    this.teamScopeService = teamScopeService;
  }

  public boolean hasPermission(ServiceUserDetail user, Collection<RolePermission> requiredPermissions) {
    var teamMembers =  teamMemberService.getUserAsTeamMembers(user);

    if (teamMembers == null) {
      return false;
    }

    return teamMembers
        .stream()
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(teamRole -> teamRole.getRolePermissions().stream())
        .anyMatch(requiredPermissions::contains);
  }

  public boolean hasPermission(ServiceUserDetail user, RolePermission requiredPermission) {
    return hasPermission(user, Collections.singleton(requiredPermission));
  }

  public Map<TeamType, Collection<RolePermission>> getTeamTypePermissionMap(ServiceUserDetail user) {
    var roles = teamMemberService.getUserAsTeamMembers(user);
    Map<TeamType, List<TeamMember>> teamTypeMemberMap = roles.stream()
        .collect(Collectors.groupingBy(teamMember -> teamMember.teamView().teamType()));

    return teamTypeMemberMap.entrySet()
        .stream()
        .map(entry -> Map.entry(entry.getKey(), getRolePermissionsFromMembers(entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Set<RolePermission> getRolePermissionsFromMembers(Collection<TeamMember> members) {
    return members.stream()
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(teamRole -> teamRole.getRolePermissions().stream())
        .collect(Collectors.toSet());
  }

  public boolean hasPermissionForTeam(TeamId teamId, ServiceUserDetail user, Collection<RolePermission> requiredPermissions) {
    var teamMembers = teamMemberService.getUserAsTeamMembers(user);

    if (teamMembers == null) {
      return false;
    }

    return teamMembers
        .stream()
        .filter(teamMember -> teamMember.teamView().teamId().equals(teamId))
        .map(TeamMember::roles)
        .flatMap(Collection::stream)
        .map(TeamRole::getRolePermissions)
        .flatMap(Collection::stream)
        .anyMatch(requiredPermissions::contains);
  }

  public boolean hasPermissionForNomination(NominationDetail nominationDetail,
                                            ServiceUserDetail userDetail,
                                            Collection<RolePermission> requiredPermissions) {
    var applicantPortalOrganisationId = applicantDetailPersistenceService.getApplicantDetail(nominationDetail)
        .map(ApplicantDetail::getPortalOrganisationId)
        .orElseThrow(() -> new EntityNotFoundException(
            "No applicant detail found for nomination detail with id %s".formatted(nominationDetail.getId().toString())
        ));

    var organisationGroupsForApplicant = organisationGroupQueryService.getOrganisationGroupsByOrganisationId(
            applicantPortalOrganisationId,
            REQUEST_PURPOSE
        )
        .stream()
        .map(PortalOrganisationGroupDto::organisationGroupId)
        .map(String::valueOf)
        .toList();

    var applicantOrganisationTeamIds = teamScopeService.getTeamScope(
            organisationGroupsForApplicant,
            PortalTeamType.ORGANISATION_GROUP
        )
        .stream()
        .map(TeamScope::getTeam)
        .map(Team::toTeamId)
        .toList();

    return teamMemberService.getUserAsTeamMembers(userDetail)
        .stream()
        .filter(teamMember -> applicantOrganisationTeamIds.contains(teamMember.teamView().teamId()))
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(userPermissionsForTeam -> userPermissionsForTeam.getRolePermissions().stream())
        .anyMatch(requiredPermissions::contains);
  }
}
