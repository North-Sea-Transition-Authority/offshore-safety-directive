package uk.co.nstauthority.offshoresafetydirective.workarea;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.StartNominationController;

 @ContextConfiguration(classes = WorkAreaController.class)
class WorkAreaControllerTest extends AbstractControllerTest {

  private static final ServiceUserDetail WORK_AREA_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockitoBean
  private WorkAreaItemService workAreaItemService;

  @SecurityTest
  void getWorkArea_whenNotLoggedIn_thenRedirectionToLoginUrl() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea())))
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getWorkArea_whenLoggedIn_thenOk() throws Exception {
    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
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

    when(workAreaItemService.getWorkAreaItems()).thenReturn(List.of(workAreaItem));

    when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId()))
        .thenReturn(true);

    mockMvc.perform(
        get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
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

     when(workAreaItemService.getWorkAreaItems()).thenReturn(List.of(workAreaItem));

     when(nominationRoleService.userCanStartNomination(WORK_AREA_USER.wuaId()))
         .thenReturn(false);

     mockMvc.perform(
         get(ReverseRouter.route(on(WorkAreaController.class).getWorkArea()))
             .with(user(WORK_AREA_USER)
         )
     )
         .andExpect(status().isOk())
         .andExpect(view().name("osd/workarea/workArea"))
         .andExpect(model().attributeDoesNotExist("startNominationUrl"))
         .andExpect(model().attribute("workAreaItems", List.of(workAreaItem)));
   }
}