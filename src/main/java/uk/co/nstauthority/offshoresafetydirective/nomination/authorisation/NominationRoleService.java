package uk.co.nstauthority.offshoresafetydirective.nomination.authorisation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailAccessService;
import uk.co.nstauthority.offshoresafetydirective.nomination.applicantdetail.ApplicantDetailDto;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRole;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@Service
public class NominationRoleService {

  private final TeamQueryService teamQueryService;

  private final ApplicantDetailAccessService applicantDetailAccessService;

  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  public NominationRoleService(TeamQueryService teamQueryService,
                               ApplicantDetailAccessService applicantDetailAccessService,
                               PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.teamQueryService = teamQueryService;
    this.applicantDetailAccessService = applicantDetailAccessService;
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  public boolean userCanStartNomination(long wuaId) {
    return teamQueryService.getTeamRolesForUser(wuaId)
        .stream()
        .filter(teamRole -> TeamType.ORGANISATION_GROUP.equals(teamRole.getTeam().getTeamType()))
        .anyMatch(teamRole -> Role.NOMINATION_SUBMITTER.equals(teamRole.getRole()));
  }

  public boolean userHasRoleInApplicantOrganisationGroupTeam(
      long userWuaId,
      NominationDetail nominationDetail,
      Role role
  ) {
    return userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        userWuaId,
        nominationDetail,
        Set.of(role)
    );
  }

  public boolean userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
      long userWuaId,
      NominationDetail nominationDetail,
      Set<Role> roles
  ) {

    if (!teamQueryService.areRolesValidForTeamType(roles, TeamType.ORGANISATION_GROUP)) {
      throw new IllegalArgumentException(
          "Not all roles %s are valid for the %s team type".formatted(roles, TeamType.ORGANISATION_GROUP)
      );
    }

    ApplicantDetailDto applicantDetail = applicantDetailAccessService
        .getApplicantDetailDtoByNominationDetail(nominationDetail)
        .orElseThrow(() -> new IllegalArgumentException(
            "Could not find applicant detail for nomination detail with ID %s".formatted(nominationDetail.getId())
        ));

    var requestPurpose = new RequestPurpose("Query applicant organisation group");

    Set<PortalOrganisationGroupDto> organisationGroups = portalOrganisationGroupQueryService
        .getOrganisationGroupsByOrganisationId(applicantDetail.applicantOrganisationId().id(), requestPurpose);

    if (CollectionUtils.isEmpty(organisationGroups)) {
      return false;
    }

    Set<TeamScopeReference> teamScopeReferences = organisationGroups
        .stream()
        .map(organisationGroup -> TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP"))
        .collect(Collectors.toSet());

    // An organisation unit can only have one group but the API returns a list of groups
    // as this caters for SDK and REG type groups in legacy cases. The API called in this case only returns REG type
    // so this should never loop more than once.
    return teamScopeReferences
        .stream()
        .anyMatch(teamScopeReference -> teamQueryService.userHasAtLeastOneScopedRole(
            userWuaId,
            TeamType.ORGANISATION_GROUP,
            teamScopeReference,
            roles
        ));
  }

  public Set<Role> getUserRolesInApplicantOrganisationGroupTeam(long wuaId, NominationDetail nominationDetail) {

    Set<PortalOrganisationGroupDto> organisationGroups = getOrganisationGroupsForApplicant(nominationDetail);

    if (CollectionUtils.isEmpty(organisationGroups)) {
      return Set.of();
    }

    Set<String> organisationGroupIds = organisationGroups
        .stream()
        .map(PortalOrganisationGroupDto::organisationGroupId)
        .collect(Collectors.toSet());

    return teamQueryService.getTeamRolesForUser(wuaId)
        .stream()
        .filter(teamRole -> TeamType.ORGANISATION_GROUP.equals(teamRole.getTeam().getTeamType()))
        .filter(teamRole -> organisationGroupIds.contains(teamRole.getTeam().getScopeId()))
        .map(TeamRole::getRole)
        .collect(Collectors.toSet());
  }

  public Set<EnergyPortalUserDto> getUsersInApplicantOrganisationTeamWithAnyRoleOf(NominationDetail nominationDetail,
                                                                                   Set<Role> roles) {

    if (!teamQueryService.areRolesValidForTeamType(roles, TeamType.ORGANISATION_GROUP)) {
      throw new IllegalArgumentException(
          "Not all roles %s are valid for the %s team type".formatted(roles, TeamType.ORGANISATION_GROUP)
      );
    }

    Set<PortalOrganisationGroupDto> organisationGroups = getOrganisationGroupsForApplicant(nominationDetail);

    if (CollectionUtils.isEmpty(organisationGroups)) {
      return Set.of();
    }

    Set<TeamScopeReference> teamScopeReferences = organisationGroups
        .stream()
        .map(organisationGroup -> TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP"))
        .collect(Collectors.toSet());

    Set<EnergyPortalUserDto> users = new HashSet<>();

    // An organisation unit can only have one group but the API returns a list of groups
    // as this caters for SDK and REG type groups in legacy cases. The API called in this case only returns REG type
    // so this should never loop more than once.
    teamScopeReferences.forEach(teamScopeReference ->
        teamQueryService.getUsersInScopedTeam(TeamType.ORGANISATION_GROUP, teamScopeReference)
            .forEach((role, usersWithRole) -> {
              if (roles.contains(role)) {
                users.addAll(usersWithRole);
              }
            })
    );

    return users;
  }

  private Set<PortalOrganisationGroupDto> getOrganisationGroupsForApplicant(NominationDetail nominationDetail) {

    ApplicantDetailDto applicantDetail = applicantDetailAccessService
        .getApplicantDetailDtoByNominationDetail(nominationDetail)
        .orElseThrow(() -> new IllegalArgumentException(
            "Could not find applicant detail for nomination detail with ID %s".formatted(nominationDetail.getId())
        ));

    var requestPurpose = new RequestPurpose("Query applicant organisation groups");

    return portalOrganisationGroupQueryService
        .getOrganisationGroupsByOrganisationId(applicantDetail.applicantOrganisationId().id(), requestPurpose);
  }
}
