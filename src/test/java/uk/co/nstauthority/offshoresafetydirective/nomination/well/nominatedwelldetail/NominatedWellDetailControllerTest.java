package uk.co.nstauthority.offshoresafetydirective.nomination.well.nominatedwelldetail;

import static org.assertj.core.api.Assertions.assertThat;
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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;
import uk.co.nstauthority.offshoresafetydirective.nomination.well.WellSelectionSetupController;

@WebMvcTest
@ContextConfiguration(classes = NominatedWellDetailController.class)
@WithMockUser
class NominatedWellDetailControllerTest extends AbstractControllerTest {

  private static final int NOMINATION_ID = 1;

  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @MockBean
  private NominatedWellDetailService nominatedWellDetailService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @Test
  void renderSpecificSetupWells_assertModelProperties() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedWellDetailService.getForm(NOMINATION_DETAIL)).thenReturn(new NominatedWellDetailForm());
    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(NominatedWellDetailController.class).renderSpecificSetupWells(NOMINATION_ID)))
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
        ReverseRouter.route(on(NominatedWellDetailController.class).saveSpecificSetupWells(NOMINATION_ID, null, null));
    assertEquals(NominatedWellDetailForm.class, model.get("form").getClass());
    assertEquals(expectedBackLinkUrl, model.get("backLinkUrl"));
    assertEquals(NominatedWellDetailController.PAGE_TITLE, model.get("pageTitle"));
    assertEquals(expectedActionUrl, model.get("actionUrl"));
  }

  @Test
  void saveSpecificSetupWells_whenNoValidationErrors_verifyMethodCall() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedWellDetailForm(), "form");

    when(nominatedWellDetailService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominatedWellDetailController.class).saveSpecificSetupWells(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(nominatedWellDetailService, times(1)).createOrUpdateSpecificWellsNomination(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveSpecificSetupWells_whenValidationErrors_verifyRedirection() throws Exception {
    var bindingResult = new BeanPropertyBindingResult(new NominatedWellDetailForm(), "form");
    bindingResult.addError(new FieldError("error", "error field", "error message"));

    when(nominatedWellDetailService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(NominatedWellDetailController.class).saveSpecificSetupWells(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(nominatedWellDetailService, never()).createOrUpdateSpecificWellsNomination(any(), any());
  }
}