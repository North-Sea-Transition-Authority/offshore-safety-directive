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
import static uk.co.nstauthority.offshoresafetydirective.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
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

@ContextConfiguration(classes = InstallationInclusionController.class)
class InstallationInclusionControllerTest extends AbstractNominationControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(UUID.randomUUID());

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private NominationDetail nominationDetail;

  @MockitoBean
  private InstallationInclusionPersistenceService installationInclusionPersistenceService;

  @MockitoBean
  private InstallationInclusionFormService installationInclusionFormService;

  @MockitoBean
  private InstallationInclusionValidationService installationInclusionValidationService;

  @BeforeEach
  void setup() {

    nominationDetail = new NominationDetailTestUtil.NominationDetailBuilder()
        .withNominationId(NOMINATION_ID)
        .withStatus(NominationStatus.DRAFT)
        .build();

    givenLatestNominationDetail(nominationDetail);

    givenUserHasRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);
  }

  @SecurityTest
  void getInstallationInclusion_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(InstallationInclusionController.class)
        .getInstallationInclusion(NOMINATION_ID))))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void saveInstallationInclusion_whenUserNotLoggedIn() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(InstallationInclusionController.class)
        .saveInstallationInclusion(NOMINATION_ID, null, null)))
        .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectionToLoginUrl());
  }

  @SecurityTest
  void getInstallationInclusion_whenUserNotPartOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(get(ReverseRouter.route(on(InstallationInclusionController.class)
        .getInstallationInclusion(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void saveInstallationInclusion_whenUserNotPartOfApplicantTeam() throws Exception {

    givenUserHasNoRoleInApplicantTeamForDraftNominationAccess(USER.wuaId(), nominationDetail);

    mockMvc.perform(post(ReverseRouter.route(on(InstallationInclusionController.class)
        .saveInstallationInclusion(NOMINATION_ID, null, null)))
        .with(user(USER))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @SecurityTest
  void smokeTestNominationStatuses_onlyDraftPermitted() {

    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    when(installationInclusionFormService.getForm(nominationDetail)).thenReturn(form);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    NominationStatusSecurityTestUtil.smokeTester(mockMvc)
        .withPermittedNominationStatus(NominationStatus.DRAFT)
        .withNominationDetail(nominationDetail)
        .withUser(USER)
        .withGetEndpoint(
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
        )
        .withPostEndpoint(
            ReverseRouter.route(on(InstallationInclusionController.class)
                .saveInstallationInclusion(NOMINATION_ID, null, null)),
            status().is3xxRedirection(),
            status().isForbidden()
        )
        .withBodyParam("includeInstallationsInNomination", "true")
        .test();
  }

  @Test
  void getInstallationAdvice_assertModelAndViewProperties() throws Exception {

    var form = new InstallationInclusionForm();
    when(installationInclusionFormService.getForm(nominationDetail)).thenReturn(form);

    mockMvc.perform(get(ReverseRouter.route(on(InstallationInclusionController.class)
        .getInstallationInclusion(NOMINATION_ID)))
        .with(user(USER)))
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationInclusion"))
        .andExpect(model().attribute("form", form))
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

    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(InstallationInclusionController.class)
        .saveInstallationInclusion(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER))
        .param("includeInstallationsInNomination", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(
            on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID))));

    verify(installationInclusionPersistenceService, times(1))
        .createOrUpdateInstallationInclusion(eq(nominationDetail), any());
  }

  @Test
  void saveInstallationAdvice_whenIncludeInstallationsInNominationFalse_verifyRedirection() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(InstallationInclusionController.class)
        .saveInstallationInclusion(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER))
        .param("includeInstallationsInNomination", "false"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(NominationTaskListController.class).getTaskList(NOMINATION_ID))));

    verify(installationInclusionPersistenceService, times(1))
        .createOrUpdateInstallationInclusion(eq(nominationDetail), any());
  }

  @Test
  void saveInstallationAdvice_whenErrors_assertStatusOk() throws Exception {
    var form = new InstallationInclusionFormTestUtil.InstallationInclusionFormBuilder().build();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default msg"));

    when(installationInclusionValidationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(on(InstallationInclusionController.class)
        .saveInstallationInclusion(NOMINATION_ID, null, null)))
        .with(csrf())
        .with(user(USER)))
        .andExpect(status().isOk());

    verify(installationInclusionPersistenceService, never())
        .createOrUpdateInstallationInclusion(any(), any());
  }
}