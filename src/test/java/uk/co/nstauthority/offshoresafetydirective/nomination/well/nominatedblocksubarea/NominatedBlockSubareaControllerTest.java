package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.licenceblocksubarea.LicenceBlockSubareaRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@ContextConfiguration(classes = NominatedBlockSubareaController.class)
@WithMockUser
class NominatedBlockSubareaControllerTest extends AbstractControllerTest {

  private static final int NOMINATION_ID = 42;
  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private NominatedBlockSubareaDetailService nominatedBlockSubareaDetailService;

  @MockBean
  private NominatedBlockSubareaService nominatedBlockSubareaService;

  @MockBean
  private LicenceBlockSubareaQueryService licenceBlockSubareaQueryService;

  @Test
  void getLicenceBlockSubareas_assertModelAndViewProperties() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedBlockSubareaDetailService.getForm(NOMINATION_DETAIL)).thenReturn(new NominatedBlockSubareaForm());
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertEquals("osd/nomination/well/blockSubarea", modelAndView.getViewName());

    var model = modelAndView.getModel();
    assertThat(model).containsOnlyKeys(
        "form",
        "pageTitle",
        "backLinkUrl",
        "actionUrl",
        "alreadyAddedSubareas",
        "blockSubareaRestUrl",
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
    var expectedActionUrl = ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID));
    var expectedBlockSubareaRestUrl = RestApiUtil.route(on(LicenceBlockSubareaRestController.class).searchWells(null));
    assertEquals(NominatedBlockSubareaForm.class, model.get("form").getClass());
    assertEquals(expectedBackLinkUrl, model.get("backLinkUrl"));
    assertEquals(NominatedBlockSubareaController.PAGE_TITLE, model.get("pageTitle"));
    assertEquals(expectedActionUrl, model.get("actionUrl"));
    assertEquals(expectedBlockSubareaRestUrl, model.get("blockSubareaRestUrl"));
  }

  @Test
  void getLicenceBlockSubareas_whenSavedBlockSubareas_assertSubareasAreSorted() throws Exception {
    var formWithSubareas = new NominatedBlockSubareaFormTestUtil.NominatedBlockSubareaFormBuilder().build();
    var licenceBlockSubareaDto1 = new LicenceBlockSubareaDto(1, "blockSubarea1", "1");
    var licenceBlockSubareaDto2 = new LicenceBlockSubareaDto(2, "blockSubarea2", "2");
    var licenceBlockSubareaDto3 = new LicenceBlockSubareaDto(3, "blockSubarea3", "3");
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedBlockSubareaDetailService.getForm(NOMINATION_DETAIL)).thenReturn(formWithSubareas);
    when(licenceBlockSubareaQueryService.getLicenceBlockSubareasByIdIn(formWithSubareas.getSubareas()))
        .thenReturn(List.of(licenceBlockSubareaDto2, licenceBlockSubareaDto3, licenceBlockSubareaDto1));
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);
    var returnedAlreadyAddedSubareas = (List<LicenceBlockSubareaAddToListView>) modelAndView.getModel().get("alreadyAddedSubareas");
    assertThat(returnedAlreadyAddedSubareas)
        .extracting(
            LicenceBlockSubareaAddToListView::getId,
            LicenceBlockSubareaAddToListView::getName,
            LicenceBlockSubareaAddToListView::getSortKey
        )
        .containsExactly(
            tuple(String.valueOf(licenceBlockSubareaDto1.id()), licenceBlockSubareaDto1.name(), licenceBlockSubareaDto1.sortKey()),
            tuple(String.valueOf(licenceBlockSubareaDto2.id()), licenceBlockSubareaDto2.name(), licenceBlockSubareaDto2.sortKey()),
            tuple(String.valueOf(licenceBlockSubareaDto3.id()), licenceBlockSubareaDto3.name(), licenceBlockSubareaDto3.sortKey())
        );
  }

  @Test
  void saveLicenceBlockSubareas_whenNoErrors_thenVerifyServiceCall() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedBlockSubareaForm(), "form");

    when(nominatedBlockSubareaDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(
                ReverseRouter.route(
                    on(NominatedBlockSubareaController.class).saveLicenceBlockSubareas(NOMINATION_ID, null, null)))
            .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(nominatedBlockSubareaDetailService, times(1)).createOrUpdateNominatedBlockSubareaDetail(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveLicenceBlockSubareas_whenErrors_thenStatusIsOk() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedBlockSubareaForm(), "form");
    bindingResult.addError(new FieldError("error", "error field", "error message"));

    when(nominatedBlockSubareaDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(
                ReverseRouter.route(on(NominatedBlockSubareaController.class)
                    .saveLicenceBlockSubareas(NOMINATION_ID, null, null))
            )
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nominatedBlockSubareaDetailService, never()).createOrUpdateNominatedBlockSubareaDetail(any(), any());
  }
}