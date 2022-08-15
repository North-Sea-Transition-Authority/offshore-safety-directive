package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.nominatedinstallationdetail.NominatedInstallationController;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@WebMvcTest
@ContextConfiguration(classes = InstallationInclusionController.class)
@WithMockUser
class InstallationInclusionControllerTest extends AbstractControllerTest {

  private static final int NOMINATION_ID = 42;
  private static final NominationDetail NOMINATION_DETAIL = NominationDetailTestUtil.getNominationDetail();

  @MockBean
  private InstallationInclusionService installationInclusionService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @Test
  void getInstallationAdvice_assertModelAndViewProperties() throws Exception {
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(installationInclusionService.getForm(NOMINATION_DETAIL)).thenReturn(new InstallationInclusionForm());

    var modelAndView = mockMvc.perform(
            get(ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID)))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView();

    assertNotNull(modelAndView);
    assertEquals("osd/nomination/installation/installationInclusion", modelAndView.getViewName());

    var model = modelAndView.getModel();
    assertThat(model).containsOnlyKeys(
        "form",
        "pageTitle",
        "backLinkUrl",
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

    var expectedBackUrl = ReverseRouter.route(on(NominationTaskListController.class).getTaskList());
    var expectedActionUrl =
        ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(NOMINATION_ID, null, null));
    assertEquals(InstallationInclusionForm.class, model.get("form").getClass());
    assertEquals(expectedBackUrl,  model.get("backLinkUrl"));
    assertEquals(expectedActionUrl,  model.get("actionUrl"));
    assertEquals(InstallationInclusionController.PAGE_TITLE,  model.get("pageTitle"));
  }

  @Test
  void saveInstallationAdvice_whenIncludeInstallationsInNominationTrue_verifyRedirection() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(installationInclusionService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(NOMINATION_ID, null, null)))
                .with(csrf())
                .param("includeInstallationsInNomination", "true")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))));

    verify(installationInclusionService, times(1)).createOrUpdateInstallationInclusion(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveInstallationAdvice_whenIncludeInstallationsInNominationFalse_verifyRedirection() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(installationInclusionService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(NOMINATION_ID, null, null)))
                .with(csrf())
                .param("includeInstallationsInNomination", "false")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList())));

    verify(installationInclusionService, times(1)).createOrUpdateInstallationInclusion(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveInstallationAdvice_whenErrors_assertStatusOk() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default msg"));

    when(installationInclusionService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(NOMINATION_ID, null, null)))
                .with(csrf())
        )
        .andExpect(status().isOk());

    verify(installationInclusionService, never()).createOrUpdateInstallationInclusion(any(), any());
  }
}