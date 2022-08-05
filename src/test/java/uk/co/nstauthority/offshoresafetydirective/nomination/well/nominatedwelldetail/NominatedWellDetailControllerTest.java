package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.well.WellQueryService;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.NominatedWellService;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;

@ContextConfiguration(classes = NominatedWellDetailController.class)
@WithMockUser
class NominatedWellDetailControllerTest extends AbstractControllerTest {

  private static final int NOMINATION_ID = 1;

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @MockBean
  private NominatedWellDetailService nominatedWellDetailService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private NominatedWellService nominatedWellService;

  @MockBean
  private WellQueryService wellQueryService;

  @Test
  void renderNominatedWellDetail_assertModelProperties() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedWellDetailService.getForm(NOMINATION_DETAIL)).thenReturn(new NominatedWellDetailForm());
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertEquals("osd/nomination/well/specificWells", modelAndView.getViewName());

    var model = modelAndView.getModel();
    assertThat(model).containsOnlyKeys(
        "form",
        "backLinkUrl",
        "pageTitle",
        "actionUrl",
        "wellsRestUrl",
        "alreadyAddedWells",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.form"
    );

    var expectedBackLinkUrl = ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID));
    var expectedActionUrl =
        ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(NOMINATION_ID, null, null));
    assertEquals(NominatedWellDetailForm.class, model.get("form").getClass());
    assertEquals(expectedBackLinkUrl, model.get("backLinkUrl"));
    assertEquals(NominatedWellDetailController.PAGE_TITLE, model.get("pageTitle"));
    assertEquals(expectedActionUrl, model.get("actionUrl"));
  }

  @Test
  void renderNominatedWellDetail_whenSaveedWells_assertWellsAreSorted() throws Exception {
    var formWithWells = NominatedWellDetailTestUtil.getValidForm();
    var wellDto1 = new WellDto(1, "wellDto1", "1");
    var wellDto2 = new WellDto(2, "wellDto2", "2");
    var wellDto3 = new WellDto(3, "wellDto3", "3");
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedWellDetailService.getForm(NOMINATION_DETAIL)).thenReturn(formWithWells);
    when(wellQueryService.getWellsByIdIn(formWithWells.getWells())).thenReturn(List.of(wellDto2, wellDto3, wellDto1));
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    var returnedAlreadyAddedWells = (List<WellAddToListView>) modelAndView.getModel().get("alreadyAddedWells");
    assertThat(returnedAlreadyAddedWells)
        .extracting(
            WellAddToListView::getId,
            WellAddToListView::getName,
            WellAddToListView::getSortKey
        )
        .containsExactly(
            tuple(String.valueOf(wellDto1.id()), wellDto1.name(), wellDto1.sortKey()),
            tuple(String.valueOf(wellDto2.id()), wellDto2.name(), wellDto2.sortKey()),
            tuple(String.valueOf(wellDto3.id()), wellDto3.name(), wellDto3.sortKey())
        );
  }

  @Test
  void saveNominatedWellDetail_whenNoValidationErrors_verifyMethodCall() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedWellDetailForm(), "form");

    when(nominatedWellDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(nominatedWellDetailService, times(1)).createOrUpdateNominatedWellDetail(eq(NOMINATION_DETAIL), any());
    verify(nominatedWellService, times(1)).saveNominatedWells(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveNominatedWellDetail_whenValidationErrors_verifyRedirection() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedWellDetailForm(), "form");
    bindingResult.addError(new FieldError("error", "error field", "error message"));

    when(nominatedWellDetailService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominatedWellDetailController.class).saveNominatedWellDetail(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nominatedWellDetailService, never()).createOrUpdateNominatedWellDetail(any(), any());
    verify(nominatedWellService, never()).saveNominatedWells(any(), any());
  }
}