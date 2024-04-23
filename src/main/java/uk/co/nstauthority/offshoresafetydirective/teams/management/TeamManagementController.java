package uk.co.nstauthority.offshoresafetydirective.teams.management;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authorisation.AccessibleByServiceUsers;
import uk.co.nstauthority.offshoresafetydirective.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.offshoresafetydirective.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.streamutil.StreamUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.Team;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamQueryService;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;
import uk.co.nstauthority.offshoresafetydirective.teams.management.access.InvokingUserCanManageTeam;
import uk.co.nstauthority.offshoresafetydirective.teams.management.access.InvokingUserCanViewTeam;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.AddMemberForm;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.AddMemberFormValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.MemberRolesForm;
import uk.co.nstauthority.offshoresafetydirective.teams.management.form.MemberRolesFormValidator;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamTypeView;
import uk.co.nstauthority.offshoresafetydirective.teams.management.view.TeamView;

@RestController
@RequestMapping("/team-management")
public class TeamManagementController {

  private final TeamManagementService teamManagementService;
  private final TeamQueryService teamQueryService;
  private final MemberRolesFormValidator memberRolesFormValidator;
  private final AddMemberFormValidator addMemberFormValidator;
  private final EnergyPortalConfiguration energyPortalConfiguration;

  public TeamManagementController(TeamManagementService teamManagementService, TeamQueryService teamQueryService,
                                  MemberRolesFormValidator memberRolesFormValidator,
                                  AddMemberFormValidator addMemberFormValidator,
                                  EnergyPortalConfiguration energyPortalConfiguration) {
    this.teamManagementService = teamManagementService;
    this.teamQueryService = teamQueryService;
    this.memberRolesFormValidator = memberRolesFormValidator;
    this.addMemberFormValidator = addMemberFormValidator;
    this.energyPortalConfiguration = energyPortalConfiguration;
  }

  @GetMapping
  @AccessibleByServiceUsers
  public ModelAndView renderTeamTypeList(ServiceUserDetail user) {

    var teamTypes = new HashSet<>(teamManagementService.getTeamTypesUserIsMemberOf(user.wuaId()));

    if (teamQueryService.userHasStaticRole(user.wuaId(), TeamType.REGULATOR, Role.THIRD_PARTY_TEAM_MANAGER)) {
      teamTypes.add(TeamType.ORGANISATION_GROUP);
      teamTypes.add(TeamType.CONSULTEE);
    }

    if (teamTypes.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No manageable teams for wuaId %d".formatted(user.wuaId()));
    }

    if (teamTypes.size() == 1) {
      // redirect to manage that team type directly
      return ReverseRouter.redirect(on(TeamManagementController.class)
          .renderTeamsOfType(teamTypes.stream().findFirst().get().getUrlSlug(), user));
    }

    var teamTypeViews = teamTypes.stream()
        .map(teamType -> new TeamTypeView(
            teamType.getDisplayName(),
            ReverseRouter.route(on(TeamManagementController.class).renderTeamsOfType(teamType.getUrlSlug(), user)))
        )
        .sorted(Comparator.comparing(teamTypeView -> teamTypeView.teamTypeName().toLowerCase()))
        .toList();

    return new ModelAndView("osd/teamManagement/teamTypes")
        .addObject("teamTypeViews", teamTypeViews);
  }

  @GetMapping("/{teamTypeSlug}")
  @AccessibleByServiceUsers
  public ModelAndView renderTeamsOfType(@PathVariable String teamTypeSlug, ServiceUserDetail user) {
    var teamType = TeamType.fromUrlSlug(teamTypeSlug)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No team type for url slug %s".formatted(teamTypeSlug)));

    if (teamType.isScoped()) {
      // if it's a scoped team, show the list of instances

      boolean userCanCreateOrgs = teamQueryService.userHasStaticRole(
          user.wuaId(),
          TeamType.REGULATOR,
          Role.THIRD_PARTY_TEAM_MANAGER
      );

      Set<Team> teams = new HashSet<>(teamManagementService.getScopedTeamsOfTypeUserIsMemberOf(teamType, user.wuaId()));

      if (teams.isEmpty() && !userCanCreateOrgs) {
        // If user can create organisations, don't error as they need to be able to create new teams.
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No manageable teams of type %s for wuaId %d".formatted(teamType, user.wuaId()));
      }

      if (teams.size() == 1 && !userCanCreateOrgs) {
        return ReverseRouter.redirect(on(TeamManagementController.class)
            .renderTeamMemberList(teams.stream().findFirst().get().getId(), null));
      }

      var teamsViews = teams.stream()
          .map(team -> new TeamView(
              team.getName(),
              ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null)))
          )
          .sorted(Comparator.comparing(teamView -> teamView.teamName().toLowerCase()))
          .toList();

      var modelAndView = new ModelAndView("osd/teamManagement/teamInstances")
          .addObject("teamViews", teamsViews);

      if (userCanCreateOrgs) {
        modelAndView.addObject("createNewInstanceUrl", teamType.getCreateNewInstanceRoute());
      }

