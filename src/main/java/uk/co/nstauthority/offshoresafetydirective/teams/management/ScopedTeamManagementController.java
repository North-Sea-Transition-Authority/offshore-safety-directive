package uk.co.nstauthority.offshoresafetydirective.teams.management;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.Optional;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchItem;
import uk.co.nstauthority.offshoresafetydirective.fds.RestSearchResult;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamScopeReference;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.access.InvokingUserHasStaticRole;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.NewOrganisationGroupTeamForm;

@Controller
@RequestMapping("/team-management")
public class ScopedTeamManagementController {

  private final TeamManagementService teamManagementService;
  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;
  private final TeamQueryService teamQueryService;

  public ScopedTeamManagementController(TeamManagementService teamManagementService,
                                        PortalOrganisationGroupQueryService portalOrganisationGroupQueryService,
                                        TeamQueryService teamQueryService) {
    this.teamManagementService = teamManagementService;
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
    this.teamQueryService = teamQueryService;
  }

  @GetMapping("/organisation/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.THIRD_PARTY_TEAM_MANAGER)
  public ModelAndView renderCreateNewOrganisationGroupTeam(@ModelAttribute("form") NewOrganisationGroupTeamForm form) {
    return getModelAndView();
  }

  @PostMapping("/organisation/new")
  @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.THIRD_PARTY_TEAM_MANAGER)
  public ModelAndView handleCreateNewOrganisationGroupTeam(@Valid @ModelAttribute("form") NewOrganisationGroupTeamForm form,
                                                           BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      return getModelAndView();
    }

    Optional<Team> existingScopedTeam = teamQueryService.getScopedTeam(
        TeamType.ORGANISATION_GROUP,
        TeamScopeReference.from(form.getOrgGroupId(), "ORGANISATION_GROUP")
    );

    if (existingScopedTeam.isPresent()) {
      return ReverseRouter.redirect(on(TeamManagementController.class)
          .renderTeamMemberList(existingScopedTeam.get().getId(), null));
    }

    PortalOrganisationGroupDto organisationGroup = portalOrganisationGroupQueryService
        .findOrganisationById(Integer.parseInt(form.getOrgGroupId()), new RequestPurpose("Find org group to create team"))
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Org group with id %s not found".formatted(form.getOrgGroupId())
        ));

    var scopeRef = TeamScopeReference.from(organisationGroup.organisationGroupId(), "ORGANISATION_GROUP");
    var team = teamManagementService.createScopedTeam(organisationGroup.name(), TeamType.ORGANISATION_GROUP, scopeRef);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  @GetMapping("/organisation/search")
  @ResponseBody
  public Object searchOrganisationGroups(@RequestParam("term") String searchTerm) {

    var selectorResults = portalOrganisationGroupQueryService
        .queryOrganisationByName(searchTerm, new RequestPurpose("Find org group to create team"))
        .stream()
        .sorted(Comparator.comparing(PortalOrganisationGroupDto::name, String.CASE_INSENSITIVE_ORDER))
        .map(organisationGroup ->
            new RestSearchItem(organisationGroup.organisationGroupId(), organisationGroup.name())
        )
        .toList();

    return new RestSearchResult(selectorResults);
  }

  private ModelAndView getModelAndView() {
    return new ModelAndView("osd/teamManagement/scoped/createOrganisationTeam")
        .addObject(
            "organisationSearchUrl",
            StringUtils.stripEnd(
                ReverseRouter.route(on(ScopedTeamManagementController.class).searchOrganisationGroups(null)),
                "?term"
            )
        );
  }
}
