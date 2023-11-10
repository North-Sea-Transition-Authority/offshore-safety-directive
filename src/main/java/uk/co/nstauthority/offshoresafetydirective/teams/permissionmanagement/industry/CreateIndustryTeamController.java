package uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.industry;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.offshoresafetydirective.authorisation.HasPermission;
import uk.co.nstauthority.offshoresafetydirective.controllerhelper.ControllerHelperService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.portalorganisation.organisationgroup.PortalOrganisationGroupRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.permissionmanagement.RolePermission;

@Controller
@RequestMapping("/permission-management/industry/new")
@HasPermission(permissions = RolePermission.MANAGE_INDUSTRY_TEAMS)
public class CreateIndustryTeamController {

  static final RequestPurpose INDUSTRY_TEAM_PURPOSE = new RequestPurpose("Create industry team");
  private final CreateIndustryTeamValidator createIndustryTeamValidator;
  private final ControllerHelperService controllerHelperService;
  private final IndustryTeamService industryTeamService;
  private final PortalOrganisationGroupQueryService portalOrganisationGroupQueryService;

  @Autowired
  public CreateIndustryTeamController(CreateIndustryTeamValidator createIndustryTeamValidator,
                                      ControllerHelperService controllerHelperService,
                                      IndustryTeamService industryTeamService,
                                      PortalOrganisationGroupQueryService portalOrganisationGroupQueryService) {
    this.createIndustryTeamValidator = createIndustryTeamValidator;
    this.controllerHelperService = controllerHelperService;
    this.industryTeamService = industryTeamService;
    this.portalOrganisationGroupQueryService = portalOrganisationGroupQueryService;
  }

  @GetMapping
  public ModelAndView renderCreateIndustryTeam() {
    return getModelAndView(new CreateIndustryTeamForm());
  }

  @PostMapping
  public ModelAndView createIndustryTeam(@ModelAttribute("form") CreateIndustryTeamForm form,
                                         BindingResult bindingResult) {

    createIndustryTeamValidator.validate(form, bindingResult);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getModelAndView(form), form, () -> {

      var orgGroup = portalOrganisationGroupQueryService.findOrganisationById(form.getOrgGroupId(), INDUSTRY_TEAM_PURPOSE)
          .orElseThrow(() -> new ResponseStatusException(
              HttpStatus.INTERNAL_SERVER_ERROR,
              "Organisation group with id [%d] does not exist".formatted(form.getOrgGroupId())
          ));

      var existingTeam = industryTeamService.findIndustryTeamForOrganisationGroup(orgGroup);

      if (existingTeam.isEmpty()) {
        var team = industryTeamService.createIndustryTeam(orgGroup);
        existingTeam = Optional.of(team);
      }

      return ReverseRouter.redirect(on(IndustryTeamManagementController.class)
          .renderMemberList(existingTeam.get().toTeamId()));
    });
  }

  private ModelAndView getModelAndView(CreateIndustryTeamForm form) {
    return new ModelAndView("osd/permissionmanagement/createIndustryTeam")
        .addObject("pageTitle", "Select an organisation")
        .addObject("form", form)
        .addObject(
            "orgGroupRestUrl",
            RestApiUtil.route(on(PortalOrganisationGroupRestController.class).searchPortalOrganisationGroups(null))
        );
  }

}