      return modelAndView;

    } else {
      // if it's a static team, redirect to the single instance
      var team = teamManagementService.getStaticTeamOfTypeUserIsMemberOf(teamType, user.wuaId())
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              "No manageable team of type %s for wuaId %d".formatted(teamType, user.wuaId())));

      return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
    }
  }

  @GetMapping("/team/{teamId}")
  @InvokingUserCanViewTeam
  public ModelAndView renderTeamMemberList(@PathVariable UUID teamId, ServiceUserDetail user) {

    var team = getTeamOrThrow(teamId);
    var teamMemberViews = teamManagementService.getTeamMemberViewsForTeam(team);

    return new ModelAndView("osd/teamManagement/teamMembers")
        .addObject("teamName", team.getName())
        .addObject("teamMemberViews", teamMemberViews)
        .addObject("rolesInTeam", team.getTeamType().getAllowedRoles())
        .addObject("canManageTeam", teamManagementService.canManageTeam(team, user.wuaId()))
        .addObject(
            "addMemberUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderAddMemberToTeam(team.getId(), null))
        );
  }

  @GetMapping("/team/{teamId}/add-member")
  @InvokingUserCanManageTeam
  public ModelAndView renderAddMemberToTeam(@PathVariable UUID teamId,
                                            @ModelAttribute("form") AddMemberForm form) {
    var team = getTeamOrThrow(teamId);
    return new ModelAndView("osd/teamManagement/addMember")
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null))
        )
        .addObject("registerUrl", energyPortalConfiguration.registrationUrl());
  }

  @PostMapping("/team/{teamId}/add-member")
  @InvokingUserCanManageTeam
  public ModelAndView handleAddMemberToTeam(@PathVariable UUID teamId,
                                            @ModelAttribute("form") AddMemberForm form,
                                            BindingResult bindingResult) {
    if (!addMemberFormValidator.isValid(form, bindingResult)) {
      return new ModelAndView("osd/teamManagement/addMember")
          .addObject(
              "cancelUrl",
              ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(teamId, null))
          )
          .addObject("registerUrl", energyPortalConfiguration.registrationUrl());
    }

    var wuaId = teamManagementService.getEnergyPortalUser(form.getUsername()).stream()
        .filter(user -> !user.isSharedAccount() && user.canLogin())
        .map(EnergyPortalUserDto::webUserAccountId)
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "user with username %s not found or is shared account".formatted(form.getUsername()))
        );

    return ReverseRouter.redirect(on(TeamManagementController.class).renderUserTeamRoles(teamId, wuaId, null));
  }

  @GetMapping("/team/{teamId}/member/{wuaId}")
  @InvokingUserCanManageTeam
  public ModelAndView renderUserTeamRoles(@PathVariable UUID teamId,
                                          @PathVariable Long wuaId,
                                          @ModelAttribute("form") MemberRolesForm form) {
    var team = getTeamOrThrow(teamId);
    var teamMemberView = teamManagementService.getTeamMemberView(team, wuaId);

    form.setRoles(teamMemberView.roles().stream().map(Role::name).toList());

    return getUserTeamRolesModelAndView(team, wuaId);
  }

  @PostMapping("/team/{teamId}/member/{wuaId}")
  @InvokingUserCanManageTeam
  public ModelAndView updateUserTeamRoles(@PathVariable UUID teamId,
                                          @PathVariable Long wuaId,
                                          @ModelAttribute("form") MemberRolesForm form,
                                          BindingResult bindingResult) {
    var team = getTeamOrThrow(teamId);

    if (!memberRolesFormValidator.isValid(form, wuaId, team, bindingResult)) {
      return getUserTeamRolesModelAndView(team, wuaId);
    }

    var roles = form.getRoles().stream()
        .map(Role::valueOf)
        .toList();

    teamManagementService.setUserTeamRoles(wuaId, team, roles);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  @GetMapping("/team/{teamId}/member/{wuaId}/remove")
  @InvokingUserCanManageTeam
  public ModelAndView renderRemoveTeamMember(@PathVariable UUID teamId, @PathVariable Long wuaId) {
    var team = getTeamOrThrow(teamId);

    var teamMemberView = teamManagementService.getTeamMemberView(team, wuaId);
    var canRemoveTeamMember = teamManagementService.willManageTeamRoleBePresentAfterMemberRemoval(team, wuaId);

    return new ModelAndView("osd/teamManagement/removeMember")
        .addObject("teamMemberView", teamMemberView)
        .addObject("teamName", team.getName())
        .addObject("canRemoveTeamMember", canRemoveTeamMember)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null))
        );
  }

  @PostMapping("/team/{teamId}/member/{wuaId}/remove")
  @InvokingUserCanManageTeam
  public ModelAndView handleRemoveTeamMember(@PathVariable UUID teamId, @PathVariable Long wuaId) {
    var team = getTeamOrThrow(teamId);
    teamManagementService.removeUserFromTeam(wuaId, team);
    return ReverseRouter.redirect(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null));
  }

  private ModelAndView getUserTeamRolesModelAndView(Team team, Long wuaId) {
    var teamMemberView = teamManagementService.getTeamMemberView(team, wuaId);
    var availableRoles = team.getTeamType().getAllowedRoles();

    Map<String, String> rolesNamesMap = availableRoles.stream()
        .collect(StreamUtil.toLinkedHashMap(Enum::name, Role::getName));

    return new ModelAndView("osd/teamManagement/editMemberRoles")
        .addObject("rolesNamesMap", rolesNamesMap)
        .addObject("rolesInTeam", availableRoles)
        .addObject("teamMemberView", teamMemberView)
        .addObject(
            "cancelUrl",
            ReverseRouter.route(on(TeamManagementController.class).renderTeamMemberList(team.getId(), null))
        );
  }

  private Team getTeamOrThrow(UUID teamId) {
    return teamManagementService.getTeam(teamId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team with id %s not found".formatted(teamId)));
  }
}