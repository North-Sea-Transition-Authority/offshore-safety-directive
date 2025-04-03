package uk.co.nstauthority.offshoresafetydirective.nomination.well.managewells;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.authorisation.SecurityTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.AbstractNominationControllerTest;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatus;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationStatusSecurityTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedBlockSubareaDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellDetailViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupViewTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellView;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.exclusions.ExcludedWellboreController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.subareawells.NominatedSubareaWellsView;
import uk.co.nstauthority.offshoresafetydirective.workarea.WorkAreaController;

@ContextConfiguration(classes = ManageWellsController.class)
class ManageWellsControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @MockitoBean
  private ManageWellsService manageWellsService;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(nominationDetail);

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);
  }

  @SecurityTest
  void getWellManagementPage_whenNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(ManageWellsController.class)
        .getWellManagementPage(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getWellManagementPage_whenNotPartOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(ManageWellsController.class)
        .getWellManagementPage(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var wellSelectionView = new WellSelectionSetupViewTestUtil.WellSelectionSetupViewBuilder().build();

    when(manageWellsService.getWellSelectionSetupView(nominationDetail))
        .thenReturn(Optional.of(wellSelectionView));

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(ReverseRouter.route(on(ManageWellsController.class).getWellManagementPage(NOMINATION_ID)))
        .test();
  }

  @Test
  void getWellManagementPage_assertModelProperties() throws Exception {

    var wellSelectionView = new WellSelectionSetupViewTestUtil.WellSelectionSetupViewBuilder().build();

    when(manageWellsService.getWellSelectionSetupView(nominationDetail))
        .thenReturn(Optional.of(wellSelectionView));

    var nominatedWellDetailView = NominatedWellDetailViewTestUtil.builder().build();

    when(manageWellsService.getNominatedWellDetailView(nominationDetail))
        .thenReturn(Optional.of(nominatedWellDetailView));

    var nominatedBlockSubareaDetailView = new NominatedBlockSubareaDetailView();

    when(manageWellsService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.of(nominatedBlockSubareaDetailView));

    var excludedWellView = new ExcludedWellView();

    when(manageWellsService.getExcludedWellView(nominationDetail))
        .thenReturn(Optional.of(excludedWellView));

    var nominatedSubareaWellsView = new NominatedSubareaWellsView();

    when(manageWellsService.getNominatedSubareaWellsView(nominationDetail))
        .thenReturn(Optional.of(nominatedSubareaWellsView));

    mockMvc.perform(get(ReverseRouter.route(on(ManageWellsController.class)
        .getWellManagementPage(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/well/managewells/wellManagement"))
        .andExpect(model().attribute("pageTitle", ManageWellsController.PAGE_TITLE))
        .andExpect(model().attribute(
            "wellSelectionSetupChangeUrl",
            ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID))
        ))
        .andExpect(model().attribute("wellSelectionSetupView", wellSelectionView))
        .andExpect(model().attribute(
            "nominatedWellDetailViewChangeUrl",
            ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(NOMINATION_ID))))
        .andExpect(model().attribute("nominatedWellDetailView", nominatedWellDetailView))
        .andExpect(model().attribute(
            "saveAndContinueUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute("nominatedBlockSubareaDetailView", nominatedBlockSubareaDetailView))
        .andExpect(model().attribute(
            "nominatedBlockSubareaDetailViewChangeUrl",
            ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "breadcrumbsList",
            Map.of(
                ReverseRouter.route(on(WorkAreaController.class).getWorkArea()),
                WorkAreaController.WORK_AREA_TITLE,
                ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID)),
                NominationTaskListController.PAGE_NAME
            )
        ))
        .andExpect(model().attribute("currentPage", ManageWellsController.PAGE_TITLE))
        .andExpect(model().attribute(
            "excludedWellView",
            excludedWellView
        ))
        .andExpect(model().attribute(
            "excludedWellChangeUrl",
            ReverseRouter.route(on(ExcludedWellboreController.class).renderPossibleWellsToExclude(NOMINATION_ID))
        ))
        .andExpect(model().attribute("nominatedSubareaWellsView", nominatedSubareaWellsView));
  }

  @Test
  void getWellManagementPage_whenNoViewsExist_assertViewsEmpty() throws Exception {

    when(manageWellsService.getWellSelectionSetupView(nominationDetail))
        .thenReturn(Optional.empty());

    when(manageWellsService.getNominatedWellDetailView(nominationDetail))
        .thenReturn(Optional.empty());

    when(manageWellsService.getNominatedBlockSubareaDetailView(nominationDetail))
        .thenReturn(Optional.empty());

    when(manageWellsService.getExcludedWellView(nominationDetail))
        .thenReturn(Optional.empty());

    when(manageWellsService.getNominatedSubareaWellsView(nominationDetail))
        .thenReturn(Optional.empty());

    var modelAndView = mockMvc.perform(get(ReverseRouter.route(on(ManageWellsController.class)
        .getWellManagementPage(NOMINATION_ID)))
        .with(user(USER)))
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

    assertThat((NominatedBlockSubareaDetailView) model.get("nominatedBlockSubareaDetailView"))
        .extracting(
            NominatedBlockSubareaDetailView::getLicenceBlockSubareas,
            NominatedBlockSubareaDetailView::getValidForFutureWellsInSubarea,
            NominatedBlockSubareaDetailView::getForAllWellPhases,
            NominatedBlockSubareaDetailView::getWellPhases
        )
        .containsExactly(
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList()
        );

    assertThat((ExcludedWellView) model.get("excludedWellView"))
        .extracting(
            ExcludedWellView::hasWellsToExclude,
            ExcludedWellView::excludedWells
        )
        .containsExactly(
            null,
            Collections.emptyList()
        );

    assertThat(((NominatedSubareaWellsView) model.get("nominatedSubareaWellsView")).nominatedSubareaWellbores())
        .isEmpty();
  }
}