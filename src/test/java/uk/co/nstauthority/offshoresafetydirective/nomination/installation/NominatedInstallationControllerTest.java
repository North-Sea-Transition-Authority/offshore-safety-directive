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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetail;
import uk.co.nstauthority.offshoresafetydirective.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.displayableutil.DisplayableEnumOptionUtil;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationAddToListView;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationDto;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationQueryService;
import uk.co.nstauthority.offshoresafetydirective.energyportal.installation.InstallationRestController;
import uk.co.nstauthority.offshoresafetydirective.mvc.AbstractControllerTest;
import uk.co.nstauthority.offshoresafetydirective.mvc.ReverseRouter;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetail;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailService;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationDetailTestUtil;
import uk.co.nstauthority.offshoresafetydirective.nomination.NominationId;
import uk.co.nstauthority.offshoresafetydirective.nomination.installation.manageinstallations.ManageInstallationsController;
import uk.co.nstauthority.offshoresafetydirective.restapi.RestApiUtil;

@WebMvcTest
@ContextConfiguration(classes = NominatedInstallationController.class)
class NominatedInstallationControllerTest extends AbstractControllerTest {

  private static final NominationId NOMINATION_ID = new NominationId(1);
  private static final NominationDetail NOMINATION_DETAIL =  new NominationDetailTestUtil.NominationDetailBuilder()
      .build();

  private static final ServiceUserDetail NOMINATION_EDITOR_USER = ServiceUserDetailTestUtil.Builder().build();

  @MockBean
  private NominatedInstallationDetailPersistenceService nominatedInstallationDetailPersistenceService;

  @MockBean
  private NominatedInstallationDetailFormService nominatedInstallationDetailFormService;

  @MockBean
  private NominationDetailService nominationDetailService;

  @MockBean
  private InstallationQueryService installationQueryService;

  @Test
  void getNominatedInstallationDetail_assertModelProperties() throws Exception {

    var installationDto1 = new InstallationDto(1, "installation1");
    var installationDto2 = new InstallationDto(2, "installation2");

    var form = new NominatedInstallationDetailFormTestUtil.NominatedInstallationDetailFormBuilder()
        .withInstallations(List.of(installationDto1.id(), installationDto2.id()))
        .build();

    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);
    when(nominatedInstallationDetailFormService.getForm(NOMINATION_DETAIL)).thenReturn(form);
    when(installationQueryService.getInstallationsByIdIn(List.of(installationDto1.id(), installationDto2.id())))
        .thenReturn(List.of(installationDto2, installationDto1));

    mockMvc.perform(
            get(ReverseRouter.route(on(NominatedInstallationController.class).getNominatedInstallationDetail(NOMINATION_ID)))
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"))
        .andExpect(model().attribute("pageTitle", NominatedInstallationController.PAGE_TITLE))
        .andExpect(model().attribute(
            "backLinkUrl",
            ReverseRouter.route(on(InstallationInclusionController.class).getInstallationInclusion(NOMINATION_ID))
        ))
        .andExpect(model().attribute(
            "actionUrl",
            ReverseRouter.route(on(NominatedInstallationController.class)
                .saveNominatedInstallationDetail(NOMINATION_ID, null, null))
        ))
        .andExpect(model().attribute(
            "installationPhases",
            DisplayableEnumOptionUtil.getDisplayableOptions(InstallationPhase.class)
        ))
        .andExpect(model().attribute(
            "alreadyAddedInstallations",
            List.of(
                new InstallationAddToListView(installationDto1.id(), installationDto1.name(), true),
                new InstallationAddToListView(installationDto2.id(), installationDto2.name(), true)
            )
        ))
        .andExpect(model().attribute(
            "installationsRestUrl",
            RestApiUtil.route(on(InstallationRestController.class).searchInstallationsByName(null))
        ))
        .andExpect(model().attribute("form", form));
  }

  @Test
  void saveNominatedInstallationDetail_whenNoErrors_verifyServiceMethodCall() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);
    when(nominationDetailService.getLatestNominationDetail(NOMINATION_ID)).thenReturn(NOMINATION_DETAIL);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ReverseRouter.route(on(ManageInstallationsController.class)
            .getManageInstallations(NOMINATION_ID))));

    verify(nominatedInstallationDetailPersistenceService, times(1))
        .createOrUpdateNominatedInstallationDetail(eq(NOMINATION_DETAIL), any(NominatedInstallationDetailForm.class));
  }

  @Test
  void saveNominatedInstallationDetail_whenErrors_assertStatusOk() throws Exception {
    var form = new NominatedInstallationDetailForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(nominatedInstallationDetailFormService.validate(any(), any())).thenReturn(bindingResult);

    mockMvc.perform(
            post(ReverseRouter.route(
                on(NominatedInstallationController.class).saveNominatedInstallationDetail(NOMINATION_ID, null, null)))
                .with(csrf())
                .with(user(NOMINATION_EDITOR_USER))
        )
        .andExpect(status().isOk())
        .andExpect(view().name("osd/nomination/installation/installationDetail"));

    verify(nominatedInstallationDetailPersistenceService, never()).createOrUpdateNominatedInstallationDetail(any(), any());
  }
}