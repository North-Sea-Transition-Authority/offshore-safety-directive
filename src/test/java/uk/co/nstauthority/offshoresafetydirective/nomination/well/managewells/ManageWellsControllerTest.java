package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Optional;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailViewTestUtil;

@WebMvcTest
@ContextConfiguration(classes = ManageWellsController.class)
@WithMockUser
class ManageWellsControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(1);
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private ManageWellsService manageWellsService;

  @Test
  void getWellManagementPage_assertModelProperties() throws Exception {
    var nominatedWellDetailView = new NominatedWellDetailViewTestUtil.NominatedWellDetailViewBuilder().build();
    var wellSelectionView = new WellSelectionSetupViewTestUtil.WellSelectionSetupViewBuilder().build();
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(manageWellsService.getWellSelectionSetupView(NOMINATION_DETAIL))
        .thenReturn(Optional.of(wellSelectionView));
    when(manageWellsService.getNominatedWellDetailView(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetailView));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/managewells/wellManagement"))
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    var model = modelAndView.getModel();

    assertThat(model).containsOnlyKeys(
        "pageTitle",
        "wellSelectionSetupView",
        "nominatedWellDetailView",
        "wellSelectionSetupChangeUrl",
        "nominatedWellDetailViewChangeUrl",
        "nominatedBlockSubareaDetailView",
        "nominatedBlockSubareaDetailViewChangeUrl",
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
        "org.springframework.validation.BindingResult.wellSelectionSetupView",
        "org.springframework.validation.BindingResult.nominatedWellDetailView",
        "org.springframework.validation.BindingResult.nominatedBlockSubareaDetailView"
    );

    var expectedWellSelectionSetupChangeUrl =
        ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID));
    var expectedNominatedWellDetailViewChangeUrl =
        ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(NOMINATION_ID));
    var expectedSaveAndContinueUrl = ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID));
    assertEquals(ManageWellsController.PAGE_TITLE, model.get("pageTitle"));
    assertEquals(expectedWellSelectionSetupChangeUrl, model.get("wellSelectionSetupChangeUrl"));
    assertEquals(expectedNominatedWellDetailViewChangeUrl, model.get("nominatedWellDetailViewChangeUrl"));
    assertEquals(expectedSaveAndContinueUrl, model.get("saveAndContinueUrl"));
  }

  @Test
  void getWellManagementPage_whenViewsExist_assertViewsPopulated() throws Exception {
    var nominatedWellDetailView = new NominatedWellDetailViewTestUtil.NominatedWellDetailViewBuilder().build();
    var wellSelectionView = new WellSelectionSetupViewTestUtil.WellSelectionSetupViewBuilder().build();
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(manageWellsService.getWellSelectionSetupView(NOMINATION_DETAIL))
        .thenReturn(Optional.of(wellSelectionView));
    when(manageWellsService.getNominatedWellDetailView(NOMINATION_DETAIL))
        .thenReturn(Optional.of(nominatedWellDetailView));

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    var model = modelAndView.getModel();

    assertEquals(wellSelectionView, model.get("wellSelectionSetupView"));
    assertEquals(nominatedWellDetailView, model.get("nominatedWellDetailView"));
  }

  @Test
  void getWellManagementPage_whenNoViewsExist_assertViewsEmpty() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(manageWellsService.getWellSelectionSetupView(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());
    when(manageWellsService.getNominatedWellDetailView(NOMINATION_DETAIL))
        .thenReturn(Optional.empty());

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);

    var model = modelAndView.getModel();

    assertThat(model.get("wellSelectionSetupView")).hasAllNullFieldsOrProperties();
    assertThat((NominatedWellDetailView) model.get("nominatedWellDetailView"))
        .extracting(
            NominatedWellDetailView::getWells,
            NominatedWellDetailView::getIsNominationForAllWellPhases,
            NominatedWellDetailView::getWellPhases
        )
        .containsExactly(
            Collections.emptyList(),
            null,
            Collections.emptyList()
        );
  }
}