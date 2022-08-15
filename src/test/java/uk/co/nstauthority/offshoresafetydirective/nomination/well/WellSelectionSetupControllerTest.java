package uk.co.nstauthority.offshoresafetydirective.nomination.well;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedblocksubarea.NominatedBlockSubareaController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail.NominatedWellDetailController;

@ContextConfiguration(classes = WellSelectionSetupController.class)
@WithMockUser
class WellSelectionSetupControllerTest extends AbstractControllerTest {

  @MockBean
  private WellSelectionSetupService WellSelectionSetupService;

  @MockBean
  private NominationDetailService nominationDetailService;

  private static final int NOMINATION_ID = 42;
  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @Test
  void getWellSetup_assertModelAndView() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(WellSelectionSetupService.getForm(NOMINATION_DETAIL)).thenReturn(new WellSelectionSetupForm());

    var modelAndView = mockMvc.perform(
        get(ReverseRouter.route(on(WellSelectionSetupController.class).getWellSetup(NOMINATION_ID)))
    )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertEquals("osd/nomination/well/wellSelectionSetup", modelAndView.getViewName());

    var model = modelAndView.getModel();
    assertThat(model).containsOnlyKeys(
        "form",
        "backLinkUrl",
        "actionUrl",
        "pageTitle",
        "wellSelectionTypes",
        "serviceBranding",
        "customerBranding",
        "serviceHomeUrl",
        "navigationItems",
        "currentEndPoint",
        "org.springframework.validation.BindingResult.serviceBranding",
        "org.springframework.validation.BindingResult.customerBranding",
        "org.springframework.validation.BindingResult.form"
    );

    var expectedBackUrl = ReverseRouter.route(on(NominationTaskListController.class).getTaskList());
    var expectedActionUrl = ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null));
    var expectedWellSetupAnswers = DisplayableEnumOptionUtil.getDisplayableOptions(WellSelectionType.class);
    assertEquals(WellSelectionSetupForm.class, model.get("form").getClass());
    assertEquals(expectedBackUrl,  model.get("backLinkUrl"));
    assertEquals(expectedActionUrl,  model.get("actionUrl"));
    assertEquals(WellSelectionSetupController.PAGE_NAME,  model.get("pageTitle"));
    assertEquals(expectedWellSetupAnswers,  model.get("wellSelectionTypes"));
  }

  @Test
  void saveWellSetup_whenErrors_thenExpectStatusOk() throws Exception {
    var form = WellSelectionSetupTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default msg"));
    when(WellSelectionSetupService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(WellSelectionSetupService, never()).createOrUpdateWellSelectionSetup(any(), anyInt());
  }

  @Test
  void saveWellSetup_whenAnsweredSpecificWells_thenExpectRedirection() throws Exception {
    var form = WellSelectionSetupTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(WellSelectionSetupService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .param("wellSelectionType", WellSelectionType.SPECIFIC_WELLS.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominatedWellDetailController.class).renderNominatedWellDetail(NOMINATION_ID))));

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetupForm.class);
    verify(WellSelectionSetupService, times(1)).createOrUpdateWellSelectionSetup(wellSetupCaptor.capture(), eq(NOMINATION_ID));

    var capturedForm = wellSetupCaptor.getValue();
    assertEquals(WellSelectionType.SPECIFIC_WELLS.name(), capturedForm.getWellSelectionType());
  }

  @Test
  void saveWellSetup_whenAndAnsweredLicenceBlockSubarea_thenExpectRedirection() throws Exception {
    var form = WellSelectionSetupTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(WellSelectionSetupService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .param("wellSelectionType", WellSelectionType.LICENCE_BLOCK_SUBAREA.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominatedBlockSubareaController.class).getLicenceBlockSubareas(NOMINATION_ID))));

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetupForm.class);
    verify(WellSelectionSetupService, times(1)).createOrUpdateWellSelectionSetup(wellSetupCaptor.capture(), eq(NOMINATION_ID));

    var capturedForm = wellSetupCaptor.getValue();
    assertEquals(WellSelectionType.LICENCE_BLOCK_SUBAREA.name(), capturedForm.getWellSelectionType());
  }

  @Test
  void saveWellSetup_whenAndAnsweredNo_thenExpectRedirection() throws Exception {
    var form = WellSelectionSetupTestUtil.getValidForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(WellSelectionSetupService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(WellSelectionSetupController.class).saveWellSetup(NOMINATION_ID, null, null)))
                .with(csrf())
                .param("wellSelectionType", WellSelectionType.NO_WELLS.name())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    var wellSetupCaptor = ArgumentCaptor.forClass(WellSelectionSetupForm.class);
    verify(WellSelectionSetupService, times(1)).createOrUpdateWellSelectionSetup(wellSetupCaptor.capture(),
        eq(NOMINATION_ID));

    var capturedForm = wellSetupCaptor.getValue();
    assertEquals(WellSelectionType.NO_WELLS.name(), capturedForm.getWellSelectionType());
  }
}