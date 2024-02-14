package uk.co.nstauthority.offshoresafetydirective.authorisation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.NominationApplicantTeamService;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamId;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMember;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamMemberService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.TeamRole;

@Service
public class PermissionService {
  private final TeamMemberService teamMemberService;
  private final NominationApplicantTeamService nominationApplicantTeamService;

  @Autowired
  public PermissionService(TeamMemberService teamMemberService,
                           NominationApplicantTeamService nominationApplicantTeamService) {
    this.teamMemberService = teamMemberService;
    this.nominationApplicantTeamService = nominationApplicantTeamService;
  }

  public boolean hasPermission(ServiceUserDetail user, Collection<RolePermission> requiredPermissions) {
    var teamMembers = teamMemberService.getUserAsTeamMembers(user);

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
    return getPermissionsForNomination(nominationDetail, userDetail)
        .stream()
        .anyMatch(requiredPermissions::contains);
  }

  public Set<RolePermission> getPermissionsForNomination(NominationDetail nominationDetail,
                                                         ServiceUserDetail userDetail) {

    Set<TeamId> applicantTeams = nominationApplicantTeamService.getApplicantTeams(nominationDetail)
        .stream()
        .map(Team::toTeamId)
        .collect(Collectors.toSet());

    return teamMemberService.getUserAsTeamMembers(userDetail)
        .stream()
        .filter(teamMember -> applicantTeams.contains(teamMember.teamView().teamId()))
        .flatMap(teamMember -> teamMember.roles().stream())
        .flatMap(userPermissionsForTeam -> userPermissionsForTeam.getRolePermissions().stream())
        .collect(Collectors.toSet());
  }
}
