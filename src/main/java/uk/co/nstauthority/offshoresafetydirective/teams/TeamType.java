package uk.co.nstauthority.offshoresafetydirective.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.teams.management.ScopedTeamManagementController;

public enum TeamType {

  REGULATOR(
      "Licensing authority",
      "licensing-authority",
      false,
      List.of(
          Role.TEAM_MANAGER,
          Role.THIRD_PARTY_TEAM_MANAGER,
          Role.NOMINATION_MANAGER,
          Role.APPOINTMENT_MANAGER,
          Role.VIEW_ANY_NOMINATION
      ),
      null
  ),
  CONSULTEE(
      "Consultees",
      "consultee",
      false,
      List.of(Role.TEAM_MANAGER, Role.CONSULTATION_MANAGER, Role.CONSULTATION_PARTICIPANT),
      null
  ),
  ORGANISATION_GROUP(
      "Organisations",
      "organisation",
      true,
      List.of(Role.TEAM_MANAGER, Role.NOMINATION_SUBMITTER, Role.NOMINATION_EDITOR, Role.NOMINATION_VIEWER),
          () -> ReverseRouter.route(on(ScopedTeamManagementController.class)
              .renderCreateNewOrganisationGroupTeam(null))
  );

  private final String displayName;
  private final String urlSlug;
  private final boolean isScoped;
  private final List<Role> allowedRoles;
  private final Supplier<String> createNewInstanceRoute;

  TeamType(String displayName, String urlSlug, boolean isScoped, List<Role> allowedRoles,
           Supplier<String> createNewInstanceRoute) {
    this.displayName = displayName;
    this.urlSlug = urlSlug;
    this.isScoped = isScoped;
    this.allowedRoles = allowedRoles;
    this.createNewInstanceRoute = createNewInstanceRoute;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrlSlug() {
    return urlSlug;
  }

  public boolean isScoped() {
    return isScoped;
  }

  public List<Role> getAllowedRoles() {
    return allowedRoles;
  }

  public String getCreateNewInstanceRoute() {
    return createNewInstanceRoute.get();
  }

  public static Optional<TeamType> fromUrlSlug(String urlSlug) {
    return Arrays.stream(values())
        .filter(teamType -> teamType.urlSlug.equals(urlSlug))
        .findFirst();
  }
}
