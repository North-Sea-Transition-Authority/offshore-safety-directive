package uk.co.nstauthority.offshoresafetydirective.nomination.installation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.offshoresafetydirective.authentication.TestUserProvider.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.tasklist.NominationTaskListController;

@ContextConfiguration(classes = InstallationInclusionController.class)
class InstallationInclusionControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(42);
  private static final NominationDetail NOMINATION_DETAIL = new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private InstallationInclusionPersistenceService installationInclusionPersistenceService;

  @MockBean
  private InstallationInclusionFormService installationInclusionFormService;

  @MockBean
  private InstallationInclusionValidationService installationInclusionValidationService;

  @Test
  void getInstallationAdvice_assertModelAndViewProperties() throws Exception {

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    var form = new InstallationInclusionForm();
    when(installationInclusionFormService.getForm(NOMINATION_DETAIL)).thenReturn(form);

    mockMvc.perform(
            get(ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationInclusion"))
        .andExpect(model().attribute("form", form))
        .andExpect(model().attribute("pageTitle", InstallationInclusionController.PAGE_TITLE))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).saveInstallationInclusion(NOMINATION_ID, null, null))
        ));
  }

  @Test
  void saveInstallationAdvice_whenIncludeInstallationsInNominationTrue_verifyRedirection() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(InstallationInclusionController.class)
                  .saveInstallationInclusion(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
                .param("includeInstallationsInNomination", "true")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))));

    verify(installationInclusionPersistenceService, times(1)).createOrUpdateInstallationInclusion(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveInstallationAdvice_whenIncludeInstallationsInNominationFalse_verifyRedirection() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(InstallationInclusionController.class)
                  .saveInstallationInclusion(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
                .param("includeInstallationsInNomination", "false")
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))));

    verify(installationInclusionPersistenceService, times(1)).createOrUpdateInstallationInclusion(eq(NOMINATION_DETAIL), any());
  }

  @Test
  void saveInstallationAdvice_whenErrors_assertStatusOk() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default msg"));

    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(on(InstallationInclusionController.class)
                  .saveInstallationInclusion(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk());

    verify(installationInclusionPersistenceService, never()).createOrUpdateInstallationInclusion(any(), any());
  }
}