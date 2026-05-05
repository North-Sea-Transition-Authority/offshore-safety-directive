package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;
import uk.co.nstauthority.offshoresafetydirective.teams.Role;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamRoleTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamTestUtil;
import uk.co.nstauthority.offshoresafetydirective.teams.TeamType;

@ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail WORK_AREA_USER = ServiceUserDetailTestUtil.Builder().build();
  private static final WorkAreaFilter DEFAULT_FILTER = WorkAreaFilter.defaultFilter(false);
  private static final WorkAreaFilter MODIFIED_FILTER = new WorkAreaFilter(EnumSet.of(NominationStatus.APPOINTED));
  private static final String SESSION_KEY = "workAreaSession";

  @MockitoBean
  private WorkAreaItemService workAreaItemService;

  private MockHttpSession session;

  @BeforeEach
  void setUp() {
    session = new MockHttpSession();
  }

  @SecurityTest
  void getWorkArea_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null))))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getWorkArea_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
            .with(user(WORK_AREA_USER))
        )
        .andExpect(status().isOk());
  }

  @Test
  void getWorkArea_whenUserIsNominationSubmitter_thenAssertModelProperties() throws Exception {

    var workAreaItem = new WorkAreaItem(
        WorkAreaItemType.NOMINATION,
        "heading text",
        "caption text",
        "action url",
        new WorkAreaItemModelProperties()
            .addProperty("status", "status")
            .addProperty("applicantReference", "applicant ref")
            .addProperty("nominationType", "nomination type")
            .addProperty("applicantOrganisation", "applicant org")
            .addProperty("nominationOrganisation", "nominated org")
            .addProperty("hasUpdateRequest", false)
    );

    when(workAreaItemService.getWorkAreaItems(DEFAULT_FILTER)).thenReturn(List.of(workAreaItem));

    when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId()))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
            .with(user(WORK_AREA_USER)
        )
    )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/workarea/workArea"))
        .andExpect(model().attribute(
            "startNominationUrl",
            ReverseRouter.route(on(StartNominationController.class).startNomination())
        ))
        .andExpect(model().attribute("workAreaItems", List.of(workAreaItem)));
  }

   @Test
   void getWorkArea_whenUserIsNotNominationSubmitter_thenAssertModelProperties() throws Exception {

     var workAreaItem = new WorkAreaItem(
         WorkAreaItemType.NOMINATION,
         "heading text",
         "caption text",
         "action url",
         new WorkAreaItemModelProperties()
             .addProperty("status", "status")
             .addProperty("applicantReference", "applicant ref")
             .addProperty("nominationType", "nomination type")
             .addProperty("applicantOrganisation", "applicant org")
             .addProperty("nominationOrganisation", "nominated org")
             .addProperty("hasUpdateRequest", false)
     );

     when(workAreaItemService.getWorkAreaItems(DEFAULT_FILTER)).thenReturn(List.of(workAreaItem));

     when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId()))
         .thenReturn(false);

     mockMvc.perform(
         get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
             .with(user(WORK_AREA_USER)
         )
     )
         .andExpect(status().isOk())
         .andExpect(view().name("osd/workarea/workArea"))
         .andExpect(model().attributeDoesNotExist("startNominationUrl"))
         .andExpect(model().attribute("workAreaItems", List.of(workAreaItem)));
   }

  @Test
  void getWorkArea_noSessionFilter() throws Exception {
    var regTeam = TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build();
    var teamRole = TeamRoleTestUtil.newBuilder().withTeam(regTeam).withRole(Role.NOMINATION_MANAGER).build();

    when(teamQueryService.getTeamRolesForUser(WORK_AREA_USER.wuaId())).thenReturn(Set.of(teamRole));
    when(workAreaItemService.getWorkAreaItems(DEFAULT_FILTER)).thenReturn(List.of());
    when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId())).thenReturn(false);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
                .with(user(WORK_AREA_USER)))
        .andExpectAll(
            status().isOk(),
            view().name("osd/workarea/workArea"),
            model().attribute("statusMap", NominationStatus.getDisplayNameByEnumName(false)),
            model().attribute("workAreaItems", List.of()),
            model().attribute("clearFiltersUrl",
                ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilters(null, null)))
        )
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModelMap().get("form"))
        .usingRecursiveComparison()
        .isEqualTo(WorkAreaFilterForm.fromFilter(DEFAULT_FILTER));
  }

  @Test
  void getWorkArea_sessionFilter() throws Exception {
    session.setAttribute(SESSION_KEY, new WorkAreaSession(MODIFIED_FILTER, false));

    var regTeam = TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build();
    var teamRole = TeamRoleTestUtil.newBuilder().withTeam(regTeam).withRole(Role.NOMINATION_MANAGER).build();

    when(teamQueryService.getTeamRolesForUser(WORK_AREA_USER.wuaId())).thenReturn(Set.of(teamRole));
    when(workAreaItemService.getWorkAreaItems(MODIFIED_FILTER)).thenReturn(List.of());
    when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId())).thenReturn(false);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
                .with(user(WORK_AREA_USER))
                .session(session))
        .andExpectAll(
            status().isOk(),
            view().name("osd/workarea/workArea"),
            model().attribute("statusMap", NominationStatus.getDisplayNameByEnumName(false)),
            model().attribute("workAreaItems", List.of()),
            model().attribute("clearFiltersUrl",
                ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilters(null, null)))
        )
        .andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModelMap().get("form"))
        .usingRecursiveComparison()
        .isEqualTo(WorkAreaFilterForm.fromFilter(MODIFIED_FILTER));
  }

  @ParameterizedTest
  @EnumSource(value = Role.class, names = {"NOMINATION_SUBMITTER", "NOMINATION_EDITOR"}, mode =  EnumSource.Mode.INCLUDE)
  void getWorkArea_whenIndustryUserCanSeeDrafts(Role role) throws Exception {
    var orgTeam = TeamTestUtil.newBuilder().withTeamType(TeamType.ORGANISATION_GROUP).build();
    var teamRole = TeamRoleTestUtil.newBuilder().withTeam(orgTeam).withRole(role).build();
    var draftFilter = WorkAreaFilter.defaultFilter(true);

    when(teamQueryService.getTeamRolesForUser(WORK_AREA_USER.wuaId())).thenReturn(Set.of(teamRole));
    when(workAreaItemService.getWorkAreaItems(draftFilter)).thenReturn(List.of());
    when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId())).thenReturn(true);

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
                .with(user(WORK_AREA_USER)))
        .andExpectAll(
            status().isOk(),
            view().name("osd/workarea/workArea"),
            model().attribute("statusMap", NominationStatus.getDisplayNameByEnumName(true)),
            model().attribute("workAreaItems", List.of()),
            model().attribute("clearFiltersUrl",
                ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilters(null, null)))
        ).andReturn()
        .getModelAndView();

    assertThat(modelAndView).isNotNull();
    assertThat(modelAndView.getModelMap().get("form"))
        .usingRecursiveComparison()
        .isEqualTo(WorkAreaFilterForm.fromFilter(WorkAreaFilter.defaultFilter(true)));
  }

  @Test
  void getWorkArea_whenIndustryUserCannotSeeDrafts() throws Exception {
    var orgTeam = TeamTestUtil.newBuilder().withTeamType(TeamType.ORGANISATION_GROUP).build();
    var teamRole = TeamRoleTestUtil.newBuilder().withTeam(orgTeam).withRole(Role.NOMINATION_VIEWER).build();

    when(teamQueryService.getTeamRolesForUser(WORK_AREA_USER.wuaId())).thenReturn(Set.of(teamRole));
    when(workAreaItemService.getWorkAreaItems(DEFAULT_FILTER)).thenReturn(List.of());
    when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId())).thenReturn(false);

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
                .with(user(WORK_AREA_USER)))
        .andExpectAll(
            status().isOk(),
            view().name("osd/workarea/workArea"),
            model().attribute("statusMap", NominationStatus.getDisplayNameByEnumName(false)),
            model().attribute("workAreaItems", List.of()),
            model().attribute("clearFiltersUrl",
                ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilters(null, null)))
        );
  }


  @Test
  void renderWorkAreaResults_whenFormSubmitted_thenSessionUpdatedAndRedirect() throws Exception {
    mockMvc.perform(
            post(ReverseRouter.route(on(WorkAreaController.class).getWorkArea(null)))
                .flashAttr("form", WorkAreaFilterForm.fromFilter(MODIFIED_FILTER))
                .with(user(WORK_AREA_USER))
                .with(csrf())
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(WorkAreaController.class).getWorkArea(null))));

    assertThat(session.getAttribute(SESSION_KEY))
        .asInstanceOf(type(WorkAreaSession.class))
        .extracting(WorkAreaSession::getWorkAreaFilter)
        .isEqualTo(MODIFIED_FILTER);
  }

  @Test
  void clearWorkAreaFilters_thenSessionClearedAndRedirect() throws Exception {
    session.setAttribute(SESSION_KEY, new WorkAreaSession(MODIFIED_FILTER, false));

    mockMvc.perform(
            get(ReverseRouter.route(on(WorkAreaController.class).clearWorkAreaFilters(null, null)))
                .with(user(WORK_AREA_USER))
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(WorkAreaController.class).getWorkArea(null))));

    assertThat(session.getAttribute(SESSION_KEY)).isNull();
  }
}
