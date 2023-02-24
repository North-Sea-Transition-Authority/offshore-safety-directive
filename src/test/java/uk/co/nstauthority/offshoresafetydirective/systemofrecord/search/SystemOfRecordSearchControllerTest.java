package uk.co.nstauthority.offshoresafetydirective.systemofrecord.search;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;

@ContextConfiguration(classes = SystemOfRecordSearchController.class)
class SystemOfRecordSearchControllerTest extends AbstractControllerTest {

  @SecurityTest
  void renderOperatorSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch())))
        .andExpect(status().isOk());
  }

  @Test
  void renderOperatorSearch_verifyModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderOperatorSearch())))
        .andExpect(view().name("osd/systemofrecord/search/operator/searchAppointmentsByOperator"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        );
  }

  @SecurityTest
  void renderInstallationSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch())))
        .andExpect(status().isOk());
  }

  @Test
  void renderInstallationSearch_verifyModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderInstallationSearch())))
        .andExpect(view().name(
            "osd/systemofrecord/search/installation/searchInstallationAppointments"
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        );
  }

  @SecurityTest
  void renderWellSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch())))
        .andExpect(status().isOk());
  }

  @Test
  void renderWellSearch_verifyModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class).renderWellSearch())))
        .andExpect(view().name("osd/systemofrecord/search/well/searchWellAppointments"))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        );
  }

  @SecurityTest
  void renderForwardAreaApprovalSearch_verifyUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class)
            .renderForwardAreaApprovalSearch()))
    )
        .andExpect(status().isOk());
  }

  @Test
  void renderForwardAreaApprovalSearch_verifyModelProperties() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(SystemOfRecordSearchController.class)
            .renderForwardAreaApprovalSearch()))
    )
        .andExpect(view().name(
            "osd/systemofrecord/search/forwardapproval/searchForwardAreaApprovalAppointments"
        ))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(SystemOfRecordLandingPageController.class).renderLandingPage()))
        );
  }

}