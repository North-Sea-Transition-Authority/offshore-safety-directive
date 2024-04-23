package uk.co.nstauthority.offshoresafetydirective.nomination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Set;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

public abstract class AbstractNominationControllerTest extends AbstractControllerTest {

  protected void givenUserCanStartNomination(long wuaId) {
    when(nominationRoleService.userCanStartNomination(wuaId))
        .thenReturn(true);
  }

  protected void givenUserCannotStartNomination(long wuaId) {
    when(nominationRoleService.userCanStartNomination(wuaId))
        .thenReturn(false);
  }

  protected void givenLatestNominationDetail(NominationDetail nominationDetail) {
    when(nominationDetailService.getLatestNominationDetail(
        new NominationId(nominationDetail.getNomination().getId())
    ))
        .thenReturn(nominationDetail);
  }

  protected void givenUserHasRoleInApplicantTeamForDraftNominationAccess(long wuaId, NominationDetail nominationDetail) {
    when(teamQueryService.areRolesValidForTeamType(
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR),
        TeamType.ORGANISATION_GROUP
    ))
        .thenReturn(true);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        wuaId,
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR)
    ))
        .thenReturn(true);
  }

  protected void givenUserHasRoleInApplicantTeam(long wuaId, NominationDetail nominationDetail, Role role) {

    when(teamQueryService.areRolesValidForTeamType(Set.of(role), TeamType.ORGANISATION_GROUP))
        .thenReturn(true);

    when(nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(wuaId, nominationDetail, role))
        .thenReturn(true);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(wuaId, nominationDetail, Set.of(role)))
        .thenReturn(true);
  }

  protected void givenUserDoesNotHaveRoleInApplicantTeam(long wuaId, NominationDetail nominationDetail, Role role) {

    when(teamQueryService.areRolesValidForTeamType(Set.of(role), TeamType.ORGANISATION_GROUP))
        .thenReturn(true);

    when(nominationRoleService.userHasRoleInApplicantOrganisationGroupTeam(wuaId, nominationDetail, role))
        .thenReturn(false);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(wuaId, nominationDetail, Set.of(role)))
        .thenReturn(false);
  }

  protected void givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(long wuaId, NominationDetail nominationDetail) {
    when(teamQueryService.areRolesValidForTeamType(
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR),
        TeamType.ORGANISATION_GROUP
    ))
        .thenReturn(true);

    when(nominationRoleService.userHasAtLeastOneRoleInApplicantOrganisationGroupTeam(
        wuaId,
        nominationDetail,
        Set.of(Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR)
    ))
        .thenReturn(false);
  }

  protected static ResultMatcher redirectionToTaskList(NominationId nominationId) {
    return result -> assertThat(result.getResponse().getRedirectedUrl())
        .isEqualTo(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(nominationId)));
  }

  protected void givenUserIsNominationManager(long wuaId) {
    when(teamQueryService.userHasStaticRole(wuaId, TeamType.REGULATOR, Role.NOMINATION_MANAGER))
        .thenReturn(true);
  }

  protected void givenUserIsNotNominationManager(long wuaId) {
    when(teamQueryService.userHasStaticRole(wuaId, TeamType.REGULATOR, Role.NOMINATION_MANAGER))
        .thenReturn(false);
  }

  protected void givenUserHasAtLeastOneRoleInStaticTeam(long wuaId, TeamType teamType, Set<Role> roles) {

    if (teamType.isScoped()) {
      throw new IllegalArgumentException("Team type is scoped, expected static");
    }

    when(teamQueryService.userHasAtLeastOneStaticRole(wuaId, teamType, roles))
        .thenReturn(true);
  }

  protected void givenUserDoesNotHaveAtLeastOneRoleInStaticTeam(long wuaId, TeamType teamType, Set<Role> roles) {

    if (teamType.isScoped()) {
      throw new IllegalArgumentException("Team type is scoped, expected static");
    }

    when(teamQueryService.userHasAtLeastOneStaticRole(wuaId, teamType, roles))
        .thenReturn(false);
  }
}
