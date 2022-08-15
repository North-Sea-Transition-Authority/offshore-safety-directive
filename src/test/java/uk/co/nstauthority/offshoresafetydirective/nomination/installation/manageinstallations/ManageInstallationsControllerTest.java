package uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.InstallationInclusionController;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@WebMvcTest
@ContextConfiguration(classes = ManageInstallationsController.class)
@WithMockUser
class ManageInstallationsControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @MockBean
  ManageInstallationService manageInstallationService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @Test
  void getManageInstallations_assertModelAndViewProperties() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ManageInstallationsController.class).getManageInstallations(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/manageInstallations"))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    var model = modelAndView.getModel();

    assertThat(model).containsOnlyKeys(
        "pageTitle",
        "installationInclusionView",
        "installationInclusionChangeUrl",
        "nominatedInstallationDetailView",
        "nominatedInstallationDetailChangeUrl",
        "saveAndContinueUrl",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "breadcrumbsList",
        "currentPage",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.installationInclusionView",
        "org.springframework.validation.BindingResult.nominatedInstallationDetailView"
    );

    var expectedInstallationInclusionChangeUrl =
        ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID));
    var expectedNominatedInstallationDetailChangeUrl =
        ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID));
    var expectedSaveAndContinueUrl =
        ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID));
    var expectedBreadcrumbs = Map.of(
        ReverseRouter.route(on(WorkAreaController.class).getWorkArea()), WorkAreaController.WORK_AREA_TITLE,
        ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)), NominationTaskListController.PAGE_NAME
    );
    assertEquals(expectedInstallationInclusionChangeUrl, model.get("installationInclusionChangeUrl"));
    assertEquals(expectedNominatedInstallationDetailChangeUrl, model.get("nominatedInstallationDetailChangeUrl"));
    assertEquals(expectedSaveAndContinueUrl, model.get("saveAndContinueUrl"));
    assertEquals(expectedBreadcrumbs, model.get("breadcrumbsList"));
  }
}